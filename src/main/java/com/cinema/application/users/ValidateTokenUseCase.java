package com.cinema.application.users;

import com.cinema.infrastructure.security.TokenValidator;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenUseCase {
    private final TokenValidator tokenValidator;

    public ValidateTokenUseCase(TokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    public TokenData validate(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // εδώ παίρνουμε το TokenData του TokenValidator (infrastructure)
        TokenValidator.TokenData raw = tokenValidator.validate(token);

        // και το μετατρέπουμε στο δικό μας TokenData (application layer)
        return new TokenData(
                raw.userId().value(),
                raw.role().name()
        );
    }

    // Μικρό DTO για να περνάς καθαρά δεδομένα προς τα έξω
    public record TokenData(Long userId, String role) {}
}
