# ===== Build Stage =====
FROM gradle:9.2.0-jdk25-alpine AS build
WORKDIR /app

# Gradle wrapper 및 설정 파일 복사
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

# 실행 권한 부여
RUN chmod +x gradlew || true

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN ./gradlew clean build -x test --no-daemon

# ===== Runtime Stage =====
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션과 함께 실행 (메모리 자동 조정)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]