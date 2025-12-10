// application/audit/ListAuditLogsUseCase.java
package com.cinema.application.audit;

import com.cinema.domain.entity.AuditLog;
import com.cinema.domain.port.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAuditLogsUseCase {

    private final AuditLogRepository auditLogRepository;

    public ListAuditLogsUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> execute(int limit) {
        return auditLogRepository.findLatest(limit);
    }
}
