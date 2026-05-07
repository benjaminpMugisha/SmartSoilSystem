# ══════════════════════════════════════════════════════════════
# Dockerfile — Smart Soil Nutrient Monitoring & Crop Advisory System
# AUCA SENG 8240 | Benjamin Mugisha Prince | 26979
# ══════════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and POM first (layer caching — faster rebuilds)
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download all dependencies (cached layer unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copy source code and build the JAR
COPY src ./src
RUN ./mvnw package -DskipTests --no-transfer-progress

# ── Stage 2: Production Runtime (minimal image) ───────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security best practice: run as non-root user
RUN addgroup -S rabgroup && adduser -S rabuser -G rabgroup
USER rabuser

# Copy only the final JAR from build stage
COPY --from=build /app/target/smart-soil-system-1.0.0.jar app.jar

# Document exposed port
EXPOSE 8080

# Docker health check — used by docker-compose depends_on
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1

# Start the Spring Boot application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=prod", \
    "-jar", "/app/app.jar"]
