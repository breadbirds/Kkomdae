# flask_app.py

# 필요한 라이브러리 임포트
from flask import Flask, request, jsonify   # Flask 웹 프레임워크 및 JSON 응답을 위한 모듈
import os
from dotenv import load_dotenv              # 환경 변수 로딩
import boto3                                # AWS S3 연동을 위한 boto3 라이브러리 (최신 버전)
from io import BytesIO
from PIL import Image, ImageDraw
import tempfile
from loguru import logger
load_dotenv()

# AI pipeline 라이브러리
import torch
from torchvision.models.detection import fasterrcnn_resnet50_fpn
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
import torchvision.transforms as T
from ultralytics import YOLO
import os
import numpy as np
from torchvision.transforms import functional as F
import torchvision
import supervision as sv
import openai
import base64


# AWS 관련 설정 로드
aws_access_key_id = os.getenv('AWS_ACCESS_KEY_ID')
aws_secret_access_key = os.getenv('AWS_SECRET_ACCESS_KEY')
region_name = os.getenv('AWS_REGION')
bucket_name = os.getenv('BUCKET_NAME')
folder = os.getenv('S3_PREFIX')

# -------------------------------------
# AI pipeline 전역 변수 설정
# Faster R CNN
faster_model_path = "model/faster_damage.pth"                  # 모델 pth 경로
num_classes = 2                                         # class(damage, background)
faster_threshold = 0.6                                   # threshold

# YOLO
yolo_model_path = "model/yolo_laptop.pt"
yolo_threshold = 0.7                                   # threshold

# Supervision
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
class_names = ["background", "damage_bbox"]
# -------------------------------------


# Flask 애플리케이션 생성
app = Flask(__name__)

# S3 클라이언트 생성
s3_client = boto3.client(
    's3',
    aws_access_key_id=aws_access_key_id,
    aws_secret_access_key=aws_secret_access_key,
    region_name=region_name
)

@app.route('/analyze', methods=['POST'])
def analyze():
    """
    클라이언트로부터 JSON 형태의 데이터를 수신하여:
      1) s3_key (예: "lighting.png")를 읽는다.
      2) s3_key 로 S3에서 직접 해당 파일을 다운로드해 로컬에 저장한다.
      3) 다운로드된 로컬 파일을 Pillow로 열어 분석(예: 흑백 변환).
      4) 분석 결과물(analyzed_{원본파일명}.png)을 S3에 업로드.
      5) 업로드된 파일의 presigned URL을 생성해 JSON 형태로 반환.
    """
    logger.debug("Request received: %s", request.get_json()) # 요청 데이터 로깅
    data = request.get_json()
    if not data:
        logger.error("No JSON data provided.")
        return jsonify({"error": "JSON 데이터가 제공되지 않았습니다."}), 400

    try:
        # Spring에서 보내는 형식에 맞춰 처리
        s3_key = data.get('s3Key')
        
        if not s3_key:
            return jsonify({"error": "필수 필드가 누락되었습니다."}), 400
            
        logger.debug(f"s3_key: {s3_key}")
 
    except Exception as e:
        logger.error(f"Invalid JSON format: {e}")
        return jsonify({"error": "JSON 형식이 올바르지 않습니다.", "detail": str(e)}), 400

    # 1) S3에서 파일을 로컬로 다운로드
    temp_dir = tempfile.gettempdir()
    local_download_path = os.path.join(temp_dir, s3_key)
    logger.debug(f"local_download_path: {local_download_path}")
    try:
        # 디버깅을 위해 folder 변수와 s3_key 변수의 값을 출력합니다.
        logger.debug(f"Bucket: {bucket_name}, Key: {folder + s3_key}")
        s3_client.download_file(
            Bucket=bucket_name,
            Key=folder + s3_key,
            Filename=local_download_path
        )
        logger.debug(f"File downloaded successfully to {local_download_path}")
    except Exception as e:
        logger.error(f"S3 file download failed: {e}")
        return jsonify({"error": "S3 파일 다운로드 실패", "detail": str(e)}), 400

    # 2) 다운로드된 로컬 파일을 Pillow로 열기
    try:
        original_image = Image.open(local_download_path)
        logger.debug("Image opened successfully.")
    except Exception as e:
        logger.error(f"Failed to open image: {e}")
        return jsonify({"error": "이미지를 열 수 없습니다.", "detail": str(e)}), 400

    # --- 이미지 분석 로직 예시 ---
    # # Faster R-CNN
    # model = load_model()
    # image_tensor = load_image(original_image)    
    # faster_results = predict_and_get_result(model, image_tensor)

    # # YOLO
    # yolo_model = load_yolo_model()
    # yolo_results = detect_laptop_yolo(yolo_model, original_image)

    # # Post-processing (filter)
    # filtered_results = filter_faster_by_yolo(faster_results, yolo_results)

    # # 이미지 저장
    # new_image = visualize_filtered(local_download_path, filtered_results) 

    faster_model = load_faster_model()
    yolo_model = load_yolo_model()

    # 1. supervision으로 전체 이미지에서 Faster R-CNN detection
    faster_detections = run_supervision_slicer(faster_model, original_image)

    # 2. YOLO로 ssafy_laptop bbox 탐지
    laptop_bboxes = detect_laptop_bbox(yolo_model, original_image)

    # 3. Faster detection 중 laptop bbox 내부 결과만 필터링
    filtered_detections = filter_by_yolo(faster_detections, laptop_bboxes)
    # filtered_detections = filter_and_classify(faster_detections, laptop_bboxes, image)

    # 4. 이미지 시각화만 저장
    new_image = save_annotated_image(original_image, filtered_detections)
    # ---------------------------------

    # 3) 새로운 이미지 S3 키 만들기
    new_key = f"{folder}analyzed_{s3_key}"
    
    # => folder + analyzed_{...} 형태로 업로드할 때 폴더까지 포함
    logger.debug(f"New S3 key: {new_key}")
    

    # 4) 메모리에 PNG로 저장 후 S3 업로드
    new_image_bytes = BytesIO()
    new_image.save(new_image_bytes, format="PNG")
    new_image_bytes.seek(0)  # 버퍼 포인터 초기화

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
        return jsonify({"error": "S3 업로드 실패", "detail": str(e)}), 500

    # 5) 임시파일 삭제
    try:
        os.remove(local_download_path)
        logger.debug(f"Temporary file removed: {local_download_path}")
    except Exception as e:
        logger.warning(f"Failed to remove temporary file: {e}")
        return jsonify({"error": "임시 파일 삭제 실패", "detail": str(e)}), 500


    # 5) 업로드된 파일의 presigned URL 생성
    try:
        new_image_url = s3_client.generate_presigned_url(
            'get_object',
            Params={'Bucket': bucket_name, 'Key': new_key},
            ExpiresIn=3600  # 1시간 유효
        )
        logger.debug(f"Presigned URL generated: {new_image_url}")
    except Exception as e:
        logger.error(f"Failed to generate presigned URL: {e}")
        return jsonify({"error": "사전 서명 URL 생성 실패", "detail": str(e)}), 500

    # 최종 반환
    result = {
        "damage" : len(filtered_detections), # 적용 결과 (✨수정함✨)
        "uploadName" : f"analyzed_{s3_key}",
    }
    logger.debug(f"Response: {result}")
    return jsonify(result)

# ✅ 1, Faster R-CNN 모델 로드
def load_faster_model():
    model = torchvision.models.detection.fasterrcnn_resnet50_fpn(weights=None, num_classes=2)
    model.load_state_dict(torch.load(faster_model_path, map_location=device))
    model.to(device)
    model.eval()
    print("✅ Faster R-CNN 모델 로드 완료")
    return model

# ✅ 2. YOLO 모델 로드
def load_yolo_model():
    model = YOLO(yolo_model_path)
    print("✅ YOLO 모델 로드 완료")
    return model

# ✅ 3. Faster R-CNN 전체 이미지 Slicer
def run_supervision_slicer(faster_model, pil_image):
    def callback(pil_slice: Image.Image) -> sv.Detections:
        pil_image = Image.fromarray(pil_slice)
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

    slicer = sv.InferenceSlicer(callback=callback)
    # slicer = sv.InferenceSlicer(callback=callback, overlap_wh=(100, 100)) # 얘는 겹치는 거
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

# ✅ 4. YOLO로 ssafy_laptop bbox 가져오기
def detect_laptop_bbox(yolo_model, pil_image):
    img = np.array(pil_image.convert("RGB"))  # Pillow → numpy array
    results = yolo_model.predict(img, conf=yolo_threshold)  # predict() 권장
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

# ✅ 5. Faster 결과를 YOLO bbox 내부만 필터링
def filter_by_yolo(faster_detections, laptop_bboxes):
    if not laptop_bboxes:
        print("⚠ ssafy_laptop bbox가 없습니다.")
        return []

    X1, Y1, X2, Y2 = laptop_bboxes[0]  # laptop이 1개라고 가정

    def is_inside(box):
        x1, y1, x2, y2 = box
        return (x1 >= X1) and (y1 >= Y1) and (x2 <= X2) and (y2 <= Y2)

    filtered = [det for det in faster_detections if is_inside(det['bbox'])]
    print(f"✨ 필터링된 bbox 개수: {len(filtered)}")
    return filtered

# ✅ 6. bbox 이미지 그리기 후 PIL Image 반환
def save_annotated_image(pil_image, detections):
    draw = ImageDraw.Draw(pil_image)

    for det in detections:
        box = det['bbox']
        score = det['score']
        label = det['label']
        x1, y1, x2, y2 = map(int, box)

        # ✅ bbox 그리기 (파란색)
        draw.rectangle([x1, y1, x2, y2], outline=(0, 0, 255), width=3)

        # ✅ label 및 score 출력
        text = f"{label} {score:.2f}"
        draw.text((x1, y1 - 10), text, fill=(0, 0, 255))

    print(f"✅ bbox 이미지(PIL) 변환 완료 (리턴 형태)")
    return pil_image


# Flask 앱 실행
if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)  # 모든 IP에서 접근 가능하도록 설정


# 요청 예시 (Request Examples)
# postman 주소창에 붙여넣으면 됨

"""
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"lighting.png": "https://your-bucket.s3.your-region.amazonaws.com/lighting.png"}' \
  http://127.0.0.1:5000/analyze

"""