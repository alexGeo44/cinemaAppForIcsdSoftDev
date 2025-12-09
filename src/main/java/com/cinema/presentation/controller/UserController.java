package com.cinema.presentation.controller;

import com.cinema.application.users.*;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.presentation.dto.responses.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cinema.presentation.dto.requests.RegisterUserRequest;
import com.cinema.presentation.dto.requests.ChangePasswordRequest;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUser;
    private final ChangePasswordUseCase changePassword;
    private final DeactivateUserUseCase deactivateUser;
    private final DeleteUserUseCase deleteUser;
    private final UserRepository userRepository;
    private final ActivateUserUseCase activateUserUseCase;

    public UserController(
            RegisterUserUseCase registerUser,
            ChangePasswordUseCase changePassword,
            DeactivateUserUseCase deactivateUser,
            DeleteUserUseCase deleteUser,
            UserRepository userRepository,
            ActivateUserUseCase activateUserUseCase
    ) {
        this.registerUser = registerUser;
        this.changePassword = changePassword;
        this.deactivateUser = deactivateUser;
        this.deleteUser = deleteUser;
        this.userRepository = userRepository;
        this.activateUserUseCase = activateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody RegisterUserRequest request) {
        registerUser.register(
                request.username(),
                request.password(),
                request.fullName()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {

        changePassword.changePassword(
                new UserId(id),
                request.oldPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        activateUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateUser.deactivate(new UserId(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUser.delete(new UserId(id));
        return ResponseEntity.noContent().build();
    }

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
