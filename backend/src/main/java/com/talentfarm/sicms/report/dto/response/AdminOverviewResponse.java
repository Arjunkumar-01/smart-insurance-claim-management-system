package com.talentfarm.sicms.report.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.policy.domain.PolicyStatus;

import java.math.BigDecimal;
import java.util.List;

public record AdminOverviewResponse(long customerCount, List<PolicyStatusCount> policiesByStatus,
                                    List<ClaimStatusCount> claimsByStatus, long totalClaims, long approvedClaims,
                                    long rejectedClaims, long pendingClaims, BigDecimal premiumCollected,
                                    BigDecimal settlementsPaid, BigDecimal claimsRatioPercent) {
    public record PolicyStatusCount(PolicyStatus status, long count) { }
}