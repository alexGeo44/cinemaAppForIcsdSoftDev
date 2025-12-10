// presentation/dto/responses/AuditLogResponse.java
package com.cinema.presentation.dto.responses;

import java.time.Instant;

public record AuditLogResponse(
        Long actorUserId,
        String action,
        String target,
        Instant timestamp
) {}
