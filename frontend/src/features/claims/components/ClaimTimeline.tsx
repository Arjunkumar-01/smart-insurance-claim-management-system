import { Box, Paper, Typography } from "@mui/material";
import type { ClaimStatusHistoryResponse } from "../types/claim-types";
export function ClaimTimeline({
  entries,
}: {
  entries: ClaimStatusHistoryResponse[];
}) {
  return (
    <Box sx={{ display: "grid", gap: 1.5 }}>
      {entries.map((entry) => (
        <Paper
          elevation={0}
          key={entry.historyId}
          sx={{ p: 2, border: 1, borderColor: "divider" }}
        >
          <Typography fontWeight={700}>
            {entry.fromStatus ?? "Initial"} → {entry.toStatus}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {entry.changedAt} · {entry.actor}
          </Typography>
          {entry.reason ? (
            <Typography variant="body2" sx={{ mt: 0.5 }}>
              {entry.reason}
            </Typography>
          ) : null}
        </Paper>
      ))}
    </Box>
  );
}
