import { useQuery } from "@tanstack/react-query";
import {
  Pagination,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { useSearchParams } from "react-router-dom";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { auditKeys } from "../api/audit-api";
import { getAuditLogs } from "../api/audit-api";
export function AuditLogsPage() {
  const [params, setParams] = useSearchParams();
  const page = Number(params.get("page") ?? 0);
  const query = useQuery({
    queryKey: auditKeys.list(page),
    queryFn: () => getAuditLogs(page),
  });
  if (query.isPending) return <SkeletonState rows={7} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load audit logs."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="System"
        title="Audit Logs"
        description="Append-only operational history."
      />
      <DataTableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Occurred</TableCell>
              <TableCell>Action</TableCell>
              <TableCell>Actor</TableCell>
              <TableCell>Resource</TableCell>
              <TableCell>Description</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {query.data.logs.map((log) => (
              <TableRow key={log.auditId}>
                <TableCell>{log.occurredAt}</TableCell>
                <TableCell>{log.action.replaceAll("_", " ")}</TableCell>
                <TableCell>{log.actorId ?? "Not recorded"}</TableCell>
                <TableCell>{log.resourceType}</TableCell>
                <TableCell>{log.description}</TableCell>
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
  );
}
