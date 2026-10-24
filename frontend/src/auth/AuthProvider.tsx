import { useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState, type PropsWithChildren } from "react";
import { authStorage } from "./auth-storage";
import type { AuthSession } from "./auth-types";
import { login as loginRequest } from "../features/authentication/api/auth-api";
import { AuthContext, type AuthContextValue } from "./auth-context";

export function AuthProvider({ children }: PropsWithChildren) {
  const queryClient = useQueryClient();
  const [session, setSession] = useState<AuthSession | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    setSession(authStorage.get());
    setIsInitializing(false);
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => {
      authStorage.clear();
      setSession(null);
      void queryClient.clear();
    };
    window.addEventListener("sicms:auth-expired", handleUnauthorized);
    return () =>
      window.removeEventListener("sicms:auth-expired", handleUnauthorized);
  }, [queryClient]);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      user: session?.user ?? null,
      isAuthenticated: session !== null,
      isInitializing,
      async login(request) {
        const nextSession = await loginRequest(request);
        await queryClient.clear();
        authStorage.set(nextSession);
        setSession(nextSession);
        return nextSession;
      },
      logout() {
        authStorage.clear();
        setSession(null);
        void queryClient.clear();
      },
    }),
    [isInitializing, queryClient, session],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
