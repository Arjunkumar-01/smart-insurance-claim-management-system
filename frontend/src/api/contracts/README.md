# Frozen Backend Contract Map

This document is the frontend Phase 0 contract reference. It is manually maintained from the frozen Spring Boot controllers, DTOs, enums, and OpenAPI surface. The backend remains the source of truth; frontend business DTOs will be added with their feature implementation.

## Transport

- API base URL: `VITE_API_BASE_URL`, normalized without a trailing slash. Local default: `http://localhost:8080/api`.
- Route prefix: `/api`.
- Authentication: `POST /api/auth/register` and `POST /api/auth/login` are public. Login returns `LoginResponse { accessToken, tokenType, expiresAt, user }`; registration returns `RegisterResponse { user }` and does not create an authenticated session.
- JSON: request/response bodies use `application/json` except document upload.
- Multipart: `POST /api/claims/me/{claimId}/documents` accepts a `file` part and returns JSON metadata. Downloads return the file resource with `Content-Disposition`.
- IDs: UUID strings.
- `Instant`: ISO-8601 UTC timestamp strings.
- `LocalDate`: ISO-8601 date-only strings; do not apply a timezone conversion.
- `BigDecimal`: JSON numeric values from the backend. Phase 0 performs no financial calculations.

## Error contract

`ApiError` is:

```text
timestamp: Instant
status: number
error: string
message: string
path: string
fieldErrors: Record<string, string>
```

Known responses include 400 validation/storage/argument errors, 401 unauthenticated, 403 forbidden, 404 missing resources, 409 duplicate/optimistic-lock conflicts, and 500 configuration errors. The shared client normalizes these without redirecting or exposing raw Axios/backend objects.

## Pagination

The backend uses module-specific page responses, not Spring's raw `Page` JSON:

```text
{ <items>: DTO[], page: number, size: number, totalElements: number, totalPages: number }
```

`<items>` is `users`, `logs`, `claims`, `payments`, `policies`, `products`, or `claims` for review queues. Default query values are generally `page=0` and `size=20`. Product listing also supports filters and an allow-listed sort parameter.

## Roles and authorities

Application roles are exactly `CUSTOMER`, `AGENT`, `CLAIMS_OFFICER`, and `ADMINISTRATOR`. Spring authorities may be represented as `ROLE_<application role>`. No other frontend role values are valid.

## Endpoint inventory

Role names below are application roles. All listed endpoints require JWT authentication unless explicitly marked public.

### Authentication

| Method | Path                 | Role   | Request           | Response           | Status        |
| ------ | -------------------- | ------ | ----------------- | ------------------ | ------------- |
| POST   | `/api/auth/register` | Public | `RegisterRequest` | `RegisterResponse` | 201, 400, 409 |
| POST   | `/api/auth/login`    | Public | `LoginRequest`    | `LoginResponse`    | 200, 400, 401 |

### Customer profile and KYC

| Method | Path                        | Role     | Request                        | Response                  | Status |
| ------ | --------------------------- | -------- | ------------------------------ | ------------------------- | ------ |
| POST   | `/api/customers/me/profile` | CUSTOMER | `CreateCustomerProfileRequest` | `CustomerProfileResponse` | 201    |
| GET    | `/api/customers/me/profile` | CUSTOMER | none                           | `CustomerProfileResponse` | 200    |
| PUT    | `/api/customers/me/profile` | CUSTOMER | `UpdateCustomerProfileRequest` | `CustomerProfileResponse` | 200    |
| GET    | `/api/customers/me/kyc`     | CUSTOMER | none                           | `CustomerKycResponse`     | 200    |
| PUT    | `/api/customers/me/kyc`     | CUSTOMER | `UpdateCustomerKycRequest`     | `CustomerKycResponse`     | 200    |

Customer Profile request fields are `phoneNumber`, nullable `dateOfBirth` (`LocalDate`), `addressLine1`, nullable `addressLine2`, `city`, `state`, `postalCode`, and `country`. Profile responses also contain read-only `customerId`, User-owned `firstName`, `lastName`, and `email`, plus `createdAt` and `updatedAt`.

KYC upsert uses `PUT /api/customers/me/kyc` for both create and update. Its request contains only `identityType` and `identityNumber`. The response contains `kycId`, `identityType`, `maskedIdentityNumber`, server-controlled `kycStatus`, `createdAt`, and `updatedAt`. KYC statuses are `PENDING`, `VERIFIED`, and `REJECTED`; the frontend never submits status or persists the original identity number.

### Products

| Method | Path                        | Role                           | Request/query                   | Response                    | Status |
| ------ | --------------------------- | ------------------------------ | ------------------------------- | --------------------------- | ------ |
| POST   | `/api/products`             | ADMINISTRATOR                  | `CreateProductRequest`          | `ProductResponse`           | 201    |
| PUT    | `/api/products/{productId}` | ADMINISTRATOR                  | `UpdateProductRequest`          | `ProductResponse`           | 200    |
| GET    | `/api/products`             | CUSTOMER, AGENT, ADMINISTRATOR | filters, `page`, `size`, `sort` | `ProductPageResponse`       | 200    |
| GET    | `/api/products/{productId}` | CUSTOMER, AGENT, ADMINISTRATOR | none                            | `ProductResponse`           | 200    |
| GET    | `/api/products/compare`     | CUSTOMER, AGENT, ADMINISTRATOR | `ids` query                     | `ProductComparisonResponse` | 200    |

Product list filters are `name`, `productCode`, `category`, `status`, `page`, `size`, and `sort`. Backend-supported sort fields are `name`, `productCode`, `premiumAmount`, `coverageAmount`, and `createdAt`, with `asc` or `desc` direction. Page size is limited by the backend to 50. CUSTOMER and AGENT visibility is ACTIVE-only; ADMINISTRATOR may view ACTIVE and INACTIVE. Product comparison accepts 2 to 4 unique IDs. Product creation makes status ACTIVE server-side; update accepts status but does not accept productCode.

### Policies

| Method | Path                                               | Role          | Request/query                      | Response                       | Status |
| ------ | -------------------------------------------------- | ------------- | ---------------------------------- | ------------------------------ | ------ |
| POST   | `/api/policies`                                    | CUSTOMER      | `PurchasePolicyRequest`            | `PolicyResponse`               | 201    |
| GET    | `/api/policies/me`                                 | CUSTOMER      | `page`, `size`                     | `PolicyPageResponse`           | 200    |
| GET    | `/api/policies/me/{policyId}`                      | CUSTOMER      | none                               | `PolicyResponse`               | 200    |
| PUT    | `/api/policies/me/{policyId}/renewal`              | CUSTOMER      | `RenewPolicyRequest`               | `PolicyResponse`               | 200    |
| PUT    | `/api/policies/me/{policyId}/nominee`              | CUSTOMER      | `UpdateNomineeRequest`             | `PolicyResponse`               | 200    |
| POST   | `/api/policies/me/{policyId}/cancellation`         | CUSTOMER      | `RequestPolicyCancellationRequest` | `PolicyCancellationResponse`   | 201    |
| GET    | `/api/policies/admin`                              | ADMINISTRATOR | `page`, `size`                     | `PolicyPageResponse`           | 200    |
| GET    | `/api/policies/cancellations`                      | ADMINISTRATOR | none                               | `PolicyCancellationResponse[]` | 200    |
| PUT    | `/api/policies/cancellations/{requestId}/decision` | ADMINISTRATOR | `DecidePolicyCancellationRequest`  | `PolicyCancellationResponse`   | 200    |

### Claims and documents

| Method | Path                                              | Role     | Request/query        | Response                       | Status |
| ------ | ------------------------------------------------- | -------- | -------------------- | ------------------------------ | ------ |
| POST   | `/api/claims`                                     | CUSTOMER | `SubmitClaimRequest` | `ClaimResponse`                | 201    |
| GET    | `/api/claims/me`                                  | CUSTOMER | `page`, `size`       | `ClaimPageResponse`            | 200    |
| GET    | `/api/claims/me/{claimId}`                        | CUSTOMER | none                 | `ClaimResponse`                | 200    |
| GET    | `/api/claims/me/{claimId}/history`                | CUSTOMER | none                 | `ClaimStatusHistoryResponse[]` | 200    |
| POST   | `/api/claims/me/{claimId}/documents`              | CUSTOMER | multipart `file`     | `ClaimDocumentResponse`        | 201    |
| GET    | `/api/claims/me/{claimId}/documents`              | CUSTOMER | none                 | `ClaimDocumentResponse[]`      | 200    |
| GET    | `/api/claims/me/{claimId}/documents/{documentId}` | CUSTOMER | none                 | file resource                  | 200    |

### Claims Officer review and settlement

| Method | Path                                                  | Role           | Request/query            | Response                    | Status |
| ------ | ----------------------------------------------------- | -------------- | ------------------------ | --------------------------- | ------ |
| GET    | `/api/claims/review`                                  | CLAIMS_OFFICER | `status`, `page`, `size` | `ClaimReviewPageResponse`   | 200    |
| GET    | `/api/claims/review/{claimId}`                        | CLAIMS_OFFICER | none                     | `ClaimReviewDetailResponse` | 200    |
| GET    | `/api/claims/review/{claimId}/documents`              | CLAIMS_OFFICER | none                     | `ClaimDocumentResponse[]`   | 200    |
| GET    | `/api/claims/review/{claimId}/documents/{documentId}` | CLAIMS_OFFICER | none                     | file resource               | 200    |
| POST   | `/api/claims/review/{claimId}/start`                  | CLAIMS_OFFICER | `ReviewDecisionRequest`  | `ClaimReviewDetailResponse` | 200    |
| POST   | `/api/claims/review/{claimId}/request-documents`      | CLAIMS_OFFICER | `ReviewDecisionRequest`  | `ClaimReviewDetailResponse` | 200    |
| POST   | `/api/claims/review/{claimId}/resume`                 | CLAIMS_OFFICER | `ReviewDecisionRequest`  | `ClaimReviewDetailResponse` | 200    |
| POST   | `/api/claims/review/{claimId}/approve`                | CLAIMS_OFFICER | `ReviewDecisionRequest`  | `ClaimReviewDetailResponse` | 200    |
| POST   | `/api/claims/review/{claimId}/reject`                 | CLAIMS_OFFICER | `ReviewDecisionRequest`  | `ClaimReviewDetailResponse` | 200    |
| POST   | `/api/claims/review/{claimId}/settle`                 | CLAIMS_OFFICER | `SettleClaimRequest`     | `ClaimSettlementResponse`   | 201    |
| GET    | `/api/claims/me/{claimId}/settlement`                 | CUSTOMER       | none                     | `ClaimSettlementResponse`   | 200    |

### Payments

| Method | Path                                   | Role     | Request/query        | Response              | Status |
| ------ | -------------------------------------- | -------- | -------------------- | --------------------- | ------ |
| POST   | `/api/payments`                        | CUSTOMER | `MakePaymentRequest` | `PaymentResponse`     | 201    |
| GET    | `/api/payments/me`                     | CUSTOMER | `page`, `size`       | `PaymentPageResponse` | 200    |
| GET    | `/api/payments/me/{paymentId}`         | CUSTOMER | none                 | `PaymentResponse`     | 200    |
| GET    | `/api/payments/me/{paymentId}/invoice` | CUSTOMER | none                 | `InvoiceResponse`     | 200    |
| GET    | `/api/payments/me/{paymentId}/receipt` | CUSTOMER | none                 | `ReceiptResponse`     | 200    |

### Reports, administration, and audit

| Method | Path                                      | Role          | Request/query                | Response                       | Status |
| ------ | ----------------------------------------- | ------------- | ---------------------------- | ------------------------------ | ------ |
| GET    | `/api/reports/me/summary`                 | CUSTOMER      | none                         | `CustomerReportResponse`       | 200    |
| GET    | `/api/reports/admin/overview`             | ADMINISTRATOR | none                         | `AdminOverviewResponse`        | 200    |
| GET    | `/api/reports/admin/revenue/monthly`      | ADMINISTRATOR | optional `year`              | `MonthlyRevenueResponse[]`     | 200    |
| GET    | `/api/reports/admin/products/performance` | ADMINISTRATOR | none                         | `ProductPerformanceResponse[]` | 200    |
| GET    | `/api/admin/users`                        | ADMINISTRATOR | `page`, `size`, `sort`       | `AdminUserPageResponse`        | 200    |
| GET    | `/api/admin/users/{userId}`               | ADMINISTRATOR | none                         | `AdminUserResponse`            | 200    |
| PATCH  | `/api/admin/users/{userId}/status`        | ADMINISTRATOR | `ChangeAccountStatusRequest` | `AdminUserResponse`            | 200    |
| GET    | `/api/admin/audit-logs`                   | ADMINISTRATOR | `page`, `size`               | `AuditLogPageResponse`         | 200    |

## Enum values

- `ApplicationRole`: `CUSTOMER`, `AGENT`, `CLAIMS_OFFICER`, `ADMINISTRATOR`
- `AccountStatus`: `ACTIVE`, `INACTIVE`, `LOCKED`
- `KycStatus`: `PENDING`, `VERIFIED`, `REJECTED`
- `ProductCategory`: `HEALTH`, `LIFE`, `VEHICLE`
- `ProductStatus`: `ACTIVE`, `INACTIVE`
- `PolicyStatus`: `ACTIVE`, `EXPIRED`, `CANCELLED`
- `CancellationStatus`: `PENDING`, `APPROVED`, `REJECTED`
- `ClaimStatus`: `SUBMITTED`, `UNDER_REVIEW`, `DOCUMENTS_REQUIRED`, `APPROVED`, `REJECTED`, `SETTLED`
- `ClaimReviewAction`: `START_REVIEW`, `REQUEST_DOCUMENTS`, `RESUME_REVIEW`, `APPROVE`, `REJECT`
- `PaymentStatus`: `PENDING`, `COMPLETED`, `FAILED`
- `PaymentMethod`: `CARD`, `BANK_TRANSFER`, `UPI`
- `AuditAction`: `USER_REGISTERED`, `ACCOUNT_STATUS_CHANGED`, `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `POLICY_PURCHASED`, `CLAIM_SUBMITTED`, `CLAIM_REVIEW_STARTED`, `CLAIM_DOCUMENTS_REQUESTED`, `CLAIM_REVIEW_RESUMED`, `CLAIM_APPROVED`, `CLAIM_REJECTED`, `CLAIM_SETTLED`, `PREMIUM_PAYMENT_COMPLETED`

## Phase 0 boundary

Phase 1 implements authentication, session storage, protected routing, role routing, and minimal role landing pages. It does not implement business screens. The frontend stores the login response in `sessionStorage`; the backend has no refresh-token or server-side logout/revocation endpoint. Frontend role guards are usability controls only and do not replace backend authorization.
