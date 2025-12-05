package com.cinema.application.users;

import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCase {
    private final TokenService tokenService;

    public LogoutUseCase(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public void logout(String token) {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token cannot be null or empty");

        tokenService.invalidate(token);
    }
}
