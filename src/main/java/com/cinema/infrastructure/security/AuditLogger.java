// src/main/java/com/cinema/infrastructure/security/AuditLogger.java
package com.cinema.infrastructure.security;

import com.cinema.domain.entity.value.UserId;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditLogger {

    public void logLogin(UserId userId) {
        System.out.println("[AUDIT] LOGIN user=" + userId.value() + " at " + Instant.now());
    }

    public void logAction(UserId userId, String action, String target) {
        System.out.println("[AUDIT] user=" + userId.value() +
                " action=" + action +
                " target=" + target +
                " at " + Instant.now());
    }
}
