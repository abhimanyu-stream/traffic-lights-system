# Multi-stage build for Traffic Light Controller API

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="stream.abhimanyu@gmail.com"
LABEL version="1.0.0"
LABEL description="Traffic Light Controller API"

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create app directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/traffic-light-service-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 9900

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9900/actuator/health || exit 1

# JVM options (can be overridden via JAVA_OPTS environment variable)
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

# Run application
# ENTRYPOINT is fixed, CMD can be overridden from docker run or docker-compose
ENTRYPOINT ["java"]
CMD ["-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/app/logs", "-jar", "app.jar", "--spring.profiles.active=prod"]
