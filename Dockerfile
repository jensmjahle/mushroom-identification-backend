# Stage 1: Build the app
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk-alpine

# Install dependencies for Cloud SQL JDBC Socket Factory
RUN apk add --no-cache libc6-compat libpq curl && \
    mkdir -p /app/lib && \
    curl -L -o /app/lib/postgres-socket-factory.jar https://repo1.maven.org/maven2/com/google/cloud/sql/postgres-socket-factory/1.13.1/postgres-socket-factory-1.13.1.jar

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=builder /build/target/mushroom-identification-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar:/app/lib/postgres-socket-factory.jar", "org.springframework.boot.loader.JarLauncher"]
