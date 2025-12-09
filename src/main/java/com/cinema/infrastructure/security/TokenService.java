package com.cinema.infrastructure.security;

import com.cinema.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Key key;
    private final long expirationSeconds;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public TokenService(
            @Value("${security.jwt.secret}") String secretKey,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(String.valueOf(user.id().value()))
                .claim("username", user.username().value())
                .claim("role", user.baseRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isInvalidated(String token) {
        return blacklistedTokens.contains(token);
    }

    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}
