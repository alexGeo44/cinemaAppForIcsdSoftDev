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
public final class DeactivateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public DeactivateUserUseCase(
            UserRepository userRepository,
            AuditLogger auditLogger
    ) {
        this.userRepository = userRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * @param actorId ποιος κάνει το deactivate
     * @param targetUserId ποιος απενεργοποιείται
     */
    public void deactivate(UserId actorId, UserId targetUserId) {

        // φορτώνουμε actor
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new AuthorizationException("Invalid actor"));

        // φορτώνουμε target user
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // === RULE 1: Admin cannot deactivate another Admin ===
        if (actor.baseRole() == BaseRole.ADMIN &&
                target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("Admins cannot deactivate other admins");
        }

        // === RULE 2: (optional) Admin cannot deactivate himself ===
        if (actor.baseRole() == BaseRole.ADMIN &&
                actorId.equals(targetUserId)) {
            throw new AuthorizationException("Admin cannot deactivate himself");
        }

        // === Already inactive? no-op ===
        if (!target.isActive()) {
            return;
        }

        // === Domain action ===
        target.deactivate();

        // persist user
        userRepository.Save(target);

        //  AUDIT
        auditLogger.logAction(
                actorId,
                "DEACTIVATE_USER",
                "userId=" + targetUserId.value()
        );
    }
}
