// src/main/java/com/cinema/application/users/LogoutUseCase.java
package com.cinema.application.users;

import com.cinema.domain.entity.value.UserId;
import com.cinema.infrastructure.security.AuditLogger;
import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public final class LogoutUseCase {

    private final TokenService tokenService;
    private final AuditLogger auditLogger;

    public LogoutUseCase(TokenService tokenService,
                         AuditLogger auditLogger) {
        this.tokenService = tokenService;
        this.auditLogger = auditLogger;
    }

    public void logout(UserId actorId, String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        tokenService.invalidate(token);

        auditLogger.logAction(
                actorId,
                "LOGOUT",
                "token=" + token.substring(0, Math.min(10, token.length())) + "..."
        );
    }
}
