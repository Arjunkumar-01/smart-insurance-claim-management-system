import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { claimApi } from "./claim-api";

describe("claim API", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("constructs claim submit, list, detail, and history requests", async () => {
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: {} } as never);
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: {} } as never);
    await claimApi.submit({
      policyId: "policy-1",
      claimAmount: 100,
      incidentDate: "2026-01-01",
      description: "Incident",
    });
    await claimApi.mine(1);
    await claimApi.detail("claim-1");
    await claimApi.history("claim-1");
    expect(post).toHaveBeenCalledWith(
      "/claims",
      expect.not.objectContaining({
        customerId: expect.anything(),
        status: expect.anything(),
      }),
    );
    expect(get).toHaveBeenCalledWith("/claims/me", {
      params: { page: 1, size: 20 },
    });
    expect(get).toHaveBeenCalledWith("/claims/me/claim-1");
    expect(get).toHaveBeenCalledWith("/claims/me/claim-1/history");
  });

  it("uses the file multipart part and authenticated blob download", async () => {
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: {} } as never);
    const get = vi
      .spyOn(apiClient, "get")
      .mockResolvedValue({ data: new Blob(["test"]) } as never);
    const file = new File(["test"], "evidence.pdf", {
      type: "application/pdf",
    });
    await claimApi.upload("claim-1", file);
    await claimApi.download("claim-1", "document-1");
    const form = post.mock.calls[0]?.[1] as FormData;
    expect(form.get("file")).toBeInstanceOf(File);
    expect(get).toHaveBeenCalledWith(
      "/claims/me/claim-1/documents/document-1",
      { responseType: "blob" },
    );
  });
});
