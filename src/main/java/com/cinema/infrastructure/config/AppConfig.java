package com.cinema.infrastructure.config;

import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.service.ProgramStateMachine;
import com.cinema.domain.service.ScreeningStateMachine;
import com.cinema.infrastructure.security.TokenService;
import com.cinema.infrastructure.security.TokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


@Configuration
public class AppConfig {



    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ProgramStateMachine programStateMachine() {
        return new ProgramStateMachine();
    }

    @Bean
    public ScreeningStateMachine screeningStateMachine() {
        return new ScreeningStateMachine();
    }


    @Bean
    public PasswordPolicy passwordPolicy() {
        return new PasswordPolicy(PasswordPolicy.Config.strongDefaults());
    }

    @Bean
    public TokenService tokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds}") long expirationSeconds
    ) {
        return new TokenService(secret, expirationSeconds);
    }

    @Bean
    public TokenValidator tokenValidator(@Value("${jwt.secret}") String secret) {
        return new TokenValidator(secret);
    }

}
