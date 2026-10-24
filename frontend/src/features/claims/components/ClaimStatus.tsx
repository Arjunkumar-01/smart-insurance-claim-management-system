import {
  StatusBadge,
  type StatusTone,
} from "../../../components/layout/StatusBadge";
import { Box, Typography } from "@mui/material";
import type { ClaimStatus } from "../types/claim-types";
const tones: Record<ClaimStatus, StatusTone> = {
  SUBMITTED: "info",
  UNDER_REVIEW: "info",
  DOCUMENTS_REQUIRED: "warning",
  APPROVED: "success",
  REJECTED: "error",
  SETTLED: "success",
};
const guidance: Record<ClaimStatus, string> = {
  SUBMITTED: "Your claim has been submitted.",
  UNDER_REVIEW: "Your claim is being reviewed.",
  DOCUMENTS_REQUIRED: "Additional supporting documents are required.",
  APPROVED: "Your claim has been approved.",
  REJECTED: "Your claim was not approved.",
  SETTLED: "Your claim has been settled.",
};
export function ClaimStatusView({ status }: { status: ClaimStatus }) {
  return (
    <Box sx={{ minWidth: 0 }}>
      <StatusBadge label={status} tone={tones[status]} />
      <Typography variant="body2" sx={{ mt: 0.5, overflowWrap: "anywhere" }}>
        {guidance[status]}
      </Typography>
    </Box>
  );
}
