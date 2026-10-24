import { Alert, Box, Button } from "@mui/material";

export function ErrorState({
  title = "Unable to load this content",
  message = "Please try again.",
  onRetry,
}: {
  title?: string;
  message?: string;
  onRetry?: () => void;
}) {
  return (
    <Alert severity="error" role="alert">
      <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
        <strong>{title}</strong>
        <span>{message}</span>
        {onRetry ? (
          <Button size="small" onClick={onRetry}>
            Try again
          </Button>
        ) : null}
      </Box>
    </Alert>
  );
}
