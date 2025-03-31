import torch
from torchvision.models.detection import fasterrcnn_resnet50_fpn
from torchvision.models.detection.faster_rcnn import FastRCNNPredictor
import torchvision.transforms as T
from PIL import Image
import json
import cv2
from ultralytics import YOLO
import os


# -------------------------------------
# 전역 변수 설정
# Faster R CNN
model_path = "model/faster_damage.pth"                  # 모델 pth 경로
image_path = "C:/S12P21D101/ai/pipelinev1/photo.jpg"    # 로컬에 저장된 이미지
json_save_path = "result.json"                          # json 저장 경로
visual_save_path = "result_vis.jpg"                     # 결과 이미지 저장 경로(bbox 친 이미지)
num_classes = 2                                         # class(damage, background)
score_threshold = 0.2                                   # threshold

# YOLO
yolo_model_path = "model/yolo_laptop.pt"
yolo_result_json = "yolo_result.json"
yolo_result_image = "yolo_result_vis.jpg"
yolo_threshold = 0.7                                   # threshold
# -------------------------------------

# ✅ 1. model 불러오기
def load_model():
    model = fasterrcnn_resnet50_fpn(weights=None)
    in_features = model.roi_heads.box_predictor.cls_score.in_features
    model.roi_heads.box_predictor = FastRCNNPredictor(in_features, num_classes)
    state_dict = torch.load(model_path, map_location=torch.device('cpu'))
    model.load_state_dict(state_dict)
    model.eval()
    print("✅ 모델 로드 완료")
    return model

# ✅ 2. 이미지 로드 및 전처리
def load_image(image_path):
    image = Image.open(image_path).convert("RGB")   # 3채널 RGB 이미지로 변환
    transform = T.ToTensor()                        # 픽셀값 정규화(float 32)
    return transform(image).unsqueeze(0), image     # tensor, pil image 둘 다 리턴

# ✅ 3. 모델 추론 결과 반환
def predict_and_get_result(model, image_tensor):
    with torch.no_grad():
        output = model(image_tensor)[0]

    result = []
    for box, score in zip(output['boxes'], output['scores']):
        if score >= score_threshold:
            result.append({
                "bbox": box.tolist(),
                "score": float(score)
            })
    print(f"✅ Faster 결과 추론 완료: {len(result)}개 bbox")
    return result


# ✅ 4. bbox 이미지 저장 (label마다 색 다르게)
def visualize_and_save(image_path, json_path, save_path):
    image = cv2.imread(image_path)
    with open(json_path, 'r') as f:
        detections = json.load(f)

    for det in detections:
        box = det['bbox']
        score = det['score']
        label = det.get('label', 'damage')  # label 없으면 damage로 가정 (기존 Faster R-CNN 결과 호환)
        x1, y1, x2, y2 = map(int, box)

        # ✅ label에 따라 색상 다르게
        if label == 'damage':
            color = (255, 0, 0)   # 파란색
        else:
            color = (0, 0, 255)   # 빨간색

        cv2.rectangle(image, (x1, y1), (x2, y2), color, 2)
        cv2.putText(image, f"{label} {score:.2f}", (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 1)

    cv2.imwrite(save_path, image)
    print(f"✅ bbox 이미지 저장 완료: {save_path}")

# ✅ 5. YOLO 모델 로드
def load_yolo_model():
    model = YOLO(yolo_model_path)
    print("✅ YOLO 모델 로드 완료")
    return model

# ✅ 6. YOLO 추론 결과 반환
def detect_laptop_yolo(model, image_path):
    results = model(image_path, conf=yolo_threshold)
    result = []
    for box in results[0].boxes:
        cls_id = int(box.cls)
        label = model.names[cls_id]
        conf = float(box.conf)
        xyxy = box.xyxy.cpu().tolist()[0]
        if label == "ssafy_laptop":
            result.append({
                "bbox": xyxy,
                "label": label,
                "score": conf
            })
    print(f"✅ YOLO 결과 추론 완료: {len(result)}개 bbox")
    return result


    # # bbox 이미지 저장
    # annotated_img = results[0].plot()
    # cv2.imwrite(yolo_result_image, annotated_img)
    # print(f"✅ YOLO bbox 이미지 저장 완료: {yolo_result_image}")

# 범위 안에 있는지 없는지 확인하는 함수
def is_inside(inner_box, outer_box):
    x1, y1, x2, y2 = inner_box
    X1, Y1, X2, Y2 = outer_box
    return (x1 >= X1) and (y1 >= Y1) and (x2 <= X2) and (y2 <= Y2)

# ✅ 7. Faster 결과를 YOLO bbox 내부 결과만 필터링
def filter_faster_by_yolo(faster_results, yolo_results):
    if len(yolo_results) == 0:
        print("⚠ YOLO 결과가 없습니다.")
        return []

    laptop_box = yolo_results[0]['bbox']
    filtered = [det for det in faster_results if is_inside(det['bbox'], laptop_box)]
    print(f"✨ 필터링된 bbox 개수: {len(filtered)}")
    return filtered

# ✅ 8. bbox 이미지로 저장
def visualize_filtered(image_path, filtered_results, save_image_path):
    image = cv2.imread(image_path)
    for det in filtered_results:
        box = det['bbox']
        score = det['score']
        x1, y1, x2, y2 = map(int, box)
        cv2.rectangle(image, (x1, y1), (x2, y2), (255, 0, 0), 2)
        cv2.putText(image, f"damage {score:.2f}", (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 1)
    cv2.imwrite(save_image_path, image)
    print(f"✅ 필터링된 bbox 이미지 저장 완료: {save_image_path}")



# -------------------------------------
# ▶ 전체 파이프라인
if __name__ == '__main__':
    # Faster R-CNN
    model = load_model()
    image_tensor, pil_image = load_image(image_path)
    faster_results = predict_and_get_result(model, image_tensor)

    # YOLO
    yolo_model = load_yolo_model()
    yolo_results = detect_laptop_yolo(yolo_model, image_path)

    # Post-processing (filter)
    filtered_results = filter_faster_by_yolo(faster_results, yolo_results)

    # ✅ 저장 파일 경로 자동 생성
    filename = os.path.basename(image_path)  # photo.jpg
    name, ext = os.path.splitext(filename)   # ('photo', '.jpg')
    save_image_path = os.path.join(os.path.dirname(image_path), f"{name}_result.jpg")

    # 이미지 저장
    visualize_filtered(image_path, filtered_results, save_image_path)
# -------------------------------------