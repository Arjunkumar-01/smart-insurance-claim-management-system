import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { adminApi } from "./admin-api";

describe("administrator API", () => {
  beforeEach(() => vi.restoreAllMocks());
  it("constructs paginated user list and status update requests", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: {} } as never);
    const patch = vi
      .spyOn(apiClient, "patch")
      .mockResolvedValue({ data: {} } as never);
    await adminApi.list(2, 20, "email");
    await adminApi.detail("user-1");
    await adminApi.changeStatus("user-1", { status: "INACTIVE" });
    expect(get).toHaveBeenCalledWith("/admin/users", {
      params: { page: 2, size: 20, sort: "email" },
    });
    expect(get).toHaveBeenCalledWith("/admin/users/user-1");
    expect(patch).toHaveBeenCalledWith("/admin/users/user-1/status", {
      status: "INACTIVE",
    });
  });
});
