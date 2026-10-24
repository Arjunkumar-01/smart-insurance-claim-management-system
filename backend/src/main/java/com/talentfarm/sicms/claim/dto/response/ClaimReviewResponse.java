package com.talentfarm.sicms.claim.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimReviewAction;

import java.time.Instant;
import java.util.UUID;

public record ClaimReviewResponse(UUID reviewId, ClaimReviewAction action, String notes,
                                  UUID reviewerId, Instant createdAt) { }