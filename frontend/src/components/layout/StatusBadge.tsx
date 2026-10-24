import { Chip } from "@mui/material";

export type StatusTone = "success" | "warning" | "error" | "info" | "neutral";

const toneStyles: Record<StatusTone, { color: string; background: string }> = {
  success: { color: "#16A36A", background: "#FFFFFF" },
  warning: { color: "#D99A24", background: "#FFFFFF" },
  error: { color: "#DC4545", background: "#FFFFFF" },
  info: { color: "#2563EB", background: "#FFFFFF" },
  neutral: { color: "#64748B", background: "#FFFFFF" },
};

export function StatusBadge({
  label,
  tone = "neutral",
}: {
  label: string;
  tone?: StatusTone;
}) {
  const styles = toneStyles[tone];
  return (
    <Chip
      label={label}
      size="small"
      sx={{
        color: styles.color,
        bgcolor: styles.background,
        border: 1,
        borderColor: styles.color,
      }}
    />
  );
}
