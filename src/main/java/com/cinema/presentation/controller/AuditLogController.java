// presentation/controller/AuditLogController.java
package com.cinema.presentation.controller;

import com.cinema.application.audit.ListAuditLogsUseCase;
import com.cinema.presentation.dto.responses.AuditLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final ListAuditLogsUseCase listAuditLogs;

    public AuditLogController(ListAuditLogsUseCase listAuditLogs) {
        this.listAuditLogs = listAuditLogs;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> list() {

        var logs = listAuditLogs.execute(50);

        var dto = logs.stream()
                .map(l -> new AuditLogResponse(
                        l.actorUserId(),
                        l.action(),
                        l.target(),
                        l.timestamp()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }
}
