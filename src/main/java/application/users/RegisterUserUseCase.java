package application.users;

import domain.Exceptions.DuplicateException;
import domain.entity.User;
import domain.entity.value.HashedPassword;
import domain.entity.value.UserId;
import domain.entity.value.Username;
import domain.enums.BaseRole;
import domain.policy.PasswordPolicy;
import domain.port.UserRepository;

import java.util.UUID;

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
