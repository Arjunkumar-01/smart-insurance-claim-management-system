import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  InvoiceResponse,
  MakePaymentRequest,
  PaymentPageResponse,
  PaymentResponse,
  ReceiptResponse,
} from "../types/payment-types";

export const paymentKeys = {
  all: ["payments"] as const,
  mine: (page: number) => [...paymentKeys.all, "mine", page] as const,
  detail: (id: string) => [...paymentKeys.all, "detail", id] as const,
  invoice: (id: string) => [...paymentKeys.all, "invoice", id] as const,
  receipt: (id: string) => [...paymentKeys.all, "receipt", id] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
export const paymentApi = {
  pay: (payload: MakePaymentRequest) =>
    request<PaymentResponse>(() => apiClient.post("/payments", payload)),
  mine: (page = 0, size = 20) =>
    request<PaymentPageResponse>(() =>
      apiClient.get("/payments/me", { params: { page, size } }),
    ),
  detail: (id: string) =>
    request<PaymentResponse>(() => apiClient.get(`/payments/me/${id}`)),
  invoice: (id: string) =>
    request<InvoiceResponse>(() => apiClient.get(`/payments/me/${id}/invoice`)),
  receipt: (id: string) =>
    request<ReceiptResponse>(() => apiClient.get(`/payments/me/${id}/receipt`)),
  downloadReceipt: (id: string) =>
    request<Blob>(() =>
      apiClient.get(`/payments/me/${id}/receipt/download`, {
        headers: { Accept: "application/pdf" },
        responseType: "blob",
      }),
    ),
};
