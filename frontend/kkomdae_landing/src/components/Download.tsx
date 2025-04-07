import kkomdae_logo from "../assets/kkomdae_logo.svg";
import { useEffect } from "react";
import { initScrollAnimations } from "../utils/animations";
import "../styles/Download.css";
import axios from "axios";

const Download: React.FC = () => {
  useEffect(() => {
    initScrollAnimations();
  }, []);

  const handleAppDownload = () => {
    window.location.href = "https://drive.usercontent.google.com/download?id=12UrM-tJEcpOQhEA5i8ou_QmuQEkXagLH&export=download&authuser=0";
  };

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
            <p className="download-description">꼼대로 노트북 관리를 더 간편하고 효율적으로 시작해보세요.</p>
            <div className="download-buttons">
              <button onClick={handleAppDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M17 2H7c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-5 18c-.83 0-1.5-.67-1.5-1.5S11.17 17 12 17s1.5.67 1.5 1.5S12.83 20 12 20zm5-4H7V4h10v12z" />
                </svg>
                <span>
                  <small>다운로드</small>꼼대 설치하기
                </span>
              </button>
              <button onClick={handleDownload} className="download-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                </svg>
                <span>
                  <small>다운로드</small>
                  자가진단 프로그램
                </span>
              </button>
            </div>
            <div className="download-note">
              <p>* 자가진단 프로그램은 Windows PC에서만 실행 가능합니다.</p>
            </div>
          </div>
          <div className="download-image">
            <img src={kkomdae_logo} alt="꼼대 앱 스크린샷" />
            <div className="download-circle-1"></div>
            <div className="download-circle-2"></div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Download;
