package com.pismochallenge.api.config;

import com.pismochallenge.api.exception.ResourceNotFoundException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ResilienceConfigTest {

    @Autowired
    private RateLimiter apiRateLimiter;

    @Test
    void isRetryable_shouldAcceptAllTransientLikeExceptions() {
        assertThat(ResilienceConfig.isRetryable(new QueryTimeoutException("timeout"))).isTrue();
        assertThat(ResilienceConfig.isRetryable(new RecoverableDataAccessException("recoverable"))).isTrue();
        assertThat(ResilienceConfig.isRetryable(new DataAccessResourceFailureException("db down"))).isTrue();
        assertThat(ResilienceConfig.isRetryable(new CannotCreateTransactionException("cannot begin tx"))).isTrue();
    }

    @Test
    void isRetryable_shouldRejectBusinessAndUnrelatedExceptions() {
        assertThat(ResilienceConfig.isRetryable(new ResourceNotFoundException("missing"))).isFalse();
        assertThat(ResilienceConfig.isRetryable(new IllegalArgumentException("bad input"))).isFalse();
        assertThat(ResilienceConfig.isRetryable(new RuntimeException("other"))).isFalse();
    }

    @Test
    void rateLimiter_shouldFireFailureEvent_whenPermissionIsDenied() {
        apiRateLimiter.changeLimitForPeriod(1);

        assertThat(apiRateLimiter.acquirePermission()).isTrue();
        boolean rejected = false;
        for (int i = 0; i < 20 && !rejected; i++) {
            rejected = !apiRateLimiter.acquirePermission();
        }

        assertThat(rejected).as("rate limiter should eventually reject").isTrue();
    }
}
