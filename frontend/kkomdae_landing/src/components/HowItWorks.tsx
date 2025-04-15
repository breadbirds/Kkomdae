import { useEffect } from "react";
import "../styles/HowItWorks.css";
import mobileFrame from "../assets/mobile-frame.png";
import step1_gif from "../assets/step1.gif";
import step2_gif from "../assets/step2.gif";
import step3_gif from "../assets/step3.gif";

const HowItWorks = () => { 
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("visible");
          }
        });
      },
      {
        threshold: 0.2,
      }
    );

    document.querySelectorAll(".step-item").forEach((item) => {
      observer.observe(item);
    });

    return () => observer.disconnect();
  }, []);

  return (
    <section id="how-it-works" className="how-it-works">
      <div className="container">
        <div className="section-header">
          <h2 className="section-title">이용 방법</h2>
          <p className="section-subtitle">꼼대로 SSAFY 노트북 상태를 기록하고 관리하는 방법을 알려드립니다.</p>
        </div>

        <div className="steps-container">
          <div className="step-item">
            <div className="step-content-wrapper">
              <div className="step-header">
                <div className="step-number">1</div>
                <h3 className="step-title">노트북 외관 촬영</h3>
              </div>
              <div className="step-content">
                <p className="step-description">
                  노트북의 상태를 정확하게 진단하기 위해 꼼대 앱에서 안내하는 각도와 방법에 따라 노트북의 상판, 하판, 측면, 모니터, 키보드 등을 꼼꼼하게 촬영해주세요. 자동 촬영 기능을 활용하여 최적의
                  각도와 거리에서 촬영하세요.
                </p>
              </div>
            </div>
            <div className="step-image-wrapper">
              <div className="mobile-device-container">
                {/* 모바일 프레임 (고정 백그라운드) */}
                <div className="mobile-frame">
                  <img src={mobileFrame} alt="모바일 디바이스 프레임" className="device-frame" />
                </div>
                {/* GIF를 화면 내부에 오버레이 */}
                <div className="screen-content">
                  <img src={step1_gif} alt="노트북 사진 촬영 화면" className="screen-gif" />
                </div>
              </div>
            </div>
          </div>

          <div className="step-item">
            <div className="step-content-wrapper">
              <div className="step-header">
                <div className="step-number">2</div>
                <h3 className="step-title">자가진단 프로그램 실행</h3>
              </div>
              <div className="step-content">
                <p className="step-description">
                  모바일 앱에 표시되는 랜덤키를 노트북의 자가진단 프로그램에 입력하여 두 기기를 손쉽게 연결하세요. 기기 연동 후에는 모바일에서 편리하게 진단 결과를 확인할 수 있습니다.
                </p>
              </div>
            </div>
            <div className="step-image-wrapper">
              <div className="mobile-device-container">
                {/* 모바일 프레임 (고정 백그라운드) */}
                <div className="mobile-frame">
                  <img src={mobileFrame} alt="모바일 디바이스 프레임" className="device-frame" />
                </div>
                {/* GIF를 화면 내부에 오버레이 */}
                <div className="screen-content">
                  <img src={step2_gif} alt="자가진단 프로그램 실행 화면" className="screen-gif" />
                </div>
              </div>
            </div>
          </div>

          <div className="step-item">
            <div className="step-content-wrapper">
              <div className="step-header">
                <div className="step-number">3</div>
                <h3 className="step-title">AI 분석 및 보고서 생성</h3>
              </div>
              <div className="step-content">
                <p className="step-description">
                  입력하신 노트북 정보와 촬영된 사진을 AI가 분석하여 노트북의 상태를 정확하게 진단하고, 상세한 내용이 담긴 PDF 보고서를 자동으로 생성해드려요. 분석된 결과는 언제든지 재촬영하여 더
                  정확한 진단을 받으실 수 있습니다.
                </p>
              </div>
            </div>
            <div className="step-image-wrapper">
              <div className="mobile-device-container">
                {/* 모바일 프레임 (고정 백그라운드) */}
                <div className="mobile-frame">
                  <img src={mobileFrame} alt="모바일 디바이스 프레임" className="device-frame" />
                </div>
                {/* GIF를 화면 내부에 오버레이 */}
                <div className="screen-content">
                  <img src={step3_gif} alt="AI 분석 및 보고서 생성 화면" className="screen-gif" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="action-container">
          <a href="#download" className="btn-action">
            지금 시작하기
            <svg width="20" height="20" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M3.33334 8H12.6667" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M8 3.33334L12.6667 8.00001L8 12.6667" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
        </div>
      </div>
    </section>
  );
};

export default HowItWorks;
