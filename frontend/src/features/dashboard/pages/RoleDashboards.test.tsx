import { screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import {
  CustomerDashboardPage,
  AgentDashboardPage,
  AdministratorDashboardPage,
} from "./RoleDashboards";
import { renderWithProviders } from "../../../test/renderWithProviders";

vi.mock("../../reports/api/report-api", () => ({
  reportKeys: {
    customer: () => ["customer-report"],
    overview: () => ["overview"],
  },
  reportApi: {
    customer: () => ({
      policyCount: 2,
      activePolicyCount: 1,
      expiredPolicyCount: 1,
      claimCount: 1,
      claimsByStatus: [{ status: "SUBMITTED", count: 1 }],
      premiumPaid: 100,
      settlementTotal: 0,
    }),
    overview: () => ({
      customerCount: 3,
      premiumCollected: 200,
      settlementsPaid: 50,
      claimsRatioPercent: 25,
      totalClaims: 4,
      pendingClaims: 2,
    }),
  },
}));
vi.mock("../../products/api/product-api", () => ({
  productKeys: { list: () => ["products"] },
  productApi: { list: () => ({ products: [] }) },
}));
vi.mock("../../claims/api/claim-review-api", () => ({
  reviewKeys: { queue: () => ["review"] },
  claimReviewApi: { queue: () => ({ totalElements: 1 }) },
}));

vi.mock("@tanstack/react-query", async () => {
  const actual = await vi.importActual<typeof import("@tanstack/react-query")>(
    "@tanstack/react-query",
  );
  return {
    ...actual,
    useQuery: ({ queryFn }: { queryFn: () => unknown }) => ({
      isPending: false,
      data: queryFn(),
      error: null,
    }),
  };
});

describe("role dashboards", () => {
  it("renders Customer report-backed metrics and actions", () => {
    renderWithProviders(
      <MemoryRouter>
        <CustomerDashboardPage />
      </MemoryRouter>,
    );
    expect(
      screen.getByRole("heading", { name: "Your insurance overview" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "View policies" })).toHaveAttribute(
      "href",
      "/app/policies",
    );
    expect(screen.getByText("Active policies")).toBeInTheDocument();
  });

  it("keeps Agent scope to Product discovery", () => {
    renderWithProviders(
      <MemoryRouter>
        <AgentDashboardPage />
      </MemoryRouter>,
    );
    expect(
      screen.getByRole("heading", { name: "Product discovery workspace" }),
    ).toBeInTheDocument();
    expect(
      screen.getByText("No policy or claim servicing access"),
    ).toBeInTheDocument();
  });

  it("renders Administrator oversight shortcuts", () => {
    renderWithProviders(
      <MemoryRouter>
        <AdministratorDashboardPage />
      </MemoryRouter>,
    );
    expect(
      screen.getByRole("heading", { name: "Operational oversight" }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Reporting" })).toHaveAttribute(
      "href",
      "/app/admin/reports",
    );
  });
});
