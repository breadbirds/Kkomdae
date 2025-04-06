import { useEffect } from "react";
import { initScrollAnimations } from "../utils/animations";
import "../styles/Features.css";

const Features: React.FC = () => {
  useEffect(() => {
    initScrollAnimations();
  }, []);

  return (
    <section id="features" className="features section">
      <div className="container">
        <div className="section-header-feature fade-in">
          <h2 className="section-title-feature">주요 기능</h2>
          <p className="section-subtitle-feature">꼼대의 핵심 기능으로 노트북의 대여부터 반납까지 모든 과정을 스마트하게 관리해보세요.</p>
        </div>

        <div className="features-grid">
          <div className="feature-card fade-in">
            <div className="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </div>
            <h3 className="feature-title">AI 기반 노트북 상태 분석</h3>
            <p className="feature-description">AI를 통해 대여받은 SSAFY 노트북의 스크래치, 흠집 등 노트북의 결함을 자동으로 찾아냅니다.</p>
          </div>

          <div className="feature-card fade-in">
            <div className="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <h3 className="feature-title">자동화된 문서화 시스템</h3>
            <p className="feature-description">PDF 형태의 자동화된 문서화 시스템을 제공하여 기기 상태를 쉽게 기록하고 관리합니다.</p>
          </div>

          <div className="feature-card fade-in">
            <div className="feature-icon">
              <img src="/src/assets/connection.svg" alt="노트북 연동 자가진단" />
            </div>
            <h3 className="feature-title">노트북 연동 자가진단</h3>
            <p className="feature-description">자가진단 프로그램을 실행하고, 앱과 연동하여 모바일에서 결과를 확인할 수 있습니다.</p>
          </div>

          <div className="feature-card fade-in">
            <div className="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
            </div>
            <h3 className="feature-title">상세한 사진 촬영 가이드</h3>
            <p className="feature-description">상판, 하판, 측면, 화면, 키보드 등 노트북 외관 촬영 시 필요한 상세 가이드를 제공합니다.</p>
          </div>

          <div className="feature-card fade-in">
            <div className="feature-icon">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
              </svg>
            </div>
            <h3 className="feature-title">관리자 웹 대시보드</h3>
            <p className="feature-description">대여와 반납에 대한 기록들을 관리자 웹에서 편리하게 확인하고 관리할 수 있습니다.</p>
          </div>

          <div className="feature-card fade-in">
            <div className="feature-icon">
              <img src="/src/assets/calendar_reminder.svg" alt="반납 일정 관리" />
            </div>
            <h3 className="feature-title">반납 일정 관리</h3>
            <p className="feature-description">반납 기한이 가까워지면 알림을 제공하여 원활한 반납 절차를 도와줍니다.</p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Features;
