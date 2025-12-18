# FitAPI

A production-oriented Spring Boot REST API for tracking workouts, exercises, and training sessions. FitAPI emphasizes clean architecture, security-first design, and cloud-native deployment patterns while remaining intentionally cost-aware and pragmatic.

---

## Overview

FitAPI is a backend service for tracking workouts, exercises, and training sessions. The project is designed to resemble a real-world production API rather than a tutorial application, with explicit attention to authorization boundaries, failure handling, and long-term maintainability.

The service is deployed to Azure using container-based infrastructure, managed secrets, and a relational database. More complex platform patterns are intentionally deferred until scale or operational needs justify them.

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
- Cloud deployment on Azure Container Apps

---

## Core Architecture

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
- User-scoped repository queries (for example, fetch-by-id-and-user)
- Unauthorized access returns `404` where appropriate to reduce resource enumeration

### Rate Limiting

- Application-level rate limiting applied to login endpoints
- Protects against brute-force and credential-stuffing attacks

**Design decision:**  
Distributed rate limiting (for example, Redis-backed) is intentionally deferred. For a single-service deployment with minimal traffic, application-level limiting is sufficient and avoids unnecessary infrastructure cost and complexity. The architecture allows this to be added later if scaling requires it.

---

## Database & Migrations

- PostgreSQL-compatible schema
- Flyway is the single source of truth for schema evolution
- Hibernate automatic DDL generation is disabled
- Schema changes are versioned and immutable

### Query Performance & Indexing

The schema includes explicit indexes to optimize common access patterns:

- **`workout_sessions (user_id, started_at DESC)`**  
  Optimizes per-user workout history and recent-session queries.

- **`workout_sets (workout_session_id, set_number)`**  
  Ensures efficient retrieval of sets in execution order.

- Foreign-key indexes are retained to support joins and ownership checks.

Indexes are chosen based on observed query patterns rather than premature optimization.

---

## Configuration & Secrets

- All configuration is driven via environment variables
- No secrets are committed to source control or container images
- Secrets are stored in **Azure Key Vault**
- The application accesses secrets using a **system-assigned managed identity**

This approach ensures:
- secrets are never baked into images
- credentials can be rotated without redeploying code
- the same image runs unchanged across environments

---

## Containerization

- Multi-stage Docker build (build stage + minimal runtime stage)
- No credentials baked into container images
- Explicit port exposure for HTTP traffic

The same container image runs locally, in Docker Compose, and in Azure Container Apps without modification.

---

## Cloud Deployment (Azure)

FitAPI is deployed using the following Azure services:

- **Azure Container Apps** (Consumption plan)
- **Azure Container Registry** for container images
- **Azure Key Vault** for secrets
- **Azure PostgreSQL Flexible Server**
- **System-assigned managed identity** for secure secret access

Ingress is externally accessible but protected via IP allowlisting and application-level authentication.

**Design Choice: Azure Container Apps:**  
FitAPI is deployed using Azure Container Apps to avoid the cost and operational overhead of managing container orchestration manually.

Lower-level container hosting options, such as Azure Container Instances, would require additional always-on infrastructure (for example, load balancers, gateways, and custom scaling logic) to support safe deployments, ingress management, and availability. Azure Container Apps provides these capabilities natively, including managed ingress, autoscaling (with scale-to-zero), and revision-based deployments.

This approach keeps the deployment cost-efficient, operationally simple, and aligned with modern cloud-native practices, without sacrificing deployment safety or future extensibility.

---

## Deployment Workflow (Pre-CI/CD)

Deployment is intentionally manual at this stage:

1. Build the Docker image locally
2. Push the image to Azure Container Registry
3. Update the image tag on the Container App
4. Azure creates a new revision and routes traffic
5. Rollback is possible by switching revisions

This workflow provides immutable deployments and safe rollbacks without CI/CD complexity. Automated pipelines are planned as a later phase.

---

## Health & Observability

Application health is currently verified using application endpoints.  
Spring Boot actuator endpoints are intentionally not exposed publicly at this stage and will be integrated alongside CI/CD and automated health checks in a future phase.

---

## Testing Strategy

- Context-load validation
- Targeted unit tests for critical authorization behavior
- Manual integration testing via Postman against the deployed API

Testing focuses on preventing security regressions and validating real deployment behavior.

---

## Design Philosophy

This project prioritizes:

- Correctness over premature optimization
- Security-aware defaults
- Service-layer enforcement of business rules
- Cost-conscious architectural decisions
- Clear extension points for future scaling

Infrastructure complexity is added only when justified by real requirements.

---

## Future Enhancements

- CI/CD automation
- Automated health checks and deployment gates
- Distributed rate limiting and edge protection
- Role-based access control hardening
- Metrics, tracing, and structured observability

---

## License

This project is intended for learning, demonstration, and portfolio purposes.
