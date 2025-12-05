package com.cinema.application.users;

import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;

@Service
public final class DeleteUserUseCase {
    private UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository){ this.userRepository = userRepository; }
    public void delete(UserId userId){

        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("User", "User not found");

        userRepository.deleteById(userId);

    }

}
