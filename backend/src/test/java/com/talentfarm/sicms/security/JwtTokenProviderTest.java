package com.talentfarm.sicms.security;

import com.talentfarm.sicms.security.jwt.JwtProperties;
import com.talentfarm.sicms.security.jwt.JwtTokenProvider;
import com.talentfarm.sicms.security.service.CustomUserDetails;
import com.talentfarm.sicms.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.mockito.Mockito;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private static final String SECRET = "test-only-secret-that-is-long-enough-for-hs256";

    @Test
    void generatesAndParsesMinimalExpiringClaims() {
        User user = persistedUser();
        CustomUserDetails details = CustomUserDetails.from(user);
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofMinutes(15)));

        var generated = provider.generateAccessToken(details);
        var claims = provider.parseAndValidate(generated.value());

        assertEquals("jwt@example.test", claims.getSubject());
        assertEquals(user.getId(), provider.userId(claims));
        assertTrue(generated.expiresAt().isAfter(claims.getIssuedAt().toInstant()));
        assertTrue(claims.get("roles", java.util.List.class).isEmpty());
    }

    @Test
    void rejectsMalformedAndTamperedTokens() {
        User user = persistedUser();
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, Duration.ofMinutes(15)));
        String token = provider.generateAccessToken(CustomUserDetails.from(user)).value();

        assertThrows(RuntimeException.class, () -> provider.parseAndValidate("not-a-jwt"));
        assertThrows(RuntimeException.class, () -> provider.parseAndValidate(token.substring(0, token.length() - 1) + "x"));
    }

    @Test
    void rejectsExpiredTokens() {
        User user = persistedUser();
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(SECRET, Duration.ZERO));
        String token = provider.generateAccessToken(CustomUserDetails.from(user)).value();

        assertThrows(RuntimeException.class, () -> provider.parseAndValidate(token));
    }

    private User persistedUser() {
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(java.util.UUID.randomUUID());
        when(user.getEmail()).thenReturn("jwt@example.test");
        when(user.getPasswordHash()).thenReturn("hash");
        when(user.getAccountStatus()).thenReturn(com.talentfarm.sicms.user.domain.AccountStatus.ACTIVE);
        when(user.getRoles()).thenReturn(java.util.Set.of());
        return user;
    }
}