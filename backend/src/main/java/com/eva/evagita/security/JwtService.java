package com.eva.evagita.security;

import com.eva.evagita.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getId() == null || user.getId() <= 0) {
            throw new IllegalArgumentException("User must have a positive database id");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("User must have a non-blank username");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("user_id", user.getId())
                .claim("token_version", user.getTokenVersion())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject() != null
                    && !claims.getSubject().isBlank()
                    && claims.getIssuedAt() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(claims.getIssuedAt());
        } catch (Exception e) {
            return false;
        }
    }

    public long extractTokenVersion(String token) {
        Object version = extractAllClaims(token).get("token_version");
        if (version == null) {
            return 0L;
        }
        if (version instanceof Number number) {
            long tokenVersion = number.longValue();
            if (tokenVersion < 0) {
                throw new IllegalArgumentException("Invalid token version");
            }
            return tokenVersion;
        }
        throw new IllegalArgumentException("Invalid token version");
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("user_id");
        if (userId == null) {
            throw new IllegalArgumentException("Missing user id");
        }
        if (userId instanceof Number number) {
            return parsePositiveUserId(number);
        }
        throw new IllegalArgumentException("Invalid user id");
    }

    private long parsePositiveUserId(Number number) {
        final BigInteger integerValue;
        try {
            integerValue = new BigDecimal(number.toString()).toBigIntegerExact();
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException("Invalid user id", exception);
        }

        if (integerValue.signum() <= 0
                || integerValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("Invalid user id");
        }

        return integerValue.longValueExact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
