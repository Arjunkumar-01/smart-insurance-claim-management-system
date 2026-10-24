import { createContext } from "react";
import type { AuthSession, AuthUser, LoginRequest } from "./auth-types";

export interface AuthContextValue {
  session: AuthSession | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login(request: LoginRequest): Promise<AuthSession>;
  logout(): void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined,
);
