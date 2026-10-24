import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { paymentApi } from "./payment-api";

describe("payment API", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("submits only policy and permitted payment method", async () => {
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: {} } as never);
    await paymentApi.pay({ policyId: "policy-1", paymentMethod: "CARD" });
    expect(post).toHaveBeenCalledWith("/payments", {
      policyId: "policy-1",
      paymentMethod: "CARD",
    });
    expect(post.mock.calls[0]?.[1]).not.toHaveProperty("amount");
    expect(post.mock.calls[0]?.[1]).not.toHaveProperty("customerId");
  });

  it("constructs history, detail, invoice, and receipt requests", async () => {
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: {} } as never);
    await paymentApi.mine(2);
    await paymentApi.detail("payment-1");
    await paymentApi.invoice("payment-1");
    await paymentApi.receipt("payment-1");
    await paymentApi.downloadReceipt("payment-1");
    expect(get).toHaveBeenCalledWith("/payments/me", {
      params: { page: 2, size: 20 },
    });
    expect(get).toHaveBeenCalledWith("/payments/me/payment-1");
    expect(get).toHaveBeenCalledWith("/payments/me/payment-1/invoice");
    expect(get).toHaveBeenCalledWith("/payments/me/payment-1/receipt");
    expect(get).toHaveBeenCalledWith(
      "/payments/me/payment-1/receipt/download",
      {
        headers: { Accept: "application/pdf" },
        responseType: "blob",
      },
    );
  });
});
