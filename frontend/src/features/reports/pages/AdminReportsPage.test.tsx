import { screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { renderWithProviders } from "../../../test/renderWithProviders";
import { AdminReportsPage } from "./AdminReportsPage";

vi.mock("../api/report-api", () => ({
  reportKeys: {
    overview: () => ["overview"],
    revenue: (year: number) => ["revenue", year],
    products: () => ["products"],
    settlements: (year: number) => ["settlements", year],
  },
  reportApi: {
    overview: async () => ({
      customerCount: 4,
      totalClaims: 8,
      approvedClaims: 2,
      rejectedClaims: 1,
      pendingClaims: 3,
      premiumCollected: 500,
      settlementsPaid: 100,
      claimsRatioPercent: 20,
      policiesByStatus: [{ status: "ACTIVE", count: 2 }],
      claimsByStatus: [{ status: "UNDER_REVIEW", count: 3 }],
    }),
    revenue: async (year: number) => [
      { year, month: 10, paymentCount: 2, completedPremium: 500 },
    ],
    products: async () => [
      {
        productId: "p1",
        productCode: "HLT-1",
        productName: "Health Secure",
        policyCount: 2,
        claimCount: 1,
        premiumCollected: 500,
        settlementsPaid: 100,
      },
    ],
    settlements: async (year: number) => [
      { year, month: 10, settlementCount: 1, settlementAmount: 100 },
    ],
  },
}));

describe("Administrator reporting", () => {
  it("presents explicit claim measures and numeric settlement trend", async () => {
    renderWithProviders(
      <MemoryRouter>
        <AdminReportsPage />
      </MemoryRouter>,
    );
    expect(
      await screen.findByRole("heading", { name: "Enterprise Reporting" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Total claims")).toBeInTheDocument();
    expect(screen.getByText("Approved claims")).toBeInTheDocument();
    expect(screen.getByText("Rejected claims")).toBeInTheDocument();
    expect(screen.getByText("Pending claims")).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Claim settlement trend" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("columnheader", { name: "Settlements" }),
    ).toBeInTheDocument();
  });
});
