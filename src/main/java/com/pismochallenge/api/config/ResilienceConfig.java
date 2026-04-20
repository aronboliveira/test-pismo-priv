package com.pismochallenge.api.config;

import com.pismochallenge.api.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);
        private final ResilienceProperties properties;

        public ResilienceConfig(ResilienceProperties properties) {
                this.properties = properties;
        }

    @Bean
        public Retry dbRetry() {
                ResilienceProperties.Retry retryProperties = properties.getRetry();

        RetryConfig config = RetryConfig.custom()
                                .maxAttempts(retryProperties.getMaxAttempts())
                                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                                                retryProperties.getInitialIntervalMs(),
                                                retryProperties.getMultiplier()))
                .retryOnException(ResilienceConfig::isRetryable)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("dbRetry");

        retry.getEventPublisher()
                .onRetry(event -> log.warn("[RETRY] Attempt #{} for '{}' after {}ms — cause: {}",
                        event.getNumberOfRetryAttempts(),
                        event.getName(),
                        event.getWaitInterval().toMillis(),
                        event.getLastThrowable().getMessage()))
                .onSuccess(event -> {
                    if (event.getNumberOfRetryAttempts() > 0) {
                        log.info("[RETRY] '{}' succeeded after {} attempt(s)",
                                event.getName(), event.getNumberOfRetryAttempts());
                    }
                })
                .onError(event -> log.error("[RETRY] '{}' exhausted after {} attempt(s) — final cause: {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().getMessage()));

        return retry;
    }

    @Bean
        public CircuitBreaker dbCircuitBreaker() {
                ResilienceProperties.CircuitBreaker circuitBreakerProperties = properties.getCircuitBreaker();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                                .failureRateThreshold(circuitBreakerProperties.getFailureRateThreshold())
                                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperties.getWaitDurationOpenS()))
                                .slidingWindowSize(circuitBreakerProperties.getSlidingWindowSize())
                                .minimumNumberOfCalls(circuitBreakerProperties.getMinimumCalls())
                                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperties.getPermittedHalfOpenCalls())
                .recordException(ResilienceConfig::isRetryable)
                .ignoreExceptions(ResourceNotFoundException.class, DataIntegrityViolationException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("dbCircuitBreaker");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("[CIRCUIT-BREAKER] State transition: {}",
                        event.getStateTransition()))
                .onCallNotPermitted(event -> log.warn("[CIRCUIT-BREAKER] Call rejected — circuit OPEN for '{}'",
                        event.getCircuitBreakerName()))
                .onFailureRateExceeded(event -> log.error("[CIRCUIT-BREAKER] Failure rate {}% exceeded threshold for '{}'",
                        event.getFailureRate(), event.getCircuitBreakerName()));

        return circuitBreaker;
    }

    @Bean
        public RateLimiter apiRateLimiter() {
                ResilienceProperties.RateLimiter rateLimiterProperties = properties.getRateLimiter();

        RateLimiterConfig config = RateLimiterConfig.custom()
                                .limitForPeriod(rateLimiterProperties.getLimitPerPeriod())
                                .limitRefreshPeriod(Duration.ofMillis(rateLimiterProperties.getPeriodMs()))
                                .timeoutDuration(Duration.ofMillis(rateLimiterProperties.getTimeoutMs()))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = registry.rateLimiter("apiRateLimiter");

        rateLimiter.getEventPublisher()
                .onFailure(event -> log.warn("[RATE-LIMITER] Request rejected for '{}'",
                        event.getRateLimiterName()));

        return rateLimiter;
    }

    static boolean isRetryable(Throwable ex) {
        return ex instanceof TransientDataAccessException
                || ex instanceof RecoverableDataAccessException
                || ex instanceof DataAccessResourceFailureException
                || ex instanceof CannotCreateTransactionException;
    }
}
