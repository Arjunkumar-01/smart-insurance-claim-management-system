package com.talentfarm.sicms.customer.dto.response;

import com.talentfarm.sicms.customer.domain.KycStatus;

import java.time.Instant;
import java.util.UUID;

public record CustomerKycResponse(UUID kycId, String identityType, String maskedIdentityNumber,
                                  KycStatus kycStatus, Instant createdAt, Instant updatedAt) {
}