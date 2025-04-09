# OpenJDK 17 이미지 사용
FROM openjdk:17-jdk-slim

# 애플리케이션 JAR 파일을 컨테이너로 복사
COPY *.jar /app/

# 애플리케이션 포트 설정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
