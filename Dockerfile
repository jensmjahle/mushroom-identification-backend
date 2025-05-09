# Use a lightweight JDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy built JAR file into container (use your actual jar name)
COPY target/mushroom-identification-backend.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
