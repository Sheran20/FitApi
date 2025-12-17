# FitAPI

A production-oriented Spring Boot REST API built as a personal portfolio project. FitAPI focuses on clean architecture, security-first design, and cloud-ready patterns while remaining intentionally cost-aware and simple.

---

## Overview

FitAPI is a backend service for tracking workouts, exercises, and training sessions. The project is designed to resemble a real-world production API rather than a tutorial application, with explicit attention to authorization boundaries, failure handling, and long-term maintainability.

The codebase is cloud-ready by design but does not assume large-scale traffic or enterprise infrastructure from day one. More complex patterns are intentionally deferred until they are justified.

---

## Key Features

- RESTful API for workouts, exercises, and training sessions
- Stateless JWT-based authentication (Spring Security)
- Secure password storage using BCrypt
- Application-level login rate limiting
- Strict service-layer authorization and data isolation
- Flyway-managed database schema migrations
- PostgreSQL-compatible persistence
- Environment-variable–driven configuration
- Multi-stage Docker containerization

---

## Architecture

FitAPI follows a layered architecture with clear separation of concerns:

- **Controllers**
  - Handle HTTP requests and responses
  - Responsible only for request validation and HTTP semantics

- **Services**
  - Contain all business logic
  - Enforce authorization and ownership rules
  - Prevent cross-user data access even if controllers change

- **Repositories**
  - Abstract persistence using Spring Data JPA

- **DTOs & Mappers**
  - Decouple API contracts from persistence models
  - Prevent entity leakage and over-fetching

This structure ensures the API remains testable, maintainable, and resilient as features grow.

---

## Security Design

### Authentication

- Stateless JWT authentication
- Custom JWT filter with robust error handling
- Invalid or malformed tokens fail safely without crashing the API

### Authorization & Multi-Tenancy

- Ownership checks enforced in the service layer
- User-scoped repository queries (e.g., fetch-by-id-and-user)
- Unauthorized access returns `404` where appropriate to avoid resource enumeration

### Rate Limiting

- Application-level rate limiting applied to login endpoints
- Protects against brute-force and credential-stuffing attacks

**Design decision:**
Distributed rate limiting (e.g., Redis-backed) is intentionally deferred. For a single-service deployment with minimal traffic, application-level limiting is sufficient and avoids unnecessary infrastructure cost and complexity. The architecture allows distributed rate limiting to be added later if scaling requires it.

---

## Database & Migrations

- PostgreSQL-compatible schema
- Flyway is the single source of truth for schema evolution
- Hibernate automatic DDL generation is disabled
- Schema changes are versioned, immutable migrations

This approach prevents schema drift and ensures predictable deployments across environments.

---

## Configuration & Secrets

- All configuration is driven via environment variables
- No secrets are committed to source control
- Supports local development, Docker, and cloud environments without code changes

---

## Containerization

- Multi-stage Docker build (build stage + minimal runtime stage)
- No credentials baked into container images
- Explicit port exposure

The same container image can run locally or in cloud environments unchanged.

---

## Testing Strategy

- Context-load validation
- Targeted unit tests for critical authorization behavior
- Test focus is on preventing security regressions rather than maximizing coverage

---

## Design Philosophy

This project prioritizes:

- Correctness over premature optimization
- Security-aware defaults
- Service-layer enforcement of business rules
- Cost-conscious architectural decisions
- Clear extension points for future scaling

More complex infrastructure patterns (API gateways, WAFs, distributed rate limiting) are intentionally deferred until traffic or scale justifies them.

---

## Future Enhancements

- Cloud deployment (Azure Container Apps)
- Managed secrets via cloud key vault
- CI/CD automation
- Distributed rate limiting and edge protection
- Role-based access control hardening
- Observability and metrics

---

## License

This project is intended for learning, demonstration, and portfolio purposes.