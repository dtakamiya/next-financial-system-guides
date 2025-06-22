# sample01: DDD Bank System Sample

This is a sample banking application built with Domain-Driven Design (DDD) principles.
It is based on the series of guides (`guide01` to `guide06`) and demonstrates a clean architecture, tactical DDD patterns, and the Saga pattern for handling distributed transactions.

## Features

-   **Account Management**: Open accounts, deposit money, and inquire balances.
-   **Money Transfer**: Request money transfers between accounts, handled with eventual consistency via the Saga pattern.

## Tech Stack

-   Java 17
-   Spring Boot 3
-   MyBatis 3
-   Spock (for testing)
-   PostgreSQL / H2
-   Testcontainers
-   Docker

## Architecture

This project follows the **Clean Architecture** (Onion Architecture), strictly separating concerns into four layers:

1.  **Domain**: Core business logic, entities, value objects, and repository interfaces. Contains no technology-specific code.
2.  **Application**: Use cases that orchestrate the domain layer.
3.  **Infrastructure**: Implementations of interfaces defined in the domain layer (e.g., repositories using MyBatis, external service clients).
4.  **Presentation**: Exposes the application services as a REST API.

## How to Run

### Prerequisites

-   Java 17
-   Docker (for running PostgreSQL in tests or for production)

### Building the Application

You can build the project using the Gradle wrapper:

```bash
./gradlew build
```

### Running in Development Mode

To run the application with the `dev` profile (using an in-memory H2 database):

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Once started, you can access:
-   **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bankdb`)
-   **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Running Tests

To run all unit and integration tests:

```bash
./gradlew test
```

## API Endpoints

See the [Swagger UI documentation](http://localhost:8080/swagger-ui.html) for a full, interactive API specification.

-   `POST /api/accounts`: Open a new bank account.
-   `GET /api/accounts/{accountId}`: Get details for a specific account.
-   `POST /api/accounts/{accountId}/deposits`: Deposit money into an account.
-   `POST /api/transfers`: Request a new money transfer (asynchronous). 