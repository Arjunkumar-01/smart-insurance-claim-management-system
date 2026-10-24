import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "../../../api/api-error";
import { apiClient } from "../../../api/client";
import { login } from "./auth-api";

const validResponse = {
  accessToken: "synthetic-token",
  tokenType: "Bearer",
  expiresAt: new Date(Date.now() + 60_000).toISOString(),
  user: {
    userId: "user-1",
    email: "customer@example.test",
    firstName: "Test",
    lastName: "Customer",
    roles: ["CUSTOMER"],
    accountStatus: "ACTIVE",
    createdAt: new Date().toISOString(),
  },
};

describe("auth API contract", () => {
  beforeEach(() => vi.restoreAllMocks());

  it("normalizes a valid login response", async () => {
    vi.spyOn(apiClient, "post").mockResolvedValue({
      data: validResponse,
    } as never);
    const session = await login({
      email: "customer@example.test",
      password: "Password!1",
    });
    expect(session.user.roles).toEqual(["CUSTOMER"]);
  });

  it("rejects an unknown application role", async () => {
    vi.spyOn(apiClient, "post").mockResolvedValue({
      data: {
        ...validResponse,
        user: { ...validResponse.user, roles: ["SUPER_ADMIN"] },
      },
    } as never);
    await expect(
      login({ email: "customer@example.test", password: "Password!1" }),
    ).rejects.toMatchObject({
      message: "The authentication response was not understood.",
    } satisfies Partial<ApiError>);
  });
});
