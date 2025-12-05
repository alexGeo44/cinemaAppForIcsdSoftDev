package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.TokenService;
import org.springframework.stereotype.Service;

@Service
public final class AuthenticateUserUseCase {


    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                   TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }


    public String authenticate(String rawUsername, String rawPassword) {
        if (rawUsername == null || rawUsername.isBlank() ||
                rawPassword == null || rawPassword.isBlank()) {
            throw new AuthorizationException("Invalid credentials");
        }

        Username username = Username.of(rawUsername);

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!user.isActive() || user.isLocked()) {
            throw new AuthorizationException("User is inactive or locked");
        }

        // Έλεγχος password (HashedPassword έχει μέθοδο matches)
        if (!user.password().matches(rawPassword)) {
            user.registerFailedLogin();
            userRepository.Save(user);
            throw new AuthorizationException("Invalid username or password");
        }

        // επιτυχία → μηδενίζουμε failedAttempts
        user.resetFailedAttempts();
        userRepository.Save(user);

        // δημιουργούμε JWT
        return tokenService.generateToken(user);
    }

}
