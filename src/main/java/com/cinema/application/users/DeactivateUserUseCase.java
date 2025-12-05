package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;

@Service
public final class DeactivateUserUseCase {

    private UserRepository userRepository;

    public DeactivateUserUseCase(UserRepository userRepository){ this.userRepository = userRepository; }

    public void deactivate(UserId userId){

        User user = userRepository.findById(userId).orElseThrow(()->new NotFoundException("User", "User not found"));


        user.deactivate();
        userRepository.Save(user);
    }

}
