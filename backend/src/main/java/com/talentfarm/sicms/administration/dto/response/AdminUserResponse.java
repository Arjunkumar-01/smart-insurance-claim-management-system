package com.talentfarm.sicms.administration.dto.response;

import com.talentfarm.sicms.user.domain.AccountStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(UUID userId, String email, String firstName, String lastName,
                                Set<String> roles, AccountStatus accountStatus, Instant createdAt, Instant updatedAt) { }