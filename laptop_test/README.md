
# KkomDae Diagnostics

## 소개
**KkomDae Diagnostics**는 삼성 노트북의 하드웨어 상태를 쉽고 빠르게 점검할 수 있는 진단 프로그램입니다.  
이 프로그램은 키보드, 카메라, USB 포트, 배터리, 충전 상태 등을 자동으로 점검하며, 테스트 결과를 QR 코드로 생성해 빠르게 공유할 수 있도록 도와줍니다.


## 주요 기능

- **키보드 테스트**
  - 모든 키의 동작 상태를 실시간으로 확인합니다.
  - 누르지 않은 키를 별도 창에서 확인할 수 있습니다.
- **카메라 테스트**
  - 내장된 카메라(웹캠)의 동작 상태를 점검합니다.
- **USB 포트 테스트**
  - USB 포트의 연결 상태를 WMI를 이용하여 점검합니다.
- **충전 상태 점검**
  - 배터리 충전 여부와 충전기의 인식 상태를 확인합니다.
- **배터리 리포트**
  - Windows의 `powercfg` 명령어를 이용해 배터리 리포트를 HTML 파일로 생성합니다.
- **QR 코드 생성**
  - 위 5개의 테스트를 모두 수행한 후, 테스트 결과를 JSON 형식으로 정리하여 QR 코드로 생성합니다.

---

## 개발 환경

- **Python 버전:** 3.11.9
- **GUI 라이브러리:** ttkbootstrap (최신 버전)
- **주요 사용 라이브러리:**
  - `ttkbootstrap`
  - `PIL (Pillow)`
  - `opencv-python`
  - `pywin32`
  - `psutil`
  - `qrcode`

---

## 사용법

1. 프로그램 실행 시 메인 창이 나타나며, 각 테스트 항목(키보드, 카메라, USB, 충전, 배터리, QR 코드)이 카드 형태로 표시됩니다.
2. 원하는 테스트 카드(예: "키보드")를 클릭하면 해당 테스트 창이 열리며, 테스트가 시작됩니다.
3. 테스트 진행 도중 각 항목의 상태는 이미지와 메시지 박스로 확인할 수 있습니다.
4. 모든 테스트가 완료되면, 테스트 결과에 따라 QR 코드가 생성됩니다.
5. 생성된 배터리 리포트나 QR 코드는 별도의 창에서 확인할 수 있습니다.

---

## 실행 파일 빌드 방법 (개발자용)

아래는 PyInstaller를 사용하여 단일 실행 파일로 빌드하는 예시입니다:

##  pyinstaller 빌드 명령 예시
### port의 수를 인식

---
### port 3개
```bash
python -m PyInstaller --onefile --windowed \
--icon "./resource/image/kkomdae.ico" \
--add-data "./resource/image/ssafy_logo.png;resource/image/" \
--add-data "./resource/image/kkomdae.ico;resource/image" \
--add-data "./resource/image/keyboard.png;resource/image" \
--add-data "./resource/image/camera.png;resource/image" \
--add-data "./resource/image/usb.png;resource/image" \
--add-data "./resource/image/charging.png;resource/image" \
--add-data "./resource/image/battery.png;resource/image" \
--add-data "./resource/image/qrcode.png;resource/image" \
--add-data "./resource/font/SamsungSharpSans-Bold.ttf;resource/font" \
--add-data "./resource/font/SamsungOne-400.ttf;resource/font" \
--add-data "./resource/font/SamsungOne-700.ttf;resource/font" \
--add-data "./resource/font/NotoSansKR-VariableFont_wght.ttf;resource/font" \
kkomdae_random_port3.py
```
---
### port 1개
```bash
python -m PyInstaller --onefile --windowed \
--icon "./resource/image/kkomdae.ico" \
--add-data "./resource/image/ssafy_logo.png;resource/image/" \
--add-data "./resource/image/kkomdae.ico;resource/image" \
--add-data "./resource/image/keyboard.png;resource/image" \
--add-data "./resource/image/camera.png;resource/image" \
--add-data "./resource/image/usb.png;resource/image" \
--add-data "./resource/image/charging.png;resource/image" \
--add-data "./resource/image/battery.png;resource/image" \
--add-data "./resource/image/qrcode.png;resource/image" \
--add-data "./resource/font/SamsungSharpSans-Bold.ttf;resource/font" \
--add-data "./resource/font/SamsungOne-400.ttf;resource/font" \
--add-data "./resource/font/SamsungOne-700.ttf;resource/font" \
--add-data "./resource/font/NotoSansKR-VariableFont_wght.ttf;resource/font" \
kkomdae_random_port1.py
```

> **참고:** 빌드 시 `resource_path()` 함수를 통해 리소스 파일의 경로를 동적으로 설정하므로, 리소스 파일들이 올바른 위치에 있어야 합니다.

---

## 주의 사항

- **운영체제:** 프로그램은 Windows 10 이상에서만 동작합니다.
- **보안 경고:** 최초 실행 시 Windows 보안 경고가 나타날 수 있으므로, 실행을 허용해야 정상 작동합니다.
- **Python 설치:** 빌드된 실행 파일은 별도의 Python 설치 없이 실행할 수 있도록 구성되어 있습니다.

---

## 추가 참고 자료

- **ttkbootstrap 문서:** [ttkbootstrap GitHub](https://github.com/israel-dryer/ttkbootstrap)
- **Pillow 문서:** [Pillow 공식 문서](https://pillow.readthedocs.io/)
- **OpenCV 문서:** [OpenCV 공식 문서](https://docs.opencv.org/)
