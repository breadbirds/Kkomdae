import React from 'react';
import axios from 'axios';
import '../styles/Modal.css'; // 모달 스타일

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  overlayClose?: boolean;
}

const Modal: React.FC<ModalProps> = ({ isOpen, onClose, overlayClose = true }) => {
  if (!isOpen) return null;

  const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (overlayClose && e.target === e.currentTarget) {
      onClose();
    }
  };

  // (1) 특정 파일명을 기반으로 Django 서버에서 presigned URL 획득 → 브라우저 이동
  const handleDownload = async (filename: string) => {
    const confirmed = window.confirm(
      "자가진단 프로그램을 다운로드하시겠습니까?\n" +
      "다운로드 후 실행하여 노트북 점검을 진행할 수 있습니다."
    );

    if (!confirmed) return;

    try {
      
      // 배포
      const response = await axios.get(`https://j12d101.p.ssafy.io/django/s3app/presigned-url/?file=${filename}`);

      // 개발 
      // const response = await axios.get(`http://127.0.0.1:8000/s3app/presigned-url/?file=${filename}`);
      
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
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal">
        <div className="modal-header">
          <h3>자가진단 프로그램 다운로드</h3>
          <button className="modal-close-btn" onClick={onClose}>
            &times;
          </button>
        </div>
        <div className="modal-body">
          <p>노트북의 USB 포트 수에 맞는 버전을 선택하여 다운로드하세요.</p>
          <p className="modal-warning">※ 다운로드 후 프로그램을 실행하여 노트북 점검을 진행할 수 있습니다.</p>
          <div className="modal-version-buttons">

            {/* (2) 기존처럼 a 태그 href를 직접 S3 링크로 하지 말고,
                onClick에서 서버에 요청하여 presigned URL을 동적으로 받아온다 */}
            <button
              className="modal-version-btn"
              onClick={() => handleDownload('kkomdae_random_port1.exe')}
            >
              USB 포트 1개 버전 다운로드 (13, 14세대)
            </button>

            <button
              className="modal-version-btn"
              onClick={() => handleDownload('kkomdae_random_port3.exe')}
            >
              USB 포트 3개 버전 다운로드 (10, 11세대)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Modal;
