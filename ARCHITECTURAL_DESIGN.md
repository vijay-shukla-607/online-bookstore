# Online Bookstore Backend — Architectural Design Document

## 1. Purpose

This document describes the backend's logical architecture, component responsibilities, request flows, security approach, persistence approach, and key design decisions.

## 2. Technology Stack

| Concern | Technology / approach |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| API style | REST over HTTP |
| Persistence abstraction | Spring Data JPA |
| Default local database | H2 in-memory |
| Authentication | JWT-based |
| Build and tests | Maven |
| Manual API verification | Postman collection |

## 3. Logical Architecture

```text
API Client (Browser / Postman)
             |
             v
       REST API Layer
            (api)
             |
             v
        Service Layer
          (service)
             |
             v
      Repository Layer
            (repo)
             |
             v
       Relational Database
        (H2 locally / configured DB)

Cross-cutting packages:
security — authentication and authorization
config   — application and development configuration
domain   — domain entities and behavior
error    — exception handling and error responses
```

### 3.1 API Layer — `api`

- Exposes HTTP endpoints and translates HTTP requests into application operations.
- Applies request validation where configured.
- Returns HTTP status codes and response DTOs.
- Should avoid embedding business rules or persistence logic in controllers.

### 3.2 Service Layer — `service`

- Coordinates use cases such as authentication, book management, cart operations, and order processing.
- Enforces business rules and coordinates repository calls.
- Uses transactions for workflows that require related database changes to succeed or fail together.

### 3.3 Repository Layer — `repo`

- Provides persistence operations through Spring Data JPA.
- Separates database access from business logic.
- Does not construct HTTP responses.

### 3.4 Domain Layer — `domain`

- Contains domain entities and related behavior.
- Represents concepts such as books, users, cart items, orders, and order lines, subject to the actual entity definitions.
- Domain invariants should be tested.

### 3.5 Security Layer — `security`

- Implements or supports JWT authentication and request authorization.
- Protects endpoints that require authentication.
- Applies role- or ownership-based authorization where configured.
- Development secrets must not be used in deployed environments.

### 3.6 Configuration Layer — `config`

- Holds application configuration and local development setup.
- May include development data seeding as described in the backend README.
- Environment-specific settings should be externalized for deployment.

### 3.7 Error Layer — `error`

- Centralizes exception-to-HTTP response mapping.
- Provides consistent error response structures.
- Must not return stack traces, SQL details, secrets, or internal diagnostics to API clients.

## 4. Typical Request Flows

### 4.1 Public Book Retrieval

1. A client calls a public book endpoint.
2. Security configuration permits the request.
3. The controller delegates to the book service.
4. The service retrieves data through a repository.
5. The result is mapped to a response DTO and returned.

### 4.2 Authenticated Request

1. The client submits a JWT using the configured authorization header.
2. Security components validate the token and establish the authenticated principal.
3. Authorization rules determine whether the request is permitted.
4. The controller delegates to the relevant service.
5. The service applies business rules and accesses repositories as needed.
6. The API returns the response.

### 4.3 Checkout

1. An authenticated customer submits a checkout request.
2. The service loads the customer's cart and required book data.
3. Business rules are validated.
4. The order and related records are persisted.
5. Inventory is updated and the cart is cleared if those operations are part of the implementation.
6. Related database changes should be transactional to avoid partial checkout state.
7. The API returns the resulting order representation.

Confirm the actual transaction boundary and inventory behavior in the service implementation.

## 5. Persistence and Data

- Spring Data JPA repositories provide the persistence abstraction.
- The default local setup uses H2 in-memory storage; data is reset when the backend stops.
- Entity relationships, constraints, cascading, and deletion behavior are defined by the actual entity mappings.
- Production deployments should use a persistent database and securely managed credentials.

## 6. Security Design

- JWT is used for token-based authentication.
- Public endpoints should be explicitly defined.
- Protected endpoints should require a valid authenticated identity.
- Administrative operations should be restricted by configured role rules.
- Customer-specific resources, especially carts and orders, must be checked for ownership.
- Verify that passwords are stored using a suitable password encoder.
- Development JWT secrets and seeded administrator credentials are local-only.
- CORS origins should be configured deliberately per environment.

## 7. API Contracts and Mapping

- Request and response DTOs define the external API contract.
- Controllers should not return persistence entities directly.
- Mapping logic should be consistent and straightforward.
- Manual mapping is reasonable for small mappings; consider MapStruct only if mapping complexity or duplication justifies another dependency.
- Validate input at the API boundary and enforce business invariants in the appropriate application/domain layer.

## 8. Error Handling

The global exception-handling layer should map known application and framework exceptions to safe, consistent responses. Typical semantics include:

- `400` — invalid input or malformed request.
- `401` — missing or invalid authentication.
- `403` — authenticated caller lacks permission.
- `404` — resource not found.
- `409` — business conflict where applicable.
- `500` — unexpected server failure.

The implemented exception handler is authoritative for exact mappings. Unexpected exceptions should be logged server-side with appropriate context while the client receives a generic response. Never log tokens, passwords, or secrets.

## 9. Testing Strategy

- **Domain tests:** verify domain rules and invariants.
- **Service tests:** verify business logic, edge cases, and failure paths.
- **Controller/API tests:** verify HTTP behavior, validation, and response contracts.
- **Security tests:** verify public/protected access, invalid tokens, roles, and resource ownership.
- **Exception-handler tests:** verify status codes and safe error structures.
- **Integration tests:** verify persistence and workflows spanning multiple components where appropriate.

Run the suite from the backend directory:

```powershell
.\mvnw.cmd clean test
```

A passing test suite is necessary but does not alone prove complete coverage or strict test-driven development.

## 10. Key Design Decisions

1. **Layered architecture:** separates HTTP concerns, business logic, and persistence.
2. **Dependency injection:** Spring manages dependencies and supports isolated testing.
3. **Repository abstraction:** separates persistence operations from service logic.
4. **DTO-based API contracts:** decouples external payloads from database entities.
5. **Centralized exception handling:** keeps error responses consistent.
6. **JWT authentication:** supports token-based API access.
7. **Transactional multi-step workflows:** protects consistency when a use case changes related records.
8. **Avoid unnecessary abstractions:** introduce patterns and libraries only when they improve clarity, testability, or maintainability.

## 11. Production Considerations

Before production deployment, review persistent database configuration and migrations, backup and recovery, secure secret management and rotation, CORS and authorization rules, safe logging and monitoring, health checks, resource limits, and appropriate integration/security testing.

## 12. Related Documentation

- `online-bookstore-backend-README.md` — local setup, API examples, Postman usage, and troubleshooting.
- Repository Postman collection — manual API testing.
- Source configuration and automated tests — authoritative details for runtime behavior and endpoint contracts.
