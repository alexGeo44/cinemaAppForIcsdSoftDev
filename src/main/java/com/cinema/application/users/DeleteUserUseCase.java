package com.cinema.application.users;

import com.cinema.application.security.OwnershipGuard;
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
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;
    private final OwnershipGuard ownershipGuard;

    public DeleteUserUseCase(
            UserRepository userRepository,
            AuditLogger auditLogger,
            OwnershipGuard ownershipGuard
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
        this.ownershipGuard = Objects.requireNonNull(ownershipGuard);
    }

    /**
     * Spec:
     * - Allowed by ADMIN or by the user (self-delete).
     * - ADMIN accounts are NON-deletable (this concerns deletion of non-ADMIN accounts).
     * - Deletes account and all associated tokens.
     * - If token user != requester (self-only violation) => deactivate BOTH accounts.
     */
    @Transactional
    public void delete(UserId actorId, UserId targetId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetId == null) throw new ValidationException("", "targetId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        if (!actor.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        if (target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("ADMIN accounts cannot be deleted");
        }

        // Spec: not-owner => deactivate both (unless ADMIN)
        ownershipGuard.requireSelfOrAdminOtherwiseDeactivateBoth(actor, target);

        // invalidate token before delete (best-effort; delete anyway)
        target.invalidateSession();
        userRepository.Save(target);

        userRepository.deleteById(targetId);

        boolean isSelfDelete = actorId.equals(targetId);
        auditLogger.logAction(
                actorId,
                "DELETE_USER",
                "userId=" + targetId.value() + (actor.baseRole() == BaseRole.ADMIN ? " (admin)" : (isSelfDelete ? " (self)" : ""))
        );
    }
}
