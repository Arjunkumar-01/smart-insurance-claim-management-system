import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Box, Button, Paper, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { Link } from "react-router-dom";
import { useAuth } from "../../../auth/useAuth";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { policyApi, policyKeys } from "../api/policy-api";
import {
  CancellationForm,
  NomineeForm,
  RenewalForm,
} from "../components/PolicyForms";
import { formatAmount } from "../../products/types/product-formatters";

export function PolicyDetailPage() {
  const { policyId = "" } = useParams();
  const navigate = useNavigate();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const { user } = useAuth();
  const query = useQuery({
    queryKey: policyKeys.detail(policyId),
    queryFn: () => policyApi.detail(policyId),
    enabled: Boolean(policyId),
  });
  const invalidate = async (message: string) => {
    await client.invalidateQueries({ queryKey: policyKeys.detail(policyId) });
    await client.invalidateQueries({ queryKey: policyKeys.mine(0) });
    notify(message, "success");
  };
  const nominee = useMutation({
    mutationFn: (payload: {
      name: string;
      relationship: string;
      contactNumber: string;
    }) => policyApi.nominee(policyId, payload),
    onSuccess: () => invalidate("Nominee updated."),
  });
  const renewal = useMutation({
    mutationFn: (payload: Record<string, never>) =>
      policyApi.renew(policyId, payload),
    onSuccess: () => invalidate("Policy renewed."),
  });
  const cancellation = useMutation({
    mutationFn: (payload: { reason: string }) =>
      policyApi.requestCancellation(policyId, payload),
    onSuccess: () => invalidate("Cancellation request submitted."),
  });
  if (query.isPending) return <SkeletonState rows={7} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Policy unavailable"
        message="This policy was not found or is unavailable."
        onRetry={() => void query.refetch()}
      />
    );
  const policy = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Policy detail"
        title={policy.policyNumber}
        description={policy.productName}
        actions={
          <>
            <Button onClick={() => navigate("/app/policies")}>
              Back to policies
            </Button>
            {user?.roles.includes("CUSTOMER") &&
            query.data?.status === "ACTIVE" ? (
              <Button
                component={Link}
                to={`/app/policies/${policyId}/payment`}
                variant="contained"
              >
                Pay premium
              </Button>
            ) : null}
          </>
        }
      />
      <Box sx={{ display: "grid", gap: 3 }}>
        <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
          <Box sx={{ display: "flex", gap: 1, alignItems: "center" }}>
            <Typography variant="h6">Policy overview</Typography>
            <StatusBadge
              label={policy.status}
              tone={
                policy.status === "ACTIVE"
                  ? "success"
                  : policy.status === "CANCELLED"
                    ? "error"
                    : "neutral"
              }
            />
          </Box>
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
              gap: 2,
              mt: 2,
            }}
          >
            <Typography>Product: {policy.productName}</Typography>
            <Typography>
              Premium snapshot: {formatAmount(policy.premiumAmount)}
            </Typography>
            <Typography>
              Coverage snapshot: {formatAmount(policy.coverageAmount)}
            </Typography>
            <Typography>
              Period: {policy.startDate} to {policy.endDate}
            </Typography>
            <Typography>
              Product tenure: {policy.productTenureMonths} months
            </Typography>
          </Box>
        </Paper>
        <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
          <Typography variant="h6">Nominee</Typography>
          {policy.nominee ? (
            <Typography sx={{ mt: 1 }}>
              {policy.nominee.name} · {policy.nominee.relationship} ·{" "}
              {policy.nominee.contactNumber}
            </Typography>
          ) : (
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              No nominee recorded.
            </Typography>
          )}
          <NomineeForm
            pending={nominee.isPending}
            onSubmit={(value) => nominee.mutate(value)}
          />
        </Paper>
        {policy.status === "ACTIVE" ? (
          <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
            <Typography variant="h6">Policy actions</Typography>
            <RenewalForm
              pending={renewal.isPending}
              onSubmit={(value) => renewal.mutate(value)}
              currentEndDate={policy.endDate}
              newEndDate={addMonths(policy.endDate, policy.productTenureMonths)}
              renewalPremium={formatAmount(policy.premiumAmount)}
            />
            <CancellationForm
              pending={cancellation.isPending}
              onSubmit={(value) => cancellation.mutate(value)}
            />
            {[nominee, renewal, cancellation].some((item) => item.error) ? (
              <Alert severity="error" sx={{ mt: 2 }}>
                The requested policy action could not be completed.
              </Alert>
            ) : null}
          </Paper>
        ) : null}
      </Box>
    </>
  );
}

function addMonths(date: string, months: number) {
  const value = new Date(`${date}T00:00:00Z`);
  value.setUTCMonth(value.getUTCMonth() + months);
  return value.toISOString().slice(0, 10);
}
