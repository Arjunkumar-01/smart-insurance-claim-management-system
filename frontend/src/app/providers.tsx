import { CssBaseline, ThemeProvider } from "@mui/material";
import { QueryClientProvider } from "@tanstack/react-query";
import type { PropsWithChildren } from "react";
import { queryClient } from "../api/query-client";
import { NotificationProvider } from "../components/feedback/NotificationProvider";
import { theme } from "../theme/theme";

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <NotificationProvider>{children}</NotificationProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
