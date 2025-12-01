package domain.port;

import domain.entity.User;
import domain.entity.value.UserId;
import domain.entity.value.Username;
import domain.enums.BaseRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(UserId id);
    Optional<User> findByUserName(Username username);

    List<User> findAll();
    List<User> findByRole(BaseRole role);

    long countByRole(BaseRole role);

    User Save(User user);
    void deleteById(UserId id);

    boolean existsByUsername(Username username);



}
