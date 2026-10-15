package com.example.bookstore.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes:60}") long minutes) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            decoded = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (decoded.length < 32)
            throw new IllegalStateException("JWT secret must be at least 32 bytes (Base64 encoded recommended)");
        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationSeconds = Math.max(1, minutes) * 60;
    }

    public String generate(String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder().subject(email).claim("role", role).issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds))).signWith(key).compact();
    }

    public String subject(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }
}
