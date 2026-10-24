import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Typography,
} from "@mui/material";
import type { ProductResponse } from "../types/product-types";
import { formatAmount } from "../types/product-formatters";

export function ProductComparison({
  products,
}: {
  products: ProductResponse[];
}) {
  const rows: Array<[string, (product: ProductResponse) => string]> = [
    ["Product code", (p) => p.productCode],
    ["Name", (p) => p.name],
    ["Category", (p) => p.category],
    ["Policy tenure", (p) => `${p.tenureMonths} months`],
    ["Premium", (p) => formatAmount(p.premiumAmount)],
    ["Coverage", (p) => formatAmount(p.coverageAmount)],
    ["Coverage description", (p) => p.coverageDescription],
  ];
  return (
    <Paper
      elevation={0}
      sx={{ border: 1, borderColor: "divider", overflowX: "auto" }}
    >
      <Box sx={{ minWidth: 640 }}>
        <Typography variant="h6" sx={{ p: 2 }}>
          Product comparison
        </Typography>
        <Table size="small">
          <TableBody>
            {rows.map(([label, value]) => (
              <TableRow key={label}>
                <TableCell sx={{ fontWeight: 700, width: 180 }}>
                  {label}
                </TableCell>
                {products.map((product) => (
                  <TableCell key={product.productId}>
                    {value(product)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Box>
    </Paper>
  );
}
