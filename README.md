# Banking App Backend

Spring Boot backend for the banking app login flow. It uses:

- Spring Web
- Spring Security
- Spring Data JPA
- H2 in-memory database
- REST API
- Swagger UI
- JUnit unit tests
- Cucumber functional tests

## Run

Install Maven if it is not already installed, then run:

```bash
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8090
```

## API Docs

Swagger UI:

```text
http://localhost:8090/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8090/v3/api-docs
```

## Tests

Run unit and functional tests:

```bash
mvn test
```

The test suite includes:

- JUnit tests for `AuthService`
- Cucumber functional test for register, login, and `/api/me`
- In-memory H2 database for the app and Cucumber Spring Boot test

## Auth API

Register a user:

```bash
curl -X POST http://localhost:8090/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}'
```

Login:

```bash
curl -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}'
```

The login response includes a token:

```json
{
  "token": "your-token",
  "username": "demo"
}
```

Use the token for protected routes:

```bash
curl http://localhost:8090/api/me \
  -H "Authorization: Bearer your-token"
```

## H2 Console

Open:

```text
http://localhost:8090/h2-console
```

Use these settings:

```text
JDBC URL: jdbc:h2:mem:banking-app
User Name: sa
Password:
```

Because this is an in-memory database, data is reset each time the backend restarts.
