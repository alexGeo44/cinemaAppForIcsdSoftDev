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
public class RegisterUserUseCase {

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
     * Self-register (spec):
     * - Store all info BUT create account as INACTIVE.
     * - Fail if username already exists or username/password pattern checks fail.
     */
    public User register(String rawUsername, String rawPassword, String fullName) {

        // basic normalization (avoid " user " duplicates)
        String normalizedUsername = rawUsername == null ? null : rawUsername.trim();
        String normalizedFullName = fullName == null ? null : fullName.trim();

        Username username = Username.of(normalizedUsername);

        if (userRepository.existsByUsername(username)) {
            // spec: fails if username already exists
            throw new DuplicateException("username", "Username already exists");
        }

        // password rules (spec: >=8, upper/lower/digit/special; also username starts with letter etc)
        passwordPolicy
                .validate(rawPassword, username, normalizedFullName)
                .ensureValid();

        HashedPassword hashedPassword = HashedPassword.fromRaw(rawPassword);

        // SPEC: account must be INACTIVE upon registration request
        User user = new User(
                null,            // id: DB generated
                username,
                hashedPassword,
                normalizedFullName,
                BaseRole.USER,    // permanent role
                false,            // active=false (critical spec requirement)
                0,                // failed attempts counter
                null,             // currentJti
                null              // lastLoginAt
        );

        User saved = userRepository.Save(user);

        auditLogger.logAction(
                saved.id(),
                "REGISTER_REQUESTED",
                "Account created INACTIVE (awaiting ADMIN activation)"
        );

        return saved;
    }
}
