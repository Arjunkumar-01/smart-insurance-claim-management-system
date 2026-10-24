# SICMS Backend

Java 21 and Spring Boot 3 modular-monolith API for SICMS. Feature packages cover authentication, users, customers/KYC, products, policies, payments, claims, reports, administration, audit, security, and storage.

## Configuration

Required shell environment variables:

- `DB_PASSWORD`: local PostgreSQL password.
- `JWT_SECRET`: random JWT signing secret of at least 32 bytes.

Common optional variables:

- `DB_USERNAME` (default `postgres`)
- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/sicms`)
- `JWT_ACCESS_TOKEN_EXPIRATION` (default `PT15M`)
- `CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`)
- `DOCUMENT_STORAGE_PATH` (default `./var/documents`)
- `MAX_FILE_SIZE` and `MAX_REQUEST_SIZE`

[.env.example](.env.example) is a safe checklist only; Spring Boot does not automatically import it. Do not commit a populated `.env` file.

Privileged development-account bootstrapping is disabled by default. Local use requires `BOOTSTRAP_ACCOUNTS_ENABLED=true` and environment-provided `DEV_AGENT_PASSWORD`, `DEV_CLAIMS_OFFICER_PASSWORD`, and `DEV_ADMINISTRATOR_PASSWORD`. Public registration remains Customer-only.

## Database

Create an empty local database named `sicms`, then start the application. Flyway applies migrations V1-V13; applied migrations must not be edited. Hibernate validates the schema with `ddl-auto=validate`.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Validation

```powershell
.\mvnw.cmd test
.\mvnw.cmd package -DskipTests=false
```

Tests use H2 in PostgreSQL compatibility mode with Flyway and Hibernate validation enabled.

## API

Swagger UI is available at `/swagger-ui/index.html`, OpenAPI JSON at `/v3/api-docs`, and health at `/actuator/health`. Authentication is stateless Bearer JWT. Controllers enforce the four application roles and ownership-scoped Customer access.

The Postman collection and environment are under [postman](postman). They contain variable names and synthetic examples only; committed token/password variables remain empty.

## Document Storage

Claim document metadata is stored in PostgreSQL while file content is stored under `DOCUMENT_STORAGE_PATH`. The default is `./var/documents` relative to the backend working directory. Runtime files are ignored and are not packaged. Local storage is intended for development only and is not horizontally scalable without shared storage.

## Security Notes

Passwords are BCrypt hashes. JWT and database secrets are environment-only. Responses do not expose password hashes, KYC identity values, storage paths, payment credentials, or authorization tokens. Receipt PDFs contain only persisted payment/policy references, amount, method, status, timestamps, and the simulated-payment notice.
