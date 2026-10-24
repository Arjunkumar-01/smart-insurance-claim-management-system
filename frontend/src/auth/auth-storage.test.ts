import { beforeEach, describe, expect, it } from "vitest";
import { authStorage } from "./auth-storage";
import type { AuthSession } from "./auth-types";

const session: AuthSession = {
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

describe("auth storage", () => {
  beforeEach(() => authStorage.clear());

  it("stores and restores a valid session", () => {
    authStorage.set(session);
    expect(authStorage.get()?.user.email).toBe("customer@example.test");
  });

  it("clears malformed and expired sessions", () => {
    sessionStorage.setItem("sicms.auth.session", "not-json");
    expect(authStorage.get()).toBeNull();
    authStorage.set({
      ...session,
      expiresAt: new Date(Date.now() - 1_000).toISOString(),
    });
    expect(authStorage.get()).toBeNull();
  });
});
