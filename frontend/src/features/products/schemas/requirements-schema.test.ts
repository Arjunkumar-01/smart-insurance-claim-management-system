import { describe, expect, it } from "vitest";
import { claimSchema } from "../../claims/schemas/claim-schema";
import { productFormSchema } from "./product-schema";

const product = {
  productCode: "TRV-1",
  name: "Travel Secure",
  description: "Travel cover",
  category: "TRAVEL" as const,
  coverageDescription: "Covered travel events",
  coverageAmount: 1000,
  premiumAmount: 25,
  tenureMonths: 6,
};

describe("requirements remediation schemas", () => {
  it("accepts controlled Travel Products and rejects non-positive tenure", () => {
    expect(productFormSchema.safeParse(product).success).toBe(true);
    expect(
      productFormSchema.safeParse({ ...product, tenureMonths: 0 }).success,
    ).toBe(false);
  });

  it("requires a controlled Claim Type", () => {
    const claim = {
      policyId: "550e8400-e29b-41d4-a716-446655440000",
      claimAmount: 100,
      incidentDate: "2026-10-25",
      description: "Incident",
    };
    expect(claimSchema.safeParse(claim).success).toBe(false);
    expect(
      claimSchema.safeParse({ ...claim, claimType: "TRAVEL" }).success,
    ).toBe(true);
  });
});
