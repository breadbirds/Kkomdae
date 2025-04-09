import { useEffect } from "react";
import "../styles/Header.css";
import { initHeaderScroll } from "../utils/animations";

const Header: React.FC = () => {
  useEffect(() => {
    initHeaderScroll();
  }, []);

  return (
    <header className="header">
      <div className="container header-container">
        <a href="/" className="logo">
          <span className="logo-text">꼼대</span>
        </a>

        <nav className="nav">
          <ul className="nav-list">
            <li className="nav-item">
              <a href="#features" className="nav-link">
                주요 기능
              </a>
            </li>
            <li className="nav-item">
              <a href="#how-it-works" className="nav-link">
                이용 방법
              </a>
            </li>
          </ul>
        </nav>

        <div className="header-buttons">
          <a href="#download" className="btn-download">
            다운로드
          </a>
        </div>

        <button className="menu-toggle" aria-label="메뉴 열기">
          <span className="bar"></span>
          <span className="bar"></span>
          <span className="bar"></span>
        </button>
      </div>
    </header>
  );
};

export default Header;
