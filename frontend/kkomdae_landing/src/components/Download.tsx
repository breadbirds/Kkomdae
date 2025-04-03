import character from "../assets/character.svg";
import { useEffect } from "react";
import { initScrollAnimations } from "../utils/animations";
import "../styles/Download.css";
import axios from 'axios';

const Download: React.FC = () => {
  useEffect(() => {
    initScrollAnimations();
  }, []);

  const handleDownload = async () => {
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

  return (
    <section id="download" className="download">
      <div className="container">
        <div className="download-wrapper fade-in">
          <div className="download-content">
            <h2 className="download-title">지금 바로 꼼대를 다운로드하세요</h2>
            <p className="download-description">꼼대로 노트북 관리를 더 간편하고 효율적으로 시작해보세요. iOS와 Android에서 무료로 이용 가능합니다.</p>
            <div className="download-buttons">
              <a href="https://apps.apple.com/kr/app/" className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17.1839 12.0371C17.1716 9.53799 19.2531 8.16724 19.3305 8.11683C17.9878 6.16301 15.888 5.94993 15.1396 5.92358C13.4343 5.74686 11.776 6.91333 10.9155 6.91333C10.0259 6.91333 8.66613 5.94407 7.25105 5.97628C5.39358 6.00849 3.6873 7.05773 2.7499 8.65255C0.820501 11.8992 2.22999 16.679 4.07453 19.136C5.00311 20.338 6.09098 21.67 7.48578 21.6183C8.82815 21.5626 9.31498 20.705 10.9155 20.705C12.4988 20.705 12.951 21.6183 14.3633 21.5861C15.8069 21.5626 16.7633 20.3864 17.6674 19.168C18.7357 17.783 19.1606 16.4317 19.1758 16.3721C19.14 16.3584 17.1976 15.5806 17.1839 12.0371Z"></path>
                  <path d="M14.5555 4.05063C15.3023 3.1342 15.8028 1.86541 15.6715 0.572266C14.5798 0.615248 13.2466 1.29354 12.4695 2.17864C11.7873 2.95551 11.1848 4.28202 11.3283 5.51672C12.5597 5.61167 13.7783 4.93865 14.5555 4.05063Z"></path>
                </svg>
                <span>
                  <small>다운로드</small>
                  App Store
                </span>
              </a>
              <a href="https://play.google.com/store/apps/" className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3.00293 2.82257C2.83071 3.00921 2.74731 3.2743 2.74731 3.60694V20.9719C2.74731 21.3045 2.83071 21.5696 3.00293 21.7563L3.09523 21.8398L12.7614 12.3455V12.2267L3.09523 2.73911L3.00293 2.82257Z"></path>
                  <path d="M16.9365 16.4329L12.7607 12.3455V12.2267L16.9373 8.14043L17.0482 8.20521L22.0011 10.9778C23.3653 11.7429 23.3653 12.8294 22.0011 13.5952L17.0482 16.3677L16.9365 16.4329Z"></path>
                  <path d="M16.9366 16.3677L12.7608 12.2861L3.00226 21.7563C3.42251 22.2011 4.10367 22.2616 4.86457 21.8179L16.9366 16.3677Z"></path>
                  <path d="M16.9366 8.14036L4.86457 2.69011C4.10367 2.24636 3.42251 2.3076 3.00226 2.75239L12.7608 12.2267L16.9366 8.14036Z"></path>
                </svg>
                <span>
                  <small>다운로드</small>
                  Google Play
                </span>
              </a>
            </div>
            <div className="download-note">
              <p>* 자가진단 프로그램은 Windows PC에서만 실행 가능합니다.</p>
              <button onClick={handleDownload} className="diagnostics-link">
                자가진단 프로그램 다운로드
              </button>
            </div>
          </div>
          <div className="download-image">
            <img src={character} alt="꼼대 앱 스크린샷" />
            <div className="download-circle-1"></div>
            <div className="download-circle-2"></div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Download;
