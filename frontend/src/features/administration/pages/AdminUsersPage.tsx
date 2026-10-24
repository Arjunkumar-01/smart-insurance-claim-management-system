import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from "@mui/material";
import { useAuth } from "../../../auth/useAuth";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { adminApi, adminUserKeys } from "../api/admin-api";
export function AdminUsersPage() {
  const { user } = useAuth();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const query = useQuery({
    queryKey: adminUserKeys.list(0, "createdAt"),
    queryFn: () => adminApi.list(),
  });
  const mutation = useMutation({
    mutationFn: ({
      id,
      status,
    }: {
      id: string;
      status: "ACTIVE" | "INACTIVE" | "LOCKED";
    }) => adminApi.changeStatus(id, { status }),
    onSuccess: async () => {
      await client.invalidateQueries({ queryKey: adminUserKeys.all });
      notify("Account status updated.", "success");
    },
  });
  if (query.isPending) return <SkeletonState rows={7} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load users."
        onRetry={() => void query.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Administration"
        title="User Administration"
        description="Review accounts and apply controlled status changes."
      />
      <DataTableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Roles</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {query.data.users.map((item) => {
              const self = item.userId === user?.userId;
              return (
                <TableRow key={item.userId}>
                  <TableCell>
                    {item.firstName} {item.lastName}
                  </TableCell>
                  <TableCell>{item.email}</TableCell>
                  <TableCell>{item.roles.join(", ")}</TableCell>
                  <TableCell>
                    <StatusBadge
                      label={item.accountStatus}
                      tone={
                        item.accountStatus === "ACTIVE"
                          ? "success"
                          : item.accountStatus === "LOCKED"
                            ? "error"
                            : "neutral"
                      }
                    />
                  </TableCell>
                  <TableCell>
                    {!self && item.accountStatus !== "ACTIVE" ? (
                      <Button
                        size="small"
                        onClick={() =>
                          mutation.mutate({ id: item.userId, status: "ACTIVE" })
                        }
                      >
                        Activate
                      </Button>
                    ) : null}
                    {!self && item.accountStatus !== "INACTIVE" ? (
                      <Button
                        size="small"
                        onClick={() =>
                          mutation.mutate({
                            id: item.userId,
                            status: "INACTIVE",
                          })
                        }
                      >
                        Deactivate
                      </Button>
                    ) : null}
                    {!self && item.accountStatus !== "LOCKED" ? (
                      <Button
                        size="small"
                        color="error"
                        onClick={() =>
                          mutation.mutate({ id: item.userId, status: "LOCKED" })
                        }
                      >
                        Lock
                      </Button>
                    ) : null}
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </DataTableContainer>
    </>
  );
}
