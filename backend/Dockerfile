# Build stage
FROM gradle:9.2.0-jdk25-alpine AS build
WORKDIR /app

# Copy gradle wrapper (if exists)
COPY gradlew* ./
COPY gradle gradle

# Copy gradle files
COPY build.gradle settings.gradle ./

# Download dependencies (for caching)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application with JVM options
ENTRYPOINT ["java", \
    "-Xms4g", \
    "-Xmx12g", \
    "-jar", "/app/app.jar"]