export interface PolicyStatusCount {
  status: "ACTIVE" | "EXPIRED" | "CANCELLED";
  count: number;
}
export interface ClaimStatusCount {
  status: string;
  count: number;
}
export interface AdminOverview {
  customerCount: number;
  policiesByStatus: PolicyStatusCount[];
  claimsByStatus: ClaimStatusCount[];
  totalClaims: number;
  approvedClaims: number;
  rejectedClaims: number;
  pendingClaims: number;
  premiumCollected: number;
  settlementsPaid: number;
  claimsRatioPercent: number;
}
export interface MonthlyRevenue {
  year: number;
  month: number;
  paymentCount: number;
  completedPremium: number;
}
export interface ProductPerformance {
  productId: string;
  productCode: string;
  productName: string;
  policyCount: number;
  claimCount: number;
  premiumCollected: number;
  settlementsPaid: number;
}
export interface CustomerReport {
  policyCount: number;
  activePolicyCount: number;
  expiredPolicyCount: number;
  claimCount: number;
  claimsByStatus: Array<{ status: string; count: number }>;
  completedPaymentCount: number;
  premiumPaid: number;
  settlementTotal: number;
}
export interface SettlementTrend {
  year: number;
  month: number;
  settlementCount: number;
  settlementAmount: number;
}
