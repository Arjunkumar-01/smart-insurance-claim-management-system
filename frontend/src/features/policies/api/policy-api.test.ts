import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { policyApi } from "./policy-api";

describe("policy API", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("constructs customer purchase and policy management requests", async () => {
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: {} } as never);
    const put = vi
      .spyOn(apiClient, "put")
      .mockResolvedValue({ data: {} } as never);
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: { policies: [] } } as never);
    await policyApi.purchase({
      productId: "product-1",
      startDate: "2026-01-01",
    });
    await policyApi.mine(1);
    await policyApi.detail("policy-1");
    await policyApi.renew("policy-1", {});
    await policyApi.nominee("policy-1", {
      name: "Nominee",
      relationship: "Parent",
      contactNumber: "+15551234567",
    });
    await policyApi.requestCancellation("policy-1", {
      reason: "No longer required",
    });
    expect(post).toHaveBeenCalledWith("/policies", {
      productId: "product-1",
      startDate: "2026-01-01",
    });
    expect(get).toHaveBeenCalledWith("/policies/me", {
      params: { page: 1, size: 20 },
    });
    expect(get).toHaveBeenCalledWith("/policies/me/policy-1");
    expect(put).toHaveBeenCalledWith("/policies/me/policy-1/renewal", {});
    expect(put).toHaveBeenCalledWith(
      "/policies/me/policy-1/nominee",
      expect.any(Object),
    );
    expect(post).toHaveBeenCalledWith("/policies/me/policy-1/cancellation", {
      reason: "No longer required",
    });
  });

  it("constructs administrator oversight requests", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: { policies: [] } } as never);
    const put = vi
      .spyOn(apiClient, "put")
      .mockResolvedValue({ data: {} } as never);
    await policyApi.admin(0);
    await policyApi.cancellations();
    await policyApi.decideCancellation("request-1", {
      status: "REJECTED",
      decisionComment: "Reviewed",
    });
    expect(get).toHaveBeenCalledWith("/policies/admin", {
      params: { page: 0, size: 20 },
    });
    expect(get).toHaveBeenCalledWith("/policies/cancellations");
    expect(put).toHaveBeenCalledWith(
      "/policies/cancellations/request-1/decision",
      { status: "REJECTED", decisionComment: "Reviewed" },
    );
  });
});
