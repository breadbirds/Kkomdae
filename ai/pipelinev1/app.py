# 수정된 YOLO2YOLO 기반 FastAPI 예시 (흠집 탐지 + 노트북 영역 필터링)

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
import torch
from ultralytics import YOLO
import numpy as np
from loguru import logger
import torchvision.ops as ops

# 환경변수 로딩
load_dotenv()

# AWS 설정
aws_access_key_id = os.getenv('AWS_ACCESS_KEY_ID')
aws_secret_access_key = os.getenv('AWS_SECRET_ACCESS_KEY')
region_name = os.getenv('AWS_REGION')
bucket_name = os.getenv('BUCKET_NAME')
folder = os.getenv('S3_PREFIX')

# 모델 경로
yolo_damage_path = "model/yolo_damage.pt"
yolo_laptop_path = "C:/S12P21D101/backend/kkomdae_flask/model/yolo_laptop.pt"

# 탐지 임계값
damage_threshold = 0.08
laptop_threshold = 0.5

# 전역 변수
yolo_damage_model = None
yolo_laptop_model = None

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
    global yolo_damage_model, yolo_laptop_model
    yolo_damage_model = YOLO(yolo_damage_path)
    yolo_laptop_model = YOLO(yolo_laptop_path)
    print("✅ YOLO 모델 로드 완료")

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
        original_image = Image.open(local_download_path).convert("RGB")
    except Exception as e:
        logger.error(f"Failed to open image: {e}")
        raise HTTPException(status_code=400, detail="이미지를 열 수 없습니다.")

    # === AI 로직 시작 ===

    # 1. 흠집 탐지
    damage_boxes = detect_damage(yolo_damage_model, original_image)

    # 2. 노트북 탐지
    laptop_boxes = detect_laptop(yolo_laptop_model, original_image)

    # 3. 노트북 내부에 있는 흠집만 남김
    filtered_results = filter_inside_laptop(damage_boxes, laptop_boxes)

    # 4. 중복 제거 (NMS)
    filtered_results = remove_overlapping_boxes(filtered_results, iou_threshold=0.3)

    # 5. 시각화
    new_image = draw_boxes(original_image, filtered_results)

    # === AI 로직 끝 ===

    # S3 업로드
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

# ---- YOLO inference 및 후처리 함수 ----

def detect_damage(model, image):
    img = np.array(image)
    results = model.predict(img, conf=damage_threshold)
    detections = []
    for box in results[0].boxes:
        xyxy = box.xyxy.cpu().tolist()[0]
        conf = float(box.conf)
        detections.append({"bbox": xyxy, "score": conf})
    print(f"📦 흠집 탐지 개수: {len(detections)}")
    return detections

def detect_laptop(model, image):
    img = np.array(image)
    results = model.predict(img, conf=laptop_threshold)
    boxes = []
    for box in results[0].boxes:
        xyxy = box.xyxy.cpu().tolist()[0]
        boxes.append(xyxy)
    print(f"💻 노트북 탐지 개수: {len(boxes)}")
    return boxes

def filter_inside_laptop(damage_boxes, laptop_boxes):
    if not laptop_boxes:
        print("⚠ 노트북 bbox 없음")
        return []

    laptop_box = laptop_boxes[0]
    X1, Y1, X2, Y2 = laptop_box

    def is_inside(box):
        x1, y1, x2, y2 = box["bbox"]
        return x1 >= X1 and y1 >= Y1 and x2 <= X2 and y2 <= Y2

    filtered = [b for b in damage_boxes if is_inside(b)]
    print(f"🧹 노트북 내부 흠집 개수: {len(filtered)}")
    return filtered

def remove_overlapping_boxes(detections, iou_threshold=0.3):
    if not detections:
        return []
    boxes = torch.tensor([det["bbox"] for det in detections], dtype=torch.float32)
    scores = torch.tensor([det["score"] for det in detections], dtype=torch.float32)
    keep = ops.nms(boxes, scores, iou_threshold)
    return [detections[i] for i in keep]

def draw_boxes(image, detections):
    image = image.copy()
    draw = ImageDraw.Draw(image)
    for det in detections:
        box = det["bbox"]
        score = det["score"]
        x1, y1, x2, y2 = map(int, box)
        draw.rectangle([(x1, y1), (x2, y2)], outline="red", width=2)
        draw.text((x1, y1 - 10), f"damage {score:.2f}", fill="red")
    return image