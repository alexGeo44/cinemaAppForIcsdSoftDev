package infrastructure.persistence.spring;

import domain.enums.BaseRole;
import infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserJpa extends JpaRepository<UserEntity, Long>  {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    List<UserEntity> findByBaseRole(BaseRole role);

    long countByBaseRole(BaseRole role);
}
