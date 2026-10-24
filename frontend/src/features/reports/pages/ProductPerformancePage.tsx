import { useQuery } from "@tanstack/react-query";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { formatAmount } from "../../products/types/product-formatters";
import { reportApi, reportKeys } from "../api/report-api";
export function ProductPerformancePage() {
  const query = useQuery({
    queryKey: reportKeys.products(),
    queryFn: reportApi.products,
  });
  if (query.isPending) return <SkeletonState rows={6} />;
  if (query.error)
    return (
      <ErrorState
        message="Unable to load product performance."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Reporting"
        title="Product Performance"
        description="Read-only Policy, Claim, premium, and settlement measures by product."
      />
      <DataTableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Product</TableCell>
              <TableCell>Policies</TableCell>
              <TableCell>Claims</TableCell>
              <TableCell>Premium collected</TableCell>
              <TableCell>Settlements paid</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {query.data?.map((row) => (
              <TableRow key={row.productId}>
                <TableCell>
                  {row.productCode} · {row.productName}
                </TableCell>
                <TableCell>{row.policyCount}</TableCell>
                <TableCell>{row.claimCount}</TableCell>
                <TableCell>{formatAmount(row.premiumCollected)}</TableCell>
                <TableCell>{formatAmount(row.settlementsPaid)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </DataTableContainer>
    </>
  );
}
