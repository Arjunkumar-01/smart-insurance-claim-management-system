import {
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import type {
  ProductFilters as Filters,
  ProductCategory,
  ProductStatus,
} from "../types/product-types";

export function ProductFilters({
  filters,
  admin,
  onChange,
  onReset,
}: {
  filters: Filters;
  admin: boolean;
  onChange: (next: Partial<Filters>) => void;
  onReset: () => void;
}) {
  const active = Boolean(
    filters.name ||
    filters.productCode ||
    filters.category ||
    (admin && filters.status),
  );
  return (
    <Box
      component="section"
      aria-label="Product filters"
      sx={{
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          sm: "repeat(2, 1fr)",
          lg: "2fr 2fr 1fr 1fr auto",
        },
        gap: 1.5,
        alignItems: "center",
        mb: 3,
      }}
    >
      <TextField
        label="Search by name"
        value={filters.name ?? ""}
        onChange={(e) => onChange({ name: e.target.value, page: 0 })}
      />
      <TextField
        label="Product code"
        value={filters.productCode ?? ""}
        onChange={(e) => onChange({ productCode: e.target.value, page: 0 })}
      />
      <FormControl size="small">
        <InputLabel>Category</InputLabel>
        <Select
          label="Category"
          value={filters.category ?? ""}
          onChange={(e) =>
            onChange({
              category: (e.target.value || undefined) as
                ProductCategory | undefined,
              page: 0,
            })
          }
        >
          <MenuItem value="">All categories</MenuItem>
          <MenuItem value="HEALTH">Health</MenuItem>
          <MenuItem value="LIFE">Life</MenuItem>
          <MenuItem value="VEHICLE">Vehicle</MenuItem>
          <MenuItem value="TRAVEL">Travel</MenuItem>
        </Select>
      </FormControl>
      {admin ? (
        <FormControl size="small">
          <InputLabel>Status</InputLabel>
          <Select
            label="Status"
            value={filters.status ?? ""}
            onChange={(e) =>
              onChange({
                status: (e.target.value || undefined) as
                  ProductStatus | undefined,
                page: 0,
              })
            }
          >
            <MenuItem value="">All statuses</MenuItem>
            <MenuItem value="ACTIVE">Active</MenuItem>
            <MenuItem value="INACTIVE">Inactive</MenuItem>
          </Select>
        </FormControl>
      ) : null}
      {active ? <Button onClick={onReset}>Reset filters</Button> : null}
    </Box>
  );
}
