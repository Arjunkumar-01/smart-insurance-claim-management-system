export type KycStatus = "PENDING" | "VERIFIED" | "REJECTED";

export interface CustomerProfileResponse {
  customerId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string | null;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerProfileRequest {
  phoneNumber: string;
  dateOfBirth: string | null;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export interface CustomerKycResponse {
  kycId: string;
  identityType: string;
  maskedIdentityNumber: string;
  kycStatus: KycStatus;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateCustomerKycRequest {
  identityType: string;
  identityNumber: string;
}
