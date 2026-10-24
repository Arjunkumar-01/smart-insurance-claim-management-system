package com.talentfarm.sicms.claim.dto.request;

import com.talentfarm.sicms.claim.domain.ClaimType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubmitClaimRequest(
        @NotNull UUID policyId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal claimAmount,
        @NotNull ClaimType claimType,
        @NotNull LocalDate incidentDate,
        @NotBlank @Size(max = 2000) String description
) { }