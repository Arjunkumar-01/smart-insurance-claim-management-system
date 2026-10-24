import { useQuery } from "@tanstack/react-query";
import { Box, Button, Paper, Typography } from "@mui/material";
import { Link, useParams } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { formatAmount } from "../../products/types/product-formatters";
import { paymentApi, paymentKeys } from "../api/payment-api";
import { DownloadReceiptButton } from "../components/DownloadReceiptButton";

export function PaymentDetailPage() {
  const { paymentId = "" } = useParams();
  const query = useQuery({
    queryKey: paymentKeys.detail(paymentId),
    queryFn: () => paymentApi.detail(paymentId),
    enabled: Boolean(paymentId),
  });
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Payment unavailable"
        message="This payment was not found or is unavailable."
        onRetry={() => void query.refetch()}
      />
    );
  const payment = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Premium payment"
        title={payment.paymentNumber}
        description="Simulated premium transaction record."
        actions={
          <Button component={Link} to="/app/payments">
            Back to payments
          </Button>
        }
      />
      <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
        <Box sx={{ display: "grid", gap: 2 }}>
          <Typography>Amount: {formatAmount(payment.amount)}</Typography>
          <Typography>Policy reference: {payment.policyId}</Typography>
          <Typography>Method: {payment.paymentMethod}</Typography>
          <Typography>
            Transaction reference: {payment.transactionReference}
          </Typography>
          <StatusBadge
            label={payment.status}
            tone={
              payment.status === "COMPLETED"
                ? "success"
                : payment.status === "FAILED"
                  ? "error"
                  : "warning"
            }
          />
          <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
            {payment.invoice ? (
              <Button
                component={Link}
                to={`/app/payments/${payment.paymentId}/invoice`}
              >
                View invoice
              </Button>
            ) : null}
            {payment.receipt ? (
              <>
                <Button
                  component={Link}
                  to={`/app/payments/${payment.paymentId}/receipt`}
                >
                  View receipt
                </Button>
                <DownloadReceiptButton
                  paymentId={payment.paymentId}
                  receiptNumber={payment.receipt.receiptNumber}
                />
              </>
            ) : null}
          </Box>
        </Box>
      </Paper>
    </>
  );
}
