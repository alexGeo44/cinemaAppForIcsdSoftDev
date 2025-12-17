package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCase {

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

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!user.isActive()) {
            throw new AuthorizationException("User is inactive");
        }

        // Password check
        if (!user.password().matches(rawPassword)) {
            user.registerFailedLogin();

            // ✅ save (lowercase)
            userRepository.Save(user);

            if (!user.isActive()) {
                throw new AuthorizationException("Account deactivated after 3 failed attempts");
            }
            throw new AuthorizationException("Invalid username or password");
        }

        // ✅ issue new token (must include jti)
        TokenService.IssuedToken issued = tokenService.generateToken(user);

        // ✅ set currentJti + lastLoginAt + reset attempts
        user.startSession(issued.jti());

        // ✅ save (lowercase)
        userRepository.Save(user);

        auditLogger.logLogin(user.id());

        return issued.token();
    }
}
