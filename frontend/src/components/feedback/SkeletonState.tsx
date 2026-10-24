import { Box, Skeleton } from "@mui/material";

export function SkeletonState({ rows = 3 }: { rows?: number }) {
  return (
    <Box
      role="status"
      aria-label="Loading content"
      sx={{ display: "grid", gap: 1.5 }}
    >
      {Array.from({ length: rows }, (_, index) => (
        <Skeleton key={index} variant="rounded" height={42} />
      ))}
    </Box>
  );
}
