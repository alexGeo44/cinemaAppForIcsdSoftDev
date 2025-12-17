package com.cinema.infrastructure.persistence.entity;

import com.cinema.domain.enums.BaseRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "uk_users_username", columnList = "username", unique = true)
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 1000)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_role", nullable = false, length = 20)
    private BaseRole baseRole;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    // --- Token tracking (για current token invalidation rules) ---
    @Column(name = "current_jti", length = 36)
    private String currentJti;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BaseRole getBaseRole() { return baseRole; }
    public void setBaseRole(BaseRole baseRole) { this.baseRole = baseRole; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public String getCurrentJti() { return currentJti; }
    public void setCurrentJti(String currentJti) { this.currentJti = currentJti; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
