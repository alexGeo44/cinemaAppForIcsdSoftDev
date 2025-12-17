package com.cinema.domain.entity;

import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;

import java.time.Instant;

public class User {
    private final UserId id;

    private Username username;
    private HashedPassword password;
    private String fullName;
    private BaseRole baseRole;

    private boolean active;
    private int failedAttempts;

    // ✅ για token invalidation rules (spec)
    private String currentJti;      // null σημαίνει “κανένα active session/token”
    private Instant lastLoginAt;

    public User(
            UserId id,
            Username username,
            HashedPassword password,
            String fullName,
            BaseRole baseRole,
            boolean active,
            int failedAttempts,
            String currentJti,
            Instant lastLoginAt
    ) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        if (baseRole == null) throw new IllegalArgumentException("Base role cannot be null");

        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.baseRole = baseRole;
        this.active = active;
        this.failedAttempts = failedAttempts;
        this.currentJti = currentJti;
        this.lastLoginAt = lastLoginAt;
    }

    public void updateFullName(String newFullName) {
        if (newFullName == null || newFullName.isBlank())
            throw new IllegalArgumentException("Full name cannot be blank");
        this.fullName = newFullName.trim();
    }

    public void changeUsername(Username newUsername) {
        if (newUsername == null) throw new IllegalArgumentException("Username cannot be null");
        this.username = newUsername;

        // spec: αν αλλάξει username -> invalidate token
        invalidateSession();
    }

    public void changePassword(HashedPassword newPassword) {
        if (newPassword == null) throw new IllegalArgumentException("Password cannot be null");
        this.password = newPassword;
        this.failedAttempts = 0;

        // spec: password change -> invalidate token (always)
        invalidateSession();
    }

    public void changeRole(BaseRole newRole) {
        if (newRole == null) throw new IllegalArgumentException("Role cannot be null");
        this.baseRole = newRole;
    }

    public void activate() {
        this.active = true;
        this.failedAttempts = 0;
    }

    public void deactivate() {
        this.active = false;

        // spec: deactivation invalidates token
        invalidateSession();
    }

    /** spec: 3 consecutive failures => DEACTIVATE */
    public void registerFailedLogin() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            deactivate();
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    /** Καλείται στο επιτυχές login */
    public void startSession(String jti) {
        if (jti == null || jti.isBlank()) throw new IllegalArgumentException("jti cannot be blank");
        this.currentJti = jti;
        this.lastLoginAt = Instant.now();
        this.failedAttempts = 0;
    }

    public void invalidateSession() {
        this.currentJti = null;
    }

    // Getters
    public UserId id() { return id; }
    public BaseRole baseRole() { return baseRole; }
    public Username username() { return username; }
    public HashedPassword password() { return password; }
    public String fullName() { return fullName; }
    public boolean isActive() { return active; }
    public int failedAttempts() { return failedAttempts; }

    public String currentJti() { return currentJti; }
    public Instant lastLoginAt() { return lastLoginAt; }
}
