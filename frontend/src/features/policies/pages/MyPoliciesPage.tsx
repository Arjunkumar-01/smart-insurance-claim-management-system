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
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { policyApi, policyKeys } from "../api/policy-api";
import { formatAmount } from "../../products/types/product-formatters";

export function MyPoliciesPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") ?? 0);
  const query = useQuery({
    queryKey: policyKeys.mine(page),
    queryFn: () => policyApi.mine(page),
  });
  if (query.isPending)
    return (
      <>
        <PageHeader
          eyebrow="Policies"
          title="My Policies"
          description="Review your insurance contracts and policy status."
        />
        <SkeletonState rows={5} />
      </>
    );
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load your policies."
        onRetry={() => void query.refetch()}
      />
    );
  if (!query.data.policies.length)
    return (
      <>
        <PageHeader
          eyebrow="Policies"
          title="My Policies"
          description="Review your insurance contracts and policy status."
        />
        <EmptyState title="No policies yet." />
        <Button onClick={() => navigate("/app/products")} variant="contained">
          Browse products
        </Button>
      </>
    );
  return (
    <>
      <PageHeader
        eyebrow="Policies"
        title="My Policies"
        description="Review your insurance contracts and policy status."
      />
      <DataTableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Policy number</TableCell>
              <TableCell>Product</TableCell>
              <TableCell>Premium</TableCell>
              <TableCell>Coverage</TableCell>
              <TableCell>Period</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {query.data.policies.map((policy) => (
              <TableRow hover key={policy.policyId}>
                <TableCell>{policy.policyNumber}</TableCell>
                <TableCell>{policy.productName}</TableCell>
                <TableCell>{formatAmount(policy.premiumAmount)}</TableCell>
                <TableCell>{formatAmount(policy.coverageAmount)}</TableCell>
                <TableCell>
                  {policy.startDate} to {policy.endDate}
                </TableCell>
                <TableCell>
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
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    onClick={() => navigate(`/app/policies/${policy.policyId}`)}
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
  );
}
