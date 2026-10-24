import { apiClient } from "../../../api/client";
import { normalizeApiError } from "../../../api/api-error";
import type {
  AdminOverview,
  CustomerReport,
  MonthlyRevenue,
  ProductPerformance,
  SettlementTrend,
} from "../types/report-types";
export const reportKeys = {
  all: ["admin-reports"] as const,
  overview: () => [...reportKeys.all, "overview"] as const,
  revenue: (year: number) => [...reportKeys.all, "revenue", year] as const,
  products: () => [...reportKeys.all, "products"] as const,
  settlements: (year: number) =>
    [...reportKeys.all, "settlements", year] as const,
  customer: () => [...reportKeys.all, "customer"] as const,
};
async function request<T>(operation: () => Promise<{ data: T }>): Promise<T> {
  try {
    return (await operation()).data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}
export const reportApi = {
  overview: () =>
    request<AdminOverview>(() => apiClient.get("/reports/admin/overview")),
  revenue: (year: number) =>
    request<MonthlyRevenue[]>(() =>
      apiClient.get("/reports/admin/revenue/monthly", { params: { year } }),
    ),
  products: () =>
    request<ProductPerformance[]>(() =>
      apiClient.get("/reports/admin/products/performance"),
    ),
  settlements: (year: number) =>
    request<SettlementTrend[]>(() =>
      apiClient.get("/reports/admin/settlements/trend", { params: { year } }),
    ),
  customer: () =>
    request<CustomerReport>(() => apiClient.get("/reports/me/summary")),
};
