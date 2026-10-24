import { Navigate, Outlet, useLocation } from "react-router-dom";
import { LoadingState } from "../components/feedback/LoadingState";
import { useAuth } from "./useAuth";

export function ProtectedRoute() {
  const { isAuthenticated, isInitializing } = useAuth();
  const location = useLocation();
  if (isInitializing) return <LoadingState />;
  if (!isAuthenticated)
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <Outlet />;
}
