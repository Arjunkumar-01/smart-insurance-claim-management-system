package com.talentfarm.sicms.policy.dto.request;

import jakarta.validation.constraints.Size;

public record RequestPolicyCancellationRequest(@Size(max = 1000) String reason) { }