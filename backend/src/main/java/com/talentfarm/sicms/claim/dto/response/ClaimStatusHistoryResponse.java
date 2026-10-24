package com.talentfarm.sicms.claim.dto.response;

import com.talentfarm.sicms.claim.domain.ClaimStatus;

import java.time.Instant;
import java.util.UUID;

public record ClaimStatusHistoryResponse(UUID historyId, ClaimStatus fromStatus, ClaimStatus toStatus,
                                          String actor, String reason, Instant changedAt) { }