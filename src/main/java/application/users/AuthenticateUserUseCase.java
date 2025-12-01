package application.users;

import domain.Exceptions.NotFoundException;
import domain.Exceptions.ValidationException;
import domain.entity.User;
import domain.entity.value.Username;
import domain.port.UserRepository;

public final class AuthenticateUserUseCase {

    public final UserRepository userRepository;


    public AuthenticateUserUseCase(UserRepository userRepository){ this.userRepository = userRepository; }

    public User authentication(String rawUsername , String rawPassword){

        Username username = new Username(rawUsername);

        User user = userRepository.findByUserName(username).orElseThrow(()-> new NotFoundException("User", "Invalid credentials"));

        if(!user.isActive()){ throw new ValidationException("account" , "Account is deactivated"); }

        if (!user.password().matches(rawPassword)){
            user.registerFailedLogin();
            userRepository.Save(user);
            throw new ValidationException("password", "Invalid credentials");
        }

        user.resetFailedAttempts();
        return userRepository.Save(user);

    }

}
