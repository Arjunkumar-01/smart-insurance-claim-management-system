package com.talentfarm.sicms.auth.dto.response;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, UserSummary user) {
}