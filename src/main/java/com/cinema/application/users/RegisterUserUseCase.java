package com.cinema.application.users;

import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public final class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.passwordPolicy = passwordPolicy;
        this.auditLogger = auditLogger;
    }

    /**
     * Δημιουργεί νέο USER (self-register ή admin register)
     */
    public User register(String rawUsername, String rawPassword, String fullName) {

        Username username = Username.of(rawUsername);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateException("username", "Username already exists");
        }

        // password rules
        passwordPolicy
                .validate(rawPassword, username, fullName)
                .ensureValid();

        HashedPassword hashedPassword = HashedPassword.fromRaw(rawPassword);

        UserId userId = generateUserId();

        User user = new User(
                userId,
                username,
                hashedPassword,
                fullName,
                BaseRole.USER,
                true,
                0
        );

        User saved = userRepository.Save(user);

        // 🔎 AUDIT (self-register)
        auditLogger.logAction(
                saved.id(),
                "REGISTER_USER",
                "self-registration"
        );

        return saved;
    }

    private UserId generateUserId() {
        long id = UUID.randomUUID()
                .getLeastSignificantBits() & Long.MAX_VALUE;
        return new UserId(id);
    }
}
