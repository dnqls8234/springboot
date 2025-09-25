# Multi-stage build for UMS Service
FROM gradle:8.5-jdk21-alpine AS build

# Set working directory
WORKDIR /app

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

# Copy source code
COPY src ./src

# Build the application
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:21-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r ums && useradd -r -g ums ums

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership to app user
RUN chown -R ums:ums /app

# Switch to app user
USER ums

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseG1GC \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]