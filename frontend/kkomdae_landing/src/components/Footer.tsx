import "../styles/Footer.css";
import axios from "axios";
import { useState } from "react";
import qrCodeImage from "../assets/qr-code.png";
import Modal from "./Modal";

const Footer: React.FC = () => {
  const [showQRModal, setShowQRModal] = useState(false);
  const [showDownloadModal, setShowDownloadModal] = useState(false);

  const handleAppDownload = () => {
    setShowQRModal(true);
  };

  const closeQRModal = () => {
    setShowQRModal(false);
  };

  const handleDownload = async () => {
    try {
      setShowDownloadModal(true);
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
              노트북 관리의 새로운 기준,
              <br />
              꼼대가 SSAFY 교육생 여러분의 노트북을 안전하게 지켜드립니다.
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
              </ul>
            </div>

            <div className="footer-links-column">
              <h4 className="footer-links-title">다운로드</h4>
              <ul className="footer-links-list">
                <li>
                  <button onClick={handleAppDownload} className="footer-modal-link">
                    꼼대
                  </button>
                </li>
                <li>
                  <button onClick={handleDownload} className="footer-modal-link">
                    자가진단 프로그램 다운로드
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="footer-bottom">
          <p className="copyright">© 2025 꼼대(KKOMDAE). All rights reserved.</p>
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

      {/* Download Modal */}
      <Modal 
        isOpen={showDownloadModal} 
        onClose={() => setShowDownloadModal(false)}
        overlayClose={true}
      />
    </footer>
  );
};

export default Footer;
