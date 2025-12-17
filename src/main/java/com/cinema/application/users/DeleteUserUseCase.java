package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
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

    public DeleteUserUseCase(UserRepository userRepository, AuditLogger auditLogger) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }

    @Transactional
    public void delete(UserId actorId, UserId targetId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetId == null) throw new IllegalArgumentException("targetId is required");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        if (target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("ADMIN accounts cannot be deleted");
        }

        boolean isSelfDelete = actorId.equals(targetId);
        boolean isAdminDelete = actor.baseRole() == BaseRole.ADMIN;

        if (!isSelfDelete && !isAdminDelete) {
            throw new AuthorizationException("Not allowed to delete this user");
        }

        // invalidate token before delete
        target.invalidateSession();
        userRepository.Save(target);          // ✅ save

        userRepository.deleteById(targetId);  // ✅ matches your port

        auditLogger.logAction(
                actorId,
                "DELETE_USER",
                "userId=" + targetId.value() + (isSelfDelete ? " (self)" : " (admin)")
        );
    }
}

