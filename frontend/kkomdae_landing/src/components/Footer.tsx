import "../styles/Footer.css";
import axios from "axios";

const Footer: React.FC = () => {


  const handleDownload = async () => {
    try {
      // 배포
      const response = await axios.get(
        `https://j12d101.p.ssafy.io/django/s3app/presigned-url/?file=kkomdae_diagnostics.exe`
      );

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
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-logo">
            <span className="logo-text-footer">꼼대</span>
            <p className="footer-description">
              노트북 관리의 새로운 기준, 꼼대가 SSAFY 교육생 여러분의 노트북을
              안전하게 지켜드립니다.
            </p>
          </div>

          <div className="footer-links">
            <div className="footer-links-column">
              <h4 className="footer-links-title">서비스</h4>
              <ul className="footer-links-list">
                <li>
                  <a href="#features">주요 기능</a>
                </li>
                <li>
                  <a href="#how-it-works">이용 방법</a>
                </li>
                <li>
                  <a href="#faq">자주 묻는 질문</a>
                </li>
              </ul>
            </div>

            <div className="footer-links-column">
              <h4 className="footer-links-title">다운로드</h4>
              <ul className="footer-links-list">
                <li>
                  <a href="#">iOS</a>
                </li>
                <li>
                  <a href="#">Android</a>
                </li>
                <li>
                  <button onClick={handleDownload} className="footer-modal-link">
                    자가진단 프로그램
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <p className="copyright">
            © 2025 꼼대(KKOMDAE). All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
