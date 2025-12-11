package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public final class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuditLogger auditLogger;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                   TokenService tokenService,
                                   AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.auditLogger = auditLogger;
    }

    public String authenticate(String rawUsername, String rawPassword) {

        if (rawUsername == null || rawUsername.isBlank() ||
                rawPassword == null || rawPassword.isBlank()) {
            throw new AuthorizationException("Invalid credentials");
        }

        Username username = Username.of(rawUsername);

        // ===== 1) Ανάκτηση χρήστη =====
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!user.isActive() || user.isLocked()) {
            throw new AuthorizationException("User is inactive or locked");
        }

        // ===== 2) Έλεγχος password =====
        if (!user.password().matches(rawPassword)) {
            user.registerFailedLogin();
            userRepository.Save(user);
            throw new AuthorizationException("Invalid username or password");
        }

        // ===== 3) Επιτυχία =====
        user.resetFailedAttempts();
        userRepository.Save(user);

        // ===== 4) AUDIT entry =====
        auditLogger.logLogin(user.id());

        // ===== 5) JWT =====
        return tokenService.generateToken(user);
    }
}
