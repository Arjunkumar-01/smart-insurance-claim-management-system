import { useQuery } from "@tanstack/react-query";
import { Paper, Typography } from "@mui/material";
import { Link, useParams } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { formatAmount } from "../../products/types/product-formatters";
import { paymentApi, paymentKeys } from "../api/payment-api";

export function InvoicePage() {
  const { paymentId = "" } = useParams();
  const query = useQuery({
    queryKey: paymentKeys.invoice(paymentId),
    queryFn: () => paymentApi.invoice(paymentId),
    enabled: Boolean(paymentId),
  });
  if (query.isPending) return <SkeletonState rows={4} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Invoice unavailable"
        message="This invoice was not found or is unavailable."
        onRetry={() => void query.refetch()}
      />
    );
  const invoice = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Payment record"
        title={invoice.invoiceNumber}
        description="Premium invoice"
        actions={<Link to={`/app/payments/${paymentId}`}>Back to payment</Link>}
      />
      <Paper
        elevation={0}
        sx={{ p: 4, border: 1, borderColor: "divider", maxWidth: 640 }}
      >
        <Typography>Policy reference: {invoice.policyId}</Typography>
        <Typography variant="h5" sx={{ mt: 2 }}>
          Amount: {formatAmount(invoice.amount)}
        </Typography>
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Issued: {invoice.issuedAt}
        </Typography>
      </Paper>
    </>
  );
}
