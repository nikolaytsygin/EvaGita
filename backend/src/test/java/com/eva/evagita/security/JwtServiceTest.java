package com.eva.evagita.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.eva.evagita.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        User user = mockUser(42L, "testuser", 0L);
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void shouldPreserveTokenVersionClaim() {
        User user = mockUser(42L, "testuser", 7L);
        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(7L, jwtService.extractTokenVersion(token));
    }

    @Test
    void shouldRejectTokenWithoutTokenVersion() {
        String token = signedTokenWithoutTokenVersion();

        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(token));
    }

    @Test
    void shouldRejectNullTokenVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(signedTokenWithTokenVersion(null)));
    }

    @Test
    void shouldAcceptZeroTokenVersion() {
        assertEquals(0L, jwtService.extractTokenVersion(signedTokenWithTokenVersion(0)));
    }

    @Test
    void shouldRejectNegativeTokenVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(signedTokenWithTokenVersion(-1)));
    }

    @Test
    void shouldRejectFractionalTokenVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(signedTokenWithTokenVersion(1.5)));
    }

    @Test
    void shouldRejectTokenVersionOutsideLongRange() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(
                        signedTokenWithTokenVersion(new BigInteger("9223372036854775808"))));
    }

    @Test
    void shouldRejectStringTokenVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractTokenVersion(signedTokenWithTokenVersion("1")));
    }

    @Test
    void shouldBindNewTokenToImmutableUserId() {
        User user = mockUser(42L, "testuser", 0L);

        String token = jwtService.generateToken(user);

        assertEquals(42L, jwtService.extractUserId(token));
    }

    @Test
    void shouldRejectMissingUserId() {
        String token = signedTokenWithoutUserId();

        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(token));
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(signedTokenWithUserId(null)));
    }

    @Test
    void shouldRejectZeroUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(signedTokenWithUserId(0)));
    }

    @Test
    void shouldRejectNegativeUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(signedTokenWithUserId(-1)));
    }

    @Test
    void shouldRejectFractionalUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(signedTokenWithUserId(42.5)));
    }

    @Test
    void shouldRejectUserIdOutsideLongRange() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(
                        signedTokenWithUserId(new BigInteger("9223372036854775808"))));
    }

    @Test
    void shouldRejectStringUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtService.extractUserId(signedTokenWithUserId("42")));
    }

    @Test
    void shouldRejectTokenGenerationForNullUser() {
        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken(null));
    }

    @Test
    void shouldRejectTokenGenerationForUnsavedUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(null);
        when(user.getUsername()).thenReturn("testuser");

        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken(user));
    }

    @Test
    void shouldRejectTokenGenerationForBlankUsername() {
        User user = mockUser(42L, " ", 0L);

        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken(user));
    }

    @Test
    void shouldRejectTokenGenerationForNullUsername() {
        User user = mockUser(42L, null, 0L);

        assertThrows(IllegalArgumentException.class, () -> jwtService.generateToken(user));
    }

    private String signedTokenWithoutUserId() {
        return Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey())
                .compact();
    }

    private String signedTokenWithoutTokenVersion() {
        return Jwts.builder()
                .subject("testuser")
                .claim("user_id", 42)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey())
                .compact();
    }

    private String signedTokenWithTokenVersion(Object tokenVersion) {
        return Jwts.builder()
                .subject("testuser")
                .claim("user_id", 42)
                .claim("token_version", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey())
                .compact();
    }

    private String signedTokenWithUserId(Object userId) {
        return Jwts.builder()
                .subject("testuser")
                .claim("user_id", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private User mockUser(long id, String username, long tokenVersion) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getTokenVersion()).thenReturn(tokenVersion);
        return user;
    }
}
