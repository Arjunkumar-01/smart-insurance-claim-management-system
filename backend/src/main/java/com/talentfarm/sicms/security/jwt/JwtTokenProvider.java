package com.talentfarm.sicms.security.jwt;

import com.talentfarm.sicms.security.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        if (properties.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public GeneratedToken generateAccessToken(CustomUserDetails userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.accessTokenExpiration());
        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId().toString())
                .claim("roles", userDetails.getAuthorities().stream().map(authority -> authority.getAuthority()).toList())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new GeneratedToken(token, expiresAt);
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public UUID userId(Claims claims) {
        return UUID.fromString(claims.get("userId", String.class));
    }

    public record GeneratedToken(String value, Instant expiresAt) {
    }
}