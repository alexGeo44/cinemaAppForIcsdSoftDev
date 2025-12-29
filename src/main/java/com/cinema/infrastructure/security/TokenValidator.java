package com.cinema.infrastructure.security;

import com.cinema.domain.entity.value.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class TokenValidator {

    private final Key key;

    public TokenValidator(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates cryptographic signature + expiry and extracts:
     * - subject (userId)
     * - jti
     *
     * IMPORTANT:
     * - Do NOT trust role from JWT for authorization decisions (we read base role from DB).
     * - We keep role claim optional for backward compatibility.
     */
    public TokenData validate(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                throw new InvalidTokenException("Missing subject");
            }

            Long userId;
            try {
                userId = Long.valueOf(sub);
            } catch (NumberFormatException nfe) {
                throw new InvalidTokenException("Invalid subject userId", nfe);
            }

            String jti = claims.getId();
            if (jti == null || jti.isBlank()) {
                throw new InvalidTokenException("Missing jti");
            }

            // role claim is optional and ignored for authorization (DB is source of truth)
            return new TokenData(new UserId(userId), jti);

        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            // signature invalid, malformed, unsupported, etc.
            throw new InvalidTokenException("Token invalid", e);
        }
    }

    public record TokenData(UserId userId, String jti) {}

    /** Distinguish invalid vs expired (used by JwtAuthenticationFilter) */
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) { super(message); }
        public InvalidTokenException(String message, Throwable cause) { super(message, cause); }
    }

    public static class ExpiredTokenException extends RuntimeException {
        public ExpiredTokenException(String message) { super(message); }
        public ExpiredTokenException(String message, Throwable cause) { super(message, cause); }
    }
}
