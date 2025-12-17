package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public ChangePasswordUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Spec:
     * - old + new + newRepeat
     * - on 3 consecutive failures -> deactivate
     * - in ALL cases invalidate current token/session
     */
    public void changePassword(
            UserId userId,
            String currentPassword,
            String newPassword,
            String newPasswordRepeat
    ) {
        if (userId == null) throw new AuthorizationException("Unauthorized");
        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(newPasswordRepeat)) {
            throw new IllegalArgumentException("Password fields are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // Spec: inactive blocks profile changes
        if (!user.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        // ✅ Spec: in ALL cases invalidate current token/session
        // IMPORTANT: κάνε persist το invalidate πριν από οποιοδήποτε πιθανό throw
        user.invalidateSession();
        userRepository.Save(user);

        // repeat mismatch -> failure (counts)
        if (!newPassword.equals(newPasswordRepeat)) {
            user.registerFailedLogin();          // 3 failures -> deactivate() μέσα στο domain
            userRepository.Save(user);

            if (!user.isActive()) {
                throw new AuthorizationException("Account deactivated after 3 failed attempts");
            }
            throw new IllegalArgumentException("New passwords do not match");
        }

        // old password mismatch -> failure (counts)
        if (!user.password().matches(currentPassword)) {
            user.registerFailedLogin();
            userRepository.Save(user);

            if (!user.isActive()) {
                throw new AuthorizationException("Account deactivated after 3 failed attempts");
            }
            throw new IllegalArgumentException("Current password is invalid");
        }

        // policy validation (δεν το μετράω ως “failed attempt”, είναι input validation)
        passwordPolicy
                .validate(newPassword, user.username(), user.fullName())
                .ensureValid();

        HashedPassword newHash = HashedPassword.fromRaw(newPassword);

        // domain: changePassword() μηδενίζει attempts + invalidateSession() (ok ακόμα κι αν το κάναμε ήδη)
        user.changePassword(newHash);
        userRepository.Save(user);

        auditLogger.logAction(user.id(), "CHANGE_PASSWORD", "self");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
