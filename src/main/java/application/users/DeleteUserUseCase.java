package application.users;

import domain.Exceptions.NotFoundException;
import domain.entity.User;
import domain.entity.value.UserId;
import domain.port.UserRepository;

public final class DeleteUserUseCase {
    public UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository){ this.userRepository = userRepository; }
    public void delete(UserId userId){

        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("User", "User not found");

        userRepository.deleteById(userId);

    }

}
