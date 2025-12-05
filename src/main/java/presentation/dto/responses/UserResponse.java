package presentation.dto.responses;

import java.time.LocalDate;

public record UserResponse(Long id,
                           String userName,
                           String fullName,
                           String role,
                           Boolean active) {
}
