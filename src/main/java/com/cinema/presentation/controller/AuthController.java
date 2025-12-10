package com.cinema.presentation.controller;

import com.cinema.application.users.AuthenticateUserUseCase;
import com.cinema.application.users.LogoutUseCase;
import com.cinema.application.users.RegisterUserUseCase;
import com.cinema.application.users.ValidateTokenUseCase;
import com.cinema.application.users.ValidateTokenUseCase.TokenData;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.presentation.dto.requests.LoginRequest;
import com.cinema.presentation.dto.requests.RegisterUserRequest;
import com.cinema.presentation.dto.responses.AuthResponse;
import com.cinema.presentation.dto.responses.TokenInfoResponse;
import com.cinema.presentation.dto.responses.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;
    private final LogoutUseCase logoutUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final UserRepository userRepository;

    public AuthController(AuthenticateUserUseCase authenticateUser,
                          LogoutUseCase logoutUseCase,
                          ValidateTokenUseCase validateTokenUseCase,
                          RegisterUserUseCase registerUserUseCase,
                          UserRepository userRepository) {
        this.authenticateUser = authenticateUser;
        this.logoutUseCase = logoutUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.userRepository = userRepository;
    }

    // ========= LOGIN =========
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        // 1) auth → JWT token
        String token = authenticateUser.authenticate(
                request.username(),
                request.password()
        );

        // 2) φορτώνουμε τον user από τη βάση
        User user = userRepository.findByUserName(new Username(request.username()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "User not found after successful authentication"
                ));

        UserResponse userDto = new UserResponse(
                user.id().value(),
                user.username().value(),
                user.fullName(),
                user.baseRole().name(),
                user.isActive()
        );

        AuthResponse response = new AuthResponse(token, userDto);
        return ResponseEntity.ok(response);
    }

    // ========= REGISTER =========
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {

        User user = registerUserUseCase.register(
                request.username(),
                request.password(),
                request.fullName()
        );

        UserResponse dto = new UserResponse(
                user.id().value(),
                user.username().value(),
                user.fullName(),
                user.baseRole().name(),
                user.isActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // ========= LOGOUT =========
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        String token = extractBearerToken(authHeader);

        // ο χρήστης που κάνει logout (id από το JWT / SecurityContext)
        Long actorId = (Long) authentication.getPrincipal();

        logoutUseCase.logout(new UserId(actorId), token);
        return ResponseEntity.noContent().build();
    }

    // ========= VALIDATE TOKEN =========
    @GetMapping("/validate")
    public ResponseEntity<TokenInfoResponse> validate(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        String token = extractBearerToken(authHeader);
        TokenData data = validateTokenUseCase.validate(token);
        TokenInfoResponse response = new TokenInfoResponse(data.userId(), data.role());
        return ResponseEntity.ok(response);
    }

    // ========= ME =========
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        String token = extractBearerToken(authHeader);
        TokenData data = validateTokenUseCase.validate(token);

        User user = userRepository.findById(new UserId(data.userId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        UserResponse dto = new UserResponse(
                user.id().value(),
                user.username().value(),
                user.fullName(),
                user.baseRole().name(),
                user.isActive()
        );

        return ResponseEntity.ok(dto);
    }

    // ========= helper =========
    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header"
            );
        }
        return header.substring(7);
    }
}
