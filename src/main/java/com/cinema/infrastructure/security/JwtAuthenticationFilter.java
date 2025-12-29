package com.cinema.infrastructure.security;

import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenValidator tokenValidator;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(TokenValidator tokenValidator, UserRepository userRepository) {
        this.tokenValidator = Objects.requireNonNull(tokenValidator);
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // No token => continue as VISITOR (anonymous)
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String h = header.trim();

        // If header exists but is not Bearer => client attempted auth incorrectly => 401 TOKEN_INVALID
        if (!h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "TOKEN_INVALID", "Missing or invalid Authorization header");
            return;
        }

        String token = h.substring(7).trim();
        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "TOKEN_INVALID", "Missing token");
            return;
        }

        try {
            TokenValidator.TokenData data = tokenValidator.validate(token);

            UserId userId = data.userId();
            String jti = data.jti();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenValidator.InvalidTokenException("User not found"));

            // inactive blocks all authenticated usage
            if (!user.isActive()) {
                SecurityContextHolder.clearContext();
                writeUnauthorized(response, "ACCOUNT_INACTIVE", "Account is inactive");
                return;
            }

            // single-active-token rule (invalidate previous token)
            String currentJti = user.currentJti();
            if (currentJti == null || !currentJti.equals(jti)) {
                SecurityContextHolder.clearContext();
                writeUnauthorized(response, "TOKEN_REVOKED", "Token is not current");
                return;
            }

            // role from DB (source of truth)
            BaseRole role = user.baseRole();
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId.value(),  // principal: Long
                    null,
                    List.of(authority)
            );
            ((UsernamePasswordAuthenticationToken) auth)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (TokenValidator.ExpiredTokenException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "TOKEN_EXPIRED", "Token expired");
        } catch (TokenValidator.InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "TOKEN_INVALID", "Token invalid");
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "TOKEN_INVALID", "Token invalid");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        String body = "{\"code\":\"" + escapeJson(code) + "\",\"message\":\"" + escapeJson(message) + "\"}";
        response.getWriter().write(body);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
