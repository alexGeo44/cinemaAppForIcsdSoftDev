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
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Rules:
     * - USER can delete self
     * - ADMIN can delete others
     * - ADMIN cannot delete self
     */
    @Transactional
    public void delete(UserId actorId, UserId targetUserId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new ValidationException("userId", "userId is required");

        var actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User", "Actor not found"));

        boolean actorIsAdmin = actor.baseRole() == BaseRole.ADMIN;
        boolean self = actorId.equals(targetUserId);

        if (!actorIsAdmin && !self) {
            throw new AuthorizationException("You can only delete your own account");
        }

        if (actorIsAdmin && self) {
            throw new AuthorizationException("ADMIN cannot delete their own account");
        }

        userRepository.deleteById(targetUserId);
    }
}
