package com.talentfarm.sicms.claim.dto.request;

import jakarta.validation.constraints.Size;

public record ReviewDecisionRequest(@Size(max = 2000) String notes) { }