
# Mushroom Identification Backend

This is the backend for the Mushroom Identification System – a RESTful service built using Spring Boot. It allows users to submit mushrooms for expert review and enables admins to manage and classify those submissions.

>For an in depth description of the application please visit our [WIKI](https://github.com/jensmjahle/mushroom-identification-backend/wiki).
---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Running Locally](#running-locally)
  - [Environment Variables](#environment-variables)
  - [Using a .env File (Development Only)](#using-a-env-file-development-only)
  - [Supplying Environment Variables at Runtime](#supplying-environment-variables-at-runtime)
  - [Setting Environment Variables in application.properties](#setting-environment-variables-in-applicationproperties)
  - [Setting Environment Variables in OS (Production)](#setting-environment-variables-in-os-production)
  - [Developer Mode with H2](#developer-mode-with-h2)
- [Start with Docker](#start-with-docker)
- [Run Manually](#run-manually)
- [API Overview](#api-overview)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Security](#security)
- [Testing](#testing)
- [License](#license)

---

## Features

- Anonymous mushroom request submission with image upload
- Reference-code-based access for users
- Admin login with JWT authentication
- Admin endpoints for processing, messaging, and classifying requests
- Statistics and CSV export
- Robust validation, exception handling, and security

## Tech Stack

- Java 21 + Spring Boot
- Maven
- PostgreSQL
- Spring Security + JWT
- JPA + Hibernate
- Docker + Docker Compose
- JUnit 5

## Project Structure

```
src/
├── main/
│   ├── java/ntnu/idi/mushroomidentificationbackend/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── handler/
│   │   ├── listener/
│   │   ├── mapper/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   ├── task/
│   │   ├── util/
│   ├── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── static/
```

## Running Locally

This service requires a relational database (PostgreSQL) and Java 21+ to run.  
For day-to-day development and quick testing, you can switch to an in-memory H2 database by activating the `dev` profile and using the standardized `.env.example` file.
This also deactivates Spring Security, allowing you to test the API without authentication.

### 1. Prepare your environment variables

All required variables are listed in `.env.example`. Copy it to `.env` and fill in your own values:

```bash
cp .env.example .env
```
Then open `.env` in your editor and replace the placeholders:
```
DB_URL, DB_USERNAME, DB_PASSWORD, SECRET_KEY, LOOKUP_SALT
```

#### 1.1 Using a .env File (Development Only)

```bash
docker-compose --env-file .env up --build
```

#### 1.2 Supplying Environment Variables at Runtime

If you prefer not to use a `.env` file, set the environment variables directly in your terminal before running the application:

```bash
DB_URL=... DB_USERNAME=... DB_PASSWORD=... mvn spring-boot:run
```

#### 1.3 Setting Environment Variables in application.properties

You can reference environment variables in `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
security.jwt.secret-key=${SECRET_KEY}
security.lookup.salt=${LOOKUP_SALT}
```

#### 1.4 Setting Environment Variables in OS (Production)
For production, set the environment variables in your operating system or container environment. This is the recommended approach for security and flexibility.

```bash
export DB_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
export SECRET_KEY=...
export LOOKUP_SALT=...
mvn spring-boot:run
```
### 2. Set cross-origin resource sharing (CORS) in application.properties
To allow your frontend to access the backend, set the CORS configuration in `application.properties` to your frontend URL:

```properties
app.cors.allowed-origins==http://localhost:5173
```

### 3. Run with PostgreSQL

Ensure your PostgreSQL instance is running and matches the `DB_URL` in your `.env`, then:

```bash
docker-compose --env-file .env up --build
# or, without Docker:
mvn spring-boot:run
```

### 4. Developer Mode with H2

If you don’t have PostgreSQL available, use the embedded H2 database by activating the `dev` profile:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

## Start with Docker

```bash
docker-compose up --build
```

## Run Manually

```bash
mvn spring-boot:run
```


## API Overview

The backend exposes a set of REST- and WebSocket-based endpoints, organized by authentication level and functionality.

---

### Authentication Endpoints

| Method | Endpoint               | Description                                                      |
|--------|------------------------|------------------------------------------------------------------|
| POST   | `/auth/admin/login`    | Authenticate an admin (expert) user and receive a JWT token.    |
| POST   | `/auth/user/login`     | Authenticate an anonymous user for chat access and receive a JWT token. |

---

### Public / User Endpoints

#### Request Submission & Retrieval

| Method | Endpoint                    | Description                                                    |
|--------|-----------------------------|----------------------------------------------------------------|
| POST   | `/api/requests/create`      | Submit a new mushroom identification request anonymously.      |
| GET    | `/api/requests/me`          | Retrieve all requests submitted by the current user (by reference code). |

#### Mushroom Images

| Method | Endpoint                                  | Description                                                     |
|--------|-------------------------------------------|-----------------------------------------------------------------|
| GET    | `/api/mushrooms/{requestId}`              | Fetch all mushroom images and metadata for a given request.     |
| POST   | `/api/mushrooms/{requestId}/image`        | Upload additional images to an existing request.                |

#### Chat / Messaging

| Method | Endpoint                                 | Description                                                     |
|--------|------------------------------------------|-----------------------------------------------------------------|
| GET    | `/api/messages/{requestId}`              | Fetch the chat history (messages) associated with a request.    |

#### Image Retrieval

| Method | Endpoint                                 | Description                                                     |
|--------|------------------------------------------|-----------------------------------------------------------------|
| GET    | `/api/images?token={signedToken}`        | Download an uploaded image using a signed, time-limited token.  |

#### Usage Statistics

| Method | Endpoint                                     | Description                                                    |
|--------|----------------------------------------------|----------------------------------------------------------------|
| POST   | `/api/stats/registration-button-press`       | Record a “registration” button press for analytics purposes.   |

---

### Admin Endpoints

#### Admin Account

| Method | Endpoint                            | Description                                                      |
|--------|-------------------------------------|------------------------------------------------------------------|
| GET    | `/api/admin/me`                     | Retrieve the profile of the currently authenticated admin.       |
| PUT    | `/api/admin/profile`                | Update the authenticated admin’s profile details.                |
| PUT    | `/api/admin/password`               | Change the authenticated admin’s password.                       |
| DELETE | `/api/admin/delete`                 | Delete the authenticated admin’s own account.                    |

#### Superuser Operations

| Method | Endpoint                                         | Description                                                    |
|--------|--------------------------------------------------|----------------------------------------------------------------|
| POST   | `/api/admin/superuser/create`                    | Create a new admin account (superuser only).                   |
| DELETE | `/api/admin/superuser/delete/{username}`         | Delete any admin account by username (superuser only).         |

#### Request Management

| Method | Endpoint                                 | Description                                                      |
|--------|------------------------------------------|------------------------------------------------------------------|
| GET    | `/api/admin/requests`                    | List all user requests (admin queue).                            |
| GET    | `/api/admin/requests/next`               | Retrieve the next pending request in the queue.                  |
| GET    | `/api/admin/requests/count`              | Get the count of pending requests.                               |
| GET    | `/api/admin/requests/{requestId}`        | Fetch detailed information for a specific request.               |
| POST   | `/api/admin/requests/change-status`      | Update the status of a user request.                             |

#### Mushroom Classification

| Method | Endpoint                                         | Description                                                  |
|--------|--------------------------------------------------|--------------------------------------------------------------|
| POST   | `/api/admin/mushrooms/{requestId}/status`        | Update the classification status for mushrooms in a request. |

#### Statistics & Reporting

| Method | Endpoint                                 | Description                                                      |
|--------|------------------------------------------|------------------------------------------------------------------|
| GET    | `/api/admin/stats/overview`              | Retrieve an overview of processing statistics.                   |
| GET    | `/api/admin/stats/rate`                  | Get the current processing rate (requests per unit time).        |
| GET    | `/api/admin/stats/categories`            | Retrieve statistics broken down by classification categories.    |
| GET    | `/api/admin/stats/export/csv`            | Export request data as a CSV file.                               |
| GET    | `/api/admin/stats/export/pdf`            | Export request data as a PDF report.                             |

---

### WebSocket Endpoints

| Destination                      | Description                                                    |
|----------------------------------|----------------------------------------------------------------|
| `/app/chat/{requestId}`          | Send a new chat message for a given request (STOMP over WS).   |
| `/topic/chat/{requestId}`        | Subscribe to receive real-time chat messages for a request.    |
| `/api/websocket/admins/online-count` | (REST) Get the number of admin users currently connected.      |

## API Documentation (Swagger)

### Overview

Swagger provides an interactive UI to explore and test the APIs in the application.

### Swagger UI Access

Once the application is running, Swagger UI can be accessed at:

```
http://localhost:8080/swagger-ui.html
```

### API Documentation Path

```
http://localhost:8080/v3/api-docs
```

### Configuration

Swagger is enabled only in the `dev` profile.

To enable it:

In `application.properties`:

```properties
spring.profiles.active=dev
```

Or pass via command line:

```bash
java -Dspring.profiles.active=dev -jar target/your-app.jar
```

## Security
Read the [WIKI](https://github.com/jensmjahle/mushroom-identification-backend/wiki) for more information on security.
- JWT authentication and bcrypt password hashing
- HTTPS recommended for production
- CORS configured for frontend
- SQL Injection/XSS protection via validation

## Testing

```bash
mvn test
```

- Unit tests (JUnit 5)
- Integration tests
- Coverage reports supported

### Display test coverage:

1. Navigate to the root folder of the project

2. Run the following command to generate the test coverage report:

```sh
 mvn clean test
```

3. Navigate to the jacoco folder

```sh
 cd target/site/jacoco
```

4. Open the index.html file in a web browser

```sh
  start index.html
  ```

5. The test coverage report will be displayed in the web browser

## Display java documentation:

1. Navigate to the root folder of the project
2. Run the following command to generate the java documentation.

```sh
 mvn javadoc:javadoc
```

3. Navigate to the apidocs folder

```sh
 cd target/site/apidocs
```

4. Open the index.html file in a web browser

```sh
  start index.html
  ```


## License
This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for full details.