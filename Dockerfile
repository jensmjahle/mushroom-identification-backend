# Use Java 21
FROM eclipse-temurin:21-jdk-alpine

# Create a working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR from target to the container
COPY target/mushroom-identification-backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port your Spring Boot application listens on
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
