package com.cinema.domain.entity;

import java.time.Instant;

public class AuditLog {

    private final Long actorUserId;
    private final String action;
    private final String target;
    private final Instant timestamp;

    public AuditLog(Long actorUserId, String action, String target, Instant timestamp) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    public Long actorUserId() { return actorUserId; }
    public String action() { return action; }
    public String target() { return target; }
    public Instant timestamp() { return timestamp; }
}
