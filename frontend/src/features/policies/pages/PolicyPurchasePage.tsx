import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Button,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { productApi, productKeys } from "../../products/api/product-api";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { policyApi, policyKeys } from "../api/policy-api";
import { purchaseSchema } from "../schemas/policy-schemas";
import type { PurchasePolicyRequest } from "../types/policy-types";

export function PolicyPurchasePage() {
  const { productId = "" } = useParams();
  const navigate = useNavigate();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const product = useQuery({
    queryKey: productKeys.detail(productId),
    queryFn: () => productApi.get(productId),
    enabled: Boolean(productId),
  });
  const mutation = useMutation({
    mutationFn: (payload: PurchasePolicyRequest) => policyApi.purchase(payload),
    onSuccess: async (policy) => {
      await client.invalidateQueries({ queryKey: policyKeys.all });
      notify("Policy purchased successfully.", "success");
      navigate(`/app/policies/${policy.policyId}`);
    },
  });
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PurchasePolicyRequest>({
    resolver: zodResolver(purchaseSchema),
    defaultValues: { productId },
  });
  if (product.isPending) return <SkeletonState rows={5} />;
  if (product.error || !product.data || product.data.status !== "ACTIVE")
    return (
      <ErrorState
        title="Product unavailable"
        message="Only active products can be purchased."
        onRetry={() => void product.refetch()}
      />
    );
  const item = product.data;
  return (
    <>
      <PageHeader
        eyebrow="Policy purchase"
        title="Purchase Policy"
        description={`Confirm coverage for ${item.name}.`}
      />
      <Paper
        elevation={0}
        sx={{ p: { xs: 2.5, sm: 4 }, border: 1, borderColor: "divider" }}
      >
        <Typography variant="h6">{item.name}</Typography>
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          Premium {item.premiumAmount.toFixed(2)} · Coverage{" "}
          {item.coverageAmount.toFixed(2)} · {item.tenureMonths} months
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          The server generates the end date from the selected start date and
          Product tenure.
        </Typography>
        <form
          onSubmit={handleSubmit((values) =>
            mutation.mutate({ ...values, productId }),
          )}
        >
          <TextField
            label="Start date"
            type="date"
            InputLabelProps={{ shrink: true }}
            {...register("startDate")}
            error={Boolean(errors.startDate)}
            helperText={errors.startDate?.message as string}
            sx={{ mt: 3, mr: 2 }}
          />
          <Box sx={{ mt: 3 }}>
            {mutation.error ? (
              <Alert severity="error">Unable to purchase this policy.</Alert>
            ) : null}
            <Button
              type="submit"
              variant="contained"
              disabled={mutation.isPending}
              sx={{ mt: 2 }}
            >
              {mutation.isPending ? "Purchasing..." : "Purchase policy"}
            </Button>
          </Box>
        </form>
      </Paper>
    </>
  );
}
