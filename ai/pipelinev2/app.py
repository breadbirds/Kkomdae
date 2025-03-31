import os
import cv2
import torch
import numpy as np
from PIL import Image
from torchvision.transforms import functional as F
import torchvision
import supervision as sv
from ultralytics import YOLO
import openai
from io import BytesIO
import base64

# ------------------------------
# ✅ 설정
image_path = "C:/S12P21D101/ai/pipelinev2/photo.jpg"
yolo_model_path = "model/yolo_laptop.pt"
faster_model_path = "model/faster_damage.pth"

yolo_threshold = 0.7
faster_threshold = 0.7

image_save_path = "filtered_result_vis.jpg"

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
class_names = ["background", "damage_bbox"]

client = openai.OpenAI(api_key="")
# ------------------------------

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
def run_supervision_slicer(faster_model, image):
    def callback(image_slice: np.ndarray) -> sv.Detections:
        image_pil = Image.fromarray(cv2.cvtColor(image_slice, cv2.COLOR_BGR2RGB))
        image_tensor = F.to_tensor(image_pil).unsqueeze(0).to(device)

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
    detections = slicer(image)

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
def detect_laptop_bbox(yolo_model, image_path):
    results = yolo_model(image_path, conf=yolo_threshold)
    laptop_bboxes = []

    for box in results[0].boxes:
        cls_id = int(box.cls)
        label = yolo_model.names[cls_id]
        conf = float(box.conf)
        xyxy = box.xyxy.cpu().tolist()[0]
        if label == "ssafy_laptop":
            laptop_bboxes.append([int(x) for x in xyxy])

    print(f"✅ YOLO ssafy_laptop bbox 개수: {len(laptop_bboxes)}")
    return laptop_bboxes

# ✅ 5-1. Faster 결과를 YOLO bbox 내부만 필터링 & GPT 분석
def filter_and_classify(faster_detections, laptop_bboxes, image):
    if not laptop_bboxes:
        print("⚠ ssafy_laptop bbox가 없습니다.")
        return []

    X1, Y1, X2, Y2 = laptop_bboxes[0]

    results = []

    for det in faster_detections:
        box = det['bbox']
        x1, y1, x2, y2 = map(int, box)

        if x1 >= X1 and y1 >= Y1 and x2 <= X2 and y2 <= Y2:
            crop = image[y1:y2, x1:x2]
            crop_pil = Image.fromarray(cv2.cvtColor(crop, cv2.COLOR_BGR2RGB))

            gpt_result = classify_damage_with_gpt(crop_pil)
            print(f"🤖GPT 분석 결과: {gpt_result}")

            det['gpt_result'] = gpt_result
            results.append(det)

    print(f"✨ GPT 분석 bbox 개수: {len(results)}")
    return results

# ✅ 5-2. GPT 분석
def classify_damage_with_gpt(crop_image):
    buffer = BytesIO()
    crop_image.save(buffer, format="JPEG")
    buffer.seek(0)

    # ✅ base64로 이미지 encode
    base64_image = base64.b64encode(buffer.read()).decode('utf-8')

    # ✅ 최신 GPT Vision API 호출
    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "이미지는 분석해서 다음 클래스를 구분해줘.\n 1. 스크래치야 아님 흠집이야?\n2. 각 손상에 대해서 손상도를 판단해줘.(상, 중, 하)"},
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/jpeg;base64,{base64_image}"
                        }
                    }
                ]
            }
        ],
        max_tokens=300
    )

    result = response.choices[0].message.content
    return result

# # ✅ 5. Faster 결과를 YOLO bbox 내부만 필터링
# def filter_by_yolo(faster_detections, laptop_bboxes):
#     if not laptop_bboxes:
#         print("⚠ ssafy_laptop bbox가 없습니다.")
#         return []

#     X1, Y1, X2, Y2 = laptop_bboxes[0]  # laptop이 1개라고 가정

#     def is_inside(box):
#         x1, y1, x2, y2 = box
#         return (x1 >= X1) and (y1 >= Y1) and (x2 <= X2) and (y2 <= Y2)

#     filtered = [det for det in faster_detections if is_inside(det['bbox'])]
#     print(f"✨ 필터링된 bbox 개수: {len(filtered)}")
#     return filtered

# ✅ 6. bbox 이미지 저장
def save_annotated_image(image, detections, save_path):
    box_annotator = sv.BoxAnnotator()
    label_annotator = sv.LabelAnnotator()

    sv_detections = sv.Detections(
        xyxy=np.array([det["bbox"] for det in detections]),
        confidence=np.array([det["score"] for det in detections]),
        class_id=np.zeros(len(detections), dtype=int)
    )

    labels = [det["label"] for det in detections]

    annotated_image = box_annotator.annotate(scene=image.copy(), detections=sv_detections)
    annotated_image = label_annotator.annotate(scene=annotated_image, detections=sv_detections, labels=labels)

    cv2.imwrite(save_path, annotated_image)
    print(f"✅ bbox 이미지 저장 완료: {save_path}")

# ------------------------------
# ▶ 파이프라인 실행
if __name__ == "__main__":
    faster_model = load_faster_model()
    yolo_model = load_yolo_model()

    image = cv2.imread(image_path)

    # 1. supervision으로 전체 이미지에서 Faster R-CNN detection
    faster_detections = run_supervision_slicer(faster_model, image)

    # 2. YOLO로 ssafy_laptop bbox 탐지
    laptop_bboxes = detect_laptop_bbox(yolo_model, image_path)

    # 3. Faster detection 중 laptop bbox 내부 결과만 필터링
    # filtered_detections = filter_by_yolo(faster_detections, laptop_bboxes)
    filtered_detections = filter_and_classify(faster_detections, laptop_bboxes, image)

    # 4. 이미지 시각화만 저장
    save_annotated_image(image, filtered_detections, image_save_path)
# ------------------------------