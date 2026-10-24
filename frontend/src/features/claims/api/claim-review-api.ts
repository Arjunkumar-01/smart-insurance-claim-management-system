import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type { ClaimDocumentResponse } from "../types/claim-types";
import type {
  ClaimReviewDetailResponse,
  ClaimReviewPageResponse,
  ClaimSettlementResponse,
} from "../types/claim-review-types";
export const reviewKeys = {
  all: ["claim-review"] as const,
  queue: (status?: string, page = 0) =>
    [...reviewKeys.all, "queue", status, page] as const,
  detail: (id: string) => [...reviewKeys.all, "detail", id] as const,
  documents: (id: string) => [...reviewKeys.all, "documents", id] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
const action = (id: string, endpoint: string, notes: string) =>
  request<ClaimReviewDetailResponse>(() =>
    apiClient.post(`/claims/review/${id}/${endpoint}`, { notes }),
  );
export const claimReviewApi = {
  queue: (status?: string, page = 0) =>
    request<ClaimReviewPageResponse>(() =>
      apiClient.get("/claims/review", {
        params: { ...(status ? { status } : {}), page, size: 20 },
      }),
    ),
  detail: (id: string) =>
    request<ClaimReviewDetailResponse>(() =>
      apiClient.get(`/claims/review/${id}`),
    ),
  documents: (id: string) =>
    request<ClaimDocumentResponse[]>(() =>
      apiClient.get(`/claims/review/${id}/documents`),
    ),
  download: (claimId: string, documentId: string) =>
    request<Blob>(() =>
      apiClient.get(`/claims/review/${claimId}/documents/${documentId}`, {
        responseType: "blob",
      }),
    ),
  start: (id: string, notes = "") => action(id, "start", notes),
  requestDocuments: (id: string, notes = "") =>
    action(id, "request-documents", notes),
  resume: (id: string, notes = "") => action(id, "resume", notes),
  approve: (id: string, notes = "") => action(id, "approve", notes),
  reject: (id: string, notes = "") => action(id, "reject", notes),
  settle: (id: string, notes = "") =>
    request<ClaimSettlementResponse>(() =>
      apiClient.post(`/claims/review/${id}/settle`, { notes }),
    ),
};
