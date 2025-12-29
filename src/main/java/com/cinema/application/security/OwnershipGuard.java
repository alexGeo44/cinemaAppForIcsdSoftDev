package com.cinema.application.security;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class OwnershipGuard {

    private final UserRepository userRepository;

    public OwnershipGuard(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Spec:
     * If a valid token belongs to another user than the requester, BOTH accounts must be deactivated.
     *
     * Use for self-only operations (unless ADMIN).
     * - If actor is ADMIN -> allowed.
     * - If actor != target -> deactivate both + throw.
     */
    @Transactional
    public void requireSelfOrAdminOtherwiseDeactivateBoth(User actor, User target) {
        if (actor.baseRole() == BaseRole.ADMIN) return;

        if (actor.id().equals(target.id())) return;

        actor.deactivate();
        target.deactivate();

        userRepository.Save(actor);
        userRepository.Save(target);

        throw new AuthorizationException(
                "TOKEN_NOT_OWNER: token user differs from requester; both accounts deactivated"
        );
    }
}
