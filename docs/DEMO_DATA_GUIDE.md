# SICMS local demo data

The demo dataset is fictional, local-development-only presentation data. It must never be used with a production database or real personal information.

## Safety controls

- The Spring seeder is inactive by default.
- It can run only with the `local`, `dev`, or `demo` profile, `APP_DEMO_DATA_ENABLED=true`, and `APP_DEMO_DATA_RESET=true`.
- It rejects a production-like active profile and a non-loopback PostgreSQL host.
- It verifies the connected database against `APP_DEMO_DATA_DATABASE` (default: `sicms`).
- The wrapper creates a timestamped PostgreSQL custom-format backup outside the repository at `%LOCALAPPDATA%\SICMS\backups` before startup.
- The reset deletes only application business data. `flyway_schema_history`, all V1-V13 schema objects, database roles/grants, and controlled application roles remain intact.

Install PostgreSQL client tools so `pg_dump` is on `PATH`. The wrapper will refuse to reset if it cannot create a backup.

## Required local environment variables

Set these in an ignored local environment file or your shell; never commit their values:

- `DB_PASSWORD` and `JWT_SECRET`
- `SICMS_DEMO_CUSTOMER_PASSWORD`
- `SICMS_DEMO_AGENT_PASSWORD`
- `SICMS_DEMO_CLAIMS_OFFICER_PASSWORD`
- `SICMS_DEMO_ADMIN_PASSWORD`
- `APP_DEMO_DATA_ENABLED=true`
- `APP_DEMO_DATA_RESET=true`

Optional: set `SPRING_DATASOURCE_URL` and `APP_DEMO_DATA_DATABASE` when using a non-default local database.

## Reset command

From `backend`, after setting the variables above:

```powershell
.\scripts\backup-and-reset-demo-data.ps1 -Profile local -DatabaseName sicms
```

The command logs only profile/host/database, table count, and domain counts. It never logs passwords, hashes, tokens, database passwords, KYC values, or document content. Remove `APP_DEMO_DATA_RESET` after completion; the wrapper does this in its current shell.

## Seeded inventory

Primary active demo identities are Maya Thompson (Customer), Ethan Brooks (Agent), Priya Raman (Claims Officer), and Daniel Mercer (Administrator), all at `example.test`. Sophia Lee is an inactive synthetic Customer account for the account-status screen. Passwords are intentionally not documented.

The catalog contains active HealthSecure, LifeGuard, and DriveAssure products plus an inactive TravelSafe Essential product. Product codes are `HSE-ESS-12`, `HSE-PRM-12`, `LGT-TERM-24`, `LGT-FAM-24`, `DRA-CMP-12`, `DRA-STD-12`, and `TSA-ESS-06`.

The customer receives paid and unpaid active policies, an expired policy, and an active policy with a pending cancellation request. The paid policy has a generated invoice, completed card payment, generated receipt, downloadable receipt PDF, and a synthetic nominee.

Claims are seeded through the application workflow in `SUBMITTED`, `DOCUMENTS_REQUIRED`, `APPROVED`, `SETTLED`, and `REJECTED` states. Their status history, review activity, audit records, and settlement data are generated from the same services used by the API. Safe synthetic document metadata and local files are created for the selected claims:

- `claim-incident-summary.pdf`
- `vehicle-damage-overview.jpg`
- `expense-summary.pdf`

Runtime documents are stored beneath `DOCUMENT_STORAGE_PATH/claims`, are ignored by Git, and are not source fixtures. Receipt PDFs are generated on demand.

## Reseeding and recovery

Run the reset command again to return all demo workflow states to their recording-ready values. To recover the previous local state, use PostgreSQL tooling to restore the custom-format backup created by the wrapper. Do not copy backups, runtime documents, `.env` files, logs, or secrets into Git.
