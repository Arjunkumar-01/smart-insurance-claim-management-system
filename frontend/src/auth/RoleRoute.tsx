import { Navigate, Outlet } from "react-router-dom";
import type { ApplicationRole } from "../types/roles";
import { ForbiddenPage } from "../routes/ForbiddenPage";
import { useAuth } from "./useAuth";

export function RoleRoute({
  allowedRoles,
}: {
  allowedRoles: ApplicationRole[];
}) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!allowedRoles.some((role) => user.roles.includes(role)))
    return <ForbiddenPage />;
  return <Outlet />;
}
