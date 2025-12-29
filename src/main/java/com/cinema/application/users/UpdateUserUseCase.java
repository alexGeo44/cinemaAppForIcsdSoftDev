package com.cinema.application.users;

import com.cinema.application.security.OwnershipGuard;
import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;
    private final OwnershipGuard ownershipGuard;

    public UpdateUserUseCase(
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
     * - Requester must be authenticated.
     * - Allowed: self OR ADMIN
     * - All fields except password can be changed.
     * - If username changes => invalidate current token/session (target token).
     * - Inactive account blocks profile changes.
     * - If token user != requester (self-only violation) => deactivate BOTH accounts.
     */
    @Transactional
    public User update(
            UserId actorId,
            UserId targetId,
            String newUsername,
            String newFullName
    ) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetId == null) throw new ValidationException("", "targetId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        if (!actor.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // Spec: not-owner => deactivate both (unless ADMIN)
        ownershipGuard.requireSelfOrAdminOtherwiseDeactivateBoth(actor, target);

        // Spec: inactive target blocks profile changes
        if (!target.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        // update full name (optional)
        if (newFullName != null) {
            String trimmedName = newFullName.trim();
            if (!trimmedName.isEmpty()) {
                target.updateFullName(trimmedName);
            }
        }

        // update username (optional)
        if (newUsername != null) {
            String trimmedUsername = newUsername.trim();
            if (!trimmedUsername.isEmpty()) {

                String current = target.username().value();
                if (!trimmedUsername.equals(current)) {

                    Username candidate = Username.of(trimmedUsername);

                    if (userRepository.existsByUsername(candidate)) {
                        throw new DuplicateException("username", "Username already exists");
                    }

                    // domain: changeUsername() MUST invalidateSession()
                    target.changeUsername(candidate);
                }
            }
        }

        User saved = userRepository.Save(target);

        auditLogger.logAction(
                actorId,
                "UPDATE_USER",
                "targetUserId=" + targetId.value()
        );

        return saved;
    }
}
