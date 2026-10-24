import axios, { type InternalAxiosRequestConfig } from "axios";
import { describe, expect, it, vi } from "vitest";
import { apiClient } from "./client";
import { authStorage } from "../auth/auth-storage";

describe("api client", () => {
  it("uses the centralized API base URL and timeout", () => {
    expect(apiClient.defaults.baseURL).toBe("http://localhost:8080/api");
    expect(apiClient.defaults.timeout).toBe(10_000);
  });

  it("does not force a JSON content type, preserving multipart support", () => {
    expect(apiClient.defaults.headers.common["Content-Type"]).toBeUndefined();
  });

  it("adds a bearer token centrally when a valid session exists", async () => {
    authStorage.set({
      accessToken: "synthetic-token",
      tokenType: "Bearer",
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      user: {
        userId: "user-1",
        email: "user@example.test",
        firstName: "Test",
        lastName: "User",
        roles: ["CUSTOMER"],
        accountStatus: "ACTIVE",
        createdAt: new Date().toISOString(),
      },
    });
    const requestHandler =
      apiClient.interceptors.request.handlers[0]?.fulfilled;
    const config = await requestHandler?.({
      headers: {},
    } as InternalAxiosRequestConfig);
    expect(config?.headers.Authorization).toBe("Bearer synthetic-token");
    authStorage.clear();
  });

  it("does not clear a session for login 401 responses", async () => {
    const session = {
      accessToken: "synthetic-token",
      tokenType: "Bearer" as const,
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      user: {
        userId: "user-1",
        email: "user@example.test",
        firstName: "Test",
        lastName: "User",
        roles: ["CUSTOMER"] as const,
        accountStatus: "ACTIVE" as const,
        createdAt: new Date().toISOString(),
      },
    };
    authStorage.set(session);
    const rejectedHandler =
      apiClient.interceptors.response.handlers[0]?.rejected;
    const error = new axios.AxiosError("Unauthorized", "401", {
      url: "/auth/login",
    } as InternalAxiosRequestConfig);
    error.response = {
      status: 401,
      statusText: "Unauthorized",
      headers: {},
      config: error.config ?? ({} as InternalAxiosRequestConfig),
      data: {},
    };
    await expect(rejectedHandler?.(error)).rejects.toBeDefined();
    expect(authStorage.get()?.accessToken).toBe("synthetic-token");
    authStorage.clear();
  });

  it("clears a session for protected-request 401 responses without page reload", async () => {
    authStorage.set({
      accessToken: "synthetic-token",
      tokenType: "Bearer",
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      user: {
        userId: "user-1",
        email: "user@example.test",
        firstName: "Test",
        lastName: "User",
        roles: ["CUSTOMER"],
        accountStatus: "ACTIVE",
        createdAt: new Date().toISOString(),
      },
    });
    const dispatchSpy = vi.spyOn(window, "dispatchEvent");
    const rejectedHandler =
      apiClient.interceptors.response.handlers[0]?.rejected;
    const error = new axios.AxiosError("Unauthorized", "401", {
      url: "/customers/me/profile",
    } as InternalAxiosRequestConfig);
    error.response = {
      status: 401,
      statusText: "Unauthorized",
      headers: {},
      config: error.config ?? ({} as InternalAxiosRequestConfig),
      data: {},
    };
    await expect(rejectedHandler?.(error)).rejects.toBeDefined();
    expect(authStorage.get()).toBeNull();
    expect(dispatchSpy).toHaveBeenCalledWith(
      expect.objectContaining({ type: "sicms:auth-expired" }),
    );
    dispatchSpy.mockRestore();
  });
});
