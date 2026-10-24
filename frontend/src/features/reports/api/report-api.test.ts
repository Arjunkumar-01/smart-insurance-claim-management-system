import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { reportApi } from "./report-api";

describe("report API", () => {
  beforeEach(() => vi.restoreAllMocks());
  it("constructs exact Administrator report requests", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: {} } as never);
    await reportApi.overview();
    await reportApi.revenue(2026);
    await reportApi.products();
    await reportApi.settlements(2026);
    expect(get).toHaveBeenCalledWith("/reports/admin/overview");
    expect(get).toHaveBeenCalledWith("/reports/admin/revenue/monthly", {
      params: { year: 2026 },
    });
    expect(get).toHaveBeenCalledWith("/reports/admin/products/performance");
    expect(get).toHaveBeenCalledWith("/reports/admin/settlements/trend", {
      params: { year: 2026 },
    });
  });
});
