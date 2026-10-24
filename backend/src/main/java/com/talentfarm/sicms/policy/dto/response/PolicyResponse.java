package com.talentfarm.sicms.policy.dto.response;

import com.talentfarm.sicms.policy.domain.PolicyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyResponse(UUID policyId, String policyNumber, UUID productId, String productCode,
                             String productName, Integer productTenureMonths, BigDecimal premiumAmount, BigDecimal coverageAmount,
                             LocalDate startDate, LocalDate endDate, PolicyStatus status,
                             PolicyNomineeResponse nominee, Instant createdAt, Instant updatedAt) { }