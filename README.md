# Smart Insurance Claim Management System (SICMS)

**An enterprise insurance operations platform for policy servicing, premium processing, controlled Claim review, settlement, reporting, and auditability.**

## 📌 Project Overview

SICMS is a full-stack insurance management application built around a controlled, traceable Claim lifecycle. It gives Customers a self-service workspace for profile and KYC data, Product discovery, Policy purchase and servicing, simulated premium Payments, and Claim submission with supporting evidence. Claims Officers work from a dedicated operations queue where they verify Claim information, inspect documents, request additional evidence, approve or reject Claims, and settle approved outcomes.

The backend is a Java 21 and Spring Boot 3.4.5 modular monolith. It exposes JWT-protected REST APIs, persists transactional data in PostgreSQL, evolves the schema through Flyway, stores Claim document metadata in the database, and stores document bytes on the local filesystem. The React 19 frontend consumes these APIs through a centralized Axios client and presents role-specific workspaces for Customers, Agents, Claims Officers, and Administrators.

The application deliberately separates Product definitions, purchased Policies, premium-payment records, Claims, review activity, lifecycle history, and settlement records. This keeps operational responsibilities explicit and makes state changes, financial flows, and administrative actions easier to inspect and test.

## 🎯 Business Objective

SICMS addresses the fragmented workflows commonly found across insurance servicing and Claim operations:

- Give Customers one secure place to manage identity, coverage, premiums, Claims, evidence, and Claim outcomes.
- Keep Product configuration separate from Policy snapshots so historical premium and coverage values remain stable.
- Enforce a controlled Claim workflow instead of exposing arbitrary status changes.
- Preserve a clear record of Claims Officer activity and Claim state transitions.
- Distinguish premium inflow from Claim settlement outflow.
- Provide Administrators with operational reporting, account controls, cancellation decisions, and append-only audit visibility.
- Keep role and ownership enforcement authoritative in the backend rather than relying on frontend navigation alone.

## ✨ Key Capabilities

### Customer Self-Service

- Customer registration and JWT login with finite session expiry.
- Profile and KYC submission with masked identity presentation.
- Active Product discovery, category filtering, details, and comparison.
- Policy purchase with optional nominee details, a Customer-selected start date, and a server-derived end date based on Product tenure.
- Nominee maintenance, tenure-based renewal, and cancellation requests.
- Simulated premium Payment using controlled methods: `CARD`, `BANK_TRANSFER`, or `UPI`.
- Invoice and Receipt views plus authenticated PDF Receipt download.
- Claim submission with Policy context, Claim Type, incident date, description, amount, and server-generated Claim Number.
- PDF, JPEG, and PNG evidence upload, metadata listing, and authenticated download.
- Current Claim status, lifecycle history, and read-only settlement visibility.
- Dashboard reporting for active/expired Policies, Claims, premium paid, settlements, and Claim status distribution.

### Product & Policy Management

- Administrator Product creation and update.
- Controlled Product categories: `HEALTH`, `LIFE`, `VEHICLE`, and `TRAVEL`.
- Positive coverage, premium, and Policy tenure validation.
- Active/inactive Product lifecycle without deleting historical Product references.
- Product search, filtering, safe sorting, pagination, and comparison of two to four Products.
- Policy premium and coverage snapshots captured at purchase.
- Server-derived Policy end dates based on Product tenure.
- Administrator Policy oversight and approval/rejection of pending cancellation requests.

### Claims Operations

- Claims Officer dashboard with actionable queue metrics.
- Paginated queue with status filtering and Claim Type visibility.
- Claim context, coverage, requested amount, incident details, and supporting evidence review.
- Contextual workflow actions only: Start Review, Request Documents, Resume Review, Approve, Reject, and Settle.
- Separate views of Claims Officer activity and lifecycle transitions.
- One settlement record per approved Claim, with no arbitrary settlement amount supplied by the client.

### Financial Processing

- Policy-associated premium Payments use the Policy premium snapshot.
- Invoice, Payment, and Receipt records are created as separate persisted concepts.
- Completed Payment history includes method, status, amount, timestamp, Invoice, and Receipt references.
- Receipt PDFs contain persisted SICMS payment data and a simulated-payment disclosure only.
- Claim Settlement remains Claim-associated and does not create Payment, Invoice, or Receipt rows.

### Administrative Governance

- Product management and Policy oversight.
- Pending Policy cancellation decisions.
- User listing, detail, and controlled `ACTIVE`, `INACTIVE`, or `LOCKED` account status changes.
- Protection against self-deactivation and removal of the final active Administrator.
- Customer, Policy, Claim, Payment, Product, and settlement reporting.
- Read-only paginated Audit Log access.

### Reporting & Analytics

- Customer portfolio and Claim summary scoped to the authenticated Customer.
- Administrator operational overview with Policy and Claim status counts.
- Monthly completed premium revenue.
- Product performance across Policies, Claims, premium, and settlements.
- Monthly Claim Settlement Trend derived from settlement timestamps and amounts.

## 👥 User Roles & Access Matrix

| Role             | Workspace             | Key Capabilities                                                                                                                                                     |
| ---------------- | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `CUSTOMER`       | `/app/customer`       | Profile/KYC, Products, comparison, Policy purchase and servicing, Payments, Invoice/Receipt, Claims, documents, lifecycle, settlement visibility, Customer reporting |
| `AGENT`          | `/app/agent`          | Product-discovery dashboard, active Product list, Product detail, and comparison                                                                                     |
| `CLAIMS_OFFICER` | `/app/claims-officer` | Claims queue, Claim detail, document review/download, controlled review actions, approval/rejection, and settlement                                                  |
| `ADMINISTRATOR`  | `/app/administrator`  | Product management, Policy oversight, cancellation decisions, Reports, Users, account status, and Audit Logs                                                         |

Shared Product routes are available to Customer, Agent, and Administrator roles:

- `/app/products`
- `/app/products/compare`
- `/app/products/:productId`

## 🔄 Claim Lifecycle

SICMS does not provide a generic “change status” operation. The backend validates every transition through the Claim workflow service.

```text
Customer submits Claim
        │
        ▼
    SUBMITTED
        │ Start Review
        ▼
  UNDER_REVIEW ───────────────► REJECTED
        │                          terminal
        │ Request Documents
        ▼
DOCUMENTS_REQUIRED
        │ Customer uploads evidence
        │ Resume Review
        └──────────────► UNDER_REVIEW
                              │ Approve
                              ▼
                          APPROVED
                              │ Settle
                              ▼
                           SETTLED
                            terminal
```

- New Claims always begin as `SUBMITTED`.
- `UNDER_REVIEW` may move to `DOCUMENTS_REQUIRED`, `APPROVED`, or `REJECTED`.
- `DOCUMENTS_REQUIRED` may resume to `UNDER_REVIEW`; the backend currently does not enforce that an additional document was uploaded first.
- Only an `APPROVED` Claim can be settled.
- `REJECTED` and `SETTLED` are terminal states.

## 🔍 Claim Review vs Lifecycle vs Settlement

These are separate persisted concerns:

- **`ClaimReview`** records Claims Officer business activity, reviewer identity, controlled action, notes, and timestamp.
- **`ClaimStatusHistory`** records append-only state transitions such as `SUBMITTED → UNDER_REVIEW`.
- **`ClaimSettlement`** records the financial outcome for one approved Claim and completes `APPROVED → SETTLED`.

Keeping them separate avoids treating operational notes, current status, transition history, and financial settlement as the same data.

## 🏗 System Architecture

```text
┌──────────────────────────────────────────────────────────────────┐
│                        Client Browser                            │
└──────────────────────────────┬───────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│ React 19 + TypeScript + Vite + Material UI                      │
│ React Router │ TanStack Query │ Axios │ React Hook Form + Zod   │
└──────────────────────────────┬───────────────────────────────────┘
                               │ REST / JSON / Bearer JWT
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│ Spring Boot 3.4.5 Modular Monolith                              │
│ Controllers → Services → Repositories / Domain                  │
│ Spring Security │ Bean Validation │ Audit │ OpenAPI             │
└───────────────┬───────────────────────────────────┬──────────────┘
                │ JPA / Hibernate                  │ StorageService
                ▼                                  ▼
┌───────────────────────────────┐     ┌────────────────────────────┐
│ PostgreSQL                    │     │ Local Claim File Storage   │
│ Transactional data + metadata │     │ Generated paths/filenames  │
│ Schema managed by Flyway      │     │ PDF / JPEG / PNG           │
└───────────────────────────────┘     └────────────────────────────┘
```

The backend uses package-by-feature under `com.talentfarm.sicms`. Controllers remain thin, services own business rules and transactions, repositories provide persistence access, and DTOs keep persistence entities out of API responses.

## 💻 Technology Stack

### Backend

| Technology                               | Version / Purpose                                           |
| ---------------------------------------- | ----------------------------------------------------------- |
| Java                                     | 21                                                          |
| Spring Boot                              | 3.4.5                                                       |
| Spring Web                               | REST controllers and JSON APIs                              |
| Spring Security                          | Stateless JWT authentication and method-level authorization |
| Spring Data JPA / Hibernate              | Repository persistence and schema validation                |
| PostgreSQL                               | Primary relational database                                 |
| Flyway                                   | Versioned schema migrations                                 |
| Jakarta Bean Validation                  | Request validation                                          |
| JJWT                                     | 0.12.6                                                      |
| Springdoc OpenAPI                        | 2.8.6                                                       |
| Apache PDFBox                            | 3.0.3, Receipt PDF generation                               |
| Spring Boot Actuator                     | Health and application information                          |
| Maven Wrapper                            | Reproducible backend build                                  |
| JUnit 5 / Spring Security Test / MockMvc | Unit and integration testing                                |
| H2                                       | 2.2.224, PostgreSQL-compatible test profile                 |

Spring Security, Hibernate, PostgreSQL JDBC, and Flyway library versions are managed by Spring Boot dependency management rather than pinned independently.

### Frontend

| Technology            | Repository Version |
| --------------------- | ------------------ |
| React / React DOM     | `^19.1.0`          |
| TypeScript            | `~5.8.3`           |
| Vite                  | `^6.3.3`           |
| Material UI           | `^7.1.0`           |
| React Router DOM      | `^7.5.3`           |
| Axios                 | `^1.8.4`           |
| TanStack React Query  | `^5.74.9`          |
| React Hook Form       | `^7.56.1`          |
| Zod                   | `^3.24.3`          |
| Vitest                | `^3.1.2`           |
| React Testing Library | `^16.3.0`          |
| ESLint                | `^9.24.0`          |
| Prettier              | `^3.5.3`           |

## 🎨 Frontend Design System

SICMS uses a restrained corporate FinTech and Claims-operations visual identity.

| Token           | Value     | Usage                                       |
| --------------- | --------- | ------------------------------------------- |
| Primary Navy    | `#08204D` | Primary actions and structural emphasis     |
| Navigation Dark | `#071A33` | Authenticated workspace navigation          |
| Corporate Blue  | `#2563EB` | Secondary emphasis and focus treatment      |
| Accent Cyan     | `#06B6D4` | Restrained informational accents            |
| Background      | `#F5F7FA` | Application workspace                       |
| Surface         | `#FFFFFF` | Forms, tables, and bounded content surfaces |
| Heading         | `#0F172A` | Primary text                                |
| Body            | `#334155` | Body copy                                   |
| Secondary       | `#64748B` | Supporting text                             |
| Muted           | `#94A3B8` | Disabled and muted text                     |
| Border          | `#E2E8F0` | Dividers and surface outlines               |
| Success         | `#16A36A` | Successful/positive status                  |
| Warning         | `#D99A24` | Attention-required status                   |
| Danger          | `#DC4545` | Rejected/error status                       |

Typography uses `Inter`, `Segoe UI`, `system-ui`, and `sans-serif`. The UI favors compact information density, restrained shadows, 6–8px radii, responsive grids, horizontally scrollable operational tables, semantic status text, and a visible Corporate Blue keyboard-focus outline.

## 🧩 Functional Modules

1. **Authentication & Authorization** — Customer registration, login, JWT issuance, session expiry, protected routes, and four controlled roles.
2. **Customer Profile & KYC** — Contact/address management and masked identity submission with controlled KYC status.
3. **Product Management** — Categories, Product lifecycle, coverage, premium, description, and positive Policy tenure.
4. **Policy Management** — Purchase, server-generated Policy Number, Customer-selected start date, tenure-derived end date, snapshots, nominee, renewal, cancellation request, and Administrator decision.
5. **Premium Payment** — One deterministic completed premium Payment per Policy using a controlled method.
6. **Invoice & Receipt** — Persisted Invoice/Receipt records and ownership-protected PDF Receipt download.
7. **Claim Management** — Owned Claim submission, controlled Claim Type, amount/coverage checks, status, detail, and history.
8. **Claim Documents** — Metadata in PostgreSQL and protected local file upload/download.
9. **Claims Officer Review** — Queue, review context, evidence inspection, notes, and controlled actions.
10. **Claim Status History** — Append-only transition history, including initial submission and controlled backfill for pre-existing Claims.
11. **Claim Settlement** — One full-amount settlement for an approved Claim.
12. **Reports & Analytics** — Customer summary, Administrator overview, monthly revenue, Product performance, and settlement trend.
13. **User Administration** — Safe User listing/detail and controlled account-status changes.
14. **Audit Logging** — Append-only records for selected significant business and administrative operations.

## 📊 Database Schema & Entities

| Area                | Tables / Persisted Concepts                                                               |
| ------------------- | ----------------------------------------------------------------------------------------- |
| Identity and access | `app_users`, `application_roles`, `user_roles`                                            |
| Customer            | `customers`, `customer_kyc`                                                               |
| Products            | `products`                                                                                |
| Policies            | `policies`, `policy_nominees`, `policy_cancellation_requests`                             |
| Claims              | `claims`, `claim_documents`, `claim_reviews`, `claim_status_history`, `claim_settlements` |
| Premium processing  | `invoices`, `payments`, `receipts`                                                        |
| Governance          | `audit_logs`                                                                              |

Flyway migrations currently run from **V1 through V13**:

| Migration Range | Purpose                                                              |
| --------------- | -------------------------------------------------------------------- |
| V1–V4           | Users/roles, Customer/KYC, Products, Policies/nominees/cancellations |
| V5–V8           | Claims, document metadata, review activity, status history           |
| V9–V11          | Payments/Invoices/Receipts, Claim Settlements, Audit Logs            |
| V12             | Positive Product Policy tenure and `TRAVEL` category                 |
| V13             | Persisted controlled Claim Type with backfill and index              |

Applied migrations are immutable. Hibernate uses `ddl-auto=validate`; it does not create or update the schema.

## 🔐 Security Architecture

- BCrypt password hashing.
- Stateless Spring Security with HMAC-signed JWT access tokens.
- Configurable token expiry; default `PT15M`.
- Controller-level role authorization through `@PreAuthorize`.
- Customer ownership checks for Policies, Claims, documents, Payments, Invoices, Receipts, settlements, and Customer reports.
- Cross-customer resources generally surface as not found instead of leaking ownership information.
- Controlled account statuses: `ACTIVE`, `INACTIVE`, and `LOCKED`.
- Request validation for identity, dates, amounts, pagination, uploads, and controlled values.
- Allow-listed Product sort fields and typed parameter binding.
- Controlled Claim transitions and server-controlled financial amounts.
- File-size and declared MIME-type allow-list validation, safe original filenames, generated storage names, and path-normalization checks. File signatures and malware are not inspected.
- CORS allow-list with explicit methods and `Authorization`, `Content-Type`, and `Accept` headers.
- DTO responses exclude password hashes, raw KYC identity values, document storage paths, and payment credentials. The Login response returns the JWT access token.
- Audit descriptions contain business metadata rather than request bodies or secrets.

Public backend paths are registration, login, health, OpenAPI/Swagger, and CORS preflight. All business APIs require authentication. Local development uses HTTP; HTTPS termination and token revocation are not implemented by this repository.

## 💳 Premium Payment vs Claim Settlement

| Concern         | Premium Payment                    | Claim Settlement                         |
| --------------- | ---------------------------------- | ---------------------------------------- |
| Direction       | Customer → Insurer                 | Insurer obligation → Claim outcome       |
| Business owner  | Policy                             | Claim                                    |
| Trigger         | Customer pays Policy premium       | Claims Officer settles an approved Claim |
| Persisted model | `Invoice` + `Payment` + `Receipt`  | `ClaimSettlement`                        |
| Amount source   | Policy premium snapshot            | Submitted Claim amount                   |
| Result          | Completed simulated premium record | Claim transitions `APPROVED → SETTLED`   |

This boundary prevents premium revenue records from being confused with Claim settlement outflow.

## 📈 Reports & Analytics

### Customer Summary

The authenticated Customer report exposes:

- Total, active, and expired Policy counts.
- Claims submitted and counts by current Claim status.
- Completed Payment count and premium paid.
- Settlement total.
- Direct navigation to Policy, Payment, and Claim history views.

### Administrator Reporting

- Customer count.
- Policy counts by status.
- Claim counts by status.
- Total Claims and counts currently in `APPROVED`, `REJECTED`, or pending statuses. Settled Claims are not counted as currently approved.
- Completed premium collection.
- Settlements paid and Claims ratio.
- Monthly completed premium revenue.
- Product performance: Policy count, Claim count, premium, and settlements.
- Monthly Claim Settlement Trend: settlement count and amount.

`pendingClaims` is defined as:

```text
SUBMITTED + UNDER_REVIEW + DOCUMENTS_REQUIRED
```

Claims Ratio is settlement total divided by completed premium collection, expressed as a percentage. Reporting reads transactional tables directly; SICMS does not create separate analytics tables.

## 🧾 Audit Logging

The application creates `AuditLog` governance records with a controlled `AuditAction`, optional actor, resource type/ID, safe description, and timestamp. Administrator access is read-only and paginated; database-level immutability is not enforced.

Implemented audit call sites cover:

- User registration.
- Account-status changes.
- Product creation and update.
- Claim submission.
- Claim review start, document request, resume, approval, and rejection.
- Claim settlement.
- Completed premium Payment.

Audit records do not store passwords, JWTs, KYC identity values, document bytes/paths, or payment credentials. `POLICY_PURCHASED` exists in the controlled enum but is not currently emitted. Policy renewal, nominee changes, cancellation, Profile/KYC, and document operations are not currently audited, and some Product/Payment events do not include actor attribution.

## 🚀 Local Development Setup

### Prerequisites

- Java 21
- Node.js and npm
- PostgreSQL

Maven is provided through the committed wrapper; a global Maven installation is not required.

### 1. Clone Repository

```bash
git clone <repository-url>
cd <repository-directory>
```

### 2. PostgreSQL Setup

Create the local database using the included script:

```powershell
psql -U <database-admin-user> -f .\scripts\create-local-database.sql
```

Equivalent SQL:

```sql
CREATE DATABASE sicms;
```

Do not commit a database password. SICMS defaults to `jdbc:postgresql://localhost:5432/sicms`; override the full URL with `SPRING_DATASOURCE_URL` when needed.

### 3. Backend Environment

[backend/.env.example](backend/.env.example) is a safe checklist. Spring Boot does **not** automatically load `.env` files; export values into the shell or configure them through your environment.

Required:

| Variable      | Purpose                               |
| ------------- | ------------------------------------- |
| `DB_PASSWORD` | PostgreSQL password                   |
| `JWT_SECRET`  | HMAC signing secret; minimum 32 bytes |

Optional:

| Variable                      | Default / Purpose                                  |
| ----------------------------- | -------------------------------------------------- |
| `SPRING_DATASOURCE_URL`       | `jdbc:postgresql://localhost:5432/sicms`           |
| `DB_USERNAME`                 | `postgres`                                         |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `PT15M`                                            |
| `CORS_ALLOWED_ORIGINS`        | `http://localhost:5173`                            |
| `DOCUMENT_STORAGE_PATH`       | `./var/documents`                                  |
| `MAX_FILE_SIZE`               | `10MB`                                             |
| `MAX_REQUEST_SIZE`            | `12MB`                                             |
| `BOOTSTRAP_ACCOUNTS_ENABLED`  | `false`                                            |
| `DEV_AGENT_PASSWORD`          | Required only when privileged bootstrap is enabled |
| `DEV_CLAIMS_OFFICER_PASSWORD` | Required only when privileged bootstrap is enabled |
| `DEV_ADMINISTRATOR_PASSWORD`  | Required only when privileged bootstrap is enabled |

PowerShell example:

```powershell
$env:DB_PASSWORD = '<local-database-password>'
$env:JWT_SECRET = '<random-secret-of-at-least-32-bytes>'
```

Privileged development accounts are disabled by default. Public registration creates only Customer accounts.

### 4. Backend Startup

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The API starts at `http://localhost:8080`. Flyway applies V1–V13 and Hibernate validates the resulting schema.

### 5. Frontend Environment

The committed [frontend/.env.example](frontend/.env.example) contains:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

`VITE_*` values are embedded in browser output and must never contain secrets. The frontend already defaults to this API URL, so a local `.env` is optional.

### 6. Frontend Startup

```powershell
cd frontend
npm ci
npm run dev -- --host localhost
```

Open `http://localhost:5173`.

## 🔌 API Documentation

With the backend running:

| Resource        | URL                                           |
| --------------- | --------------------------------------------- |
| Swagger UI      | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI JSON    | `http://localhost:8080/v3/api-docs`           |
| Actuator Health | `http://localhost:8080/actuator/health`       |

The backend contract is generated from current controllers and DTOs.

## 📬 API Testing with Postman

Import both files from [backend/postman](backend/postman):

- `SICMS.postman_collection.json`
- `SICMS-local.postman_environment.json`

Suggested workflow:

1. Start the backend.
2. Import the collection and local environment.
3. Register a synthetic Customer or configure optional privileged development accounts.
4. Login with the intended role.
5. Place the returned token into the role-specific environment variable when needed.
6. Run Product → Policy → Payment or Product → Policy → Claim → Review workflows.
7. Select a local file interactively for multipart Claim document requests.

Committed token and password variables are blank. Clear populated environments before exporting or sharing them.

## 🧪 Testing & Validation

### Backend

Backend tests use JUnit 5, Spring Boot Test, MockMvc, Spring Security Test, Mockito, repository tests, and H2 in PostgreSQL compatibility mode. Flyway and Hibernate schema validation run in the test profile.

Coverage includes authentication, JWT behavior, development account seeding, Customer/KYC, Product rules, Policy ownership and cancellation, Claim submission, documents, review transitions, settlement, Payments, reports, administration, local storage, and persistence constraints.

```powershell
cd backend
.\mvnw.cmd clean test
.\mvnw.cmd package -DskipTests=false
```

### Frontend

Frontend tests use Vitest, jsdom, Testing Library, and `jest-dom`. Coverage includes API construction/error handling, schemas, auth storage and route guards, query behavior, role navigation, dashboards, reports, Receipt download, feedback states, and shared UI semantics.

```powershell
cd frontend
npm ci
npm run format:check
npm run lint
npm test -- --run
npm run build
```

See [requirements traceability](docs/SICMS_REQUIREMENTS_TRACEABILITY.md) for FR-001 through FR-015 evidence and [non-functional validation](docs/SICMS_NON_FUNCTIONAL_VALIDATION.md) for verified limitations.

## 🖼 UI Showcase

All images below use local synthetic development data.

### Secure Access

![SICMS login](docs/screenshots/login.png)

### Customer Insurance Overview

![Customer dashboard](docs/screenshots/customer-dashboard.png)

### Claims Officer Operations

![Claims Officer dashboard](docs/screenshots/claims-officer-dashboard.png)

### Administrator Oversight

![Administrator dashboard](docs/screenshots/administrator-dashboard.png)

### Enterprise Reporting

![Administrator reporting](docs/screenshots/administrator-reports.png)

## 📁 Repository Structure

```text
smart-insurance-claim-management-system/
├── backend/
│   ├── .mvn/                  # Maven wrapper configuration
│   ├── postman/               # Collection and sanitized local environment
│   ├── src/main/java/         # Feature-oriented backend source
│   ├── src/main/resources/    # Configuration, logging, Flyway migrations
│   ├── src/test/              # Backend tests and test profile
│   ├── .env.example
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── src/                   # React application and tests
│   ├── .env.example
│   ├── package.json
│   ├── package-lock.json
│   └── vite.config.ts
├── docs/
│   ├── api/
│   ├── architecture/
│   ├── database/
│   ├── screenshots/
│   ├── SICMS_NON_FUNCTIONAL_VALIDATION.md
│   └── SICMS_REQUIREMENTS_TRACEABILITY.md
├── scripts/
│   └── create-local-database.sql
├── .gitignore
├── CONTRIBUTING.md
└── README.md
```

Generated build output, dependencies, logs, local environment files, runtime Claim documents, and database dumps are excluded through [.gitignore](.gitignore).

## ⚠️ Known Limitations

- Premium processing and Claim settlement are deterministic simulations; no external payment or payout provider is integrated.
- Local development uses HTTP. TLS termination is a deployment responsibility.
- Access tokens have finite expiry, but refresh tokens and server-side token revocation are not implemented.
- Claim files use local filesystem storage. Multi-instance deployment requires shared storage or an equivalent coordinated strategy.
- No malware scanning, external KYC provider, email verification, password reset, or automatic account lockout is included.
- KYC verification administration is not implemented; the module stores controlled Customer submissions and status.
- The 1,000-concurrent-user / sub-three-second performance target has not been proven in a production-equivalent environment.
- Load balancing, infrastructure automation, monitoring, backup/recovery, and high-availability configuration are outside this repository.
- The frontend production build currently emits a non-blocking shared-chunk size advisory.

## 📚 Project Documentation

- [Backend guide](backend/README.md)
- [Frontend guide](frontend/README.md)
- [Postman guide](backend/postman/README.md)
- [Architecture notes](docs/architecture/modular-monolith.md)
- [API notes](docs/api/README.md)
- [Database notes](docs/database/README.md)
- [Requirements traceability](docs/SICMS_REQUIREMENTS_TRACEABILITY.md)
- [Non-functional validation](docs/SICMS_NON_FUNCTIONAL_VALIDATION.md)
- [Contributing guide](CONTRIBUTING.md)
