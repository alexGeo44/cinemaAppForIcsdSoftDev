package com.cinema.infrastructure.persistence.spring;

import com.cinema.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAuditLogJpa extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findTop100ByOrderByTimestampDesc();
}
