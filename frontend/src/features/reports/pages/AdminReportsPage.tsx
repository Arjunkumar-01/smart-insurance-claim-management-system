import { useQuery } from "@tanstack/react-query";
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { Link } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { formatAmount } from "../../products/types/product-formatters";
import { reportApi, reportKeys } from "../api/report-api";

export function AdminReportsPage() {
  const year = new Date().getFullYear();
  const overview = useQuery({
    queryKey: reportKeys.overview(),
    queryFn: reportApi.overview,
  });
  const revenue = useQuery({
    queryKey: reportKeys.revenue(year),
    queryFn: () => reportApi.revenue(year),
  });
  const products = useQuery({
    queryKey: reportKeys.products(),
    queryFn: reportApi.products,
  });
  const settlements = useQuery({
    queryKey: reportKeys.settlements(year),
    queryFn: () => reportApi.settlements(year),
  });
  const queries = [overview, revenue, products, settlements];
  if (queries.some((query) => query.isPending))
    return <SkeletonState rows={10} />;
  if (queries.some((query) => query.error) || !overview.data)
    return (
      <ErrorState
        message="Unable to load administrator reporting."
        onRetry={() =>
          void Promise.all(queries.map((query) => query.refetch()))
        }
      />
    );
  const report = overview.data;
  return (
    <>
      <PageHeader
        eyebrow="Reporting"
        title="Enterprise Reporting"
        description={`Operational and financial reporting for ${year}. Pending Claims are Submitted, Under Review, and Documents Required.`}
        actions={
          <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
            <Button
              component={Link}
              to="/app/admin/reports/revenue"
              variant="outlined"
            >
              Monthly revenue
            </Button>
            <Button
              component={Link}
              to="/app/admin/reports/products"
              variant="outlined"
            >
              Product performance
            </Button>
          </Box>
        }
      />
      <Typography variant="h6" sx={{ mb: 2 }}>
        Operational overview
      </Typography>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: {
            xs: "1fr",
            sm: "1fr 1fr",
            md: "repeat(4, 1fr)",
          },
          gap: 2,
          mb: 4,
        }}
      >
        <Metric label="Customers" value={String(report.customerCount)} />
        <Metric label="Total claims" value={String(report.totalClaims)} />
        <Metric label="Approved claims" value={String(report.approvedClaims)} />
        <Metric label="Rejected claims" value={String(report.rejectedClaims)} />
        <Metric label="Pending claims" value={String(report.pendingClaims)} />
        <Metric
          label="Premium collection"
          value={formatAmount(report.premiumCollected)}
        />
        <Metric
          label="Settlements paid"
          value={formatAmount(report.settlementsPaid)}
        />
        <Metric
          label="Claims ratio"
          value={`${report.claimsRatioPercent.toFixed(2)}%`}
        />
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", lg: "1fr 1fr" },
          gap: 3,
          mb: 3,
        }}
      >
        <StatusTable
          title="Claims summary"
          rows={report.claimsByStatus.map((row) => [row.status, row.count])}
        />
        <StatusTable
          title="Policy portfolio"
          rows={report.policiesByStatus.map((row) => [row.status, row.count])}
        />
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", lg: "1fr 1fr" },
          gap: 3,
        }}
      >
        <DataSection title="Monthly revenue">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Month</TableCell>
                <TableCell align="right">Payments</TableCell>
                <TableCell align="right">Premium</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {revenue.data?.map((row) => (
                <TableRow key={`${row.year}-${row.month}`}>
                  <TableCell>
                    {monthName(row.month)} {row.year}
                  </TableCell>
                  <TableCell align="right">{row.paymentCount}</TableCell>
                  <TableCell align="right">
                    {formatAmount(row.completedPremium)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {!revenue.data?.length ? <EmptyReport /> : null}
        </DataSection>
        <DataSection title="Claim settlement trend">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Month</TableCell>
                <TableCell align="right">Settlements</TableCell>
                <TableCell align="right">Amount</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {settlements.data?.map((row) => (
                <TableRow key={`${row.year}-${row.month}`}>
                  <TableCell>
                    {monthName(row.month)} {row.year}
                  </TableCell>
                  <TableCell align="right">{row.settlementCount}</TableCell>
                  <TableCell align="right">
                    {formatAmount(row.settlementAmount)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {!settlements.data?.length ? <EmptyReport /> : null}
        </DataSection>
        <DataSection title="Product performance">
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Product</TableCell>
                <TableCell align="right">Policies</TableCell>
                <TableCell align="right">Claims</TableCell>
                <TableCell align="right">Premium</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {products.data?.map((row) => (
                <TableRow key={row.productId}>
                  <TableCell>
                    {row.productName}
                    <Typography
                      variant="caption"
                      display="block"
                      color="text.secondary"
                    >
                      {row.productCode}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">{row.policyCount}</TableCell>
                  <TableCell align="right">{row.claimCount}</TableCell>
                  <TableCell align="right">
                    {formatAmount(row.premiumCollected)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {!products.data?.length ? <EmptyReport /> : null}
        </DataSection>
      </Box>
    </>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <Paper elevation={0} sx={{ p: 2.5, border: 1, borderColor: "divider" }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="h5" sx={{ mt: 1 }}>
        {value}
      </Typography>
    </Paper>
  );
}
function DataSection({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <Paper
      elevation={0}
      sx={{ border: 1, borderColor: "divider", overflowX: "auto" }}
    >
      <Typography variant="h6" sx={{ p: 2 }}>
        {title}
      </Typography>
      {children}
    </Paper>
  );
}
function EmptyReport() {
  return (
    <Typography color="text.secondary" sx={{ px: 2, pb: 2 }}>
      No data for this period.
    </Typography>
  );
}
function monthName(month: number) {
  return new Intl.DateTimeFormat(undefined, { month: "short" }).format(
    new Date(Date.UTC(2026, month - 1, 1)),
  );
}
function StatusTable({
  title,
  rows,
}: {
  title: string;
  rows: Array<[string, number]>;
}) {
  return (
    <DataSection title={title}>
      <Table size="small" aria-label={title}>
        <TableHead>
          <TableRow>
            <TableCell>Status</TableCell>
            <TableCell align="right">Count</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map(([status, count]) => (
            <TableRow key={status}>
              <TableCell>
                <StatusBadge label={status} tone="neutral" />
              </TableCell>
              <TableCell align="right">{count}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </DataSection>
  );
}
