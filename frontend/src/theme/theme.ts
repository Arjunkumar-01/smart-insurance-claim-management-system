import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    primary: { main: "#08204D", contrastText: "#FFFFFF" },
    secondary: { main: "#2563EB", contrastText: "#FFFFFF" },
    success: { main: "#16A36A" },
    warning: { main: "#D99A24" },
    error: { main: "#DC4545" },
    info: { main: "#06B6D4" },
    background: { default: "#F5F7FA", paper: "#FFFFFF" },
    text: {
      primary: "#0F172A",
      secondary: "#64748B",
      disabled: "#94A3B8",
    },
    divider: "#E2E8F0",
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'Inter, "Segoe UI", system-ui, sans-serif',
    h1: {
      fontSize: "2.5rem",
      fontWeight: 700,
      lineHeight: 1.2,
    },
    h2: { fontWeight: 700 },
    h6: { letterSpacing: 0 },
    body1: { color: "#334155" },
  },
  components: {
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 6, textTransform: "none", fontWeight: 700 },
      },
    },
    MuiTextField: { defaultProps: { size: "small" } },
    MuiOutlinedInput: { styleOverrides: { root: { borderRadius: 6 } } },
    MuiPaper: { styleOverrides: { root: { borderRadius: 8 } } },
    MuiTableCell: { styleOverrides: { root: { borderColor: "#E2E8F0" } } },
    MuiChip: { styleOverrides: { root: { borderRadius: 6, fontWeight: 700 } } },
    MuiLink: { defaultProps: { underline: "hover" } },
    MuiCssBaseline: {
      styleOverrides: {
        "*:focus-visible": {
          outline: "3px solid #2563EB !important",
          outlineOffset: 2,
        },
      },
    },
  },
});
