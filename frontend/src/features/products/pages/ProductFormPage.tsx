import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert } from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { productApi, productKeys } from "../api/product-api";
import { ProductForm } from "../components/ProductForm";
import type {
  CreateProductRequest,
  UpdateProductRequest,
} from "../types/product-types";
import type { ProductFormValues } from "../schemas/product-schema";

export function ProductFormPage({ editing = false }: { editing?: boolean }) {
  const { productId = "" } = useParams();
  const navigate = useNavigate();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const detail = useQuery({
    queryKey: productKeys.detail(productId),
    queryFn: () => productApi.get(productId),
    enabled: editing,
  });
  const mutation = useMutation({
    mutationFn: (payload: CreateProductRequest | UpdateProductRequest) =>
      editing
        ? productApi.update(productId, payload as UpdateProductRequest)
        : productApi.create(payload as CreateProductRequest),
    onSuccess: async (product) => {
      await client.setQueryData(productKeys.detail(product.productId), product);
      await client.invalidateQueries({ queryKey: productKeys.all });
      notify(editing ? "Product updated." : "Product created.", "success");
      navigate(`/app/products/${product.productId}`, { replace: true });
    },
  });
  if (editing && detail.isPending) return <SkeletonState rows={7} />;
  if (editing && (detail.error || !detail.data))
    return (
      <ErrorState
        title="Product unavailable"
        message="The product could not be loaded for editing."
        onRetry={() => void detail.refetch()}
      />
    );
  return (
    <>
      <PageHeader
        eyebrow="Product management"
        title={editing ? "Edit Product" : "Create Product"}
        description={
          editing
            ? "Update product configuration and visibility."
            : "Add an insurance product to the catalog."
        }
      />
      {mutation.error ? (
        <Alert severity="error" sx={{ mb: 3 }}>
          {mutation.error instanceof Error
            ? mutation.error.message
            : "Unable to save product."}
        </Alert>
      ) : null}
      <ProductForm
        product={detail.data}
        pending={mutation.isPending}
        onCancel={() =>
          navigate(editing ? `/app/products/${productId}` : "/app/products")
        }
        onSubmit={(values: ProductFormValues) => {
          if (editing && values.status) {
            mutation.mutate({
              name: values.name,
              description: values.description,
              category: values.category,
              coverageDescription: values.coverageDescription,
              coverageAmount: values.coverageAmount,
              premiumAmount: values.premiumAmount,
              tenureMonths: values.tenureMonths,
              status: values.status,
            } satisfies UpdateProductRequest);
          } else if (!editing && values.productCode) {
            mutation.mutate({
              productCode: values.productCode,
              name: values.name,
              description: values.description,
              category: values.category,
              coverageDescription: values.coverageDescription,
              coverageAmount: values.coverageAmount,
              premiumAmount: values.premiumAmount,
              tenureMonths: values.tenureMonths,
            } satisfies CreateProductRequest);
          }
        }}
      />
    </>
  );
}
