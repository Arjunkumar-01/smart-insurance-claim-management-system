import { useQuery } from "@tanstack/react-query";
import {
  Box,
  Button,
  Pagination,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { useNavigate, useSearchParams } from "react-router-dom";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { formatAmount } from "../../products/types/product-formatters";
import { paymentApi, paymentKeys } from "../api/payment-api";
import { DownloadReceiptButton } from "../components/DownloadReceiptButton";

export function MyPaymentsPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") ?? 0);
  const query = useQuery({
    queryKey: paymentKeys.mine(page),
    queryFn: () => paymentApi.mine(page),
  });
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load premium payments."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Premium payments"
        title="My Payments"
        description="Review your simulated premium payment records."
      />
      {!query.data.payments.length ? (
        <EmptyState title="No premium payments yet." />
      ) : (
        <>
          <DataTableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Payment number</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Method</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Paid at</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {query.data.payments.map((payment) => (
                  <TableRow key={payment.paymentId}>
                    <TableCell>{payment.paymentNumber}</TableCell>
                    <TableCell>{formatAmount(payment.amount)}</TableCell>
                    <TableCell>{payment.paymentMethod}</TableCell>
                    <TableCell>
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
                    </TableCell>
                    <TableCell>{payment.paidAt}</TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() =>
                          navigate(`/app/payments/${payment.paymentId}`)
                        }
                      >
                        View
                      </Button>
                      {payment.receipt ? (
                        <DownloadReceiptButton
                          paymentId={payment.paymentId}
                          receiptNumber={payment.receipt.receiptNumber}
                        />
                      ) : null}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </DataTableContainer>
          <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
            <Pagination
              page={query.data.page + 1}
              count={query.data.totalPages}
              onChange={(_, value) => setParams({ page: String(value - 1) })}
            />
          </Box>
        </>
      )}
    </>
  );
}
