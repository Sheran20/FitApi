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

## API Documentation (Swagger/OpenAPI)

FitAPI ships with a production-ready Swagger UI powered by springdoc-openapi.

- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI JSON:** `/v3/api-docs`
- **Auth:** Use the "Authorize" button and provide a JWT; secured endpoints send `Authorization: Bearer <token>`.
- **Security visibility:** Secured endpoints show lock icons in the UI.
- **Sensitive endpoints:** Actuator endpoints are excluded from documentation.

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

### Index Performance Benchmark

A benchmark script is included to measure the real-world impact of database indexes. The test generates 50,000+ rows and compares query execution times with and without indexes.

**Results (50,000 sessions, 50,000 sets):**

| Query | With Index | Without Index | Speedup |
|-------|------------|---------------|---------|
| User lookup by email | 0.013ms | 0.018ms | 1.4x |
| Sessions for user (sorted) | 0.028ms | 1.477ms | **53x** |
| Sessions in date range | 0.011ms | 1.284ms | **117x** |
| Sets for session | 0.012ms | 1.729ms | **144x** |
| Sets for session + exercise | 0.007ms | 1.350ms | **193x** |
| Aggregate volume calculation | 0.024ms | 1.223ms | **51x** |

Without indexes, PostgreSQL performs sequential scans across all rows. With indexes, it reads only the relevant rows directly via B-tree traversal.

**Running the benchmark:**

```bash
# Start the database
docker compose up -d

# Run the benchmark (generates test data, benchmarks with/without indexes)
docker exec -i fitapi-postgres psql -U fitapi -d fitapidb < scripts/benchmark_indexes.sql
```

The script:
1. Generates 1,000 test users, 50,000 sessions, and 50,000 sets
2. Runs `EXPLAIN ANALYZE` on common queries with indexes
3. Drops all custom indexes
4. Runs the same queries without indexes (sequential scans)
5. Recreates the indexes

**Warning:** This adds test data to your database. Use on a dev environment only.

---

## Configuration & Secrets

- All configuration is driven via environment variables
- No secrets are committed to source control or container images
- Secrets are stored in **Azure Key Vault**
- The application accesses secrets using a **system-assigned managed identity**
- Local/dev defaults in configuration are for development only and must be overridden in production

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

## Architecture & Deployment Flow

FitAPI separates infrastructure, deployment, and runtime concerns clearly:

- **Terraform (infrastructure + IAM)**  
  Provisions long-lived Azure resources and permissions only.

- **Developers (manual setup)**  
  - Populate **Key Vault** secrets per environment  
  - Configure **GitHub Actions** environment variables to target the correct infrastructure

- **GitHub Actions (build + deploy)**  
  - Builds immutable container images  
  - Pushes images to **Azure Container Registry**  
  - Updates the **Azure Container App** image  
  - Injects **Key Vault** secret references (not secret values)

- **Azure Container Apps (runtime + secret resolution)**  
  Resolves Key Vault secrets at runtime using managed identity.

Terraform provisions long-lived infrastructure only. Application builds, deployments, and secret wiring are intentionally separated.

---

## Infrastructure as Code (Terraform)

Terraform is used to:

- Provision and recreate Azure infrastructure deterministically
- Support safe teardown and recreation of environments

Terraform does not manage runtime configuration or secrets, and it is intentionally not used for application deployments.

### Design Decisions

- Terraform is not run in CI to avoid tightly coupling infrastructure changes with application builds
- Secrets are not stored in Terraform or CI to minimize exposure risk
- Environment wiring (GitHub Actions variables, Key Vault secrets) is manual by design

These are intentional trade-offs to reduce complexity and prevent unsafe automation.

---

## Deployment Workflow (CI/CD)

- Push to `main` triggers GitHub Actions
- Pipeline runs tests, builds the Docker image, and pushes to Azure Container Registry
- Images are tagged immutably using the commit SHA
- Azure Container Apps deploys a new revision per image
- Traffic remains pinned to the current revision until the new revision is ready
- Traffic is explicitly shifted after verification
- Failed deployments do not impact live traffic
- No portal-based deployment steps are required

### Authentication & Security

- GitHub Actions authenticates to Azure using OIDC (no secrets)
- Least-privilege RBAC for the CI identity
- Container App uses a system-assigned managed identity
- Secrets are stored in Azure Key Vault and accessed via managed identity
- No secrets in repo or CI logs

### Revision-Based Deployments

- Multiple revision mode enabled
- Deterministic traffic control
- Previous revisions retained for rollback

### Rollback Strategy

- Traffic rollback by shifting traffic to a previous revision
- Image rollback by redeploying an existing immutable image tag
- No rebuilds required for rollback

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

- Production environment with gated releases
- Canary or progressive traffic shifting
- Post-deploy smoke testing
- Enhanced observability
- Distributed rate limiting and edge protection
- Role-based access control hardening

---

## Future Infrastructure Enhancements (Optional)

- Remote Terraform state + locking for multi-user or CI scenarios
- Multi-environment support (dev / prod) via `tfvars`
- Policy-as-code for guardrails
- Drift detection once infrastructure becomes long-lived

---

## License

This project is intended for learning, demonstration, and portfolio purposes.
