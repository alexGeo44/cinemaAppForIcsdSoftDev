package com.cinema.infrastructure.repository;

import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.persistence.entity.UserEntity;
import com.cinema.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.cinema.infrastructure.persistence.spring.SpringDataUserJpa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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
