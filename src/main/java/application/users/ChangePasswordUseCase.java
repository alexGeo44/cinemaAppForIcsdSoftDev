package application.users;

import domain.Exceptions.NotFoundException;
import domain.entity.User;
import domain.entity.value.HashedPassword;
import domain.entity.value.UserId;
import domain.policy.PasswordPolicy;
import domain.port.UserRepository;

public final class ChangePasswordUseCase {

    public final UserRepository userRepository;
    public final PasswordPolicy passwordPolicy;


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
