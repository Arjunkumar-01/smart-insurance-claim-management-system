import { lazy, Suspense, type ReactNode } from "react";
import { createBrowserRouter, type RouteObject } from "react-router-dom";
import { ProtectedRoute } from "../auth/ProtectedRoute";
import { RoleRoute } from "../auth/RoleRoute";
import { ApplicationLayout } from "../components/layout/ApplicationLayout";
import { LoadingState } from "../components/feedback/LoadingState";
import { ForbiddenPage } from "../routes/ForbiddenPage";
import { RootRoute } from "../routes/RootRoute";

const LoginPage = lazy(() =>
  import("../features/authentication/pages/LoginPage").then((module) => ({
    default: module.LoginPage,
  })),
);
const RegisterPage = lazy(() =>
  import("../features/authentication/pages/RegisterPage").then((module) => ({
    default: module.RegisterPage,
  })),
);
const CustomerDashboardPage = lazy(() =>
  import("../features/dashboard/pages/RoleDashboards").then((module) => ({
    default: module.CustomerDashboardPage,
  })),
);
const AgentDashboardPage = lazy(() =>
  import("../features/dashboard/pages/RoleDashboards").then((module) => ({
    default: module.AgentDashboardPage,
  })),
);
const ClaimsOfficerDashboardPage = lazy(() =>
  import("../features/dashboard/pages/RoleDashboards").then((module) => ({
    default: module.ClaimsOfficerDashboardPage,
  })),
);
const AdministratorDashboardPage = lazy(() =>
  import("../features/dashboard/pages/RoleDashboards").then((module) => ({
    default: module.AdministratorDashboardPage,
  })),
);
const CustomerProfilePage = lazy(() =>
  import("../features/customers/pages/CustomerProfilePage").then((module) => ({
    default: module.CustomerProfilePage,
  })),
);
const CustomerKycPage = lazy(() =>
  import("../features/customers/pages/CustomerKycPage").then((module) => ({
    default: module.CustomerKycPage,
  })),
);
const ProductListPage = lazy(() =>
  import("../features/products/pages/ProductListPage").then((module) => ({
    default: module.ProductListPage,
  })),
);
const ProductDetailPage = lazy(() =>
  import("../features/products/pages/ProductDetailPage").then((module) => ({
    default: module.ProductDetailPage,
  })),
);
const ProductComparePage = lazy(() =>
  import("../features/products/pages/ProductComparePage").then((module) => ({
    default: module.ProductComparePage,
  })),
);
const ProductFormPage = lazy(() =>
  import("../features/products/pages/ProductFormPage").then((module) => ({
    default: module.ProductFormPage,
  })),
);
const PolicyPurchasePage = lazy(() =>
  import("../features/policies/pages/PolicyPurchasePage").then((module) => ({
    default: module.PolicyPurchasePage,
  })),
);
const MyPoliciesPage = lazy(() =>
  import("../features/policies/pages/MyPoliciesPage").then((module) => ({
    default: module.MyPoliciesPage,
  })),
);
const PolicyDetailPage = lazy(() =>
  import("../features/policies/pages/PolicyDetailPage").then((module) => ({
    default: module.PolicyDetailPage,
  })),
);
const AdminPoliciesPage = lazy(() =>
  import("../features/policies/pages/AdminPoliciesPage").then((module) => ({
    default: module.AdminPoliciesPage,
  })),
);
const PolicyCancellationsPage = lazy(() =>
  import("../features/policies/pages/PolicyCancellationsPage").then(
    (module) => ({ default: module.PolicyCancellationsPage }),
  ),
);
const MyPaymentsPage = lazy(() =>
  import("../features/payments/pages/MyPaymentsPage").then((module) => ({
    default: module.MyPaymentsPage,
  })),
);
const MakePaymentPage = lazy(() =>
  import("../features/payments/pages/MakePaymentPage").then((module) => ({
    default: module.MakePaymentPage,
  })),
);
const PaymentDetailPage = lazy(() =>
  import("../features/payments/pages/PaymentDetailPage").then((module) => ({
    default: module.PaymentDetailPage,
  })),
);
const InvoicePage = lazy(() =>
  import("../features/payments/pages/InvoicePage").then((module) => ({
    default: module.InvoicePage,
  })),
);
const ReceiptPage = lazy(() =>
  import("../features/payments/pages/ReceiptPage").then((module) => ({
    default: module.ReceiptPage,
  })),
);
const MyClaimsPage = lazy(() =>
  import("../features/claims/pages/MyClaimsPage").then((module) => ({
    default: module.MyClaimsPage,
  })),
);
const ClaimSubmitPage = lazy(() =>
  import("../features/claims/pages/ClaimSubmitPage").then((module) => ({
    default: module.ClaimSubmitPage,
  })),
);
const ClaimDetailPage = lazy(() =>
  import("../features/claims/pages/ClaimDetailPage").then((module) => ({
    default: module.ClaimDetailPage,
  })),
);
const ClaimReviewQueuePage = lazy(() =>
  import("../features/claims/pages/ClaimReviewQueuePage").then((module) => ({
    default: module.ClaimReviewQueuePage,
  })),
);
const ClaimReviewDetailPage = lazy(() =>
  import("../features/claims/pages/ClaimReviewDetailPage").then((module) => ({
    default: module.ClaimReviewDetailPage,
  })),
);
const AdminReportsPage = lazy(() =>
  import("../features/reports/pages/AdminReportsPage").then((module) => ({
    default: module.AdminReportsPage,
  })),
);
const MonthlyRevenuePage = lazy(() =>
  import("../features/reports/pages/MonthlyRevenuePage").then((module) => ({
    default: module.MonthlyRevenuePage,
  })),
);
const ProductPerformancePage = lazy(() =>
  import("../features/reports/pages/ProductPerformancePage").then((module) => ({
    default: module.ProductPerformancePage,
  })),
);
const AdminUsersPage = lazy(() =>
  import("../features/administration/pages/AdminUsersPage").then((module) => ({
    default: module.AdminUsersPage,
  })),
);
const AuditLogsPage = lazy(() =>
  import("../features/audit/pages/AuditLogsPage").then((module) => ({
    default: module.AuditLogsPage,
  })),
);
const NotFoundPage = lazy(() =>
  import("../routes/NotFoundPage").then((module) => ({
    default: module.NotFoundPage,
  })),
);

function lazyElement(element: ReactNode) {
  return <Suspense fallback={<LoadingState />}>{element}</Suspense>;
}

export const appRoutes: RouteObject[] = [
  { path: "/", element: <RootRoute /> },
  { path: "/login", element: lazyElement(<LoginPage />) },
  { path: "/register", element: lazyElement(<RegisterPage />) },
  { path: "/forbidden", element: <ForbiddenPage /> },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <ApplicationLayout />,
        children: [
          { path: "/app", element: <RootRoute /> },
          {
            element: (
              <RoleRoute
                allowedRoles={["CUSTOMER", "AGENT", "ADMINISTRATOR"]}
              />
            ),
            children: [
              {
                path: "/app/products",
                element: lazyElement(<ProductListPage />),
              },
              {
                path: "/app/products/compare",
                element: lazyElement(<ProductComparePage />),
              },
              {
                path: "/app/products/:productId",
                element: lazyElement(<ProductDetailPage />),
              },
            ],
          },
          {
            element: <RoleRoute allowedRoles={["ADMINISTRATOR"]} />,
            children: [
              {
                path: "/app/administrator",
                element: lazyElement(<AdministratorDashboardPage />),
              },
              {
                path: "/app/products/new",
                element: lazyElement(<ProductFormPage />),
              },
              {
                path: "/app/products/:productId/edit",
                element: lazyElement(<ProductFormPage editing />),
              },
              {
                path: "/app/admin/reports",
                element: lazyElement(<AdminReportsPage />),
              },
              {
                path: "/app/admin/reports/revenue",
                element: lazyElement(<MonthlyRevenuePage />),
              },
              {
                path: "/app/admin/reports/products",
                element: lazyElement(<ProductPerformancePage />),
              },
              {
                path: "/app/admin/users",
                element: lazyElement(<AdminUsersPage />),
              },
              {
                path: "/app/admin/audit",
                element: lazyElement(<AuditLogsPage />),
              },
              {
                path: "/app/admin/policies",
                element: lazyElement(<AdminPoliciesPage />),
              },
              {
                path: "/app/admin/policy-cancellations",
                element: lazyElement(<PolicyCancellationsPage />),
              },
            ],
          },
          {
            element: <RoleRoute allowedRoles={["CUSTOMER"]} />,
            children: [
              {
                path: "/app/customer",
                element: lazyElement(<CustomerDashboardPage />),
              },
              {
                path: "/app/customer/profile",
                element: lazyElement(<CustomerProfilePage />),
              },
              {
                path: "/app/customer/kyc",
                element: lazyElement(<CustomerKycPage />),
              },
              {
                path: "/app/products/:productId/purchase",
                element: lazyElement(<PolicyPurchasePage />),
              },
              {
                path: "/app/policies",
                element: lazyElement(<MyPoliciesPage />),
              },
              {
                path: "/app/policies/:policyId",
                element: lazyElement(<PolicyDetailPage />),
              },
              {
                path: "/app/policies/:policyId/payment",
                element: lazyElement(<MakePaymentPage />),
              },
              {
                path: "/app/payments",
                element: lazyElement(<MyPaymentsPage />),
              },
              {
                path: "/app/payments/:paymentId",
                element: lazyElement(<PaymentDetailPage />),
              },
              {
                path: "/app/payments/:paymentId/invoice",
                element: lazyElement(<InvoicePage />),
              },
              {
                path: "/app/payments/:paymentId/receipt",
                element: lazyElement(<ReceiptPage />),
              },
              { path: "/app/claims", element: lazyElement(<MyClaimsPage />) },
              {
                path: "/app/claims/new",
                element: lazyElement(<ClaimSubmitPage />),
              },
              {
                path: "/app/claims/:claimId",
                element: lazyElement(<ClaimDetailPage />),
              },
            ],
          },
          {
            element: <RoleRoute allowedRoles={["AGENT"]} />,
            children: [
              {
                path: "/app/agent",
                element: lazyElement(<AgentDashboardPage />),
              },
            ],
          },
          {
            element: <RoleRoute allowedRoles={["CLAIMS_OFFICER"]} />,
            children: [
              {
                path: "/app/claims-review",
                element: lazyElement(<ClaimReviewQueuePage />),
              },
              {
                path: "/app/claims-review/:claimId",
                element: lazyElement(<ClaimReviewDetailPage />),
              },
              {
                path: "/app/claims-officer",
                element: lazyElement(<ClaimsOfficerDashboardPage />),
              },
            ],
          },
        ],
      },
    ],
  },
  { path: "*", element: lazyElement(<NotFoundPage />) },
];

export const appRouter = createBrowserRouter(appRoutes);
