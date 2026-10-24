package com.talentfarm.sicms.report.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CustomerReportResponse(long policyCount, long activePolicyCount, long expiredPolicyCount,
                                     long claimCount, List<ClaimStatusCount> claimsByStatus,
                                     long completedPaymentCount, BigDecimal premiumPaid, BigDecimal settlementTotal) { }