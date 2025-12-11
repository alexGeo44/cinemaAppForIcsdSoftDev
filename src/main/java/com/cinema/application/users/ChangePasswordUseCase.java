package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

@Service
public final class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public ChangePasswordUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.passwordPolicy = passwordPolicy;
        this.auditLogger = auditLogger;
    }

    public void changePassword(
            UserId userId,
            String currentPassword,
            String newPassword
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // έλεγχος τρέχοντος password
        if (!user.password().matches(currentPassword)) {
            throw new IllegalArgumentException("Current password is invalid!");
        }

        // policy validation
        passwordPolicy
                .validate(newPassword, user.username(), user.fullName())
                .ensureValid();

        // νέο hash
        HashedPassword newHash = HashedPassword.fromRaw(newPassword);


        user.changePassword(newHash);


        userRepository.Save(user);


        auditLogger.logAction(
                user.id(),
                "CHANGE_PASSWORD",
                "self"
        );
    }
}
