import { zodResolver } from "@hookform/resolvers/zod";
import {
  Box,
  Button,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@mui/material";
import { useForm } from "react-hook-form";
import {
  productFormSchema,
  type ProductFormValues,
} from "../schemas/product-schema";
import type { ProductResponse } from "../types/product-types";

export function ProductForm({
  product,
  pending,
  onSubmit,
  onCancel,
}: {
  product?: ProductResponse;
  pending?: boolean;
  onSubmit: (values: ProductFormValues) => void;
  onCancel: () => void;
}) {
  const editing = Boolean(product);
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
  } = useForm<ProductFormValues>({
    resolver: zodResolver(productFormSchema),
    defaultValues: product
      ? {
          productCode: product.productCode,
          name: product.name,
          description: product.description ?? "",
          category: product.category,
          coverageDescription: product.coverageDescription,
          coverageAmount: product.coverageAmount,
          premiumAmount: product.premiumAmount,
          tenureMonths: product.tenureMonths,
          status: product.status,
        }
      : {
          productCode: "",
          name: "",
          description: "",
          category: "HEALTH",
          coverageDescription: "",
          coverageAmount: 0,
          premiumAmount: 0,
          tenureMonths: 12,
          status: undefined,
        },
  });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2, maxWidth: 760 }}
    >
      <TextField
        label="Product code"
        {...register("productCode")}
        disabled={editing}
        error={Boolean(errors.productCode)}
        helperText={
          editing ? "Product code is immutable." : errors.productCode?.message
        }
      />{" "}
      <TextField
        label="Name"
        {...register("name")}
        error={Boolean(errors.name)}
        helperText={errors.name?.message}
      />
      <TextField
        label="Description"
        multiline
        minRows={3}
        {...register("description")}
        error={Boolean(errors.description)}
        helperText={errors.description?.message}
      />
      <FormControl>
        <InputLabel>Category</InputLabel>
        <Select
          label="Category"
          defaultValue={product?.category ?? "HEALTH"}
          {...register("category")}
        >
          <MenuItem value="HEALTH">Health</MenuItem>
          <MenuItem value="LIFE">Life</MenuItem>
          <MenuItem value="VEHICLE">Vehicle</MenuItem>
          <MenuItem value="TRAVEL">Travel</MenuItem>
        </Select>
      </FormControl>
      <TextField
        label="Coverage description"
        multiline
        minRows={3}
        {...register("coverageDescription")}
        error={Boolean(errors.coverageDescription)}
        helperText={errors.coverageDescription?.message}
      />
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" },
          gap: 2,
        }}
      >
        <TextField
          label="Coverage amount"
          type="number"
          inputProps={{ min: 0.01, step: "0.01" }}
          {...register("coverageAmount")}
          error={Boolean(errors.coverageAmount)}
          helperText={errors.coverageAmount?.message}
        />
        <TextField
          label="Premium amount"
          type="number"
          inputProps={{ min: 0.01, step: "0.01" }}
          {...register("premiumAmount")}
          error={Boolean(errors.premiumAmount)}
          helperText={errors.premiumAmount?.message}
        />
        <TextField
          label="Policy tenure (months)"
          type="number"
          inputProps={{ min: 1, step: 1 }}
          {...register("tenureMonths")}
          error={Boolean(errors.tenureMonths)}
          helperText={errors.tenureMonths?.message}
        />
      </Box>
      {editing ? (
        <FormControl>
          <InputLabel>Status</InputLabel>
          <Select
            label="Status"
            defaultValue={product?.status}
            {...register("status")}
          >
            <MenuItem value="ACTIVE">Active</MenuItem>
            <MenuItem value="INACTIVE">Inactive</MenuItem>
          </Select>
        </FormControl>
      ) : null}
      <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 1 }}>
        <Button type="button" onClick={onCancel} disabled={pending}>
          Cancel
        </Button>
        <Button
          type="submit"
          variant="contained"
          disabled={pending || (editing && !isDirty)}
        >
          {pending ? "Saving..." : editing ? "Save changes" : "Create product"}
        </Button>
      </Box>
    </Box>
  );
}
