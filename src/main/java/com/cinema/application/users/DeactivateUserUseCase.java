package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.domain.enums.BaseRole;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DeactivateUserUseCase {

    private final UserRepository userRepository;

    public DeactivateUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Rules:
     * - USER can deactivate self
     * - ADMIN can deactivate others
     * - ADMIN cannot deactivate self
     */
    @Transactional
    public void deactivate(UserId actorId, UserId targetUserId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new ValidationException("userId", "userId is required");

        var actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User", "Actor not found"));

        var target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        boolean actorIsAdmin = actor.baseRole() == BaseRole.ADMIN;
        boolean self = actorId.equals(targetUserId);

        if (!actorIsAdmin && !self) {
            throw new AuthorizationException("You can only deactivate your own account");
        }

        if (actorIsAdmin && self) {
            throw new AuthorizationException("ADMIN cannot deactivate their own account");
        }

        target.deactivate(); // ή target.setActive(false) ανάλογα το domain σου
        userRepository.Save(target);
    }
}
