import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { policyApi, policyKeys } from "../api/policy-api";

export function PolicyCancellationsPage() {
  const client = useQueryClient();
  const { notify } = useNotifications();
  const query = useQuery({
    queryKey: policyKeys.cancellations(),
    queryFn: policyApi.cancellations,
  });
  const decision = useMutation({
    mutationFn: ({
      id,
      status,
    }: {
      id: string;
      status: "APPROVED" | "REJECTED";
    }) => policyApi.decideCancellation(id, { status, decisionComment: "" }),
    onSuccess: async (_, variables) => {
      await client.invalidateQueries({ queryKey: policyKeys.cancellations() });
      await client.invalidateQueries({ queryKey: policyKeys.admin(0) });
      notify(`Cancellation ${variables.status.toLowerCase()}.`, "success");
    },
  });
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load cancellation requests."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Administration"
        title="Cancellation Requests"
        description="Review pending customer cancellation requests."
      />
      {!query.data.length ? (
        <EmptyState title="No pending cancellation requests." />
      ) : (
        <DataTableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Policy number</TableCell>
                <TableCell>Reason</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {query.data.map((request) => (
                <TableRow key={request.requestId}>
                  <TableCell>{request.policyNumber}</TableCell>
                  <TableCell>
                    {request.reason || "No reason provided"}
                  </TableCell>
                  <TableCell>
                    <StatusBadge label={request.status} tone="warning" />
                  </TableCell>
                  <TableCell>
                    <Button
                      size="small"
                      color="success"
                      onClick={() =>
                        decision.mutate({
                          id: request.requestId,
                          status: "APPROVED",
                        })
                      }
                    >
                      Approve
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      onClick={() =>
                        decision.mutate({
                          id: request.requestId,
                          status: "REJECTED",
                        })
                      }
                    >
                      Reject
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </DataTableContainer>
      )}
      {decision.error ? (
        <Alert severity="error" sx={{ mt: 2 }}>
          The cancellation decision could not be completed.
        </Alert>
      ) : null}
    </>
  );
}
