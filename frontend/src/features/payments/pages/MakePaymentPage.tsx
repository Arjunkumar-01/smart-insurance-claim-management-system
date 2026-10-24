import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Box, Paper, Typography } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { policyApi, policyKeys } from "../../policies/api/policy-api";
import { formatAmount } from "../../products/types/product-formatters";
import { PaymentMethodForm } from "../components/PaymentMethodForm";
import { paymentApi, paymentKeys } from "../api/payment-api";
import type { PaymentMethod } from "../types/payment-types";

export function MakePaymentPage() {
  const { policyId = "" } = useParams();
  const navigate = useNavigate();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const policy = useQuery({
    queryKey: policyKeys.detail(policyId),
    queryFn: () => policyApi.detail(policyId),
    enabled: Boolean(policyId),
  });
  const mutation = useMutation({
    mutationFn: (paymentMethod: PaymentMethod) =>
      paymentApi.pay({ policyId, paymentMethod }),
    onSuccess: async (payment) => {
      await client.invalidateQueries({ queryKey: paymentKeys.all });
      notify("Premium payment completed.", "success");
      navigate(`/app/payments/${payment.paymentId}`);
    },
  });
  if (policy.isPending) return <SkeletonState rows={5} />;
  if (policy.error || !policy.data)
    return (
      <ErrorState
        title="Policy unavailable"
        message="The policy could not be loaded for payment."
        onRetry={() => void policy.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Premium payment"
        title="Premium Payment"
        description={`Confirm the premium for ${policy.data.policyNumber}.`}
      />
      <Paper
        elevation={0}
        sx={{
          p: { xs: 2.5, sm: 4 },
          border: 1,
          borderColor: "divider",
          maxWidth: 720,
        }}
      >
        <Typography variant="h6">{policy.data.productName}</Typography>
        <Typography sx={{ mt: 1 }}>
          Policy {policy.data.policyNumber}
        </Typography>
        <Typography variant="h5" sx={{ mt: 2 }}>
          {formatAmount(policy.data.premiumAmount)}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          This project uses a simulated premium-payment workflow. No real card
          or banking information is processed.
        </Typography>
        {mutation.error ? (
          <Alert severity="error" sx={{ mt: 3 }}>
            {mutation.error instanceof Error
              ? mutation.error.message
              : "Premium payment could not be completed."}
          </Alert>
        ) : null}
        <Box sx={{ mt: 3 }}>
          <PaymentMethodForm
            pending={mutation.isPending}
            onSubmit={(value) => mutation.mutate(value.paymentMethod)}
          />
        </Box>
      </Paper>
    </>
  );
}
