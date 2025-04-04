# fastapi_app.py

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from dotenv import load_dotenv
from io import BytesIO
from PIL import Image, ImageDraw
import os
import boto3
import tempfile
import torch
from torchvision.models.detection import fasterrcnn_resnet50_fpn
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
from ultralytics import YOLO
import numpy as np
from torchvision.transforms import functional as F
import supervision as sv
from loguru import logger
from starlette.middleware.cors import CORSMiddleware

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
faster_threshold = 0.1
yolo_threshold = 0.7
class_names = ["background", "damage_bbox"]
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
num_classes = 2

# FastAPI 인스턴스 생성
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# S3 클라이언트 생성
s3_client = boto3.client(
    's3',
    aws_access_key_id=aws_access_key_id,
    aws_secret_access_key=aws_secret_access_key,
    region_name=region_name
)

class AnalyzeRequest(BaseModel):
    s3Key: str

@app.post("/analyze")
async def analyze(data: AnalyzeRequest):
    s3_key = data.s3Key
    logger.debug(f"s3_key: {s3_key}")

    temp_dir = tempfile.gettempdir()
    local_download_path = os.path.join(temp_dir, s3_key)
    logger.debug(f"local_download_path: {local_download_path}")

    try:
        logger.debug(f"Bucket: {bucket_name}, Key: {folder + s3_key}")
        s3_client.download_file(
            Bucket=bucket_name,
            Key=folder + s3_key,
            Filename=local_download_path
        )
        logger.debug(f"File downloaded successfully to {local_download_path}")
    except Exception as e:
        logger.error(f"S3 file download failed: {e}")
        raise HTTPException(status_code=400, detail="S3 파일 다운로드 실패")

    try:
        original_image = Image.open(local_download_path)
        logger.debug("Image opened successfully.")
    except Exception as e:
        logger.error(f"Failed to open image: {e}")
        raise HTTPException(status_code=400, detail="이미지를 열 수 없습니다.")

    faster_model = load_faster_model()
    yolo_model = load_yolo_model()

    faster_detections = run_supervision_slicer(faster_model, original_image)
    laptop_bboxes = detect_laptop_bbox(yolo_model, original_image)
    filtered_detections = filter_by_yolo(faster_detections, laptop_bboxes)
    new_image = save_annotated_image(original_image, filtered_detections)

    new_key = f"{folder}analyzed_{s3_key}"
    logger.debug(f"New S3 key: {new_key}")

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
        logger.debug(f"File uploaded successfully to S3: {new_key}")
    except Exception as e:
        logger.error(f"S3 upload failed: {e}")
        raise HTTPException(status_code=500, detail="S3 업로드 실패")

    try:
        os.remove(local_download_path)
        logger.debug(f"Temporary file removed: {local_download_path}")
    except Exception as e:
        logger.warning(f"Failed to remove temporary file: {e}")

    try:
        new_image_url = s3_client.generate_presigned_url(
            'get_object',
            Params={'Bucket': bucket_name, 'Key': new_key},
            ExpiresIn=3600
        )
        logger.debug(f"Presigned URL generated: {new_image_url}")
    except Exception as e:
        logger.error(f"Failed to generate presigned URL: {e}")
        raise HTTPException(status_code=500, detail="사전 서명 URL 생성 실패")

    result = {
        "damage": len(filtered_detections),
        "uploadName": f"analyzed_{s3_key}"
    }
    logger.debug(f"Response: {result}")
    return result

def load_faster_model():
    model = fasterrcnn_resnet50_fpn(weights=None)
    in_features = model.roi_heads.box_predictor.cls_score.in_features
    model.roi_heads.box_predictor = FastRCNNPredictor(in_features, num_classes)
    state_dict = torch.load(faster_model_path, map_location=device)
    model.load_state_dict(state_dict)
    model.to(device)
    model.eval()
    print("✅ Faster R-CNN 모델 로드 완료")
    return model

def load_yolo_model():
    model = YOLO(yolo_model_path)
    print("✅ YOLO 모델 로드 완료")
    return model

def run_supervision_slicer(faster_model, pil_image):
    def callback(pil_slice: Image.Image) -> sv.Detections:
        image_tensor = F.to_tensor(pil_image.convert("RGB")).unsqueeze(0).to(device)
        with torch.no_grad():
            output = faster_model(image_tensor)[0]
        boxes = output['boxes']
        scores = output['scores']
        labels = output['labels']
        keep = scores > faster_threshold
        boxes = boxes[keep].cpu().numpy()
        scores = scores[keep].cpu().numpy()
        labels = labels[keep].cpu().numpy()
        return sv.Detections(
            xyxy=boxes,
            confidence=scores,
            class_id=labels
        )

    slicer = sv.InferenceSlicer(
        callback=callback,
        slice_wh=(640, 640),
        overlap_wh=(64, 64),
        overlap_ratio_wh=None
    )
    detections = slicer(np.array(pil_image))
    all_detections = []
    for box, score, cls_id in zip(detections.xyxy, detections.confidence, detections.class_id):
        all_detections.append({
            "bbox": [float(v) for v in box],
            "score": float(score),
            "label": class_names[cls_id]
        })
    print(f"✅ Faster 전체 detection 개수: {len(all_detections)}")
    return all_detections

def detect_laptop_bbox(yolo_model, pil_image):
    img = np.array(pil_image.convert("RGB"))
    results = yolo_model.predict(img, conf=yolo_threshold)
    laptop_bboxes = []
    for box in results[0].boxes:
        cls_id = int(box.cls)
        label = yolo_model.names[cls_id]
        conf = float(box.conf)
        xyxy = box.xyxy.cpu().tolist()[0]
        if label == "ssafy_laptop":
            laptop_bboxes.append([int(x) for x in xyxy])
    print(f"✅ YOLO 결과 추론 완료: {len(laptop_bboxes)}개 bbox")
    return laptop_bboxes

def filter_by_yolo(faster_detections, laptop_bboxes):
    if not laptop_bboxes:
        print("⚠ ssafy_laptop bbox가 없습니다.")
        return []
    X1, Y1, X2, Y2 = laptop_bboxes[0]
    def is_inside(box):
        x1, y1, x2, y2 = box
        return (x1 >= X1) and (y1 >= Y1) and (x2 <= X2) and (y2 <= Y2)
    filtered = [det for det in faster_detections if is_inside(det['bbox'])]
    print(f"✨ 필터링된 bbox 개수: {len(filtered)}")
    return filtered

def save_annotated_image(pil_image, detections):
    draw = ImageDraw.Draw(pil_image)
    for det in detections:
        box = det['bbox']
        score = det['score']
        label = det['label']
        x1, y1, x2, y2 = map(int, box)
        draw.rectangle([x1, y1, x2, y2], outline=(0, 0, 255), width=3)
        text = f"{label} {score:.2f}"
        draw.text((x1, y1 - 10), text, fill=(0, 0, 255))
    print(f"✅ bbox 이미지(PIL) 변환 완료 (리턴 형태)")
    return pil_image