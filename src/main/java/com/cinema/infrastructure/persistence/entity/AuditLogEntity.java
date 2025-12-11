package com.cinema.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String target;

    @Column(nullable = false)
    private Instant timestamp;

    public AuditLogEntity() {}

    public AuditLogEntity(Long userId, String action, String target, Instant timestamp) {
        this.actorUserId = userId;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    // getters / setters
    public Long getId() { return id; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
