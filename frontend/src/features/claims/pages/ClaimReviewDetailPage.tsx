import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Box, Button, Paper, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { claimReviewApi, reviewKeys } from "../api/claim-review-api";
import {
  getAvailableClaimActions,
  type ClaimWorkflowAction,
} from "../types/claim-actions";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { ClaimTimeline } from "../components/ClaimTimeline";
import { ClaimDocumentList } from "../components/ClaimDocumentList";
import { formatAmount } from "../../products/types/product-formatters";
export function ClaimReviewDetailPage() {
  const { claimId = "" } = useParams();
  const client = useQueryClient();
  const query = useQuery({
    queryKey: reviewKeys.detail(claimId),
    queryFn: () => claimReviewApi.detail(claimId),
  });
  const mutation = useMutation<
    unknown,
    Error,
    { action: ClaimWorkflowAction; notes: string }
  >({
    mutationFn: ({
      action,
      notes,
    }: {
      action: ClaimWorkflowAction;
      notes: string;
    }) =>
      action === "START_REVIEW"
        ? claimReviewApi.start(claimId, notes)
        : action === "REQUEST_DOCUMENTS"
          ? claimReviewApi.requestDocuments(claimId, notes)
          : action === "RESUME_REVIEW"
            ? claimReviewApi.resume(claimId, notes)
            : action === "APPROVE"
              ? claimReviewApi.approve(claimId, notes)
              : action === "REJECT"
                ? claimReviewApi.reject(claimId, notes)
                : claimReviewApi.settle(claimId, notes),
    onSuccess: async () => {
      await client.invalidateQueries({ queryKey: reviewKeys.all });
      await client.invalidateQueries({ queryKey: reviewKeys.detail(claimId) });
    },
  });
  if (query.isPending) return <SkeletonState rows={8} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Claim unavailable"
        message="This claim was not found or is unavailable."
        onRetry={() => void query.refetch()}
      />
    );
  const claim = query.data;
  const actions = getAvailableClaimActions(claim.status);
  const run = (action: ClaimWorkflowAction) => {
    const notes = window.prompt(
      `${action.replaceAll("_", " ")} notes (optional)`,
    );
    if (notes === null) return;
    mutation.mutate({ action, notes });
  };
  return (
    <>
      <PageHeader
        eyebrow="Claims operations"
        title={claim.claimNumber}
        description={`Policy ${claim.policyNumber}`}
      />
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: "minmax(0, 1fr)",
          gap: 3,
          minWidth: 0,
        }}
      >
        <Paper
          elevation={0}
          sx={{
            p: 3,
            border: 1,
            borderColor: "divider",
            overflowWrap: "anywhere",
          }}
        >
          <Box
            sx={{
              display: "flex",
              gap: 2,
              alignItems: { xs: "flex-start", sm: "center" },
              flexDirection: { xs: "column", sm: "row" },
              flexWrap: "wrap",
            }}
          >
            <Typography variant="h6">Claim overview</Typography>
            <StatusBadge
              label={claim.status}
              tone={
                claim.status === "APPROVED" || claim.status === "SETTLED"
                  ? "success"
                  : claim.status === "REJECTED"
                    ? "error"
                    : claim.status === "DOCUMENTS_REQUIRED"
                      ? "warning"
                      : "info"
              }
            />
          </Box>
          <Typography sx={{ mt: 2 }}>
            Claim amount: {formatAmount(claim.claimAmount)}
          </Typography>
          <Typography>Claim type: {claim.claimType}</Typography>
          <Typography>
            Coverage amount: {formatAmount(claim.coverageAmount)}
          </Typography>
          <Typography>Incident date: {claim.incidentDate}</Typography>
          <Typography sx={{ mt: 2 }}>{claim.description}</Typography>
          <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap", mt: 3 }}>
            {actions.map((action) => (
              <Button
                key={action}
                variant={action === "REJECT" ? "outlined" : "contained"}
                color={action === "REJECT" ? "error" : "primary"}
                disabled={mutation.isPending}
                onClick={() => run(action)}
              >
                {action.replaceAll("_", " ")}
              </Button>
            ))}
          </Box>
          {mutation.error ? (
            <Alert severity="error" sx={{ mt: 2 }}>
              The workflow action could not be completed. The latest claim state
              may have changed.
            </Alert>
          ) : null}
        </Paper>
        <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Review Activity
          </Typography>
          {claim.reviews.map((review) => (
            <Box
              key={review.reviewId}
              sx={{ py: 1, borderBottom: 1, borderColor: "divider" }}
            >
              <Typography fontWeight={700}>{review.action}</Typography>
              <Typography variant="body2" color="text.secondary">
                {review.createdAt}
                {review.notes ? ` · ${review.notes}` : ""}
              </Typography>
            </Box>
          ))}
        </Paper>
        <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Lifecycle
          </Typography>
          <ClaimTimeline entries={claim.history} />
        </Paper>
        <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Documents
          </Typography>
          <ClaimDocumentList
            documents={claim.documents}
            onDownload={async (document) => {
              const blob = await claimReviewApi.download(
                claimId,
                document.documentId,
              );
              const url = URL.createObjectURL(blob);
              const anchor = window.document.createElement("a");
              anchor.href = url;
              anchor.download = document.originalFileName;
              anchor.click();
              URL.revokeObjectURL(url);
            }}
          />
        </Paper>
      </Box>
    </>
  );
}
