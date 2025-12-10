// src/main/java/com/cinema/application/users/ActivateUserUseCase.java
package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

@Service
public class ActivateUserUseCase {

    private final UserRepository userRepository;
    private final AuditLogger auditLogger;

    public ActivateUserUseCase(UserRepository userRepository,
                               AuditLogger auditLogger) {
        this.userRepository = userRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * @param actorId      ποιος κάνει την ενέργεια (ADMIN)
     * @param targetUserId ποιον χρήστη ενεργοποιεί
     */
    public void execute(long actorId, long targetUserId) {
        User user = userRepository.findById(new UserId(targetUserId))
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        user.activate();
        userRepository.Save(user);

        auditLogger.logAction(
                new UserId(actorId),
                "ACTIVATE_USER",
                "userId=" + targetUserId
        );
    }
}

