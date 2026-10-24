import { Box, Typography } from "@mui/material";
import type { PropsWithChildren } from "react";

export function FormSection({
  title,
  description,
  children,
}: PropsWithChildren<{ title: string; description?: string }>) {
  return (
    <Box
      component="section"
      sx={{
        display: "grid",
        gap: 2,
        py: 3,
        borderBottom: 1,
        borderColor: "divider",
      }}
    >
      <Box>
        <Typography variant="h6">{title}</Typography>
        {description ? (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {description}
          </Typography>
        ) : null}
      </Box>
      {children}
    </Box>
  );
}
