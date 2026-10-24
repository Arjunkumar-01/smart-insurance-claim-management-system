package com.talentfarm.sicms.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerKycRequest(
        @NotBlank @Size(max = 50) String identityType,
        @NotBlank @Size(max = 100) String identityNumber
) {
}