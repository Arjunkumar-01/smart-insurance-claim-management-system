import { screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { CustomerKycSummary } from "./CustomerKycSummary";
import { CustomerProfileSummary } from "./CustomerProfileSummary";
import { renderWithProviders } from "../../../test/renderWithProviders";

const profile = {
  customerId: "customer-1",
  firstName: "Test",
  lastName: "Customer",
  email: "customer@example.test",
  phoneNumber: "+15551234567",
  dateOfBirth: "1990-01-01",
  addressLine1: "1 Main Street",
  addressLine2: null,
  city: "City",
  state: "State",
  postalCode: "12345",
  country: "Country",
  createdAt: "2026-01-01T00:00:00Z",
  updatedAt: "2026-01-01T00:00:00Z",
};

describe("customer summaries", () => {
  it("renders account fields as read-only and does not expose ownership controls", () => {
    renderWithProviders(<CustomerProfileSummary profile={profile} />);
    expect(screen.getByText("Test Customer")).toBeInTheDocument();
    expect(screen.getByText("customer@example.test")).toBeInTheDocument();
    expect(screen.queryByText("customer-1")).not.toBeInTheDocument();
  });

  it.each([
    ["PENDING", "warning"],
    ["VERIFIED", "success"],
    ["REJECTED", "error"],
  ] as const)("renders %s KYC status with masked identity", (status) => {
    renderWithProviders(
      <CustomerKycSummary
        kyc={{
          kycId: "kyc-1",
          identityType: "National ID",
          maskedIdentityNumber: "******1234",
          kycStatus: status,
          createdAt: "2026-01-01T00:00:00Z",
          updatedAt: "2026-01-01T00:00:00Z",
        }}
      />,
    );
    expect(screen.getByText(status)).toBeInTheDocument();
    expect(screen.getByText("******1234")).toBeInTheDocument();
  });
});
