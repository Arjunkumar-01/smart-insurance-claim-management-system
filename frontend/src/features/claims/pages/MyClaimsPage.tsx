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
import { ClaimStatusView } from "../components/ClaimStatus";
import { claimApi, claimKeys } from "../api/claim-api";
import { formatAmount } from "../../products/types/product-formatters";
export function MyClaimsPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") ?? 0);
  const query = useQuery({
    queryKey: claimKeys.mine(page),
    queryFn: () => claimApi.mine(page),
  });
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load your claims."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Claims"
        title="My Claims"
        description="Track submitted claims and their current lifecycle state."
        actions={
          <Button
            variant="contained"
            onClick={() => navigate("/app/claims/new")}
          >
            Submit claim
          </Button>
        }
      />
      {!query.data.claims.length ? (
        <EmptyState title="No claims submitted yet." />
      ) : (
        <>
          <DataTableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Claim number</TableCell>
                  <TableCell>Policy</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Incident date</TableCell>
                  <TableCell>Amount</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {query.data.claims.map((claim) => (
                  <TableRow key={claim.claimId}>
                    <TableCell>{claim.claimNumber}</TableCell>
                    <TableCell>{claim.policyNumber}</TableCell>
                    <TableCell>{claim.claimType}</TableCell>
                    <TableCell>{claim.incidentDate}</TableCell>
                    <TableCell>{formatAmount(claim.claimAmount)}</TableCell>
                    <TableCell>
                      <ClaimStatusView status={claim.status} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => navigate(`/app/claims/${claim.claimId}`)}
                      >
                        View
                      </Button>
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
