import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Box, Paper, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { PageHeader } from "../../../components/layout/PageHeader";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { claimApi, claimKeys } from "../api/claim-api";
import { ClaimStatusView } from "../components/ClaimStatus";
import { ClaimTimeline } from "../components/ClaimTimeline";
import { ClaimDocumentList } from "../components/ClaimDocumentList";
import { ClaimDocumentUpload } from "../components/ClaimDocumentUpload";
import { formatAmount } from "../../products/types/product-formatters";
export function ClaimDetailPage() {
  const { claimId = "" } = useParams();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const claim = useQuery({
    queryKey: claimKeys.detail(claimId),
    queryFn: () => claimApi.detail(claimId),
    enabled: Boolean(claimId),
  });
  const history = useQuery({
    queryKey: claimKeys.history(claimId),
    queryFn: () => claimApi.history(claimId),
    enabled: Boolean(claimId),
  });
  const documents = useQuery({
    queryKey: claimKeys.documents(claimId),
    queryFn: () => claimApi.documents(claimId),
    enabled: Boolean(claimId),
  });
  const settlement = useQuery({
    queryKey: claimKeys.settlement(claimId),
    queryFn: () => claimApi.settlement(claimId),
    enabled: claim.data?.status === "SETTLED",
  });
  const upload = useMutation({
    mutationFn: (file: File) => claimApi.upload(claimId, file),
    onSuccess: async () => {
      await client.invalidateQueries({
        queryKey: claimKeys.documents(claimId),
      });
      notify("Document uploaded.", "success");
    },
  });
  if (claim.isPending) return <SkeletonState rows={7} />;
  if (claim.error || !claim.data)
    return (
      <ErrorState
        title="Claim unavailable"
        message="This claim was not found or is unavailable."
        onRetry={() => void claim.refetch()}
      />
    );
  const item = claim.data;
  const download = async (file: {
    documentId: string;
    originalFileName: string;
  }) => {
    const blob = await claimApi.download(claimId, file.documentId);
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = file.originalFileName;
    anchor.click();
    URL.revokeObjectURL(url);
  };
  return (
    <>
      <PageHeader
        eyebrow="Claim detail"
        title={item.claimNumber}
        description={`Policy ${item.policyNumber}`}
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
            <Typography variant="h6">Current status</Typography>
            <ClaimStatusView status={item.status} />
          </Box>
          <Typography sx={{ mt: 2 }}>
            Incident date: {item.incidentDate}
          </Typography>
          <Typography>Claim type: {item.claimType}</Typography>
          <Typography>
            Claim amount: {formatAmount(item.claimAmount)}
          </Typography>
          <Typography>
            Coverage amount: {formatAmount(item.coverageAmount)}
          </Typography>
          <Typography sx={{ mt: 2 }} color="text.secondary">
            {item.description}
          </Typography>
        </Paper>
        {item.status === "SETTLED" && settlement.data ? (
          <Paper
            elevation={0}
            sx={{
              p: 3,
              border: 1,
              borderColor: "divider",
              overflowWrap: "anywhere",
            }}
          >
            <Typography variant="h6">Settlement</Typography>
            <Typography sx={{ mt: 1 }}>
              Settlement number: {settlement.data.settlementNumber}
            </Typography>
            <Typography>
              Amount: {formatAmount(settlement.data.settlementAmount)}
            </Typography>
            <Typography color="text.secondary">
              Settled: {settlement.data.settledAt}
            </Typography>
          </Paper>
        ) : null}
        {history.data ? (
          <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Lifecycle
            </Typography>
            <ClaimTimeline entries={history.data} />
          </Paper>
        ) : null}
        {documents.data ? (
          <Paper elevation={0} sx={{ p: 3, border: 1, borderColor: "divider" }}>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Documents
            </Typography>
            {item.status === "DOCUMENTS_REQUIRED" ? (
              <Alert severity="warning" sx={{ mb: 2 }}>
                Additional supporting documents are required.
              </Alert>
            ) : null}
            <ClaimDocumentUpload
              pending={upload.isPending}
              onUpload={(file) => upload.mutate(file)}
            />
            {upload.error ? (
              <Alert severity="error" sx={{ mt: 2 }}>
                The document could not be uploaded.
              </Alert>
            ) : null}
            {documents.data.length ? (
              <Box sx={{ mt: 2 }}>
                <ClaimDocumentList
                  documents={documents.data}
                  onDownload={download}
                />
              </Box>
            ) : null}
          </Paper>
        ) : null}
      </Box>
    </>
  );
}
