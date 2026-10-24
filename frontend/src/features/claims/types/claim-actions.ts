import type { ClaimStatus } from "./claim-types";
export type ClaimWorkflowAction =
  | "START_REVIEW"
  | "REQUEST_DOCUMENTS"
  | "RESUME_REVIEW"
  | "APPROVE"
  | "REJECT"
  | "SETTLE";
export function getAvailableClaimActions(
  status: ClaimStatus,
): ClaimWorkflowAction[] {
  return (
    {
      SUBMITTED: ["START_REVIEW"],
      UNDER_REVIEW: ["REQUEST_DOCUMENTS", "APPROVE", "REJECT"],
      DOCUMENTS_REQUIRED: ["RESUME_REVIEW"],
      APPROVED: ["SETTLE"],
      REJECTED: [],
      SETTLED: [],
    } as Record<ClaimStatus, ClaimWorkflowAction[]>
  )[status];
}
