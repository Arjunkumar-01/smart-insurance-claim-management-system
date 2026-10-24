package com.talentfarm.sicms.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Pattern(regexp = "^\\s*[^\\s@]+@[^\\s@]+\\.[^\\s@]+\\s*$", message = "Email must be valid") String email,
        @NotBlank(message = "Password is required") String password
) {
}