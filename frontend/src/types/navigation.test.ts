import { describe, expect, it } from "vitest";
import { getNavigationItems, roleLabels } from "./navigation";

describe("role-aware navigation", () => {
  it("exposes only real role destinations", () => {
    expect(getNavigationItems(["CUSTOMER"]).map((item) => item.route)).toEqual([
      "/app",
      "/app/customer/profile",
      "/app/customer/kyc",
      "/app/products",
      "/app/policies",
      "/app/payments",
      "/app/claims",
    ]);
    expect(
      getNavigationItems(["ADMINISTRATOR"]).map((item) => item.route),
    ).toEqual([
      "/app",
      "/app/products",
      "/app/admin/policies",
      "/app/admin/reports",
      "/app/admin/users",
      "/app/admin/audit",
    ]);
  });

  it("keeps human role labels centralized", () => {
    expect(roleLabels.CLAIMS_OFFICER).toBe("Claims Officer");
    expect(roleLabels.ADMINISTRATOR).toBe("Administrator");
    expect(JSON.stringify(roleLabels)).not.toContain("ROLE_");
  });
});
