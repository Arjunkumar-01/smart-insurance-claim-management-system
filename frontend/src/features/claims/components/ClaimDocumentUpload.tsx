import { useState } from "react";
import { Alert, Box, Button, Typography } from "@mui/material";
const allowed = ["application/pdf", "image/jpeg", "image/png"];
export function ClaimDocumentUpload({
  pending,
  onUpload,
}: {
  pending?: boolean;
  onUpload: (file: File) => void;
}) {
  const [error, setError] = useState<string | null>(null);
  const handle = (file?: File) => {
    if (!file) return;
    if (!allowed.includes(file.type))
      return setError("Choose a PDF, JPEG, or PNG file.");
    if (!file.size) return setError("The selected file is empty.");
    if (file.size > 10 * 1024 * 1024)
      return setError("The file must be 10 MB or smaller.");
    setError(null);
    onUpload(file);
  };
  return (
    <Box
      sx={{
        display: "grid",
        gap: 1.5,
        p: 2,
        border: 1,
        borderColor: "divider",
      }}
    >
      <Typography variant="subtitle2">Supporting documents</Typography>
      <Typography variant="body2" color="text.secondary">
        PDF, JPEG, or PNG up to 10 MB.
      </Typography>
      {error ? <Alert severity="error">{error}</Alert> : null}
      <Button component="label" variant="outlined" disabled={pending}>
        {pending ? "Uploading..." : "Upload document"}
        <input
          hidden
          type="file"
          accept="application/pdf,image/jpeg,image/png"
          onChange={(event) => handle(event.target.files?.[0])}
        />
      </Button>
    </Box>
  );
}
