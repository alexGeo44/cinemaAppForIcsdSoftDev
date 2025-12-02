package infrastructure.repository;

import domain.entity.User;
import domain.entity.value.UserId;
import domain.entity.value.Username;
import domain.enums.BaseRole;
import domain.port.UserRepository;
import infrastructure.persistence.entity.UserEntity;
import infrastructure.persistence.mapper.UserPersistenceMapper;
import infrastructure.persistence.spring.SpringDataUserJpa;

import java.util.List;
import java.util.Optional;

public class JpaUserRepository implements UserRepository {
    private final SpringDataUserJpa jpa;
    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    public JpaUserRepository(SpringDataUserJpa jpa){ this.jpa = jpa; }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUserName(Username username) {
        return jpa.findByUsername(username.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(Username username) {
        return jpa.existsByUsername(username.value());
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<User> findByRole(BaseRole role) {
        return jpa.findByBaseRole(role).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByRole(BaseRole role) {
        return jpa.countByBaseRole(role);
    }

    @Override
    public User Save(User user) {
        UserEntity saved = jpa.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }


    @Override
    public void deleteById(UserId id) {
        jpa.deleteById(id.value());
    }
}
