package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;

@Service
public final class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;


    public ChangePasswordUseCase(UserRepository userRepository , PasswordPolicy passwordPolicy){
        this.userRepository = userRepository;
        this.passwordPolicy = passwordPolicy;
    }

    public void changePassword(
            UserId userId,
            String currentPassword,
            String newPassword
    ){
        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException("User", "User not found"));

        if(!user.password().matches(currentPassword)) throw new IllegalArgumentException("Current password is invalid!");

        passwordPolicy.validate(newPassword, user.username(), user.fullName()).ensureValid();

        HashedPassword newHash = HashedPassword
                .fromRaw(newPassword);

        userRepository.Save(user);

    }



}
