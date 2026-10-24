import { Box, Typography } from "@mui/material";

export function EmptyState({ title }: { title: string }) {
  return (
    <Box
      sx={{
        minHeight: 280,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 1,
        textAlign: "center",
      }}
    >
      <Typography variant="h5">{title}</Typography>
      <Typography color="text.secondary">
        There is nothing to show yet.
      </Typography>
    </Box>
  );
}
