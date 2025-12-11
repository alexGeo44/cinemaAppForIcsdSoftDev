// src/main/java/com/cinema/infrastructure/security/AuditLogger.java
package com.cinema.infrastructure.security;

import com.cinema.domain.entity.AuditLog;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.AuditLogRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditLogger {

    private final AuditLogRepository repo;

    public AuditLogger(AuditLogRepository repo) {
        this.repo = repo;
    }

    public void logLogin(UserId userId) {
        repo.save(new AuditLog(userId.value(), "LOGIN", null, Instant.now()));
    }

    public void logAction(UserId userId, String action, String target) {
        repo.save(new AuditLog(userId.value(), action, target, Instant.now()));
    }
}
