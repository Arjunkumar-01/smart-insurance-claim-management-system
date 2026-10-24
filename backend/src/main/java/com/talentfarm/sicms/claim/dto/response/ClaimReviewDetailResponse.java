package com.talentfarm.sicms.claim.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimStatus;
import com.talentfarm.sicms.claim.domain.ClaimType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClaimReviewDetailResponse(UUID claimId, String claimNumber, BigDecimal claimAmount,
                                        BigDecimal coverageAmount, ClaimType claimType, LocalDate incidentDate, String description,
                                        ClaimStatus status, String policyNumber, Instant submittedAt,
                                        List<ClaimDocumentResponse> documents, List<ClaimReviewResponse> reviews,
                                        List<ClaimStatusHistoryResponse> history) { }