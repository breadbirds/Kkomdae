// 스크롤시 요소가 화면에 나타날 때 애니메이션 실행
export const initScrollAnimations = (): void => {
  const observerOptions = {
    root: null,
    rootMargin: "0px",
    threshold: 0.1,
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add("visible");
        // 한번 보여진 요소는 더 이상 관찰하지 않음
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  const animatedElements = document.querySelectorAll(".fade-in");
  animatedElements.forEach((el) => observer.observe(el));
};

// 타이핑 애니메이션 효과
export const typeWriter = (element: HTMLElement, text: string, speed: number = 50, callback?: () => void): void => {
  let i = 0;
  let isInsideTag = false;
  let currentTag = "";

  function typing() {
    if (i < text.length) {
      // HTML 태그 처리
      if (text.charAt(i) === "<") {
        isInsideTag = true;
        currentTag += text.charAt(i);
      } else if (isInsideTag && text.charAt(i) === ">") {
        currentTag += text.charAt(i);
        element.innerHTML += currentTag;
        currentTag = "";
        isInsideTag = false;
      } else if (isInsideTag) {
        currentTag += text.charAt(i);
      } else {
        element.innerHTML += text.charAt(i);
      }

      i++;
      setTimeout(typing, speed);
    } else if (callback) {
      callback();
    }
  }

  // 타이핑 시작
  element.innerHTML = "";
  typing();
};

// 숫자 카운트 애니메이션
export const animateCounter = (element: HTMLElement, start: number, end: number, duration: number = 2000): void => {
  let startTimestamp: number | null = null;
  const step = (timestamp: number) => {
    if (!startTimestamp) startTimestamp = timestamp;
    const progress = Math.min((timestamp - startTimestamp) / duration, 1);
    const currentCount = Math.floor(progress * (end - start) + start);
    element.innerHTML = currentCount.toString();

    if (progress < 1) {
      window.requestAnimationFrame(step);
    } else {
      element.innerHTML = end.toString();
    }
  };

  window.requestAnimationFrame(step);
};

// 이미지 페이드 인 애니메이션
export const fadeInImages = (): void => {
  const images = document.querySelectorAll(".fade-in-image");

  images.forEach((img, index) => {
    setTimeout(() => {
      img.classList.add("visible");
    }, index * 200); // 각 이미지마다 시간차 두기
  });
};

// 헤더 스크롤 이벤트 (색 변경)
export const initHeaderScroll = (): void => {
  const header = document.querySelector("header");

  if (!header) return;

  window.addEventListener("scroll", () => {
    if (window.scrollY > 50) {
      header.classList.add("scrolled");
    } else {
      header.classList.remove("scrolled");
    }
  });
};
