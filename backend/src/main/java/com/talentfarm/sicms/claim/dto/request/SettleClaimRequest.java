package com.talentfarm.sicms.claim.dto.request;

import jakarta.validation.constraints.Size;

public record SettleClaimRequest(@Size(max = 1000) String notes) { }