import {
  Button,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TableSortLabel,
} from "@mui/material";
import { DataTableContainer } from "../../../components/layout/DataTableContainer";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import type { ProductResponse } from "../types/product-types";
import { formatAmount } from "../types/product-formatters";

export function ProductTable({
  products,
  admin,
  selected,
  onSelect,
  onView,
  onEdit,
  sort,
  onSort,
}: {
  products: ProductResponse[];
  admin: boolean;
  selected: string[];
  onSelect: (id: string) => void;
  onView: (id: string) => void;
  onEdit: (id: string) => void;
  sort: string;
  onSort: (value: string) => void;
}) {
  return (
    <DataTableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell padding="checkbox">Compare</TableCell>
            <TableCell>
              <TableSortLabel
                active={sort.startsWith("productCode")}
                direction={sort.endsWith("desc") ? "desc" : "asc"}
                onClick={() => onSort("productCode,asc")}
              >
                Product Code
              </TableSortLabel>
            </TableCell>
            <TableCell>
              <TableSortLabel
                active={sort.startsWith("name")}
                direction={sort.endsWith("desc") ? "desc" : "asc"}
                onClick={() => onSort("name,asc")}
              >
                Name
              </TableSortLabel>
            </TableCell>
            <TableCell>Category</TableCell>
            <TableCell>Tenure</TableCell>
            <TableCell>Premium</TableCell>
            <TableCell>Coverage</TableCell>
            {admin ? <TableCell>Status</TableCell> : null}
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {products.map((product) => (
            <TableRow hover key={product.productId}>
              <TableCell padding="checkbox">
                <Checkbox
                  checked={selected.includes(product.productId)}
                  onChange={() => onSelect(product.productId)}
                  inputProps={{ "aria-label": `Compare ${product.name}` }}
                />
              </TableCell>
              <TableCell>{product.productCode}</TableCell>
              <TableCell>{product.name}</TableCell>
              <TableCell>{product.category}</TableCell>
              <TableCell>{product.tenureMonths} months</TableCell>
              <TableCell>{formatAmount(product.premiumAmount)}</TableCell>
              <TableCell>{formatAmount(product.coverageAmount)}</TableCell>
              {admin ? (
                <TableCell>
                  <StatusBadge
                    label={product.status}
                    tone={product.status === "ACTIVE" ? "success" : "neutral"}
                  />
                </TableCell>
              ) : null}
              <TableCell>
                <Button size="small" onClick={() => onView(product.productId)}>
                  View
                </Button>
                {admin ? (
                  <Button
                    size="small"
                    onClick={() => onEdit(product.productId)}
                  >
                    Edit
                  </Button>
                ) : null}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </DataTableContainer>
  );
}
