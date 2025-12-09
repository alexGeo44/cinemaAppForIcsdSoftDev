package com.cinema.infrastructure.security;

import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.entity.value.UserId;
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
    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenValidator tokenValidator,
                                   TokenService tokenService) {
        this.tokenValidator = Objects.requireNonNull(tokenValidator);
        this.tokenService = Objects.requireNonNull(tokenService);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            // χωρίς token → συνεχίζουμε ως anonymous
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // αν έχει γίνει logout και είναι στο blacklist
        if (tokenService.isInvalidated(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenValidator.TokenData data = tokenValidator.validate(token);
            UserId userId = data.userId();
            BaseRole role = data.role();

            // ROLE_xxx όπως περιμένει το Spring
            GrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role.name());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userId.value(),   // principal (μπορείς να βάλεις και ολόκληρο User αν θες)
                    null,
                    List.of(authority)
            );

            ((UsernamePasswordAuthenticationToken) auth)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            // invalid JWT → καθαρίζουμε context και ΠΡΟΧΩΡΑΜΕ
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
