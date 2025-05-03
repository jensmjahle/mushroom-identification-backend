FROM eclipse-temurin:21-jdk-alpine

# Install dependencies for Cloud SQL JDBC Socket Factory
RUN apk add --no-cache libc6-compat libpq

WORKDIR /app

# Add the Cloud SQL JDBC Socket Factory manually
ADD https://repo1.maven.org/maven2/com/google/cloud/sql/postgres-socket-factory/1.13.1/postgres-socket-factory-1.13.1.jar /app/lib/postgres-socket-factory.jar

# Add the app JAR
COPY target/mushroom-identification-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar:/app/lib/postgres-socket-factory.jar", "org.springframework.boot.loader.JarLauncher"]
