package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

@Service
public final class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public DeleteUserUseCase(UserRepository userRepository,
                             AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * @param actorId ποιος κάνει τη διαγραφή (admin)
     * @param targetId ποιος διαγράφεται
     */
    public void delete(UserId actorId, UserId targetId) {

        // φορτώνουμε actor
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        // φορτώνουμε target
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // === RULE 1: Admin cannot delete another Admin ===
        if (actor.baseRole() == BaseRole.ADMIN &&
                target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("Admins cannot delete other admins");
        }

        // === RULE 2: Admin cannot delete himself ===
        if (actor.baseRole() == BaseRole.ADMIN &&
                actorId.equals(targetId)) {
            throw new AuthorizationException("Admin cannot delete himself");
        }

        // Perform delete
        userRepository.deleteById(targetId);

        // AUDIT
        auditLogger.logAction(
                actorId,
                "DELETE_USER",
                "userId=" + targetId.value()
        );
    }
}
