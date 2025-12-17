package com.cinema.presentation.controller;

import com.cinema.application.users.*;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import com.cinema.presentation.dto.requests.ChangePasswordRequest;
import com.cinema.presentation.dto.requests.UpdateUserRequest;
import com.cinema.presentation.dto.responses.UserResponse;
import com.cinema.presentation.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final ChangePasswordUseCase changePassword;
    private final DeleteUserUseCase deleteUser;
    private final LogoutUseCase logout;
    private final UpdateUserUseCase updateUser;
    private final DeactivateUserUseCase deactivateUser;
    private final UserRepository userRepository;

    public MeController(
            ChangePasswordUseCase changePassword,
            DeleteUserUseCase deleteUser,
            LogoutUseCase logout,
            UpdateUserUseCase updateUser,
            DeactivateUserUseCase deactivateUser,
            UserRepository userRepository
    ) {
        this.changePassword = changePassword;
        this.deleteUser = deleteUser;
        this.logout = logout;
        this.updateUser = updateUser;
        this.deactivateUser = deactivateUser;
        this.userRepository = userRepository;
    }

    // ✅ Αυτό χρειάζεται το frontend σου για bootstrap μετά από refresh
    @GetMapping
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UserId me = currentUserId(authentication);

        User user = userRepository.findById(me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(UserMapper.toSelfResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        UserId me = currentUserId(authentication);

        changePassword.changePassword(
                me,
                request.oldPassword(),
                request.newPassword(),
                request.newPasswordRepeat()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateMe(
            @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        UserId me = currentUserId(authentication);

        var updated = updateUser.update(
                me,
                me,
                request.username(),
                request.fullName()
        );

        return ResponseEntity.ok(UserMapper.toSelfResponse(updated));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<Void> deactivateMe(Authentication authentication) {
        UserId me = currentUserId(authentication);

        deactivateUser.deactivate(me, me);
        logout.logoutSelf(me);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMe(Authentication authentication) {
        UserId me = currentUserId(authentication);
        deleteUser.delete(me, me);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        UserId me = currentUserId(authentication);
        logout.logoutSelf(me);
        return ResponseEntity.noContent().build();
    }

    // ✅ renamed helper (ΔΕΝ συγκρούεται πια)
    private UserId currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Object p = authentication.getPrincipal();
        if (p instanceof Long l) return new UserId(l);
        if (p instanceof Integer i) return new UserId(i.longValue());
        return new UserId(Long.parseLong(String.valueOf(p)));
    }
}