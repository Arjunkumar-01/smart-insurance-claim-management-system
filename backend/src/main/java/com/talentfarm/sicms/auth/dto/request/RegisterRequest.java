package com.talentfarm.sicms.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "First name is required") @Size(max = 100, message = "First name must not exceed 100 characters") String firstName,
        @NotBlank(message = "Last name is required") @Size(max = 100, message = "Last name must not exceed 100 characters") String lastName,
        @NotBlank(message = "Email is required") @Pattern(regexp = "^\\s*[^\\s@]+@[^\\s@]+\\.[^\\s@]+\\s*$", message = "Email must be valid") @Size(max = 254, message = "Email must not exceed 254 characters") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$", message = "Password must contain upper, lower, digit, and special characters") String password,
        @NotBlank(message = "Password confirmation is required") String confirmPassword
) {

    @AssertTrue(message = "Password and confirmation must match")
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}