import { Box, Paper, Typography } from "@mui/material";
import {
  StatusBadge,
  type StatusTone,
} from "../../../components/layout/StatusBadge";
import type { CustomerKycResponse } from "../types/customer-types";

const statusTone: Record<CustomerKycResponse["kycStatus"], StatusTone> = {
  PENDING: "warning",
  VERIFIED: "success",
  REJECTED: "error",
};

export function CustomerKycSummary({ kyc }: { kyc: CustomerKycResponse }) {
  return (
    <Paper
      elevation={0}
      sx={{ p: { xs: 2.5, sm: 3 }, border: 1, borderColor: "divider" }}
    >
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr 1fr" },
          gap: 3,
        }}
      >
        <Box>
          <Typography variant="caption" color="text.secondary">
            Identity type
          </Typography>
          <Typography sx={{ mt: 0.25 }}>{kyc.identityType}</Typography>
        </Box>
        <Box>
          <Typography variant="caption" color="text.secondary">
            Masked identity number
          </Typography>
          <Typography sx={{ mt: 0.25 }}>{kyc.maskedIdentityNumber}</Typography>
        </Box>
        <Box>
          <Typography variant="caption" color="text.secondary">
            KYC status
          </Typography>
          <Box sx={{ mt: 0.5 }}>
            <StatusBadge
              label={kyc.kycStatus}
              tone={statusTone[kyc.kycStatus]}
            />
          </Box>
        </Box>
      </Box>
    </Paper>
  );
}
