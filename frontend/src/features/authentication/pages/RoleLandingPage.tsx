import { Box, Button } from "@mui/material";
import { Link } from "react-router-dom";
import type { ApplicationRole } from "../../../types/roles";
import { useAuth } from "../../../auth/useAuth";
import { PageHeader } from "../../../components/layout/PageHeader";

const roleCopy: Record<
  ApplicationRole,
  { title: string; description: string }
> = {
  CUSTOMER: {
    title: "Customer workspace",
    description: "Manage insurance services securely from one workspace.",
  },
  AGENT: {
    title: "Agent workspace",
    description:
      "Access the agent workspace for controlled insurance operations.",
  },
  CLAIMS_OFFICER: {
    title: "Claims operations workspace",
    description:
      "Review and process submitted claims through controlled workflows.",
  },
  ADMINISTRATOR: {
    title: "Administration workspace",
    description:
      "Manage the administration workspace through controlled access.",
  },
};

export function RoleLandingPage({ role }: { role: ApplicationRole }) {
  const { user } = useAuth();
  const copy = roleCopy[role];
  return (
    <Box component="section">
      <PageHeader
        eyebrow={role.replaceAll("_", " ")}
        title={copy.title}
        description={`Welcome, ${user?.firstName}. ${copy.description}`}
        actions={
          role === "CUSTOMER" ? (
            <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
              <Button
                component={Link}
                to="/app/customer/profile"
                variant="contained"
              >
                View profile
              </Button>
              <Button
                component={Link}
                to="/app/customer/kyc"
                variant="outlined"
              >
                KYC
              </Button>
            </Box>
          ) : undefined
        }
      />
    </Box>
  );
}
