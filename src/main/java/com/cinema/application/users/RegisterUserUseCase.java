package com.cinema.application.users;

import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public  class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    /**
     * Self-register: δημιουργεί νέο USER.
     * DB will generate id (IDENTITY).
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

        // ✅ id = null (DB identity)
        User user = new User(
                null,
                username,
                hashedPassword,
                fullName,
                BaseRole.USER,
                true,
                0,
                null,  // currentJti
                null   // lastLoginAt
        );

        User saved = userRepository.Save(user);

        auditLogger.logAction(
                saved.id(),
                "REGISTER_USER",
                "self-registration"
        );

        return saved;
    }
}
