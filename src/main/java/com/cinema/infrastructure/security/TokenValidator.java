package com.cinema.infrastructure.security;

import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class TokenValidator {

    private final Key key;

    public TokenValidator(@Value("${security.jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public TokenData validate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);

        return new TokenData(new UserId(userId), BaseRole.valueOf(role));
    }

    public record TokenData(UserId userId, BaseRole role) {}
}
