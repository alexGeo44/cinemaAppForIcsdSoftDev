package com.cinema.presentation.controller;

import com.cinema.application.users.*;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.presentation.dto.requests.ChangePasswordRequest;
import com.cinema.presentation.dto.requests.RegisterUserRequest;
import com.cinema.presentation.dto.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUser;
    private final ChangePasswordUseCase changePassword;
    private final DeactivateUserUseCase deactivateUser;
    private final DeleteUserUseCase deleteUser;
    private final ActivateUserUseCase activateUser;
    private final UserRepository userRepository;

    public UserController(
            RegisterUserUseCase registerUser,
            ChangePasswordUseCase changePassword,
            DeactivateUserUseCase deactivateUser,
            DeleteUserUseCase deleteUser,
            ActivateUserUseCase activateUser,
            UserRepository userRepository
    ) {
        this.registerUser = registerUser;
        this.changePassword = changePassword;
        this.deactivateUser = deactivateUser;
        this.deleteUser = deleteUser;
        this.activateUser = activateUser;
        this.userRepository = userRepository;
    }

    /* ============================
       REGISTER
       ============================ */

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody RegisterUserRequest request) {
        registerUser.register(
                request.username(),
                request.password(),
                request.fullName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /* ============================
       PASSWORD
       ============================ */

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        Long currentUserId = (Long) authentication.getPrincipal();

        // προαιρετικός έλεγχος: αλλάζεις μόνο το δικό σου password
        if (!currentUserId.equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Δεν μπορείς να αλλάξεις κωδικό άλλου χρήστη"
            );
        }

        changePassword.changePassword(
                new UserId(id),
                request.oldPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok().build();
    }

    /* ============================
       ACTIVATE / DEACTIVATE
       ============================ */

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long actorId = (Long) authentication.getPrincipal();

        activateUser.execute(actorId, id);

        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long actorId = (Long) authentication.getPrincipal();

        if (actorId.equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Δεν μπορείς να απενεργοποιήσεις τον εαυτό σου"
            );
        }

        deactivateUser.deactivate(
                new UserId(actorId),
                new UserId(id)
        );

        return ResponseEntity.ok().build();
    }

    /* ============================
       DELETE
       ============================ */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long actorId = (Long) authentication.getPrincipal();

        if (actorId.equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Δεν μπορείς να διαγράψεις τον εαυτό σου"
            );
        }

        deleteUser.delete(
                new UserId(actorId),
                new UserId(id)
        );

        return ResponseEntity.noContent().build();
    }

    /* ============================
       LIST USERS (ADMIN)
       ============================ */

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        var users = userRepository.findAll();

        var dto = users.stream()
                .map(u -> new UserResponse(
                        u.id().value(),
                        u.username().value(),
                        u.fullName(),
                        u.baseRole().name(),
                        u.isActive()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }
}
