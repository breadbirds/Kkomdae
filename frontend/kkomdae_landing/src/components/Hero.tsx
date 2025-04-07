import { useEffect, useRef, useState } from "react";
import { typeWriter } from "../utils/animations";
import "../styles/Hero.css";
import axios from "axios";
import icon1 from "../assets/icon1.png";
import icon2 from "../assets/icon2.png";
import icon3 from "../assets/icon3.png";
import icon4 from "../assets/icon4.png";
import icon5 from "../assets/icon5.png";
import icon6 from "../assets/icon6.png";
import icon7 from "../assets/icon7.png";
import icon8 from "../assets/icon8.png";
import icon9 from "../assets/icon9.png";
import qrCodeImage from "../assets/qr-code.png";

const Hero: React.FC = () => {
  const titleRef = useRef<HTMLHeadingElement>(null);
  const [showQRModal, setShowQRModal] = useState(false);

  useEffect(() => {
    // 타이틀 타이핑 애니메이션 시작
    if (titleRef.current && titleRef.current.innerHTML === "") {
      typeWriter(titleRef.current, "꼼대와 함께하는<br>스마트한 노트북 관리", 100);
    }
  }, []);

  const handleSelfProgramDownload = async () => {
    try {
      // 배포
      const response = await axios.get(`https://j12d101.p.ssafy.io/django/s3app/presigned-url/?file=kkomdae_diagnostics.exe`);

      // 개발
      // const response = await axios.get(`http://127.0.0.1:8000/s3app/presigned-url/?file=kkomdae_diagnostics.exe`);

      const presignedUrl = response.data.url;
      console.log(response);

      // presigned URL로 브라우저를 이동하면 S3에서 직접 다운로드 시작
      window.location.href = presignedUrl;
    } catch (error) {
      console.error(error);
      alert("파일 다운로드 중 오류가 발생했습니다.");
    }
  };

  const handleAppDownload = () => {
    // QR 코드 모달을 표시합니다
    setShowQRModal(true);
  };

  const closeQRModal = () => {
    setShowQRModal(false);
  };

  return (
    <section className="hero">
      <div className="hero-background-shapes">
        <div className="shape shape-1"></div>
        <div className="shape shape-2"></div>
        <div className="shape shape-3"></div>
      </div>

      <div className="container">
        <div className="hero-grid">
          <div className="hero-content">
            <h1 ref={titleRef} className="hero-title"></h1>
            <div className="hero-tagline">
              <span className="highlight">AI</span>를 통한 <span className="highlight">노트북 외관 점검</span>부터, <span className="highlight">자가진단 프로그램</span>까지
            </div>

            <div className="hero-features">
              <div className="feature-item">
                <div className="check-icon">✓</div>
                <div className="feature-text">노트북 외관 AI 점검</div>
              </div>
              <div className="feature-item">
                <div className="check-icon">✓</div>
                <div className="feature-text">하드웨어 자가진단</div>
              </div>
              <div className="feature-item">
                <div className="check-icon">✓</div>
                <div className="feature-text">대여/반납 간소화</div>
              </div>
            </div>

            <div className="download-buttons">
              <button onClick={handleAppDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17 2H7c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-5 18c-.83 0-1.5-.67-1.5-1.5S11.17 17 12 17s1.5.67 1.5 1.5S12.83 20 12 20zm5-4H7V4h10v12z" />
                </svg>
                <span>
                  <small>다운로드</small>꼼대 설치하기
                </span>
              </button>
              <button onClick={handleSelfProgramDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                </svg>
                <span>
                  <small>다운로드</small>
                  자가진단 프로그램
                </span>
              </button>
            </div>

            <div className="hero-caption">
              <a href="#how-it-works" className="link-how">
                꼼대가 처음이신가요? 이용 방법 알아보기 →
              </a>
            </div>
          </div>

          <div className="hero-image">
            <div className="kkomdae-character">
              <div className="floating-icons">
                <img src={icon1} alt="아이콘" className="floating-icon icon-1" />
                <img src={icon3} alt="아이콘" className="floating-icon icon-3" />
                <img src={icon4} alt="아이콘" className="floating-icon icon-4" />
                <img src={icon5} alt="아이콘" className="floating-icon icon-5" />
                <img src={icon7} alt="아이콘" className="floating-icon icon-7" />
                <img src={icon8} alt="아이콘" className="floating-icon icon-8" />
              </div>

              <img src={icon2} alt="꼼대 캐릭터" className="kkomdae-main" />
              <img src={icon6} alt="아이콘" className="floating-icon icon-6" />
              <img src={icon9} alt="아이콘" className="floating-icon icon-9" />
            </div>
          </div>
        </div>
      </div>

      {/* QR 코드 모달 */}
      {showQRModal && (
        <div className="qr-modal-overlay" onClick={closeQRModal}>
          <div className="qr-modal" onClick={(e) => e.stopPropagation()}>
            <div className="qr-modal-header">
              <h3>꼼대 앱 설치하기</h3>
              <button className="close-button" onClick={closeQRModal}>
                ×
              </button>
            </div>
            <div className="qr-modal-content">
              <img src={qrCodeImage} alt="꼼대 앱 QR 코드" className="qr-code-image" />
              <p>QR 코드를 스캔하여 꼼대 앱을 설치하세요.</p>
              <p className="modal-note">* 모바일 기기에서만 이용 가능합니다.</p>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default Hero;
