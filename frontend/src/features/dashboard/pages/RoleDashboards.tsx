import { useQuery } from "@tanstack/react-query";
import { Box, Button, Paper, Typography } from "@mui/material";
import { Link } from "react-router-dom";
import { ApiError } from "../../../api/api-error";
import { PageHeader } from "../../../components/layout/PageHeader";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { StatusBadge } from "../../../components/layout/StatusBadge";
import { formatAmount } from "../../products/types/product-formatters";
import { reportApi, reportKeys } from "../../reports/api/report-api";
import { claimReviewApi, reviewKeys } from "../../claims/api/claim-review-api";
import { productApi, productKeys } from "../../products/api/product-api";

function Metric({
  label,
  value,
  supporting,
}: {
  label: string;
  value: string | number;
  supporting?: string;
}) {
  return (
    <Paper elevation={0} sx={{ p: 2, border: 1, borderColor: "divider" }}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="h6" sx={{ mt: 1 }}>
        {value}
      </Typography>
      {supporting ? (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          {supporting}
        </Typography>
      ) : null}
    </Paper>
  );
}
function Grid({ children }: { children: React.ReactNode }) {
  return (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr", md: "repeat(4, 1fr)" },
        gap: 2,
        mb: 4,
      }}
    >
      {children}
    </Box>
  );
}

export function CustomerDashboardPage() {
  const query = useQuery({
    queryKey: reportKeys.customer(),
    queryFn: reportApi.customer,
  });
  if (query.isPending) return <SkeletonState rows={5} />;
  if (query.error instanceof ApiError && query.error.kind === "not-found")
    return (
      <>
        <PageHeader
          eyebrow="Customer"
          title="Your insurance overview"
          description="Manage your insurance services from one secure workspace."
        />
        <Paper elevation={0} sx={{ p: 4, border: 1, borderColor: "divider" }}>
          <Typography variant="h6" gutterBottom>
            Complete your customer profile
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 2 }}>
            We need a few more details before we can show your insurance
            overview.
          </Typography>
          <Button
            component={Link}
            to="/app/customer/profile"
            variant="contained"
          >
            Complete profile
          </Button>
        </Paper>
      </>
    );
  if (query.error || !query.data)
    return (
      <ErrorState
        message="Unable to load your insurance overview."
        onRetry={() => void query.refetch()}
      />
    );
  const data = query.data;
  return (
    <>
      <PageHeader
        eyebrow="Customer"
        title="Your insurance overview"
        description="Manage your insurance services from one secure workspace."
      />
      <Grid>
        <Metric
          label="Active policies"
          value={data.activePolicyCount}
          supporting={`${data.expiredPolicyCount} expired`}
        />
        <Metric label="Claims submitted" value={data.claimCount} />
        <Metric label="Premium paid" value={formatAmount(data.premiumPaid)} />
        <Metric
          label="Settlements"
          value={formatAmount(data.settlementTotal)}
        />
      </Grid>
      <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
        <Button component={Link} to="/app/products" variant="contained">
          Browse products
        </Button>
        <Button component={Link} to="/app/policies" variant="outlined">
          View policies
        </Button>
        <Button component={Link} to="/app/claims" variant="outlined">
          View claim history
        </Button>
        <Button component={Link} to="/app/payments" variant="outlined">
          View payments
        </Button>
      </Box>
      <Paper
        elevation={0}
        sx={{ p: 3, mt: 4, border: 1, borderColor: "divider" }}
      >
        <Typography variant="h6" sx={{ mb: 2 }}>
          Claims overview
        </Typography>
        {data.claimsByStatus.map((row) => (
          <Box
            key={row.status}
            sx={{ display: "flex", justifyContent: "space-between", py: 1 }}
          >
            <StatusBadge label={row.status} tone="neutral" />
            <Typography>{row.count}</Typography>
          </Box>
        ))}
      </Paper>
    </>
  );
}
export function AgentDashboardPage() {
  const query = useQuery({
    queryKey: productKeys.list({ page: 0, size: 20, sort: "createdAt,desc" }),
    queryFn: () =>
      productApi.list({ page: 0, size: 20, sort: "createdAt,desc" }),
  });
  return (
    <>
      <PageHeader
        eyebrow="Agent"
        title="Product discovery workspace"
        description="Explore the active insurance catalog available to Agent users."
        actions={
          <Button component={Link} to="/app/products" variant="contained">
            Browse products
          </Button>
        }
      />
      {query.isPending ? (
        <SkeletonState rows={4} />
      ) : query.error ? (
        <ErrorState
          message="Unable to load the product catalog."
          onRetry={() => void query.refetch()}
        />
      ) : (
        <>
          <Grid>
            <Metric
              label="Active catalog"
              value={
                query.data?.totalElements ?? query.data?.products.length ?? 0
              }
              supporting="Products available for discovery"
            />
            <Metric
              label="Workspace scope"
              value="Products"
              supporting="No policy or claim servicing access"
            />
          </Grid>
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", md: "repeat(3, 1fr)" },
              gap: 2,
            }}
          >
            {query.data?.products.slice(0, 3).map((product) => (
              <Paper
                key={product.productId}
                elevation={0}
                sx={{ p: 2.5, border: 1, borderColor: "divider" }}
              >
                <Typography variant="overline" color="text.secondary">
                  {product.category}
                </Typography>
                <Typography variant="h6">{product.name}</Typography>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mt: 1 }}
                >
                  {product.tenureMonths} months · Premium{" "}
                  {formatAmount(product.premiumAmount)}
                </Typography>
              </Paper>
            ))}
          </Box>
        </>
      )}
    </>
  );
}
export function ClaimsOfficerDashboardPage() {
  const query = useQuery({
    queryKey: reviewKeys.queue(undefined, 0),
    queryFn: () => claimReviewApi.queue(undefined, 0),
  });
  const submitted = useQuery({
    queryKey: reviewKeys.queue("SUBMITTED", 0),
    queryFn: () => claimReviewApi.queue("SUBMITTED", 0),
  });
  const underReview = useQuery({
    queryKey: reviewKeys.queue("UNDER_REVIEW", 0),
    queryFn: () => claimReviewApi.queue("UNDER_REVIEW", 0),
  });
  const documentsRequired = useQuery({
    queryKey: reviewKeys.queue("DOCUMENTS_REQUIRED", 0),
    queryFn: () => claimReviewApi.queue("DOCUMENTS_REQUIRED", 0),
  });
  return (
    <>
      <PageHeader
        eyebrow="Claims Officer"
        title="Claims operations workspace"
        description="Review submitted claims through controlled workflow actions."
        actions={
          <Button component={Link} to="/app/claims-review" variant="contained">
            Open claims queue
          </Button>
        }
      />
      {query.isPending ? (
        <SkeletonState rows={4} />
      ) : query.error ? (
        <ErrorState
          message="Unable to load the review queue."
          onRetry={() => void query.refetch()}
        />
      ) : (
        <Grid>
          <Metric
            label="Claims requiring review"
            value={query.data?.totalElements ?? 0}
            supporting="Backend queue total"
          />
          <Metric
            label="Submitted"
            value={submitted.data?.totalElements ?? 0}
            supporting="Awaiting review start"
          />
          <Metric
            label="Under review"
            value={underReview.data?.totalElements ?? 0}
            supporting="Active verification"
          />
          <Metric
            label="Documents required"
            value={documentsRequired.data?.totalElements ?? 0}
            supporting="Customer evidence required"
          />
        </Grid>
      )}
    </>
  );
}
export function AdministratorDashboardPage() {
  const query = useQuery({
    queryKey: reportKeys.overview(),
    queryFn: reportApi.overview,
  });
  return (
    <>
      <PageHeader
        eyebrow="Administrator"
        title="Operational oversight"
        description="Monitor read-only operational and financial measures."
        actions={
          <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
            <Button
              component={Link}
              to="/app/admin/reports"
              variant="contained"
            >
              Reporting
            </Button>
            <Button component={Link} to="/app/admin/users" variant="outlined">
              Users
            </Button>
            <Button component={Link} to="/app/admin/audit" variant="outlined">
              Audit
            </Button>
          </Box>
        }
      />
      {query.isPending ? (
        <SkeletonState rows={5} />
      ) : query.error ? (
        <ErrorState
          message="Unable to load administrator overview."
          onRetry={() => void query.refetch()}
        />
      ) : (
        <Grid>
          <Metric label="Customers" value={query.data?.customerCount ?? 0} />
          <Metric
            label="Premium collected"
            value={formatAmount(query.data?.premiumCollected ?? 0)}
          />
          <Metric
            label="Settlements paid"
            value={formatAmount(query.data?.settlementsPaid ?? 0)}
          />
          <Metric
            label="Claims ratio"
            value={`${(query.data?.claimsRatioPercent ?? 0).toFixed(2)}%`}
          />
          <Metric
            label="Total claims"
            value={query.data?.totalClaims ?? 0}
            supporting={`${query.data?.pendingClaims ?? 0} pending`}
          />
        </Grid>
      )}
    </>
  );
}
