import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
} from "@mui/material";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { formatAmount } from "../../products/types/product-formatters";
import { reportApi, reportKeys } from "../api/report-api";
export function MonthlyRevenuePage() {
  const [params, setParams] = useSearchParams();
  const year = Number(params.get("year") ?? new Date().getFullYear());
  const query = useQuery({
    queryKey: reportKeys.revenue(year),
    queryFn: () => reportApi.revenue(year),
  });
  return (
    <>
      <PageHeader
        eyebrow="Reporting"
        title="Monthly Revenue"
        description="Completed premium collection by backend-defined month."
      />
      <Box sx={{ mb: 3, maxWidth: 220 }}>
        <TextField
          label="Year"
          type="number"
          value={year}
          onChange={(event) => setParams({ year: event.target.value })}
        />
      </Box>
      {query.isPending ? (
        <SkeletonState rows={6} />
      ) : query.error ? (
        <ErrorState
          message="Unable to load monthly revenue."
          onRetry={() => void query.refetch()}
        />
      ) : (
        <DataTableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Month</TableCell>
                <TableCell>Payments</TableCell>
                <TableCell>Completed premium</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {query.data?.map((row) => (
                <TableRow key={`${row.year}-${row.month}`}>
                  <TableCell>
                    {row.year}-{String(row.month).padStart(2, "0")}
                  </TableCell>
                  <TableCell>{row.paymentCount}</TableCell>
                  <TableCell>{formatAmount(row.completedPremium)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </DataTableContainer>
      )}
    </>
  );
}
