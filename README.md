
# Mushroom Identification Backend

This is the backend for the Mushroom Identification System – a RESTful service built using Spring Boot. It allows users to submit mushrooms for expert review and enables admins to manage and classify those submissions.

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
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── handler/
│   ├── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       ├── static/
```

## Running Locally

### Environment Variables

```env
DB_URL=jdbc:postgresql://100.116.142.40:5432/casaos
DB_USERNAME=casaos
DB_PASSWORD=casaos
SECRET_KEY=your-256-bit-secret-your-256-bit-secret
LOOKUP_SALT=your-256-bit-secret-your-256-bit-secret-lookup-salt-lookup-salt-lookup-salt-lookup-salt
```

### Using a .env File (Development Only)

```bash
docker-compose --env-file .env up --build
```

### Supplying Environment Variables at Runtime

```bash
DB_URL=... DB_USERNAME=... DB_PASSWORD=... mvn spring-boot:run
```

### Setting Environment Variables in application.properties

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
security.jwt.secret-key=${SECRET_KEY}
security.lookup.salt=${LOOKUP_SALT}
```

### Setting Environment Variables in OS (Production)

```bash
export DB_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
export SECRET_KEY=...
export LOOKUP_SALT=...
mvn spring-boot:run
```

### Developer Mode with H2

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

### User Endpoints

| Method | Endpoint                    | Description                         |
|--------|-----------------------------|-------------------------------------|
| POST   | `/api/requests/create`      | Submit a new mushroom request       |
| GET    | `/api/requests/{code}`      | Fetch request by reference code     |

### Admin Endpoints

| Method | Endpoint                          | Description                         |
|--------|-----------------------------------|-------------------------------------|
| POST   | `/api/admin/auth/login`          | Admin login                         |
| GET    | `/api/admin/requests/queue`      | Get next in queue                   |
| PUT    | `/api/admin/requests/status`     | Update request status               |
| POST   | `/api/admin/messages`            | Send message to user                |
| GET    | `/api/admin/stats/overview`      | Get statistics overview             |
| GET    | `/api/admin/requests/export`     | Export requests to CSV              |

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

## License

This project is part of an academic bachelor project and is not licensed for commercial use without permission.
