import { useQuery } from "@tanstack/react-query";
import {
  Button,
  Pagination,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
} from "@mui/material";
import { useNavigate, useSearchParams } from "react-router-dom";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { claimReviewApi, reviewKeys } from "../api/claim-review-api";
import { formatAmount } from "../../products/types/product-formatters";
export function ClaimReviewQueuePage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") ?? 0);
  const status = params.get("status") || undefined;
  const query = useQuery({
    queryKey: reviewKeys.queue(status, page),
    queryFn: () => claimReviewApi.queue(status, page),
  });
  if (query.isPending) return <SkeletonState rows={6} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load the claims review queue."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Claims operations"
        title="Claims Review Queue"
        description="Review submitted claims through controlled workflow actions."
      />
      <TextField
        select
        label="Queue status"
        value={status ?? ""}
        onChange={(event) => {
          const next = new URLSearchParams(params);
          if (event.target.value) next.set("status", event.target.value);
          else next.delete("status");
          next.set("page", "0");
          setParams(next);
        }}
        SelectProps={{ native: true }}
        sx={{ minWidth: 240, mb: 2 }}
      >
        <option value="">All open claims</option>
        <option value="SUBMITTED">Submitted</option>
        <option value="UNDER_REVIEW">Under Review</option>
        <option value="DOCUMENTS_REQUIRED">Documents Required</option>
        <option value="APPROVED">Approved</option>
      </TextField>
      {!query.data.claims.length ? (
        <EmptyState title="No claims require review." />
      ) : (
        <>
          <DataTableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Claim number</TableCell>
                  <TableCell>Policy</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Incident date</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {query.data.claims.map((claim) => (
                  <TableRow hover key={claim.claimId}>
                    <TableCell>{claim.claimNumber}</TableCell>
                    <TableCell>{claim.policyNumber}</TableCell>
                    <TableCell>{claim.claimType}</TableCell>
                    <TableCell>{formatAmount(claim.claimAmount)}</TableCell>
                    <TableCell>{claim.incidentDate}</TableCell>
                    <TableCell>
                      <StatusBadge
                        label={claim.status}
                        tone={
                          claim.status === "DOCUMENTS_REQUIRED"
                            ? "warning"
                            : "info"
                        }
                      />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() =>
                          navigate(`/app/claims-review/${claim.claimId}`)
                        }
                      >
                        Review
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </DataTableContainer>
          <Pagination
            page={query.data.page + 1}
            count={query.data.totalPages}
            onChange={(_, value) => setParams({ page: String(value - 1) })}
          />
        </>
      )}
    </>
  );
}
