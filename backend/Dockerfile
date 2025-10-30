# Build stage
FROM gradle:8.14.3-jdk25 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle gradle

# Copy source code
COPY src ./src

# Build application
RUN gradle clean build -x test

# Runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
