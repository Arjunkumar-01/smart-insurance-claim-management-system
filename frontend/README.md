# SICMS Frontend

React 19 and TypeScript frontend for SICMS, built with Vite, Material UI, React Router, Axios, TanStack Query, React Hook Form, Zod, Vitest, and Testing Library.

## Setup

```powershell
npm ci
npm run dev -- --host localhost
```

The application runs at `http://localhost:5173`. Copy [.env.example](.env.example) to an ignored local `.env` only when overriding the API URL. `VITE_*` values are embedded in browser output and must never contain secrets.

## Commands

```powershell
npm run format:check
npm run lint
npm test -- --run
npm run build
```

## Application Structure

- `src/api`: shared Axios client, normalized API errors, and query infrastructure.
- `src/app`: providers and role-protected route definitions.
- `src/auth`: session storage, authentication context, and route guards.
- `src/components`: shared layout and feedback UI.
- `src/features`: authentication, customers, products, policies, payments, claims, dashboards, reports, administration, and audit.
- `src/theme`: SICMS Material UI design system.

The backend remains authoritative for roles, ownership, validation, monetary values, Claim transitions, Policy periods, and status. Frontend guards improve navigation but do not replace backend authorization.

## Roles And Workspaces

- Customer: Profile/KYC, Products, Policies, Payments, Claims, and portfolio reporting.
- Agent: Product discovery and comparison.
- Claims Officer: Claims operations queue, evidence, review activity, lifecycle actions, and settlement.
- Administrator: Products, Policies, Reporting, Users, and Audit.

Authentication state is stored in `sessionStorage`. The Axios client injects Bearer authorization centrally. Protected-request 401 responses clear the session; 403 responses preserve it and show the restricted-access view.

## Design System

SICMS uses navy and corporate blue structural colors, restrained cyan accents, semantic status colors, compact enterprise spacing, visible keyboard focus, responsive navigation, and horizontally scrollable operational tables. The palette and component rules are defined in `src/theme/theme.ts`.

## Payments And Documents

Premium payments are simulated and collect no banking/card credentials. Receipt downloads use authenticated Blob requests. Claim uploads accept only the backend-supported document types and never expose storage paths.

## Known Limitations

The local application uses HTTP. The production build currently reports a non-blocking shared-chunk size advisory. Production TLS, monitoring, shared document storage, availability, and load testing are outside frontend runtime configuration.
