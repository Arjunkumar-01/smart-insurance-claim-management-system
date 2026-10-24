package com.talentfarm.sicms.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNomineeRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 80) String relationship,
        @Pattern(regexp = "^[+0-9() .-]{7,30}$", message = "Contact number must be valid") String contactNumber
) { }