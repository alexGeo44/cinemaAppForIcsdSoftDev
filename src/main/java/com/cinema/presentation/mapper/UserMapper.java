package com.cinema.presentation.mapper;

import com.cinema.domain.entity.User;
import com.cinema.presentation.dto.responses.UserResponse;

public class UserMapper {

    private UserMapper() {}

    /** Για self/profile views (ο χρήστης βλέπει τα δικά του). */
    public static UserResponse toSelfResponse(User user) {
        return toResponseInternal(user);
    }

    /** Για admin list/view. (πάλι δεν εκθέτουμε tokens/password/attempts εδώ). */
    public static UserResponse toAdminResponse(User user) {
        return toResponseInternal(user);
    }

    private static UserResponse toResponseInternal(User user) {
        if (user == null) return null;

        return new UserResponse(
                user.id() != null ? user.id().value() : null,
                user.username() != null ? user.username().value() : null,
                user.fullName(),
                user.baseRole() != null ? user.baseRole().name() : null,
                user.isActive()
        );
    }
}
