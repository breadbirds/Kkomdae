from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from dotenv import load_dotenv
from io import BytesIO
from PIL import Image, ImageDraw
import os
import boto3
import tempfile
import torchvision.ops as ops
import torch
from torchvision.models.detection import fasterrcnn_resnet50_fpn
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
import torchvision.transforms as T
from ultralytics import YOLO
import numpy as np
from torchvision.transforms import functional as F
import supervision as sv
from loguru import logger

# 환경변수 로딩
load_dotenv()

# AWS 설정
aws_access_key_id = os.getenv('AWS_ACCESS_KEY_ID')
aws_secret_access_key = os.getenv('AWS_SECRET_ACCESS_KEY')
region_name = os.getenv('AWS_REGION')
bucket_name = os.getenv('BUCKET_NAME')
folder = os.getenv('S3_PREFIX')

# AI 모델 설정
faster_model_path = "model/faster_damage.pth"
yolo_model_path = "model/yolo_laptop.pt"
faster_threshold = 0.5
yolo_threshold = 0.7
class_names = ["background", "damage_bbox"]
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
logger.info(f"Using device: {device}")
num_classes = 2

# 모델 캐싱용 전역 변수
faster_model_cached = None
yolo_model_cached = None

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

s3_client = boto3.client(
    's3',
    aws_access_key_id=aws_access_key_id,
    aws_secret_access_key=aws_secret_access_key,
    region_name=region_name
)

class AnalyzeRequest(BaseModel):
    s3Key: str

@app.on_event("startup")
def load_models():
    global faster_model_cached, yolo_model_cached
    faster_model_cached = _load_faster_model()
    yolo_model_cached = _load_yolo_model()

@app.post("/analyze")
async def analyze(data: AnalyzeRequest):
    s3_key = data.s3Key
    temp_dir = tempfile.gettempdir()
    local_download_path = os.path.join(temp_dir, s3_key)

    try:
        s3_client.download_file(
            Bucket=bucket_name,
            Key=folder + s3_key,
            Filename=local_download_path
        )
    except Exception as e:
        logger.error(f"S3 file download failed: {e}")
        raise HTTPException(status_code=400, detail="S3 파일 다운로드 실패")

    try:
        original_image = Image.open(local_download_path)
    except Exception as e:

        
        logger.error(f"Failed to open image: {e}")
        raise HTTPException(status_code=400, detail="이미지를 열 수 없습니다.")

    # ---------------------------------------------------------------------------
    # AI 로직
    image_tensor = load_image(original_image)
    faster_results = predict_and_get_result(faster_model_cached, image_tensor)
    print(f"📦 Faster R-CNN 탐지된 damage 개수: {len(faster_results)}")
    yolo_results = detect_laptop_yolo(yolo_model_cached, original_image)
    filtered_results = filter_faster_by_yolo(faster_results, yolo_results)
    print(f"🧹 YOLO 필터링 후 damage 개수: {len(filtered_results)}")
    filtered_results = remove_overlapping_boxes(filtered_results, iou_threshold=0.3)
    print(f"✅ 중복 박스 제거 후 damage 개수: {len(filtered_results)}")
    new_image = visualize_filtered(local_download_path, filtered_results)
    # ---------------------------------------------------------------------------

    new_key = f"{folder}analyzed_{s3_key}"
    new_image_bytes = BytesIO()
    new_image.save(new_image_bytes, format="PNG")
    new_image_bytes.seek(0)

    try:
        s3_client.upload_fileobj(
            Fileobj=new_image_bytes,
            Bucket=bucket_name,
            Key=new_key,
            ExtraArgs={'ContentType': 'image/png'}
        )
    except Exception as e:
        logger.error(f"S3 upload failed: {e}")
        raise HTTPException(status_code=500, detail="S3 업로드 실패")

    try:
        os.remove(local_download_path)
    except Exception as e:
        logger.warning(f"Failed to remove temporary file: {e}")

    result = {
        "damage": len(filtered_results),
        "uploadName": f"analyzed_{s3_key}"
    }
    return JSONResponse(content=result)

def _load_faster_model():
    model = fasterrcnn_resnet50_fpn(weights=None)
    in_features = model.roi_heads.box_predictor.cls_score.in_features
    model.roi_heads.box_predictor = FastRCNNPredictor(in_features, num_classes)
    state_dict = torch.load(faster_model_path, map_location=device)
    model.load_state_dict(state_dict)
    model.eval()
    return model

def _load_yolo_model():
    return YOLO(yolo_model_path)

def load_image(pil_image):
    image = pil_image.convert("RGB")
    transform = T.ToTensor()
    return transform(image).unsqueeze(0)

def predict_and_get_result(model, image_tensor):
    with torch.no_grad():
        output = model(image_tensor)[0]
    result = []
    for box, score in zip(output['boxes'], output['scores']):
        if score >= faster_threshold:
            result.append({"bbox": box.tolist(), "score": float(score)})
    return result

def detect_laptop_yolo(model, pil_image):
    img = np.array(pil_image.convert("RGB"))
    img = img[..., ::-1]
    results = model.predict(img, conf=yolo_threshold)
    result = []
    for box in results[0].boxes:
        cls_id = int(box.cls)
        label = model.names[cls_id]
        conf = float(box.conf)
        xyxy = box.xyxy.cpu().tolist()[0]
        if label in ["ssafy_laptop", "laptop"]:
            result.append({"bbox": xyxy, "label": label, "score": conf})
        # if label == "ssafy_laptop":
        #     result.append({"bbox": xyxy, "label": label, "score": conf})
    return result

def is_inside(inner_box, outer_box):
    x1, y1, x2, y2 = inner_box
    X1, Y1, X2, Y2 = outer_box
    return (x1 >= X1) and (y1 >= Y1) and (x2 <= X2) and (y2 <= Y2)

def filter_faster_by_yolo(faster_results, yolo_results):
    if len(yolo_results) == 0:
        return []
    laptop_box = yolo_results[0]['bbox']
    filtered = [det for det in faster_results if is_inside(det['bbox'], laptop_box)]
    return filtered

def visualize_filtered(image_path, filtered_results):
    image = Image.open(image_path).convert("RGB")
    draw = ImageDraw.Draw(image)
    for det in filtered_results:
        box = det['bbox']
        score = det['score']
        x1, y1, x2, y2 = map(int, box)
        draw.rectangle([(x1, y1), (x2, y2)], outline=(255, 0, 0), width=2)
        draw.text((x1, y1 - 10), f"damage {score:.2f}", fill=(255, 0, 0))
    return image

def remove_overlapping_boxes(detections, iou_threshold=0.3):
    if not detections:
        return []

    # bbox를 Tensor로 변환
    boxes = torch.tensor([det['bbox'] for det in detections], dtype=torch.float32)
    scores = torch.tensor([det['score'] for det in detections], dtype=torch.float32)

    for i in range(len(boxes)):
        for j in range(i+1, len(boxes)):
            iou = compute_iou(boxes[i], boxes[j])
            if iou > 0.3:
                print(f"Box {i} and {j} overlap: IoU={iou:.2f}")

    # NMS로 겹치는 bbox 중에서 점수 높은 것만 남김
    keep_indices = ops.nms(boxes, scores, iou_threshold)
    print("🔍 NMS kept indices:", keep_indices.tolist())

    # 반환: 남은 인덱스만 필터링
    filtered = [detections[i] for i in keep_indices]

    return filtered

def compute_iou(box1, box2):
    # box1, box2: [x1, y1, x2, y2]
    x1 = max(box1[0], box2[0])
    y1 = max(box1[1], box2[1])
    x2 = min(box1[2], box2[2])
    y2 = min(box1[3], box2[3])

    # 교집합 넓이
    inter_area = max(0, x2 - x1) * max(0, y2 - y1)

    # 각 박스 넓이
    box1_area = (box1[2] - box1[0]) * (box1[3] - box1[1])
    box2_area = (box2[2] - box2[0]) * (box2[3] - box2[1])

    # 합집합 넓이
    union_area = box1_area + box2_area - inter_area

    # IoU 계산
    return inter_area / union_area if union_area > 0 else 0.0
