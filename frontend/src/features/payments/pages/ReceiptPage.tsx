import { useQuery } from "@tanstack/react-query";
import { Paper, Typography } from "@mui/material";
import { Link, useParams } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { paymentApi, paymentKeys } from "../api/payment-api";
import { DownloadReceiptButton } from "../components/DownloadReceiptButton";

export function ReceiptPage() {
  const { paymentId = "" } = useParams();
  const query = useQuery({
    queryKey: paymentKeys.receipt(paymentId),
    queryFn: () => paymentApi.receipt(paymentId),
    enabled: Boolean(paymentId),
  });
  if (query.isPending) return <SkeletonState rows={4} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Receipt unavailable"
        message="This receipt was not found or is unavailable."
        onRetry={() => void query.refetch()}
      />
    );
  const receipt = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Payment record"
        title={receipt.receiptNumber}
        description="Premium payment receipt"
        actions={<Link to={`/app/payments/${paymentId}`}>Back to payment</Link>}
      />
      <Paper
        elevation={0}
        sx={{ p: 4, border: 1, borderColor: "divider", maxWidth: 640 }}
      >
        <Typography>Payment reference: {receipt.paymentId}</Typography>
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Issued: {receipt.issuedAt}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ my: 2 }}>
          This receipt records a simulated SICMS premium payment.
        </Typography>
        <DownloadReceiptButton
          paymentId={paymentId}
          receiptNumber={receipt.receiptNumber}
        />
      </Paper>
    </>
  );
}
