import { useQuery } from "@tanstack/react-query";
import { Alert, Box, Button, Pagination } from "@mui/material";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/useAuth";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { ProductFilters } from "../components/ProductFilters";
import { ProductTable } from "../components/ProductTable";
import { productApi, productKeys } from "../api/product-api";
import type {
  ProductCategory,
  ProductFilters as Filters,
  ProductStatus,
} from "../types/product-types";
import { useState } from "react";

export function ProductListPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const { notify } = useNotifications();
  const admin = user?.roles.includes("ADMINISTRATOR") ?? false;
  const filters: Filters = {
    name: params.get("name") || undefined,
    productCode: params.get("productCode") || undefined,
    category: (params.get("category") as ProductCategory) || undefined,
    status: admin
      ? (params.get("status") as ProductStatus) || undefined
      : undefined,
    page: Number(params.get("page") ?? 0),
    size: 20,
    sort: params.get("sort") || "createdAt,desc",
  };
  const query = useQuery({
    queryKey: productKeys.list(filters),
    queryFn: () => productApi.list(filters),
  });
  const [selected, setSelected] = useState<string[]>([]);
  const updateFilters = (next: Partial<Filters>) => {
    const nextParams = new URLSearchParams(params);
    Object.entries(next).forEach(([key, value]) => {
      if (value === undefined || value === "") nextParams.delete(key);
      else nextParams.set(key, String(value));
    });
    setParams(nextParams);
  };
  const toggle = (id: string) => {
    if (selected.includes(id))
      setSelected(selected.filter((value) => value !== id));
    else if (selected.length < 4) setSelected([...selected, id]);
    else notify("You can compare up to four products.", "warning");
  };
  return (
    <>
      <PageHeader
        eyebrow="Product discovery"
        title="Insurance Products"
        description="Search and compare available insurance products."
        actions={
          admin ? (
            <Button
              variant="contained"
              onClick={() => navigate("/app/products/new")}
            >
              Create product
            </Button>
          ) : undefined
        }
      />
      <ProductFilters
        filters={filters}
        admin={admin}
        onChange={updateFilters}
        onReset={() => setParams(new URLSearchParams({ page: "0" }))}
      />
      {selected.length > 0 ? (
        <Alert
          severity="info"
          action={
            <Button
              color="inherit"
              size="small"
              disabled={selected.length < 2}
              onClick={() =>
                navigate(`/app/products/compare?ids=${selected.join(",")}`)
              }
            >
              Compare {selected.length}
            </Button>
          }
          sx={{ mb: 2 }}
        >
          Select 2 to 4 products for comparison.
        </Alert>
      ) : null}
      {query.isPending ? (
        <SkeletonState rows={6} />
      ) : query.error ? (
        <ErrorState
          message="Unable to load products."
          onRetry={() => void query.refetch()}
        />
      ) : query.data.products.length === 0 ? (
        <EmptyState
          title={
            admin
              ? "No products match the current filters."
              : "No available products match the current filters."
          }
        />
      ) : (
        <>
          <ProductTable
            products={query.data.products}
            admin={admin}
            selected={selected}
            onSelect={toggle}
            onView={(id) => navigate(`/app/products/${id}`)}
            onEdit={(id) => navigate(`/app/products/${id}/edit`)}
            sort={filters.sort}
            onSort={(sort) => updateFilters({ sort, page: 0 })}
          />
          <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
            <Pagination
              page={query.data.page + 1}
              count={query.data.totalPages}
              onChange={(_, page) => updateFilters({ page: page - 1 })}
            />
          </Box>
        </>
      )}
    </>
  );
}
