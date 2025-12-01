package domain.policy;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class TokenPolicy {

    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Duration allowedClockSkew;
    private final int maxActiveSessionsPerUser;
    private final String issuer;


    public TokenPolicy(
            Duration accessTtl,
                       Duration refreshTtl,
                       Duration allowedClockSkew,
                       int maxActiveSessionsPerUser,
                       String issuer
    ) {
        this.accessTtl = Objects.requireNonNull(accessTtl);
        this.refreshTtl = Objects.requireNonNull(refreshTtl);
        this.allowedClockSkew = Objects.requireNonNull(allowedClockSkew);
        if (maxActiveSessionsPerUser <= 0) throw new IllegalArgumentException("maxActiveSessionsPerUser must be > 0");
        this.maxActiveSessionsPerUser = maxActiveSessionsPerUser;
        this.issuer = Objects.requireNonNullElse(issuer, "cinema-api");
    }

    public static TokenPolicy defaults() {
        return new TokenPolicy(Duration.ofMinutes(30),
                Duration.ofDays(14),
                Duration.ofSeconds(60),
                5,
                "cinema-api");
    }


    public Instant accessExpiry(Instant now) { return now.plus(accessTtl); }
    public Instant refreshExpiry(Instant now) { return now.plus(refreshTtl); }
    public boolean isExpired(Instant issuedAt, Instant now) {
        return issuedAt.plus(accessTtl).isBefore(now.minus(allowedClockSkew));
    }



    public Duration accessTtl() { return accessTtl; }
    public Duration refreshTtl() { return refreshTtl; }
    public Duration allowedClockSkew() { return allowedClockSkew; }
    public int maxActiveSessionsPerUser() { return maxActiveSessionsPerUser; }
    public String issuer() { return issuer; }


}
