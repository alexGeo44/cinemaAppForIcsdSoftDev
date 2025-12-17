package com.cinema.infrastructure.security;

import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
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

    public TokenData validate(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            String jti = claims.getId(); // setId(...) από TokenService

            if (role == null || role.isBlank()) {
                throw new InvalidTokenException("Missing role claim");
            }
            if (jti == null || jti.isBlank()) {
                throw new InvalidTokenException("Missing jti");
            }

            return new TokenData(new UserId(userId), BaseRole.valueOf(role), jti);

        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException καλύπτει: signature invalid, malformed, unsupported κλπ
            throw new InvalidTokenException("Token invalid", e);
        }
    }

    public record TokenData(UserId userId, BaseRole role, String jti) {}

    /** Χρησιμοποίησε αυτές τις exceptions για να ξεχωρίζεις invalid vs expired. */
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) { super(message); }
        public InvalidTokenException(String message, Throwable cause) { super(message, cause); }
    }

    public static class ExpiredTokenException extends RuntimeException {
        public ExpiredTokenException(String message) { super(message); }
        public ExpiredTokenException(String message, Throwable cause) { super(message, cause); }
    }
}
