import { describe, expect, it } from "vitest";
import { customerKycSchema } from "./customer-kyc-schema";
import { customerProfileSchema } from "./customer-profile-schema";

describe("customer schemas", () => {
  it("validates the backend profile constraints", () => {
    expect(
      customerProfileSchema.safeParse({
        phoneNumber: "123",
        dateOfBirth: "2099-01-01",
        addressLine1: "",
        addressLine2: "",
        city: "",
        state: "",
        postalCode: "",
        country: "",
      }).success,
    ).toBe(false);
    expect(
      customerProfileSchema.safeParse({
        phoneNumber: "+1 (555) 123-4567",
        dateOfBirth: "1990-01-01",
        addressLine1: "1 Main Street",
        addressLine2: "",
        city: "City",
        state: "State",
        postalCode: "12345",
        country: "Country",
      }).success,
    ).toBe(true);
  });

  it("requires KYC identity fields but does not include status", () => {
    expect(
      customerKycSchema.safeParse({ identityType: "", identityNumber: "" })
        .success,
    ).toBe(false);
    expect(
      customerKycSchema.safeParse({
        identityType: "National ID",
        identityNumber: "secret",
      }).success,
    ).toBe(true);
    expect(Object.keys(customerKycSchema.shape)).not.toContain("kycStatus");
  });
});
