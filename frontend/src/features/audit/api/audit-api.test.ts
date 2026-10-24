import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { getAuditLogs } from "./audit-api";

describe("audit API", () => {
  beforeEach(() => vi.restoreAllMocks());
  it("constructs the paginated read-only audit request", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: { logs: [] } } as never);
    await getAuditLogs(3);
    expect(get).toHaveBeenCalledWith("/admin/audit-logs", {
      params: { page: 3, size: 20 },
    });
  });
});
