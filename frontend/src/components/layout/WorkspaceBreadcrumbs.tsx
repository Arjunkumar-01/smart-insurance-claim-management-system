import { Breadcrumbs, Link, Typography } from "@mui/material";
import { Link as RouterLink, useLocation } from "react-router-dom";
import { roleLabels } from "../../types/navigation";
import type { ApplicationRole } from "../../types/roles";

const workspaceNames: Record<
  string,
  { label: string; role?: ApplicationRole }
> = {
  "/app/customer": { label: "Customer workspace", role: "CUSTOMER" },
  "/app/agent": { label: "Agent workspace", role: "AGENT" },
  "/app/claims-officer": { label: "Claims operations", role: "CLAIMS_OFFICER" },
  "/app/administrator": { label: "Administration", role: "ADMINISTRATOR" },
  "/app/customer/profile": { label: "Profile", role: "CUSTOMER" },
  "/app/customer/kyc": { label: "KYC", role: "CUSTOMER" },
};

export function WorkspaceBreadcrumbs() {
  const { pathname } = useLocation();
  const current = workspaceNames[pathname];
  if (!current) return null;
  return (
    <Breadcrumbs aria-label="Breadcrumb" sx={{ mb: 3 }}>
      <Link component={RouterLink} to="/app" underline="hover" color="inherit">
        Home
      </Link>
      <Typography color="text.primary">
        {current.role ? roleLabels[current.role] : current.label}
      </Typography>
    </Breadcrumbs>
  );
}
