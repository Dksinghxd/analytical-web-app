# Multi-stage Docker build for BFIS Backend
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent POM and module POMs
COPY pom.xml .
COPY bfis-core/pom.xml bfis-core/
COPY bfis-analyzer/pom.xml bfis-analyzer/
COPY bfis-ingestor/pom.xml bfis-ingestor/
COPY bfis-api/pom.xml bfis-api/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY bfis-core/src bfis-core/src
COPY bfis-analyzer/src bfis-analyzer/src
COPY bfis-ingestor/src bfis-ingestor/src
COPY bfis-api/src bfis-api/src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/bfis-api/target/bfis-api-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
