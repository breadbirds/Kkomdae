import { useEffect } from "react";
import Header from "./components/Header";
import Hero from "./components/Hero";
import Features from "./components/Features";
import HowItWorks from "./components/HowItWorks";
import Download from "./components/Download";
import Footer from "./components/Footer";
import "./styles/global.css";
import { initScrollAnimations } from "./utils/animations";

function App() {
  useEffect(() => {
    // 스크롤 애니메이션 초기화
    initScrollAnimations();

    // 모바일 메뉴 토글 기능
    const menuToggle = document.querySelector(".menu-toggle");
    const nav = document.querySelector(".nav");

    if (menuToggle && nav) {
      menuToggle.addEventListener("click", () => {
        menuToggle.classList.toggle("active");
        nav.classList.toggle("open");
      });
    }

    // 페이지 로드시 히어로 섹션의 이미지 애니메이션
    setTimeout(() => {
      const fadeInImages = document.querySelectorAll(".fade-in-image");
      fadeInImages.forEach((img) => {
        img.classList.add("visible");
      });
    }, 500);
  }, []);

  return (
    <>
      <Header />
      <main>
        <Hero />
        <Features />
        <HowItWorks />
        <Download />
      </main>
      <Footer />
    </>
  );
}

export default App;
