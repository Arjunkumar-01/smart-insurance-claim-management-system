package com.talentfarm.sicms.report.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPerformanceResponse(UUID productId, String productCode, String productName,
                                         long policyCount, long claimCount, BigDecimal premiumCollected,
                                         BigDecimal settlementsPaid) { }