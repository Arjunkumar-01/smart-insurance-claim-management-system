import {
  Component,
  type ErrorInfo,
  type PropsWithChildren,
  type ReactNode,
} from "react";
import { Box, Button, Paper, Typography } from "@mui/material";

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<PropsWithChildren, State> {
  state: State = { hasError: false };
  static getDerivedStateFromError(): State {
    return { hasError: true };
  }
  componentDidCatch(error: Error, info: ErrorInfo) {
    void error;
    void info;
  }
  render(): ReactNode {
    if (!this.state.hasError) return this.props.children;
    return (
      <Box
        sx={{
          minHeight: "100vh",
          display: "grid",
          placeItems: "center",
          bgcolor: "background.default",
          p: 3,
        }}
      >
        <Paper
          component="section"
          elevation={0}
          sx={{ maxWidth: 520, p: 5, border: 1, borderColor: "divider" }}
        >
          <Typography variant="overline" color="error.main">
            Application error
          </Typography>
          <Typography variant="h2" sx={{ mt: 1, fontSize: "2rem" }}>
            This workspace could not be displayed
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 2 }}>
            Return to the application and try again.
          </Typography>
          <Button
            variant="contained"
            sx={{ mt: 3 }}
            onClick={() => window.location.assign("/app")}
          >
            Return Home
          </Button>
        </Paper>
      </Box>
    );
  }
}
