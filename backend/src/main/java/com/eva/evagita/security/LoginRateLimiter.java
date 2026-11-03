package com.eva.evagita.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Bounded in-memory limiter for login attempts. State is local to one
 * instance and is lost on restart; multiple instances do not share counters.
 */
@Service
public class LoginRateLimiter {

    private static final int IP_ATTEMPT_MULTIPLIER = 5;

    private final int maxAttempts;
    private final int ipMaxAttempts;
    private final Duration window;
    private final Duration blockDuration;
    private final Duration ipBlockDuration;
    private final int maxEntries;
    private final int emailSoftThreshold;
    private final Clock clock;
    private final Map<String, AttemptState> attempts = new LinkedHashMap<>();
    private final Map<String, SoftEmailState> emailFailures = new LinkedHashMap<>();

    @Autowired
    public LoginRateLimiter(
            @Value("${security.login.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${security.login.rate-limit.window-seconds:60}") long windowSeconds,
            @Value("${security.login.rate-limit.block-seconds:300}") long blockSeconds,
            @Value("${security.login.rate-limit.max-entries:10000}") int maxEntries,
            @Value("${smartcaptcha.email-soft-threshold:3}") int emailSoftThreshold
    ) {
        this(maxAttempts, Duration.ofSeconds(windowSeconds),
                Duration.ofSeconds(blockSeconds), maxEntries, emailSoftThreshold,
                Clock.systemUTC());
    }

    LoginRateLimiter(
            int maxAttempts,
            Duration window,
            Duration blockDuration,
            int maxEntries,
            Clock clock
    ) {
        this(maxAttempts, window, blockDuration, maxEntries, 3, clock);
    }

    LoginRateLimiter(
            int maxAttempts,
            Duration window,
            Duration blockDuration,
            int maxEntries,
            int emailSoftThreshold,
            Clock clock
    ) {
        if (maxAttempts < 1 || window.isNegative() || window.isZero()
                || blockDuration.isNegative() || blockDuration.isZero()
                || maxEntries < 2 || emailSoftThreshold < 1) {
            throw new IllegalArgumentException("Invalid login rate limiter configuration");
        }
        this.maxAttempts = maxAttempts;
        this.ipMaxAttempts = Math.max(maxAttempts + 1, maxAttempts * IP_ATTEMPT_MULTIPLIER);
        this.window = window;
        this.blockDuration = blockDuration;
        this.ipBlockDuration = window;
        this.maxEntries = maxEntries;
        this.emailSoftThreshold = emailSoftThreshold;
        this.clock = clock;
    }

    /**
     * Atomically checks both hard buckets and reserves one login-attempt slot.
     * A successful authentication must call {@link #recordSuccess(String, String)}
     * to clear the account-specific buckets.
     */
    public synchronized boolean tryAcquire(String email, String clientIp) {
        long now = clock.millis();
        cleanupExpired(now);

        String emailKey = key(email, clientIp);
        String ipKey = ipKey(clientIp);
        AttemptState existingEmail = attempts.get(emailKey);
        AttemptState existingIp = attempts.get(ipKey);

        if (isBlocked(existingEmail, now) || isBlocked(existingIp, now)) {
            touch(existingEmail, now);
            touch(existingIp, now);
            return false;
        }

        if (!ensureCapacity(emailKey, ipKey, now)) {
            return false;
        }

        AttemptState emailState = attempts.computeIfAbsent(
                emailKey,
                ignored -> new AttemptState(now + window.toMillis())
        );
        AttemptState ipState = attempts.computeIfAbsent(
                ipKey,
                ignored -> new AttemptState(now + window.toMillis())
        );

        increment(emailState, maxAttempts, blockDuration, now);
        increment(ipState, ipMaxAttempts, ipBlockDuration, now);
        return true;
    }

    public synchronized boolean requiresCaptcha(String email) {
        cleanupExpired(clock.millis());
        SoftEmailState state = emailFailures.get(normalizeEmail(email));
        return state != null && state.failures >= emailSoftThreshold;
    }

    public synchronized boolean isBlocked(String email, String clientIp) {
        long now = clock.millis();
        cleanupExpired(now);
        return isBlocked(attempts.get(key(email, clientIp)), now)
                || isBlocked(attempts.get(ipKey(clientIp)), now);
    }

    /**
     * Retained for callers/tests using the previous limiter API. New login
     * flows should use tryAcquire so checking and reservation stay atomic.
     */
    public synchronized void recordFailure(String email, String clientIp) {
        long now = clock.millis();
        cleanupExpired(now);
        incrementSoftEmail(normalizeEmail(email), now);
    }

    public synchronized void recordSuccess(String email, String clientIp) {
        attempts.remove(key(email, clientIp));
        emailFailures.remove(normalizeEmail(email));
        cleanupExpired(clock.millis());
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    String key(String email, String clientIp) {
        return normalizeEmail(email) + '\u0000' + normalizeIp(clientIp);
    }

    int entryCount() {
        return attempts.size();
    }

    private String ipKey(String clientIp) {
        return "ip\u0000" + normalizeIp(clientIp);
    }

    private String normalizeIp(String clientIp) {
        return clientIp == null || clientIp.isBlank()
                ? "unknown"
                : clientIp.trim();
    }

    private boolean ensureCapacity(String emailKey, String ipKey, long now) {
        int missing = (attempts.containsKey(emailKey) ? 0 : 1)
                + (attempts.containsKey(ipKey) ? 0 : 1);
        while (attempts.size() + missing > maxEntries) {
            String evictable = oldestNonBlockedKey(now, emailKey, ipKey);
            if (evictable == null) {
                return false;
            }
            attempts.remove(evictable);
            missing = (attempts.containsKey(emailKey) ? 0 : 1)
                    + (attempts.containsKey(ipKey) ? 0 : 1);
        }
        return true;
    }

    private String oldestNonBlockedKey(long now, String emailKey, String ipKey) {
        String candidate = null;
        long oldest = Long.MAX_VALUE;
        for (Map.Entry<String, AttemptState> entry : attempts.entrySet()) {
            if (entry.getKey().equals(emailKey) || entry.getKey().equals(ipKey)) {
                continue;
            }
            AttemptState state = entry.getValue();
            if (!isBlocked(state, now) && state.lastTouched < oldest) {
                candidate = entry.getKey();
                oldest = state.lastTouched;
            }
        }
        return candidate;
    }

    private void increment(
            AttemptState state,
            int limit,
            Duration currentBlockDuration,
            long now
    ) {
        if (state.windowEnds <= now
                || (state.blockedUntil > 0 && state.blockedUntil <= now)) {
            state.failures = 0;
            state.windowEnds = now + window.toMillis();
            state.blockedUntil = 0;
        }
        state.failures++;
        state.lastTouched = now;
        if (state.failures >= limit) {
            state.blockedUntil = now + currentBlockDuration.toMillis();
        }
    }

    private boolean isBlocked(AttemptState state, long now) {
        return state != null && state.blockedUntil > now;
    }

    private void touch(AttemptState state, long now) {
        if (state != null) {
            state.lastTouched = now;
        }
    }

    private void cleanupExpired(long now) {
        Iterator<Map.Entry<String, AttemptState>> iterator = attempts.entrySet().iterator();
        while (iterator.hasNext()) {
            AttemptState state = iterator.next().getValue();
            if (state.windowEnds <= now && state.blockedUntil <= now) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<String, SoftEmailState>> emailIterator =
                emailFailures.entrySet().iterator();
        while (emailIterator.hasNext()) {
            SoftEmailState state = emailIterator.next().getValue();
            if (state.windowEnds <= now) {
                emailIterator.remove();
            }
        }
    }

    private void incrementSoftEmail(String email, long now) {
        SoftEmailState state = emailFailures.get(email);
        if (state == null) {
            if (emailFailures.size() >= maxEntries) {
                String oldest = emailFailures.keySet().stream().findFirst().orElse(null);
                if (oldest != null) {
                    emailFailures.remove(oldest);
                }
            }
            state = new SoftEmailState(now + window.toMillis());
            emailFailures.put(email, state);
        }
        if (state.windowEnds <= now) {
            state.failures = 0;
            state.windowEnds = now + window.toMillis();
        }
        state.failures++;
        state.lastTouched = now;
    }

    private static final class AttemptState {
        private long windowEnds;
        private long blockedUntil;
        private long lastTouched;
        private int failures;

        private AttemptState(long windowEnds) {
            this.windowEnds = windowEnds;
        }
    }

    private static final class SoftEmailState {
        private long windowEnds;
        private long lastTouched;
        private int failures;

        private SoftEmailState(long windowEnds) {
            this.windowEnds = windowEnds;
        }
    }
}
