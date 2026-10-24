import { Box, Button, Typography } from "@mui/material";
import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <Box
      sx={{
        minHeight: "60vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 2,
        textAlign: "center",
      }}
    >
      <Typography variant="h2">Page not found</Typography>
      <Typography color="text.secondary">
        That page is not part of this foundation.
      </Typography>
      <Button component={Link} to="/" variant="contained">
        Return to SICMS
      </Button>
    </Box>
  );
}
