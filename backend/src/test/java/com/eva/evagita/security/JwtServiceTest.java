package com.eva.evagita.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET =
            "EvaGitaJwtSecretKeyForDevelopmentOnly_ChangeThisToAStrongSecretKey_2026";

    private static final long EXPIRATION = 86400000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void shouldRejectExpiredJwt() {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(now.getTime() - 2000))
                .expiration(new Date(now.getTime() - 1000))
                .signWith(key)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken));
    }

    @Test
    void shouldRejectInvalidJwt() {
        String invalidToken = "this.is.not.a.valid.jwt";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void shouldAcceptValidJwt() {
        String token = jwtService.generateToken("testuser");

        assertTrue(jwtService.isTokenValid(token));
    }
}
