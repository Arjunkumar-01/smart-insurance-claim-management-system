package com.talentfarm.sicms.customer.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerProfileResponse(UUID customerId, String firstName, String lastName, String email,
                                      String phoneNumber, LocalDate dateOfBirth, String addressLine1,
                                      String addressLine2, String city, String state, String postalCode,
                                      String country, Instant createdAt, Instant updatedAt) {
}