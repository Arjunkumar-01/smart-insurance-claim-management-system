package com.talentfarm.sicms.auth.dto.response;

import com.talentfarm.sicms.user.domain.AccountStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserSummary(UUID userId, String email, String firstName, String lastName,
                          Set<String> roles, AccountStatus accountStatus, Instant createdAt) {
}