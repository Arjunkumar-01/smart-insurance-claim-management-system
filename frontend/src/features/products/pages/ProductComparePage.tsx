import { useQuery } from "@tanstack/react-query";
import { Alert, Button } from "@mui/material";
import { Link, useSearchParams } from "react-router-dom";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { ProductComparison } from "../components/ProductComparison";
import { productApi, productKeys } from "../api/product-api";

export function ProductComparePage() {
  const [params] = useSearchParams();
  const ids = (params.get("ids") ?? "").split(",").filter(Boolean);
  const valid =
    ids.length >= 2 && ids.length <= 4 && new Set(ids).size === ids.length;
  const query = useQuery({
    queryKey: productKeys.compare(ids),
    queryFn: () => productApi.compare(ids),
    enabled: valid,
  });
  return (
    <>
      <PageHeader
        eyebrow="Product discovery"
        title="Compare Products"
        description="Review product details side by side without recommendation scoring."
        actions={
          <Button component={Link} to="/app/products">
            Back to products
          </Button>
        }
      />
      {!valid ? (
        <Alert severity="info">
          Select between two and four unique products from the product list.
        </Alert>
      ) : query.isPending ? (
        <SkeletonState rows={5} />
      ) : query.error ? (
        <ErrorState
          title="Comparison unavailable"
          message="One or more selected products are unavailable."
          onRetry={() => void query.refetch()}
        />
      ) : query.data ? (
        <ProductComparison products={query.data.products} />
      ) : null}
    </>
  );
}
