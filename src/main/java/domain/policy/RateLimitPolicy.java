package domain.policy;

import java.time.Duration;

public record RateLimitPolicy(int maxFailedLoginAttempts , Duration lockoutDuration , int loginAttemptsPerMinute) {

    public RateLimitPolicy {
        if (maxFailedLoginAttempts <= 0) throw new IllegalArgumentException("maxFailedLoginAttempts must be > 0");
        if (lockoutDuration == null || lockoutDuration.isNegative() || lockoutDuration.isZero()) throw new IllegalArgumentException("lockoutDuration must be positive");
        if (loginAttemptsPerMinute <= 0) throw new IllegalArgumentException("loginAttemptsPerMinute must be > 0");
    }


    public static RateLimitPolicy defaults() {
        return new RateLimitPolicy(5, Duration.ofMinutes(15), 20);
    }


    public boolean shouldLock(int failedAttempts) {
        return failedAttempts >= maxFailedLoginAttempts;
    }

    public int remainingAttempts(int failedAttempts) {
        return Math.max(0, maxFailedLoginAttempts - failedAttempts);
    }


}
