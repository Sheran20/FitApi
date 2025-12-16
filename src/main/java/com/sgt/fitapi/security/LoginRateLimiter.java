package com.sgt.fitapi.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

// in memory rate limiter
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, AttemptBucket> buckets = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    public void throwIfBlocked(String key) {
        AttemptBucket bucket = buckets.get(key);
        if (bucket == null) {
            return;
        }
        if (bucket.isBlocked(clock.instant())) {
            throw new TooManyLoginAttemptsException("Too many login attempts. Please wait and try again.");
        }
    }

    public void onFailure(String key) {
        Instant now = clock.instant();
        buckets.compute(key, (k, bucket) -> {
            if (bucket == null) {
                return new AttemptBucket(now, 1);
            }
            if (bucket.windowExpired(now)) {
                return new AttemptBucket(now, 1);
            }
            return new AttemptBucket(bucket.windowStart, bucket.count + 1);
        });
    }

    public void onSuccess(String key) {
        buckets.remove(key);
    }

    private record AttemptBucket(Instant windowStart, int count) {
        boolean windowExpired(Instant now) {
            return now.isAfter(windowStart.plus(WINDOW));
        }

        boolean isBlocked(Instant now) {
            if (windowExpired(now)) {
                return false;
            }
            return count >= MAX_ATTEMPTS;
        }
    }
}
