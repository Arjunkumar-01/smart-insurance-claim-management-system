import { Navigate } from "react-router-dom";
import { LoadingState } from "../components/feedback/LoadingState";
import { useAuth } from "../auth/useAuth";
import { getLandingPath } from "../types/roles";

export function RootRoute() {
  const { isAuthenticated, isInitializing, user } = useAuth();
  if (isInitializing) return <LoadingState />;
  return (
    <Navigate
      to={isAuthenticated && user ? getLandingPath(user.roles) : "/login"}
      replace
    />
  );
}
