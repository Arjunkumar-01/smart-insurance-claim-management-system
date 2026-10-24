import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  CustomerKycResponse,
  CustomerProfileRequest,
  CustomerProfileResponse,
  UpdateCustomerKycRequest,
} from "../types/customer-types";

export const customerKeys = {
  all: ["customer"] as const,
  profile: () => [...customerKeys.all, "profile"] as const,
  kyc: () => [...customerKeys.all, "kyc"] as const,
};

async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}

export const customerApi = {
  getProfile: () =>
    request<CustomerProfileResponse>(() =>
      apiClient.get("/customers/me/profile"),
    ),
  createProfile: (payload: CustomerProfileRequest) =>
    request<CustomerProfileResponse>(() =>
      apiClient.post("/customers/me/profile", payload),
    ),
  updateProfile: (payload: CustomerProfileRequest) =>
    request<CustomerProfileResponse>(() =>
      apiClient.put("/customers/me/profile", payload),
    ),
  getKyc: () =>
    request<CustomerKycResponse>(() => apiClient.get("/customers/me/kyc")),
  updateKyc: (payload: UpdateCustomerKycRequest) =>
    request<CustomerKycResponse>(() =>
      apiClient.put("/customers/me/kyc", payload),
    ),
};
