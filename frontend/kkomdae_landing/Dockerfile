# 1 Node.js22.14버전을 기반 이미지로 사용한다. builder로 이후 단계에서 이 이미지를 참조할 수 있도록 한다.
FROM node:22.14 AS builder

# 2 컨테이너 내부의 작업 경로를 /app으로 설정한다.
WORKDIR /app

# 3 package.json과 package-lock.json파일을 작업 경로로 복사한다.
COPY package*.json ./

# 4 package.json에 정이된 의존성 패키지를 설치한다.
RUN npm install

# 5 프로젝트의 모든 소스 코드를 작업 경로로 복사한다. (/app)
COPY . .

# 6 package.json에 정의된 build 스크립트 실행, Vite를 사용하여 React 애플리케이션 빌드
RUN npm run build

# --- Production Stage ---
# 7 Nginx 경량화 버전을 기반 이미지로 사용용
FROM nginx:alpine

# 8 빌드 스테이지에서 빌드된 결과물을 Nginx 웹 루트 경로로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# # 9 사용자 정의 conf파일을 Nginx 설정 경로로 복사한다. 이를 통해 Nginx동작 커스터마이징 가능
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# 10 80포트로 외부 노출
EXPOSE 80

# 11 Nginx를 데몬 모드가 아닌 포그라운드 모드로 실행
CMD ["nginx", "-g", "daemon off;"]


