package com.talentfarm.sicms.claim.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.claim.domain.ClaimType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ClaimResponse(UUID claimId, String claimNumber, UUID policyId, String policyNumber,
                            BigDecimal claimAmount, BigDecimal coverageAmount, ClaimType claimType, LocalDate incidentDate,
                            String description, ClaimStatus status, Instant submittedAt,
                            Instant createdAt, Instant updatedAt) { }