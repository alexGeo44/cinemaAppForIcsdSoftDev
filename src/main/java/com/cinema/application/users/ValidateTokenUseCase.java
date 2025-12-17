package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.TokenService;
import com.cinema.infrastructure.security.TokenValidator;
import org.springframework.stereotype.Service;

@Service
public class ValidateTokenUseCase {

    private final TokenValidator tokenValidator;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public ValidateTokenUseCase(TokenValidator tokenValidator,
                                TokenService tokenService,
                                UserRepository userRepository) {
        this.tokenValidator = tokenValidator;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public TokenData validate(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        // 1) blacklist (logout)
        if (tokenService.isInvalidated(token)) {
            throw new AuthorizationException("Token invalidated");
        }

        // 2) cryptographic validate + claims
        TokenValidator.TokenData raw = tokenValidator.validate(token);

        // 3) user must exist & be active
        User user = userRepository.findById(raw.userId())
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        if (!user.isActive()) {
            throw new AuthorizationException("Account inactive");
        }

        // 4) token must be current (currentJti match)
        String currentJti = user.currentJti();
        if (currentJti == null || !currentJti.equals(raw.jti())) {
            throw new AuthorizationException("Token is not current");
        }

        return new TokenData(
                raw.userId().value(),
                raw.role().name()
        );
    }

    public record TokenData(Long userId, String role) {}
}
