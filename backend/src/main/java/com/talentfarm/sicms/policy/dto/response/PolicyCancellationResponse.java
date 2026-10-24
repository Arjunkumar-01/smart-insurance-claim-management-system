package com.talentfarm.sicms.policy.dto.response;

import com.talentfarm.sicms.policy.domain.CancellationStatus;

import java.time.Instant;
import java.util.UUID;

public record PolicyCancellationResponse(UUID requestId, UUID policyId, String policyNumber,
                                          CancellationStatus status, String reason, String decisionComment,
                                          Instant requestedAt, Instant decidedAt) { }