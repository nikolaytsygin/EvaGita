package com.eva.evagita.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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

    public String generateToken(String username) {
        return generateToken(username, 0L);
    }

    public String generateToken(String username, long tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("token_version", tokenVersion)
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

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
