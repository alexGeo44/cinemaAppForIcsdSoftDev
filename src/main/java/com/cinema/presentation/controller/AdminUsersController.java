package com.cinema.presentation.controller;

import com.cinema.application.users.ActivateUserUseCase;
import com.cinema.application.users.DeactivateUserUseCase;
import com.cinema.application.users.DeleteUserUseCase;
import com.cinema.application.users.LogoutUseCase;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.presentation.dto.responses.UserResponse;
import com.cinema.presentation.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

    private final UserRepository userRepository;
    private final ActivateUserUseCase activateUser;
    private final DeactivateUserUseCase deactivateUser;
    private final DeleteUserUseCase deleteUser;
    private final LogoutUseCase logoutUseCase;

    public AdminUsersController(UserRepository userRepository,
                                ActivateUserUseCase activateUser,
                                DeactivateUserUseCase deactivateUser,
                                DeleteUserUseCase deleteUser,
                                LogoutUseCase logoutUseCase) {
        this.userRepository = userRepository;
        this.activateUser = activateUser;
        this.deactivateUser = deactivateUser;
        this.deleteUser = deleteUser;
        this.logoutUseCase = logoutUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        var dto = userRepository.findAll().stream()
                .map(UserMapper::toAdminResponse)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable long id, Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        activateUser.execute(new UserId(adminId), new UserId(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable long id, Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        deactivateUser.deactivate(new UserId(adminId), new UserId(id));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        deleteUser.delete(new UserId(adminId), new UserId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/logout")
    public ResponseEntity<Void> forceLogout(@PathVariable long id, Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        logoutUseCase.forceLogout(new UserId(adminId), new UserId(id));
        return ResponseEntity.noContent().build();
    }
}
