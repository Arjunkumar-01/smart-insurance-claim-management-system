import { useQuery } from "@tanstack/react-query";
import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { policyApi, policyKeys } from "../api/policy-api";
import { formatAmount } from "../../products/types/product-formatters";

export function AdminPoliciesPage() {
  const navigate = useNavigate();
  const query = useQuery({
    queryKey: policyKeys.admin(0),
    queryFn: () => policyApi.admin(0),
  });
  if (query.isPending) return <SkeletonState rows={6} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load policy oversight."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Administration"
        title="Policy Oversight"
        description="Review policies and controlled cancellation workflows."
        actions={
          <Button
            variant="outlined"
            onClick={() => navigate("/app/admin/policy-cancellations")}
          >
            Cancellation requests
          </Button>
        }
      />
      {!query.data.policies.length ? (
        <EmptyState title="No policies found." />
      ) : (
        <DataTableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Policy number</TableCell>
                <TableCell>Product</TableCell>
                <TableCell>Premium</TableCell>
                <TableCell>Coverage</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {query.data.policies.map((policy) => (
                <TableRow key={policy.policyId}>
                  <TableCell>{policy.policyNumber}</TableCell>
                  <TableCell>{policy.productName}</TableCell>
                  <TableCell>{formatAmount(policy.premiumAmount)}</TableCell>
                  <TableCell>{formatAmount(policy.coverageAmount)}</TableCell>
                  <TableCell>
                    <StatusBadge
                      label={policy.status}
                      tone={policy.status === "ACTIVE" ? "success" : "neutral"}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </DataTableContainer>
      )}
    </>
  );
}
