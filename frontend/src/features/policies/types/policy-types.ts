export type PolicyStatus = "ACTIVE" | "EXPIRED" | "CANCELLED";
export type CancellationStatus = "PENDING" | "APPROVED" | "REJECTED";
export interface PolicyNominee {
  nomineeId: string;
  name: string;
  relationship: string;
  contactNumber: string | null;
}
export interface PolicyResponse {
  policyId: string;
  policyNumber: string;
  productId: string;
  productCode: string;
  productName: string;
  productTenureMonths: number;
  premiumAmount: number;
  coverageAmount: number;
  startDate: string;
  endDate: string;
  status: PolicyStatus;
  nominee: PolicyNominee | null;
  createdAt: string;
  updatedAt: string;
}
export interface PolicyPageResponse {
  policies: PolicyResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface PurchasePolicyRequest {
  productId: string;
  startDate: string;
  nominee?: UpdateNomineeRequest;
}
export type RenewPolicyRequest = Record<string, never>;
export interface UpdateNomineeRequest {
  name: string;
  relationship: string;
  contactNumber: string;
}
export interface RequestCancellationRequest {
  reason: string;
}
export interface DecideCancellationRequest {
  status: "APPROVED" | "REJECTED";
  decisionComment: string;
}
export interface PolicyCancellationResponse {
  requestId: string;
  policyId: string;
  policyNumber: string;
  status: CancellationStatus;
  reason: string | null;
  decisionComment: string | null;
  requestedAt: string;
  decidedAt: string | null;
}
