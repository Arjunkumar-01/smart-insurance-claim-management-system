package com.talentfarm.sicms.policy.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record PurchasePolicyRequest(
        @NotNull UUID productId,
        @NotNull @FutureOrPresent LocalDate startDate,
        @Valid UpdateNomineeRequest nominee
) { }