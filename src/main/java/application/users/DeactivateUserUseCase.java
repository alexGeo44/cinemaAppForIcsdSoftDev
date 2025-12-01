package application.users;

import domain.Exceptions.NotFoundException;
import domain.entity.User;
import domain.entity.value.UserId;
import domain.port.UserRepository;

public final class DeactivateUserUseCase {

    public UserRepository userRepository;

    public DeactivateUserUseCase(UserRepository userRepository){ this.userRepository = userRepository; }

    public void deactivate(UserId userId){

        User user = userRepository.findById(userId).orElseThrow(()->new NotFoundException("User", "User not found"));


        user.deactivate();
        userRepository.Save(user);
    }

}
