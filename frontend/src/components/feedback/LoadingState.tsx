import { Box, CircularProgress, Typography } from "@mui/material";

export function LoadingState() {
  return (
    <Box
      role="status"
      aria-live="polite"
      sx={{
        p: 6,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 2,
      }}
    >
      <CircularProgress aria-label="Loading" />
      <Typography>Loading</Typography>
    </Box>
  );
}
