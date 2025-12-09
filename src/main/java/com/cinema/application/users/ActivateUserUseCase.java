package com.cinema.application.users;

import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.port.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ActivateUserUseCase {

    private final UserRepository userRepository;

    public ActivateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(long userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new NotFoundException("User","User not found"));

        user.activate();
        userRepository.Save(user);
    }
}
