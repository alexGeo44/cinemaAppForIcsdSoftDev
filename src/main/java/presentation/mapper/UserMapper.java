package presentation.mapper;

import domain.entity.User;
import presentation.dto.responses.UserResponse;

public class UserMapper {

    private UserMapper() {

    }

    public static UserResponse toResponse(User user) {
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
