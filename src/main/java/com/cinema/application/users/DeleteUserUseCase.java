package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
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

        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        userRepository.deleteById(targetId);

        // 🔎 AUDIT LOG
        auditLogger.logAction(
                actorId,
                "DELETE_USER",
                "userId=" + targetId.value()
        );
    }
}
