package com.talentfarm.sicms.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 20) String productCode,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank String category,
        @NotBlank @Size(max = 2000) String coverageDescription,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal coverageAmount,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal premiumAmount,
        @NotNull @Positive Integer tenureMonths
) {
}