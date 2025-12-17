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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Key key;
    private final long expirationSeconds;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public TokenService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    /** Επιστρέφει και το jti για να το γράψεις στο users.current_jti */
    public IssuedToken generateToken(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(user.id().value()))
                .claim("username", user.username().value())
                .claim("role", user.baseRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new IssuedToken(token, jti);
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isInvalidated(String token) {
        return token != null && blacklistedTokens.contains(token);
    }

    public void clearBlacklist() {
        blacklistedTokens.clear();
    }

    public record IssuedToken(String token, String jti) {}
}
