package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
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

        // φόρτωση target user
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        // domain action
        user.deactivate();

        // persist
        userRepository.Save(user);

        // 🔎 AUDIT
        auditLogger.logAction(
                actorId,
                "DEACTIVATE_USER",
                "userId=" + targetUserId.value()
        );
    }
}
