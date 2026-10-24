import { Paper, TableContainer } from "@mui/material";
import type { PropsWithChildren } from "react";

export function DataTableContainer({ children }: PropsWithChildren) {
  return (
    <TableContainer
      component={Paper}
      elevation={0}
      sx={{
        border: 1,
        borderColor: "divider",
        borderRadius: 1,
        overflowX: "auto",
      }}
    >
      {children}
    </TableContainer>
  );
}
