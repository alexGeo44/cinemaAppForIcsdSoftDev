package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeactivateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public DeactivateUserUseCase(UserRepository userRepository,
                                 AuditLogger auditLogger) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec (updated):
     * - deactivate allowed by ADMIN OR by the user (self-deactivate)
     * - ADMIN accounts are non-deactivatable (by anyone)
     * - upon deactivation: invalidate current token (currentJti = null)
     */
    public void deactivate(UserId actorId, UserId targetUserId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new IllegalArgumentException("targetUserId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // ✅ ADMIN accounts cannot be deactivated (by anyone)
        if (target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("ADMIN accounts cannot be deactivated");
        }

        boolean isSelf = actorId.equals(targetUserId);
        boolean isAdmin = actor.baseRole() == BaseRole.ADMIN;

        // ✅ allow only self OR admin
        if (!isSelf && !isAdmin) {
            throw new AuthorizationException("Not allowed to deactivate this user");
        }

        if (!target.isActive()) return; // idempotent

        target.deactivate(); // domain should invalidateSession() => currentJti=null
        userRepository.Save(target);     // ✅ IMPORTANT: save (όχι Save)

        auditLogger.logAction(
                actorId,
                "DEACTIVATE_USER",
                "userId=" + targetUserId.value() + (isSelf ? " (self)" : " (admin)")
        );
    }
}
