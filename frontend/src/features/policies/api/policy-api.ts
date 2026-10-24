import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  DecideCancellationRequest,
  PolicyCancellationResponse,
  PolicyPageResponse,
  PolicyResponse,
  PurchasePolicyRequest,
  RenewPolicyRequest,
  RequestCancellationRequest,
  UpdateNomineeRequest,
} from "../types/policy-types";

export const policyKeys = {
  all: ["policies"] as const,
  mine: (page: number) => [...policyKeys.all, "mine", page] as const,
  detail: (id: string) => [...policyKeys.all, "detail", id] as const,
  admin: (page: number) => [...policyKeys.all, "admin", page] as const,
  cancellations: () => [...policyKeys.all, "cancellations"] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
export const policyApi = {
  purchase: (payload: PurchasePolicyRequest) =>
    request<PolicyResponse>(() => apiClient.post("/policies", payload)),
  mine: (page = 0, size = 20) =>
    request<PolicyPageResponse>(() =>
      apiClient.get("/policies/me", { params: { page, size } }),
    ),
  detail: (id: string) =>
    request<PolicyResponse>(() => apiClient.get(`/policies/me/${id}`)),
  renew: (id: string, payload: RenewPolicyRequest) =>
    request<PolicyResponse>(() =>
      apiClient.put(`/policies/me/${id}/renewal`, payload),
    ),
  nominee: (id: string, payload: UpdateNomineeRequest) =>
    request<PolicyResponse>(() =>
      apiClient.put(`/policies/me/${id}/nominee`, payload),
    ),
  requestCancellation: (id: string, payload: RequestCancellationRequest) =>
    request<PolicyCancellationResponse>(() =>
      apiClient.post(`/policies/me/${id}/cancellation`, payload),
    ),
  admin: (page = 0, size = 20) =>
    request<PolicyPageResponse>(() =>
      apiClient.get("/policies/admin", { params: { page, size } }),
    ),
  cancellations: () =>
    request<PolicyCancellationResponse[]>(() =>
      apiClient.get("/policies/cancellations"),
    ),
  decideCancellation: (id: string, payload: DecideCancellationRequest) =>
    request<PolicyCancellationResponse>(() =>
      apiClient.put(`/policies/cancellations/${id}/decision`, payload),
    ),
};
