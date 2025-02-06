# bachelor-mushroom-identification-backend
This is a bachelorproject created by Anders Emil Bergan and Jens Martin Jahle. 


## Running the project
### For development
> Note: This will use the dev profile, which is configured to use an in-memory database. This means that the data will not be persisted between restarts.
#### Windows Powershell
```powershell 
mvn spring-boot:run -D"spring-boot.run.profiles=dev"
```
#### Linux/macOS
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### For production
```
mvn spring-boot:run
```


## Swagger API Documentation

### Overview
Swagger provides an interactive UI to explore and test the APIs in the application.

### Swagger UI Access

Once the application is running, Swagger UI can be accessed by navigating to:

```
http://localhost:8080/swagger-ui.html
```

### API Documentation Path

The API documentation can be accessed at:

```
http://localhost:8080/v3/api-docs
```

### Configuration

Swagger UI is enabled only in the `dev` profile. You can enable it by setting the `spring.profiles.active` to `dev` in your `application.properties` file:

```properties
# application.properties
spring.profiles.active=dev
```

Alternatively, you can pass the profile argument when running the application:

```bash
java -Dspring.profiles.active=dev -jar target/your-app.jar
```
`


## 🚀 Docker Setup

### Prerequisites
- Docker installed ([Download Docker](https://www.docker.com/get-started))
- Ensure your application is built (JAR file should be located in `target` folder)

### Building and Running the Docker Container
Follow these steps to build and run your **Spring Boot application** using Docker:

1. **Build the Docker image**  
   Run the following command from the root of the project directory:
   ```
   docker build -t mushroom-identification-backend .
   ```

2. **Run the Docker container**  
   Start the container using:
   ```
   docker run -p 8080:8080 mushroom-identification-backend
   ```

   This maps the application’s `8080` port to your local `8080` port.

3. **Access the Application**  
   Open your browser and go to:
   ```
   http://localhost:8080
   ```

### Multi-Stage Build (Optional)
To reduce the size of your Docker image, you can use a multi-stage build:
```
# Stage 1: Build the application
FROM maven:3.9.1-eclipse-temurin-21 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/mushroom-identification-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Common Docker Commands
- **Stop a running container**:
  ```
  docker ps              # List running containers
  docker stop <CONTAINER_ID>
  ```
- **Remove a container**:
  ```
  docker rm <CONTAINER_ID>
  ```
- **Check Docker logs**:
  ```
  docker logs <CONTAINER_ID>
  ```
