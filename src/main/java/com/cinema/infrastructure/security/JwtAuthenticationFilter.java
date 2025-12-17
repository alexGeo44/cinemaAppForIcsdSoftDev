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

        // no token => just continue (visitor)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        // empty token => treat as unauthenticated, continue
        if (token.isBlank()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenValidator.TokenData data = tokenValidator.validate(token);

            UserId userId = data.userId();
            String jti = data.jti();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenValidator.InvalidTokenException("User not found"));

            // inactive => unauthenticated
            if (!user.isActive()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ single-active-token rule
            String currentJti = user.currentJti();
            if (currentJti == null || !currentJti.equals(jti)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ role from DB (not from JWT claim)
            BaseRole role = user.baseRole();

            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId.value(), // principal: Long
                    null,
                    List.of(authority)
            );

            ((UsernamePasswordAuthenticationToken) auth)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (TokenValidator.ExpiredTokenException | TokenValidator.InvalidTokenException ex) {
            // invalid/expired token => treat as unauthenticated
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
