package com.eva.evagita.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    void shouldNormalizeEmailAndBlockAfterLimitThenRecover() {
        MutableClock clock = new MutableClock();
        LoginRateLimiter limiter = new LoginRateLimiter(
                3, Duration.ofSeconds(10), Duration.ofSeconds(20), 100, clock);

        limiter.tryAcquire(" User@Example.com ", "10.0.0.1");
        limiter.recordFailure(" User@Example.com ", "10.0.0.1");
        limiter.tryAcquire("user@example.com", "10.0.0.1");
        limiter.recordFailure("user@example.com", "10.0.0.1");
        limiter.tryAcquire("USER@example.com", "10.0.0.1");
        limiter.recordFailure("USER@example.com", "10.0.0.1");

        assertThat(limiter.isBlocked("user@example.com", "10.0.0.1")).isTrue();

        clock.advanceSeconds(21);

        assertThat(limiter.isBlocked("user@example.com", "10.0.0.1")).isFalse();
    }

    @Test
    void shouldKeepIpCountersSeparateAndClearSuccessfulLogin() {
        MutableClock clock = new MutableClock();
        LoginRateLimiter limiter = new LoginRateLimiter(
                2, Duration.ofSeconds(10), Duration.ofSeconds(20), 100, clock);

        limiter.tryAcquire("user@example.com", "10.0.0.1");
        limiter.recordFailure("user@example.com", "10.0.0.1");
        limiter.tryAcquire("user@example.com", "10.0.0.1");
        limiter.recordFailure("user@example.com", "10.0.0.1");

        assertThat(limiter.isBlocked("user@example.com", "10.0.0.1")).isTrue();
        assertThat(limiter.isBlocked("user@example.com", "10.0.0.2")).isFalse();

        limiter.recordSuccess("user@example.com", "10.0.0.2");
        assertThat(limiter.entryCount()).isEqualTo(2);
    }

    @Test
    void shouldKeepStateBounded() {
        MutableClock clock = new MutableClock();
        LoginRateLimiter limiter = new LoginRateLimiter(
                2, Duration.ofSeconds(10), Duration.ofSeconds(20), 3, clock);

        for (int index = 0; index < 20; index++) {
            limiter.tryAcquire("user" + index + "@example.com", "10.0.0.1");
            limiter.recordFailure("user" + index + "@example.com", "10.0.0.1");
        }

        assertThat(limiter.entryCount()).isLessThanOrEqualTo(3);
    }

    @Test
    void shouldReserveAttemptsAtomicallyUnderConcurrency() throws Exception {
        LoginRateLimiter limiter = new LoginRateLimiter(
                3, Duration.ofSeconds(10), Duration.ofSeconds(20), 100,
                new MutableClock());
        ExecutorService executor = Executors.newFixedThreadPool(12);
        CountDownLatch start = new CountDownLatch(1);

        var results = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
        for (int index = 0; index < 12; index++) {
            results.add(executor.submit(() -> {
                start.await();
                return limiter.tryAcquire("parallel@example.com", "10.0.0.1");
            }));
        }
        start.countDown();

        long allowed = 0;
        for (var result : results) {
            if (result.get()) {
                allowed++;
            }
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(1, TimeUnit.SECONDS)).isTrue();
        assertThat(allowed).isEqualTo(3);
        assertThat(limiter.isBlocked("parallel@example.com", "10.0.0.1"))
                .isTrue();
    }

    @Test
    void shouldCleanExpiredEntriesBelowCapacity() {
        MutableClock clock = new MutableClock();
        LoginRateLimiter limiter = new LoginRateLimiter(
                3, Duration.ofSeconds(10), Duration.ofSeconds(20), 100, clock);

        limiter.tryAcquire("stale@example.com", "10.0.0.1");
        assertThat(limiter.entryCount()).isEqualTo(2);
        clock.advanceSeconds(11);

        assertThat(limiter.isBlocked("other@example.com", "10.0.0.2")).isFalse();
        assertThat(limiter.entryCount()).isEqualTo(0);
    }

    @Test
    void shouldPreserveActiveBlocksWhenCapacityIsFull() {
        MutableClock clock = new MutableClock();
        LoginRateLimiter limiter = new LoginRateLimiter(
                1, Duration.ofSeconds(10), Duration.ofSeconds(20), 2, clock);

        assertThat(limiter.tryAcquire("blocked@example.com", "10.0.0.1"))
                .isTrue();
        assertThat(limiter.tryAcquire("other@example.com", "10.0.0.2"))
                .isFalse();
        assertThat(limiter.isBlocked("blocked@example.com", "10.0.0.1"))
                .isTrue();
    }

    @Test
    void shouldSeparateEmailAndIpLimitsAndResetEmailAfterSuccess() {
        LoginRateLimiter limiter = new LoginRateLimiter(
                2, Duration.ofSeconds(10), Duration.ofSeconds(20), 100,
                new MutableClock());

        limiter.tryAcquire("first@example.com", "10.0.0.1");
        limiter.tryAcquire("first@example.com", "10.0.0.1");
        assertThat(limiter.tryAcquire("second@example.com", "10.0.0.1"))
                .isTrue();
        assertThat(limiter.tryAcquire("first@example.com", "10.0.0.2"))
                .isTrue();

        limiter.recordSuccess("second@example.com", "10.0.0.1");
        assertThat(limiter.tryAcquire("second@example.com", "10.0.0.1"))
                .isTrue();
    }

    @Test
    void shouldCountOnlyExplicitAuthenticationFailuresInSoftEmailBucket() {
        LoginRateLimiter limiter = new LoginRateLimiter(
                5, Duration.ofSeconds(10), Duration.ofSeconds(20), 100, 2,
                new MutableClock());

        limiter.tryAcquire("user@example.com", "10.0.0.1");
        assertThat(limiter.requiresCaptcha("user@example.com")).isFalse();

        limiter.recordFailure(" USER@example.com ", "10.0.0.1");
        assertThat(limiter.requiresCaptcha("user@example.com")).isFalse();
        limiter.recordFailure("user@example.com", "10.0.0.2");
        assertThat(limiter.requiresCaptcha("user@example.com")).isTrue();

        limiter.recordSuccess("user@example.com", "10.0.0.2");
        assertThat(limiter.requiresCaptcha("user@example.com")).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-01-01T00:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }
    }
}
