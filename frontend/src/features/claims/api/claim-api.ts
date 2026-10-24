import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  ClaimDocumentResponse,
  ClaimPageResponse,
  ClaimResponse,
  ClaimSettlementResponse,
  ClaimStatusHistoryResponse,
  SubmitClaimRequest,
} from "../types/claim-types";

export const claimKeys = {
  all: ["claims"] as const,
  mine: (page: number) => [...claimKeys.all, "mine", page] as const,
  detail: (id: string) => [...claimKeys.all, "detail", id] as const,
  history: (id: string) => [...claimKeys.all, "history", id] as const,
  documents: (id: string) => [...claimKeys.all, "documents", id] as const,
  settlement: (id: string) => [...claimKeys.all, "settlement", id] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
export const claimApi = {
  submit: (payload: SubmitClaimRequest) =>
    request<ClaimResponse>(() => apiClient.post("/claims", payload)),
  mine: (page = 0, size = 20) =>
    request<ClaimPageResponse>(() =>
      apiClient.get("/claims/me", { params: { page, size } }),
    ),
  detail: (id: string) =>
    request<ClaimResponse>(() => apiClient.get(`/claims/me/${id}`)),
  history: (id: string) =>
    request<ClaimStatusHistoryResponse[]>(() =>
      apiClient.get(`/claims/me/${id}/history`),
    ),
  documents: (id: string) =>
    request<ClaimDocumentResponse[]>(() =>
      apiClient.get(`/claims/me/${id}/documents`),
    ),
  upload: (id: string, file: File) => {
    const form = new FormData();
    form.append("file", file);
    return request<ClaimDocumentResponse>(() =>
      apiClient.post(`/claims/me/${id}/documents`, form),
    );
  },
  download: (claimId: string, documentId: string) =>
    request<Blob>(() =>
      apiClient.get(`/claims/me/${claimId}/documents/${documentId}`, {
        responseType: "blob",
      }),
    ),
  settlement: (id: string) =>
    request<ClaimSettlementResponse>(() =>
      apiClient.get(`/claims/me/${id}/settlement`),
    ),
};
