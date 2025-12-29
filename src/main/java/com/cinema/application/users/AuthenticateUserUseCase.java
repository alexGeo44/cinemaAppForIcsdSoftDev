package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuditLogger auditLogger;

    public AuthenticateUserUseCase(
            UserRepository userRepository,
            TokenService tokenService,
            AuditLogger auditLogger
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.tokenService = Objects.requireNonNull(tokenService);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec:
     * - Submit username/password; on success return NEW token and invalidate previous (single active token).
     * - 3 consecutive failed attempts -> deactivate account.
     * - Inactive account blocks authentication.
     */
    @Transactional
    public String authenticate(String rawUsername, String rawPassword) {

        String normalizedUsername = rawUsername == null ? null : rawUsername.trim();

        if (normalizedUsername == null || normalizedUsername.isBlank()
                || rawPassword == null || rawPassword.isBlank()) {
            // Do not leak which field is wrong
            throw new AuthorizationException("Invalid username or password");
        }

        Username username = Username.of(normalizedUsername);

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!user.isActive()) {
            auditLogger.logAction(
                    user.id(),
                    "LOGIN_FAILED_INACTIVE",
                    "Authentication blocked: inactive account"
            );
            throw new AuthorizationException("User is inactive");
        }

        // Password check
        if (!user.password().matches(rawPassword)) {
            user.registerFailedLogin();
            userRepository.Save(user);

            if (!user.isActive()) {
                // Spec: 3 consecutive failures -> deactivate
                auditLogger.logAction(
                        user.id(),
                        "LOGIN_DEACTIVATED",
                        "Account deactivated after 3 consecutive failed authentication attempts"
                );
                throw new AuthorizationException("Account deactivated after 3 failed attempts");
            }

            auditLogger.logAction(
                    user.id(),
                    "LOGIN_FAILED",
                    "Invalid username or password"
            );
            throw new AuthorizationException("Invalid username or password");
        }

        // Success: issue new token (jti must be unique)
        TokenService.IssuedToken issued = tokenService.generateToken(user);

        // Spec: invalidate any previous token -> overwrite currentJti,
        // reset failed attempts, set lastLoginAt
        user.startSession(issued.jti());
        userRepository.Save(user);

        auditLogger.logLogin(user.id());

        return issued.token();
    }
}
