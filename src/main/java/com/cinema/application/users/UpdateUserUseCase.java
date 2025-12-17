package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Pattern;

@Service
public  class UpdateUserUseCase {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,}$"); // spec: starts with letter, >=5, alnum/_

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public UpdateUserUseCase(UserRepository userRepository,
                             AuditLogger auditLogger) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec:
     * - allowed: self OR ADMIN
     * - all fields except password can be changed
     * - if username changes => invalidate current token/session (done via domain invalidateSession)
     */
    public User update(
            UserId actorId,
            UserId targetId,
            String newUsername,
            String newFullName
    ) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetId == null) throw new IllegalArgumentException("targetId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        boolean isSelf = actorId.equals(targetId);
        boolean isAdmin = actor.baseRole() == BaseRole.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new AuthorizationException("Not allowed to update this user");
        }

        // Spec: inactive account blocks profile changes
        if (!target.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        // update full name (optional)
        if (newFullName != null && !newFullName.isBlank()) {
            target.updateFullName(newFullName);
        }

        // update username (optional)
        if (newUsername != null && !newUsername.isBlank()) {
            String trimmed = newUsername.trim();

            if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
                throw new IllegalArgumentException("Username does not match required pattern");
            }

            String current = target.username().value();
            if (!trimmed.equals(current)) {
                Username candidate = Username.of(trimmed);

                // unique check
                if (userRepository.existsByUsername(candidate)) {
                    throw new IllegalArgumentException("Username already exists");
                }

                // domain: changeUsername() κάνει invalidateSession() => currentJti=null
                target.changeUsername(candidate);
            }
        }

        User saved = userRepository.Save(target);
        auditLogger.logAction(actorId, "UPDATE_USER", "userId=" + targetId.value());

        return saved;
    }
}
