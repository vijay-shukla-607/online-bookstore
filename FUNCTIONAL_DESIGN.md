# Online Bookstore Backend — Functional Design Document

## 1. Purpose and Scope

This document describes the principal user-facing capabilities of the Online Bookstore backend. It is intended for reviewers, developers, and testers.


## 2. System Overview

The backend provides REST APIs for book browsing, customer registration and login, shopping-cart management, order checkout, order history, and administrative book management. The project uses Java 17, Spring Boot 3.4.5, Maven, Spring Data JPA, JWT-based authentication, and an in-memory H2 database by default for local development.

## 3. Actors and Permissions

| Actor | Capabilities |
|---|---|
| Anonymous visitor | Browse the public book catalogue and retrieve book details. |
| Registered customer | Register or log in, manage their cart, check out, and view their own order history/details. |
| Administrator | Perform administrative book operations subject to configured authorization rules. |

The security configuration is authoritative for exact permissions.

## 4. Functional Requirements

### 4.1 Book Catalogue

- Visitors can retrieve the book list and individual book details.
- Pagination may be used where supported by the endpoint.
- Responses should use response DTOs rather than exposing persistence entities.

### 4.2 Registration and Authentication

- A new customer can submit registration details.
- Invalid registration input should be rejected with a suitable client error.
- A registered user can log in with credentials.
- Successful authentication returns the token response defined by the API.
- Protected endpoints require valid authentication and apply configured authorization rules.

### 4.3 Shopping Cart

- An authenticated customer can view their cart.
- The customer can add a book, update a cart item's quantity, and remove a cart item.
- Invalid quantities and nonexistent or unavailable books should be handled with appropriate errors.
- Cart operations must be scoped to the authenticated customer.

### 4.4 Checkout and Orders

- An authenticated customer can check out their cart.
- The backend creates an order using server-side book and quantity data.
- Related changes—such as order creation, inventory changes if implemented, and cart cleanup—should remain consistent.
- A customer can view their own order history and order details.
- A customer must not be able to access another customer's order by changing an identifier.

### 4.5 Administrative Book Management

- An administrator can create, update, and delete books where permitted by business rules.
- Non-administrative users must not be allowed to perform administrative operations.
- Requests must be validated and errors returned using the API's standard response format.

## 5. Error and HTTP Status Semantics

The following are expected semantics, not a claim that every status is used by every endpoint. Confirm exact mappings against controllers and the global exception handler.

| Situation | Expected status |
|---|---|
| Successful retrieval | `200 OK` |
| Successful creation | Usually `201 Created`, if implemented |
| Successful operation without a response body | `204 No Content`, if implemented |
| Invalid request or validation failure | `400 Bad Request` |
| Missing or invalid authentication | `401 Unauthorized` |
| Authenticated user lacks permission | `403 Forbidden` |
| Resource does not exist | `404 Not Found` |
| Duplicate or conflicting operation | `409 Conflict`, where applicable |
| Unexpected server-side failure | `500 Internal Server Error`, without exposing internal details |

## 6. Local Data and Development

- The default local profile uses an in-memory H2 database; data is lost when the application stops.
- The backend README describes sample book data and a local administrator seeded during startup when relevant tables are empty.
- Local credentials and development JWT fallback secrets must not be reused in deployed environments.

## 7. Suggested Functional Regression Scenarios

1. Retrieve the public book list and a single book.
2. Register a valid customer and reject invalid registration input.
3. Log in with valid credentials and reject invalid credentials.
4. Reject protected requests without valid authentication.
5. View and modify a customer's own cart.
6. Reject invalid quantities and invalid book identifiers.
7. Check out a populated cart and verify the resulting order.
8. Verify cart and inventory state after checkout according to implemented rules.
9. Retrieve order history and details.
10. Prevent access to another customer's order.
11. Allow administrative book operations for an administrator.
12. Reject administrative operations for a customer or anonymous visitor.
13. Verify malformed JSON and validation errors return safe responses.
14. Verify unexpected exceptions do not leak stack traces or sensitive details.

## 8. Related Documentation

- `online-bookstore-backend-README.md` — local setup, API examples, Postman usage, and troubleshooting.
- Repository Postman collection — requests for manual API verification.
