package com.talentfarm.sicms.payment.dto.request;

import com.talentfarm.sicms.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MakePaymentRequest(@NotNull UUID policyId, @NotNull PaymentMethod paymentMethod) { }