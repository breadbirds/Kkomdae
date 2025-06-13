# <img src="/docs/로고.png" width="15" alt="로고"> 꼼대 (꼼꼼하게 대신 점검해드립니다)

[<img src="/docs/onboarding.png" width="800">](https://kkomdae01.ssafyapp.com/)

**👆 위 이미지 클릭하면 `랜딩페이지로` 이동합니다 👆**

<br/>

<div id="1"></div>

## ✨ 개요

### 🥇 SSAFY 특화 PJT 1등 🥇 | [🥈영상 포트폴리오 2등 🥈](https://www.youtube.com/watch?v=AIQsZd09HSc)

**서비스명** : 꼼대 ( 꼼꼼하게 대신 점검해드립니다 )

**한줄 설명** : AI 모델을 활용한 장비 상태 관리 서비스

**도메인** : 영상 AI

**프로젝트 기간** : 2025.03.03 ~ 2025.04.11


## 목차

1. [**주요 기능**](#-주요-기능)
1. [**주요 기능별 화면 (유저 / 관리자)**](<#-주요-기능별-화면-(유저)>)
1. [**기술적 특징**](#기술적-특징)
1. [**기술 스택**](#-기술-스택)
1. [**프로젝트 진행 및 산출물**](#-프로젝트-진행-및-산출물)
1. [**개발 멤버 및 회고**](#-개발-멤버-및-역할분담)
1. [**메뉴얼 및 상세문서**](#-메뉴얼-및-상세-문서)

<br/>


<div id="2"></div>

## ✨ 주요 기능

### 서비스 설명

-   SSAFY에서 대여하는 노트북을 체계적으로 관리할 수 있는 서비스입니다. 
- 노트북 수령 및 반납시 필요한 수령확인서 작성 절차를 앱 하나로 간편화했습니다. 
- AI가 외관 결함을 자동으로 탐지하며 기기성능을 점검하는 기능도 제공합니다.

### 주요 기능

**1. 외관 결함 자동 탐지**
- YOLOv8, Faster R-CNN 기반 모델로 노트북 외관 6개 부위 자동 촬영 및 흠집 탐지
- 사용자 환경에 맞춘 데이터 파인튜닝 및 오탐 제거 전략 적용

**2. 하드웨어 성능 자가점검**

- 자체 제작 점검 프로그램을 통해 USB, 키보드, 충전기, 배터리 등 기능 점검
- 앱-프로그램 간 랜덤 코드 연동을 통한 결과 자동 전송

**3. OCR 기반 시리얼·바코드 인식**

- Google Vision OCR API 사용
- 카메라 촬영만으로 시리얼 번호 및 바코드 자동 인식 및 입력


**4. PDF 자동 생성 및 제출**
- 외관 이미지, 탐지 결과, 점검 결과, 서약서 포함 문서 자동 생성
- 앱에서 버튼 클릭만으로 관리자 서버에 제출

**5. 관리자 웹을 통한 장비 통합 관리**
- 장비별·교육생별 이력 조회 및 제출 상태 확인
- 미제출자 대상 알림 전송 기능 포함

<br/>

<div id="3"></div>

## 💻 주요 기능별 화면 (유저)

[👉 유저 서비스 화면 바로가기](https://github.com/breadbirds/Kkomdae/wiki/%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4#-%EA%BC%BC%EB%8C%80-%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4-%EC%9C%A0%EC%A0%80)

### 1. AI 를 활용한 외관 촬영

- 노트북 6개 부위(상판, 하판, 좌측면, 우측면, 모니터, 키보드)를 순서대로 촬영합니다.
- 화면 가이드를 따라 노트북을 맞추면 자동으로 사진이 촬영됩니다.
- 촬영 후 이미지를 확인하고, 재촬영 또는 다음 단계로 이동할 수 있습니다.
- 앱이 중간에 종료되더라도 마지막 진행 단계부터 이어서 촬영이 가능합니다.
- 각 단계별로 촬영 가이드가 함께 제공됩니다.

<img src="/docs/gif/꼼대_전면부_촬영.gif" width="250">

### 2. 하드웨어 성능 자가점검

- 관리자 조언을 바탕으로 제작된 성능 점검 프로그램을 통해 교육생이 직접 노트북 점검을 진행합니다.
- 앱에 표시된 랜덤 코드를 점검 프로그램에 입력하면 테스트가 시작되고, 결과는 자동으로 앱에 연동됩니다.
- 교육생이 자가 점검을 수행함으로써 관리자 업무 부담을 줄이고, 점검 효율을 높였습니다.

<img src="/docs/gif/자가점검.gif" width="800">

### 3. PDF 생성 및 간편 제출

- 외관 이미지, AI 분석 결과, 노트북 정보, 서약서 등 모든 정보를 하나의 PDF로 자동 정리합니다.
- `제출` 버튼 한 번으로 관리자 웹에 문서가 전송되어, 간편하게 점검 결과를 제출할 수 있습니다.

<img src="/docs/gif/녹음_2025-04-11_103538.gif" width="800">


## 🔧 주요 기능별 화면 (관리자)

[👉 관리자 서비스 화면 바로가기 🔗](https://github.com/breadbirds/Kkomdae/wiki/%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4#-%EA%BC%BC%EB%8C%80-%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4-%EA%B4%80%EB%A6%AC%EC%9E%90)

### 1. 관리자 서버에서 모든 기기 간편 관리

- 기기별·교육생별 이력을 한눈에 확인할 수 있어, 지속적이고 체계적인 장비 관리가 가능합니다.
- 노트북 점검 이력, 제출 상태, 성능 점검 결과 등을 통합적으로 관리할 수 있습니다.

<img src="/docs/gif/녹음_2025-04-10_135009_1_.gif" width="800">

<br/>

## ✨ 기술적 특징

### 1. 흠집 탐지 AI 개발
- Faster R-CNN 기반 모델 적용
  - 기존 YOLO 모델에서 Faster R-CNN으로 전환하여 작은 흠집 탐지 성능을 향상
- 약 2,800장의 데이터셋 구축
  - 일반 노트북 이미지 2,000장과 사용자 환경에 적합한 추가 이미지 800장을 수집하여 데이터셋을 구성
  - Bounding Box로 라벨링
- SSAFY 노트북에 최적화된 파인튜닝
  - 일반 모델을 학습한 뒤, SSAFY 노트북 데이터로 파인튜닝을 진행해 실제 사용 환경에 맞는 성능을 확보
- 레이어 고정을 통한 오탐·미탐 감소
  - 학습 시 일부 레이어를 고정함으로써 모델의 일반화 성능을 유지하고 오류 탐지를 최소화

<img src="/docs/모델 변경.jpg" width="800">

<img src="/docs/ai 성능 향상.jpg" width="800">

### 2. 노트북 탐지 모델 개발
- YOLO 기반 노트북 탐지 모델 구축
  - 외부 배경까지 탐지하던 기존 문제를 해결하기 위해, 노트북 자체를 인식하는 전용 탐지 모델을 별도로 개발
- 배경 필터링을 통한 정확도 향상
  - 모델이 노트북 외 영역을 오탐하지 않도록 배경 정보를 효과적으로 제거하였습니다.

<img src="/docs/노트북 탐지 모델.jpg" width="800">

### 3. 자동 촬영 AI 경량화 
- 사용자 촬영 품질 기준을 AI가 자동 판단
  - 흔들림 없이 선명하고, 일정한 각도와 조도를 만족할 때만 자동 촬영되도록 구현
- 모바일 실시간 처리를 위한 모델 경량화
  - YOLO 모델을 TFLite로 변환하여 온디바이스 환경에서도 빠르고 정확한 실시간 처리가 가능하도록 최적화

<img src="/docs/사진 품질 향상.jpg" width="800">

<img src="/docs/결과.jpg" width="800">

<div id="4"></div>

## 📚 기술 스택

### Backend

<div align=left> 
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> 
  <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">
  <img src="https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">
  <img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white">
  <img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/jitpack-000000?style=for-the-badge&logo=jitpack&logoColor=white">
  <img src="https://img.shields.io/badge/openjdk-000000?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white">
  <img src="https://img.shields.io/badge/thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">

</div>

### Frontend

<div align=left> 
  <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img src="https://img.shields.io/badge/openjdk-000000?style=for-the-badge&logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/lottiefiles-00DDB3?style=for-the-badge&logo=lottiefiles&logoColor=white">
  <img src="https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=white">
  <img src="https://img.shields.io/badge/android-34A853?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/androidstudio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white">
  <img src="https://img.shields.io/badge/xml-005FAD?style=for-the-badge&logo=xml&logoColor=white">
  <img src="https://img.shields.io/badge/jitpack-000000?style=for-the-badge&logo=jitpack&logoColor=white">
  <img src="https://img.shields.io/badge/Hilt-008FC7?style=for-the-badge&logo=Hilt&logoColor=white">
  <img src="https://img.shields.io/badge/googlecloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white">

</div>

### AI

<div align=left> 
  <img src="https://img.shields.io/badge/fastapi-009688?style=for-the-badge&logo=fastapi&logoColor=white">
  <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white">
  <img src="https://img.shields.io/badge/yolo-111F68?style=for-the-badge&logo=yolo&logoColor=white">
  <img src="https://img.shields.io/badge/fasterrcnn-0A9EDC?style=for-the-badge&logo=fasterrcnn&logoColor=white">
  <img src="https://img.shields.io/badge/opencv-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white">
  <img src="https://img.shields.io/badge/tensorflow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white">
  <img src="https://img.shields.io/badge/pytorch-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white">
  <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img src="https://img.shields.io/badge/jupyter-F37626?style=for-the-badge&logo=jupyter&logoColor=white">
  <img src="https://img.shields.io/badge/googlecolab-F9AB00?style=for-the-badge&logo=googlecolab&logoColor=white">
  <img src="https://img.shields.io/badge/googleforms-7248B9?style=for-the-badge&logo=googleforms&logoColor=white">

</div>

### Infra

<div align=left> 
  <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white">
  <img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
  <img src="https://img.shields.io/badge/letsencrypt-003A70?style=for-the-badge&logo=letsencrypt&logoColor=white">
  <img src="https://img.shields.io/badge/amazonec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
  <img src="https://img.shields.io/badge/amazons3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
  <img src="https://img.shields.io/badge/django-092E20?style=for-the-badge&logo=django&logoColor=white">
  <img src="https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white">

</div>

### Project Management & DevOps

<div align=left> 
  <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
  <img src="https://img.shields.io/badge/gitlab-FC6D26?style=for-the-badge&logo=gitlab&logoColor=white">
  <img src="https://img.shields.io/badge/mattermost-0058CC?style=for-the-badge&logo=mattermost&logoColor=white">
  <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  <img src="https://img.shields.io/badge/jira-0052CC?style=for-the-badge&logo=jira&logoColor=white">
  <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
  <img src="https://img.shields.io/badge/googledrive-4285F4?style=for-the-badge&logo=googledrive&logoColor=white">
 
</div>

<div id="5"></div>

## ✨ 프로젝트 진행 및 산출물

- [화면 설계서](https://github.com/breadbirds/Kkomdae/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC#%ED%99%94%EB%A9%B4-%EC%84%A4%EA%B3%84%EC%84%9C)
- [서비스 아키텍쳐](https://github.com/breadbirds/Kkomdae/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC#%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90)
- [API 명세서](https://github.com/breadbirds/Kkomdae/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC#api-%EB%AA%85%EC%84%B8%EC%84%9C)
- [ERD](https://github.com/breadbirds/Kkomdae/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC#erd)
- [GIT](https://github.com/breadbirds/Kkomdae/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC#git)

- 이슈관리
  <img src="/docs/번다운_차트.jpg" width="800">

<br/>

<div id="6"></div>

## 👨‍👩‍👧‍👦 개발 멤버 및 역할분담

<table>
  <tr>
    <td align="center" width="300">
      <a href="https://github.com/breadbirds">
        <img src="https://github.com/breadbirds.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>정유진</strong>
      </a>
    </td>
    <td align="center" width="300">
      <a href="https://github.com/dufrane-s">
        <img src="https://github.com/dufrane-s.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>김성수</strong>
      </a>
    </td>
    <td align="center" width="300">
      <a href="https://github.com/JeongEon8">
        <img src="https://github.com/JeongEon8.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>김정언</strong>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center">
      <sub>Leader & AI Developer</sub>
    </td>
    <td align="center">
      <sub>Backend</sub>
    </td>
    <td align="center">
      <sub>AI Engineer</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="300">
      <a href="https://github.com/DoSeungGuk">
        <img src="https://github.com/DoSeungGuk.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>도승국</strong>
      </a>
    </td>
    <td align="center" width="300">
      <a href="https://github.com/hyuksanglee">
        <img src="https://github.com/hyuksanglee.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>이상혁</strong>
      </a>
    </td>
    <td align="center" width="300">
      <a href="https://github.com/shlee1103">
        <img src="https://github.com/shlee1103.png" width="300" style="border-radius: 50%;">
        <br>
        <strong>이송희</strong>
      </a>
    </td>
  </tr>
  <tr>
    <td align="center">
      <sub>Infra & Backend</sub>
    </td>
    <td align="center">
      <sub>Frontend</sub>
    </td>
    <td align="center">
      <sub>Frontend</sub>
    </td>
  </tr>
</table>
<img src="/docs/역할분담.png" width="800">

<br/>

<div id="7"></div>

## 📒 메뉴얼 및 상세 문서

-   [포팅메뉴얼](https://github.com/breadbirds/Kkomdae/wiki/%ED%8F%AC%ED%8C%85-%EB%A9%94%EB%89%B4%EC%96%BC)
-   [Git](https://github.com/breadbirds/Kkomdae/wiki/Git)
-   [서비스 화면](https://github.com/breadbirds/Kkomdae/wiki/%EC%84%9C%EB%B9%84%EC%8A%A4-%ED%99%94%EB%A9%B4)
