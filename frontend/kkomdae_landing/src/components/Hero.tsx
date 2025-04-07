import { useEffect, useRef } from "react";
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

const Hero: React.FC = () => {
  const titleRef = useRef<HTMLHeadingElement>(null);

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
  // const handleAppDownload = async () => {
  //   try {
  //     // 배포
  //     const response = await axios.get(`https://j12d101.p.ssafy.io/django/s3app/presigned-url/?file=kkomdae_diagnostics.exe`);

  //     // 개발
  //     // const response = await axios.get(`http://127.0.0.1:8000/s3app/presigned-url/?file=kkomdae_diagnostics.exe`);

  //     const presignedUrl = response.data.url;
  //     console.log(response);

  //     // presigned URL로 브라우저를 이동하면 S3에서 직접 다운로드 시작
  //     window.location.href = presignedUrl;
  //   } catch (error) {
  //     console.error(error);
  //     alert("파일 다운로드 중 오류가 발생했습니다.");
  //   }
  // };

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

            {/* <div className="hero-actions">
              <a href="#diagnostic" className="btn-action btn-diagnostic">
                자가진단 프로그램
              </a>
              <a href="#download" className="btn-action hero-download">
                앱 다운로드
              </a>
            </div> */}
            <div className="download-buttons">
              {/* Todo: handleAppDownload 수정 후 변경 필요 */}
              <button onClick={handleSelfProgramDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3.00293 2.82257C2.83071 3.00921 2.74731 3.2743 2.74731 3.60694V20.9719C2.74731 21.3045 2.83071 21.5696 3.00293 21.7563L3.09523 21.8398L12.7614 12.3455V12.2267L3.09523 2.73911L3.00293 2.82257Z"></path>
                  <path d="M16.9365 16.4329L12.7607 12.3455V12.2267L16.9373 8.14043L17.0482 8.20521L22.0011 10.9778C23.3653 11.7429 23.3653 12.8294 22.0011 13.5952L17.0482 16.3677L16.9365 16.4329Z"></path>
                  <path d="M16.9366 16.3677L12.7608 12.2861L3.00226 21.7563C3.42251 22.2011 4.10367 22.2616 4.86457 21.8179L16.9366 16.3677Z"></path>
                  <path d="M16.9366 8.14036L4.86457 2.69011C4.10367 2.24636 3.42251 2.3076 3.00226 2.75239L12.7608 12.2267L16.9366 8.14036Z"></path>
                </svg>
                <span>
                  <small>다운로드</small>꼼대 설치하기
                </span>
              </button>
              <button onClick={handleSelfProgramDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.1839 12.0371C17.1716 9.53799 19.2531 8.16724 19.3305 8.11683C17.9878 6.16301 15.888 5.94993 15.1396 5.92358C13.4343 5.74686 11.776 6.91333 10.9155 6.91333C10.0259 6.91333 8.66613 5.94407 7.25105 5.97628C5.39358 6.00849 3.6873 7.05773 2.7499 8.65255C0.820501 11.8992 2.22999 16.679 4.07453 19.136C5.00311 20.338 6.09098 21.67 7.48578 21.6183C8.82815 21.5626 9.31498 20.705 10.9155 20.705C12.4988 20.705 12.951 21.6183 14.3633 21.5861C15.8069 21.5626 16.7633 20.3864 17.6674 19.168C18.7357 17.783 19.1606 16.4317 19.1758 16.3721C19.14 16.3584 17.1976 15.5806 17.1839 12.0371Z"></path>
                  <path d="M14.5555 4.05063C15.3023 3.1342 15.8028 1.86541 15.6715 0.572266C14.5798 0.615248 13.2466 1.29354 12.4695 2.17864C11.7873 2.95551 11.1848 4.28202 11.3283 5.51672C12.5597 5.61167 13.7783 4.93865 14.5555 4.05063Z"></path>
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
    </section>
  );
};

export default Hero;
