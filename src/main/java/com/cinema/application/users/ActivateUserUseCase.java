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
public class ActivateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public ActivateUserUseCase(UserRepository userRepository, AuditLogger auditLogger) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec:
     * - ADMIN can activate accounts.
     * - Idempotent if already active.
     */
    @Transactional
    public void execute(UserId actorId, UserId targetUserId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new ValidationException("","targetUserId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        if (actor.baseRole() != BaseRole.ADMIN) {
            throw new AuthorizationException("Only ADMIN can activate accounts");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        if (target.isActive()) {
            auditLogger.logAction(actorId, "ACTIVATE_USER_NOOP", "userId=" + targetUserId.value());
            return;
        }

        target.activate();          // domain should set active=true and reset counters
        userRepository.Save(target);

        auditLogger.logAction(actorId, "ACTIVATE_USER", "userId=" + targetUserId.value());
    }
}
