import { useMutation } from "@tanstack/react-query";
import { Button, Typography } from "@mui/material";
import { paymentApi } from "../api/payment-api";

export function DownloadReceiptButton({
  paymentId,
  receiptNumber,
}: {
  paymentId: string;
  receiptNumber: string;
}) {
  const download = useMutation({
    mutationFn: () => paymentApi.downloadReceipt(paymentId),
    onSuccess: (blob) => {
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = `sicms-receipt-${receiptNumber}.pdf`;
      anchor.click();
      URL.revokeObjectURL(url);
    },
  });
  return (
    <>
      <Button
        variant="contained"
        disabled={download.isPending}
        onClick={() => download.mutate()}
      >
        {download.isPending ? "Downloading..." : "Download receipt"}
      </Button>
      {download.error ? (
        <Typography role="alert" variant="body2" color="error" sx={{ mt: 1 }}>
          Receipt download failed. Try again.
        </Typography>
      ) : null}
    </>
  );
}
