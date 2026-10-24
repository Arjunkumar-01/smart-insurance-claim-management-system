import type { ApplicationRole } from "../types/roles";

export type AccountStatus = "ACTIVE" | "INACTIVE" | "LOCKED";

export interface AuthUser {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: ApplicationRole[];
  accountStatus: AccountStatus;
  createdAt: string;
}

export interface AuthSession {
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
  user: AuthUser;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface RegisterResponse {
  user: AuthUser;
}
