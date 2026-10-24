import { Box, Paper, Typography } from "@mui/material";
import type { CustomerProfileResponse } from "../types/customer-types";

export function CustomerProfileSummary({
  profile,
}: {
  profile: CustomerProfileResponse;
}) {
  return (
    <Box sx={{ display: "grid", gap: 3 }}>
      <Paper
        elevation={0}
        sx={{ p: { xs: 2.5, sm: 3 }, border: 1, borderColor: "divider" }}
      >
        <Typography variant="h6">Account information</Typography>
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mt: 2,
          }}
        >
          <ReadOnlyField
            label="Name"
            value={`${profile.firstName} ${profile.lastName}`}
          />
          <ReadOnlyField label="Email" value={profile.email} />
        </Box>
      </Paper>
      <Paper
        elevation={0}
        sx={{ p: { xs: 2.5, sm: 3 }, border: 1, borderColor: "divider" }}
      >
        <Typography variant="h6">Personal and contact information</Typography>
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mt: 2,
          }}
        >
          <ReadOnlyField label="Phone number" value={profile.phoneNumber} />
          <ReadOnlyField
            label="Date of birth"
            value={profile.dateOfBirth ?? "Not provided"}
          />
        </Box>
      </Paper>
      <Paper
        elevation={0}
        sx={{ p: { xs: 2.5, sm: 3 }, border: 1, borderColor: "divider" }}
      >
        <Typography variant="h6">Address</Typography>
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
            gap: 2,
            mt: 2,
          }}
        >
          <ReadOnlyField label="Address line 1" value={profile.addressLine1} />
          <ReadOnlyField
            label="Address line 2"
            value={profile.addressLine2 || "Not provided"}
          />
          <ReadOnlyField label="City" value={profile.city} />
          <ReadOnlyField label="State" value={profile.state} />
          <ReadOnlyField label="Postal code" value={profile.postalCode} />
          <ReadOnlyField label="Country" value={profile.country} />
        </Box>
      </Paper>
    </Box>
  );
}

function ReadOnlyField({ label, value }: { label: string; value: string }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography sx={{ mt: 0.25 }}>{value}</Typography>
    </Box>
  );
}
