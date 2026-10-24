package com.talentfarm.sicms.claim.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimSettlementResponse(UUID settlementId, String settlementNumber, UUID claimId,
                                      String claimNumber, BigDecimal settlementAmount, Instant settledAt) { }