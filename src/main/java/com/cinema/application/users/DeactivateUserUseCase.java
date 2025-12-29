package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class DeactivateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public DeactivateUserUseCase(UserRepository userRepository, AuditLogger auditLogger) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec:
     * - ADMIN can activate/deactivate accounts.
     * - An inactive account blocks authentication/profile/cinema functions.
     * - Upon deactivation, invalidate current token (if exists).
     * - Idempotent if already inactive.
     */
    @Transactional
    public void deactivate(UserId actorId, UserId targetUserId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new ValidationException("","targetUserId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        // Only ADMIN can deactivate
        if (actor.baseRole() != BaseRole.ADMIN) {
            throw new AuthorizationException("Only ADMIN can deactivate accounts");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // Idempotent
        if (!target.isActive()) {
            auditLogger.logAction(actorId, "DEACTIVATE_USER_NOOP", "userId=" + targetUserId.value());
            return;
        }

        // Domain should: set active=false AND invalidateSession() (currentJti=null)
        target.deactivate();
        userRepository.Save(target);

        auditLogger.logAction(
                actorId,
                "DEACTIVATE_USER",
                "userId=" + targetUserId.value()
        );
    }
}
