import type { ApplicationRole } from "./roles";

export interface NavigationItem {
  id: string;
  label: string;
  route: string;
  allowedRoles: ApplicationRole[];
}

export const navigationItems: NavigationItem[] = [
  {
    id: "home",
    label: "Home",
    route: "/app",
    allowedRoles: ["CUSTOMER", "AGENT", "CLAIMS_OFFICER", "ADMINISTRATOR"],
  },
  {
    id: "profile",
    label: "Profile",
    route: "/app/customer/profile",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "kyc",
    label: "KYC",
    route: "/app/customer/kyc",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "products-customer",
    label: "Products",
    route: "/app/products",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "products-agent",
    label: "Products",
    route: "/app/products",
    allowedRoles: ["AGENT"],
  },
  {
    id: "products-admin",
    label: "Products",
    route: "/app/products",
    allowedRoles: ["ADMINISTRATOR"],
  },
  {
    id: "policies-customer",
    label: "Policies",
    route: "/app/policies",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "policies-admin",
    label: "Policies",
    route: "/app/admin/policies",
    allowedRoles: ["ADMINISTRATOR"],
  },
  {
    id: "payments-customer",
    label: "Payments",
    route: "/app/payments",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "claims-customer",
    label: "Claims",
    route: "/app/claims",
    allowedRoles: ["CUSTOMER"],
  },
  {
    id: "claims-review-officer",
    label: "Claims",
    route: "/app/claims-review",
    allowedRoles: ["CLAIMS_OFFICER"],
  },
  {
    id: "reporting-admin",
    label: "Reporting",
    route: "/app/admin/reports",
    allowedRoles: ["ADMINISTRATOR"],
  },
  {
    id: "users-admin",
    label: "Users",
    route: "/app/admin/users",
    allowedRoles: ["ADMINISTRATOR"],
  },
  {
    id: "audit-admin",
    label: "Audit",
    route: "/app/admin/audit",
    allowedRoles: ["ADMINISTRATOR"],
  },
];

export function getNavigationItems(roles: ApplicationRole[]): NavigationItem[] {
  return navigationItems.filter((item) =>
    item.allowedRoles.some((role) => roles.includes(role)),
  );
}

export const roleLabels: Record<ApplicationRole, string> = {
  CUSTOMER: "Customer",
  AGENT: "Agent",
  CLAIMS_OFFICER: "Claims Officer",
  ADMINISTRATOR: "Administrator",
};
