package com.talentfarm.sicms.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerProfileRequest(
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,30}$", message = "Phone number must be valid") String phoneNumber,
        @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @NotBlank @Size(max = 200) String addressLine1,
        @Size(max = 200) String addressLine2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 100) String country
) {
}