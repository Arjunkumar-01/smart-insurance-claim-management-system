import { Box, Typography } from "@mui/material";
import type { ReactNode } from "react";

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <Box
      component="section"
      sx={{
        display: "flex",
        alignItems: { xs: "flex-start", sm: "center" },
        justifyContent: "space-between",
        gap: 3,
        flexWrap: "wrap",
        mb: 4,
      }}
    >
      <Box sx={{ minWidth: 0 }}>
        {eyebrow ? (
          <Typography variant="overline" color="primary.main">
            {eyebrow}
          </Typography>
        ) : null}
        <Typography
          variant="h1"
          sx={{ fontSize: { xs: "2rem", sm: "2.5rem" } }}
        >
          {title}
        </Typography>
        {description ? (
          <Typography color="text.secondary" sx={{ mt: 1, maxWidth: 720 }}>
            {description}
          </Typography>
        ) : null}
      </Box>
      {actions ? (
        <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>{actions}</Box>
      ) : null}
    </Box>
  );
}
