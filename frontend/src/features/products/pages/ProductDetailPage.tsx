import { useQuery } from "@tanstack/react-query";
import { Box, Button, Paper, Typography } from "@mui/material";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../../auth/useAuth";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { PageHeader } from "../../../components/layout/PageHeader";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { productApi, productKeys } from "../api/product-api";
import { formatAmount } from "../types/product-formatters";

export function ProductDetailPage() {
  const { productId = "" } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const query = useQuery({
    queryKey: productKeys.detail(productId),
    queryFn: () => productApi.get(productId),
    enabled: Boolean(productId),
  });
  const admin = user?.roles.includes("ADMINISTRATOR") ?? false;
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error || !query.data)
    return (
      <ErrorState
        title="Product unavailable"
        message="This product was not found or is unavailable for your account."
        onRetry={() => void query.refetch()}
      />
    );
  const product = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Product detail"
        title={product.name}
        description={`Product code ${product.productCode}`}
        actions={
          <>
            <Button component={Link} to="/app/products">
              Back to products
            </Button>
            {admin ? (
              <Button
                variant="contained"
                onClick={() =>
                  navigate(`/app/products/${product.productId}/edit`)
                }
              >
                Edit product
              </Button>
            ) : null}
            {user?.roles.includes("CUSTOMER") && product.status === "ACTIVE" ? (
              <Button
                component={Link}
                to={`/app/products/${product.productId}/purchase`}
                variant="contained"
              >
                Purchase policy
              </Button>
            ) : null}
          </>
        }
      />
      <Paper
        elevation={0}
        sx={{ p: { xs: 2.5, sm: 4 }, border: 1, borderColor: "divider" }}
      >
        <Box sx={{ display: "grid", gap: 3 }}>
          <Box sx={{ display: "flex", gap: 1, alignItems: "center" }}>
            <Typography variant="h6">{product.category}</Typography>
            <StatusBadge
              label={product.status}
              tone={product.status === "ACTIVE" ? "success" : "neutral"}
            />
          </Box>
          <Typography color="text.secondary">
            {product.description || "No description provided."}
          </Typography>
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
              gap: 3,
            }}
          >
            <Box>
              <Typography variant="caption" color="text.secondary">
                Premium amount
              </Typography>
              <Typography variant="h6">
                {formatAmount(product.premiumAmount)}
              </Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Policy tenure
              </Typography>
              <Typography variant="h6">
                {product.tenureMonths} months
              </Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="text.secondary">
                Coverage amount
              </Typography>
              <Typography variant="h6">
                {formatAmount(product.coverageAmount)}
              </Typography>
            </Box>
          </Box>
          <Box>
            <Typography variant="subtitle2">Coverage description</Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              {product.coverageDescription}
            </Typography>
          </Box>
        </Box>
      </Paper>
    </>
  );
}
