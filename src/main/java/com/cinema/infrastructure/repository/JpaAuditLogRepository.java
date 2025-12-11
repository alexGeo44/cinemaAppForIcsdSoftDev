package com.cinema.infrastructure.repository;

import com.cinema.domain.entity.AuditLog;
import com.cinema.domain.port.AuditLogRepository;
import com.cinema.infrastructure.persistence.entity.AuditLogEntity;
import com.cinema.infrastructure.persistence.spring.SpringDataAuditLogJpa;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaAuditLogRepository implements AuditLogRepository {

    private final SpringDataAuditLogJpa jpa;

    public JpaAuditLogRepository(SpringDataAuditLogJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(AuditLog log) {
        AuditLogEntity e = new AuditLogEntity(
                log.actorUserId(),
                log.action(),
                log.target(),
                log.timestamp()
        );
        jpa.save(e);
    }

    @Override
    public List<AuditLog> findLatest(int limit) {
        return jpa.findTop100ByOrderByTimestampDesc()
                .stream()
                .limit(limit)
                .map(e -> new AuditLog(
                        e.getActorUserId(),
                        e.getAction(),
                        e.getTarget(),
                        e.getTimestamp()
                ))
                .toList();
    }
}

