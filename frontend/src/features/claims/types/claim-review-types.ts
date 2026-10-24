import type {
  ClaimDocumentResponse,
  ClaimStatus,
  ClaimStatusHistoryResponse,
  ClaimType,
} from "./claim-types";
export type ClaimReviewAction =
  "START_REVIEW" | "REQUEST_DOCUMENTS" | "RESUME_REVIEW" | "APPROVE" | "REJECT";
export interface ClaimReviewResponse {
  reviewId: string;
  action: ClaimReviewAction;
  notes: string | null;
  reviewerId: string;
  createdAt: string;
}
export interface ClaimReviewDetailResponse {
  claimId: string;
  claimNumber: string;
  claimAmount: number;
  coverageAmount: number;
  claimType: ClaimType;
  incidentDate: string;
  description: string;
  status: ClaimStatus;
  policyNumber: string;
  submittedAt: string;
  documents: ClaimDocumentResponse[];
  reviews: ClaimReviewResponse[];
  history: ClaimStatusHistoryResponse[];
}
export interface ClaimReviewPageResponse {
  claims: ClaimReviewDetailResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ClaimSettlementResponse {
  settlementId: string;
  settlementNumber: string;
  claimId: string;
  claimNumber: string;
  settlementAmount: number;
  settledAt: string;
}
