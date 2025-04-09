import kkomdae_logo from "../assets/kkomdae_logo.svg";
import { useEffect, useState } from "react";
import { initScrollAnimations } from "../utils/animations";
import "../styles/Download.css";
import qrCodeImage from "../assets/qr-code.png";
import Modal from "./Modal";

const Download: React.FC = () => {
  const [showQRModal, setShowQRModal] = useState(false);
  const [showDownloadModal, setShowDownloadModal] = useState(false);

  useEffect(() => {
    initScrollAnimations();
  }, []);

  const handleAppDownload = () => {
    setShowQRModal(true);
  };

  const closeQRModal = () => {
    setShowQRModal(false);
  };

  const handleDownload = () => {
    setShowDownloadModal(true);
  };

  return (
    <>
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

      <Modal 
        isOpen={showDownloadModal} 
        onClose={() => setShowDownloadModal(false)}
        overlayClose={true}
      />
    </>
  );
};

export default Download;
