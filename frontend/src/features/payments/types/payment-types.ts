export type PaymentStatus = "PENDING" | "COMPLETED" | "FAILED";
export type PaymentMethod = "CARD" | "BANK_TRANSFER" | "UPI";
export interface InvoiceResponse {
  invoiceId: string;
  invoiceNumber: string;
  policyId: string;
  amount: number;
  issuedAt: string;
}
export interface ReceiptResponse {
  receiptId: string;
  receiptNumber: string;
  paymentId: string;
  issuedAt: string;
}
export interface PaymentResponse {
  paymentId: string;
  paymentNumber: string;
  policyId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  transactionReference: string;
  paidAt: string;
  invoice: InvoiceResponse | null;
  receipt: ReceiptResponse | null;
}
export interface PaymentPageResponse {
  payments: PaymentResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface MakePaymentRequest {
  policyId: string;
  paymentMethod: PaymentMethod;
}
