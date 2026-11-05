# ColdTrace Asset Service

Locations, assets, gateways, devices, safe ranges and versioned source configuration belong to this service. Organization existence comes from Identity; plan decisions come from Subscription. No IAM or Billing entity or repository is copied. Source configuration updates preserve their transaction, immutable history and integration outbox.

## Provenance

Reuses the implementation from `Veltis-Software/coldtrace-backend` at `f66b04a`.
The existing Java 26 and Spring Boot 4 runtime is preserved for this extraction;
Monitoring, Alert and Report retain their independent Java 21 builds. No shared
business database or backend process is required by this service.

## Configuration

| Variable | Purpose |
| --- | --- |
| `PORT` | `8082` locally; `8080` in the container |
| `DB_URL` | JDBC URL for `ct_asset` only |
| `DB_USER`, `DB_PASSWORD` | Credentials restricted to that schema |
| `INTERNAL_SERVICE_KEY` | Required workload credential, minimum 32 characters |
| `IDENTITY_URL` | Default `http://localhost:8081` |
| `SUBSCRIPTION_URL` | Default `http://localhost:8088` |

Supply secrets through the process environment. Never commit credentials.
Flyway owns the initial schema; Hibernate runs with `ddl-auto=validate`.
Existing data requires an explicit owner-table import before switching routes.

## Development

Run `./mvnw test` and then `./mvnw spring-boot:run` (`.\mvnw.cmd` on Windows).
OpenAPI is served at `/v3/api-docs`, health at `/actuator/health`.
The Docker image runs as a non-root user and takes `PORT=8080` by default.

## Integration verification

The infrastructure repository contains `tests/Verify-ServiceBoundaries.ps1`.
It exercises registration, eventual subscription initialization, sessions,
location creation, subscription limits, organization isolation and CORS against
all three running services. It creates disposable organizations and must run
against isolated test databases. Output contains no tokens or passwords.

Internal endpoints require `X-Service-Key`; browser JWTs do not grant internal
access. External service errors must not be interpreted as available quota.
The Gateway cutover is a separate integration step.
