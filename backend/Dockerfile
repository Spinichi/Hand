# ===== Build Stage =====
FROM gradle:9.2.0-jdk25-alpine AS build
WORKDIR /app

# 1단계: 빌드 스크립트만 먼저 복사 (의존성 캐싱)
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./
RUN chmod +x gradlew || true

# 2단계: 의존성만 먼저 다운로드 (레이어 캐싱)
RUN ./gradlew dependencies --no-daemon || true

# 3단계: 소스 코드 복사 (마지막에)
COPY src ./src

# 4단계: 빌드
RUN ./gradlew clean build -x test --no-daemon

# ===== Runtime Stage =====
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# JAR 파일 복사 (SNAPSHOT 버전만)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션과 함께 실행 (메모리 자동 조정)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]