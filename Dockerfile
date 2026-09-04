# ==========================================
# Multi-stage build for Personal Finance Manager
# ==========================================

# 1. Build Stage
FROM maven:3.9.9-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built jar from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Configure JVM flags and launch application
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

