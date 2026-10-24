package com.talentfarm.sicms.policy.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DecidePolicyCancellationRequest(@NotBlank String status, String decisionComment) { }