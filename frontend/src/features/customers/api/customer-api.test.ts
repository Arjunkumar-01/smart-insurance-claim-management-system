import axios from "axios";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiClient } from "../../../api/client";
import { ApiError } from "../../../api/api-error";
import { customerApi } from "./customer-api";

describe("customer API", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("uses self-service profile and KYC endpoints without ownership IDs", async () => {
    const post = vi
      .spyOn(apiClient, "post")
      .mockResolvedValue({ data: { customerId: "c1" } } as never);
    const put = vi
      .spyOn(apiClient, "put")
      .mockResolvedValue({ data: { kycId: "k1" } } as never);
    await customerApi.createProfile({
      phoneNumber: "+1234567",
      dateOfBirth: "1990-01-01",
      addressLine1: "1 Main",
      addressLine2: "",
      city: "City",
      state: "State",
      postalCode: "12345",
      country: "Country",
    });
    await customerApi.updateKyc({
      identityType: "National ID",
      identityNumber: "sensitive-value",
    });
    expect(post).toHaveBeenCalledWith(
      "/customers/me/profile",
      expect.not.objectContaining({
        customerId: expect.anything(),
        userId: expect.anything(),
      }),
    );
    expect(put).toHaveBeenCalledWith("/customers/me/kyc", {
      identityType: "National ID",
      identityNumber: "sensitive-value",
    });
  });

  it("normalizes expected profile and KYC absence", async () => {
    const error = new axios.AxiosError("Not found");
    error.response = {
      status: 404,
      statusText: "Not Found",
      headers: {},
      config: error.config ?? {},
      data: { message: "Customer profile was not found" },
    };
    vi.spyOn(apiClient, "get").mockRejectedValue(error);
    await expect(customerApi.getProfile()).rejects.toMatchObject<ApiError>({
      kind: "not-found",
    });
    await expect(customerApi.getKyc()).rejects.toMatchObject<ApiError>({
      kind: "not-found",
    });
  });
});
