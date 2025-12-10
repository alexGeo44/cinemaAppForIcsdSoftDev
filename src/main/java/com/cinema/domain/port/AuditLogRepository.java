// domain/port/AuditLogRepository.java
package com.cinema.domain.port;

import com.cinema.domain.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository {

    void save(AuditLog log);

    List<AuditLog> findLatest(int limit);
}
