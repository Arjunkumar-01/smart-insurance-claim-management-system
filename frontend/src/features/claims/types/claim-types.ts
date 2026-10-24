export type ClaimStatus =
  | "SUBMITTED"
  | "UNDER_REVIEW"
  | "DOCUMENTS_REQUIRED"
  | "APPROVED"
  | "REJECTED"
  | "SETTLED";
export type ClaimType = "HEALTH" | "LIFE" | "VEHICLE" | "TRAVEL";
export interface ClaimResponse {
  claimId: string;
  claimNumber: string;
  policyId: string;
  policyNumber: string;
  claimAmount: number;
  coverageAmount: number;
  claimType: ClaimType;
  incidentDate: string;
  description: string;
  status: ClaimStatus;
  submittedAt: string;
  createdAt: string;
  updatedAt: string;
}
export interface ClaimPageResponse {
  claims: ClaimResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ClaimStatusHistoryResponse {
  historyId: string;
  fromStatus: ClaimStatus | null;
  toStatus: ClaimStatus;
  actor: string;
  reason: string;
  changedAt: string;
}
export interface ClaimDocumentResponse {
  documentId: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  uploadedAt: string;
}
export interface SubmitClaimRequest {
  policyId: string;
  claimAmount: number;
  claimType: ClaimType;
  incidentDate: string;
  description: string;
}
export interface ClaimSettlementResponse {
  settlementId: string;
  settlementNumber: string;
  claimId: string;
  claimNumber: string;
  settlementAmount: number;
  settledAt: string;
}
