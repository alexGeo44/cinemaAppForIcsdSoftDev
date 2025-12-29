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
import com.cinema.presentation.dto.requests.RegisterRequest;
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
    private final UserRepository userRepository;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(
            AuthenticateUserUseCase authenticateUser,
            LogoutUseCase logoutUseCase,
            ValidateTokenUseCase validateTokenUseCase,
            UserRepository userRepository,
            RegisterUserUseCase registerUserUseCase
    ) {
        this.authenticateUser = authenticateUser;
        this.logoutUseCase = logoutUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
        this.userRepository = userRepository;
        this.registerUserUseCase = registerUserUseCase;
    }

    private UserId actor(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return new UserId(l);
        if (p instanceof Integer i) return new UserId(i.longValue());
        return new UserId(Long.parseLong(String.valueOf(p)));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        User user = registerUserUseCase.register(req.username(), req.password(), req.fullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authenticateUser.authenticate(request.username(), request.password());

        // NOTE: we re-fetch to include updated fields (e.g., active/currentJti/lastLoginAt),
        // but we rely on Username validation already done in AuthenticateUserUseCase.
        User user = userRepository.findByUserName(Username.of(request.username().trim()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "User not found after successful authentication"
                ));

        return ResponseEntity.ok(new AuthResponse(token, toUserResponse(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        UserId me = actor(authentication);
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        logoutUseCase.logoutSelf(me);
        return ResponseEntity.noContent().build();
    }

    /**
     * Token validation endpoint:
     * Accepts token from:
     *  1) Authorization: Bearer <token>
     *  2) Authorization: <rawToken> (tolerant)
     *  3) ?token=<token>
     *
     * Error mapping (invalid/expired/revoked) should be handled via @ControllerAdvice.
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenInfoResponse> validate(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestParam(name = "token", required = false) String tokenParam
    ) {
        String token = extractToken(authHeader, tokenParam);
        TokenData data = validateTokenUseCase.validate(token);
        return ResponseEntity.ok(new TokenInfoResponse(data.userId(), data.role()));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.id().value(),
                user.username().value(),
                user.fullName(),
                user.baseRole().name(),
                user.isActive()
        );
    }

    private String extractToken(String authHeader, String tokenParam) {
        if (authHeader != null && !authHeader.isBlank()) {
            String h = authHeader.trim();
            if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
                String t = h.substring(7).trim();
                return t.isEmpty() ? null : t;
            }
            // tolerate raw token in header
            return h;
        }
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam.trim();
        }
        return null; // ValidateTokenUseCase will throw ValidationException
    }
}
