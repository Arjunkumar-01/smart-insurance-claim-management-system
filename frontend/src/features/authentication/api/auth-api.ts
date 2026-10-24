import { apiClient } from "../../../api/client";
import { ApiError, normalizeApiError } from "../../../api/api-error";
import type {
  AccountStatus,
  AuthSession,
  AuthUser,
  LoginRequest,
  RegisterRequest,
  RegisterResponse,
} from "../../../auth/auth-types";
import { APPLICATION_ROLES, type ApplicationRole } from "../../../types/roles";

export async function login(request: LoginRequest): Promise<AuthSession> {
  try {
    const response = await apiClient.post<unknown>("/auth/login", request);
    return normalizeSession(response.data);
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw normalizeApiError(error);
  }
}

export async function register(
  request: RegisterRequest,
): Promise<RegisterResponse> {
  try {
    const response = await apiClient.post<unknown>("/auth/register", request);
    return {
      user: normalizeUser((response.data as { user?: unknown }).user),
    } satisfies RegisterResponse;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw normalizeApiError(error);
  }
}

function normalizeSession(value: unknown): AuthSession {
  if (typeof value !== "object" || value === null) throw invalidResponse();
  const payload = value as Record<string, unknown>;
  const user = normalizeUser(payload.user);
  if (
    typeof payload.accessToken !== "string" ||
    payload.tokenType !== "Bearer" ||
    typeof payload.expiresAt !== "string"
  )
    throw invalidResponse();
  if (
    Number.isNaN(new Date(payload.expiresAt).getTime()) ||
    new Date(payload.expiresAt).getTime() <= Date.now()
  )
    throw invalidResponse();
  return {
    accessToken: payload.accessToken,
    tokenType: "Bearer",
    expiresAt: payload.expiresAt,
    user,
  };
}

function normalizeUser(value: unknown): AuthUser {
  if (typeof value !== "object" || value === null) throw invalidResponse();
  const user = value as Record<string, unknown>;
  const roles = Array.isArray(user.roles)
    ? user.roles
        .map(normalizeRole)
        .filter((role): role is ApplicationRole => role !== null)
    : [];
  if (
    typeof user.userId !== "string" ||
    typeof user.email !== "string" ||
    typeof user.firstName !== "string" ||
    typeof user.lastName !== "string" ||
    roles.length === 0 ||
    !isAccountStatus(user.accountStatus) ||
    typeof user.createdAt !== "string"
  )
    throw invalidResponse();
  return {
    userId: user.userId,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    roles,
    accountStatus: user.accountStatus,
    createdAt: user.createdAt,
  };
}

function normalizeRole(value: unknown): ApplicationRole | null {
  if (typeof value !== "string") return null;
  const candidate = value.replace(/^ROLE_/, "") as ApplicationRole;
  return APPLICATION_ROLES.includes(candidate) ? candidate : null;
}

function isAccountStatus(value: unknown): value is AccountStatus {
  return value === "ACTIVE" || value === "INACTIVE" || value === "LOCKED";
}

function invalidResponse(): ApiError {
  return new ApiError("The authentication response was not understood.", {
    kind: "unknown",
  });
}
