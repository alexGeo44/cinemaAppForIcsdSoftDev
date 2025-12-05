package com.cinema.application.users;

import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public final class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;

    public RegisterUserUseCase(UserRepository userRepository , PasswordPolicy passwordPolicy){

        this.userRepository = userRepository;
        this.passwordPolicy =passwordPolicy;
    }

    public User register(String rawUsername , String rawPassword, String fullName){
        Username username = new Username(rawUsername);

        if (userRepository.existsByUsername(username)){
            throw new DuplicateException("username", "Username already exists");
        }

        passwordPolicy.validate(rawPassword , username , fullName).ensureValid();

        HashedPassword password = HashedPassword.fromRaw(rawPassword);

        User user = new User(
                new UserId(UUID.randomUUID().clockSequence()),
                username,
                password,
                fullName,
                BaseRole.USER,
                true,
                0
        );

        return userRepository.Save(user);

    }


}
