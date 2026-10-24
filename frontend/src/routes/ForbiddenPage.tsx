import { Box, Button, Paper, Typography } from "@mui/material";
import { Link } from "react-router-dom";

export function ForbiddenPage() {
  return (
    <Box sx={{ minHeight: "60vh", display: "grid", placeItems: "center" }}>
      <Paper
        component="section"
        elevation={0}
        sx={{
          maxWidth: 520,
          p: { xs: 3, sm: 5 },
          border: 1,
          borderColor: "divider",
        }}
      >
        <Typography variant="overline" color="error.main">
          Access restricted
        </Typography>
        <Typography
          variant="h2"
          sx={{ mt: 1, fontSize: { xs: "2rem", sm: "2.5rem" } }}
        >
          You do not have access to this workspace
        </Typography>
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Your signed-in account cannot open this area.
        </Typography>
        <Button component={Link} to="/app" variant="contained" sx={{ mt: 3 }}>
          Return to workspace
        </Button>
      </Paper>
    </Box>
  );
}
