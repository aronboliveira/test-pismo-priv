package com.pismochallenge.api.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseResilienceAspect {

    private static final Logger log = LoggerFactory.getLogger(DatabaseResilienceAspect.class);

    private final Retry dbRetry;
    private final CircuitBreaker dbCircuitBreaker;

    public DatabaseResilienceAspect(Retry dbRetry, CircuitBreaker dbCircuitBreaker) {
        this.dbRetry = dbRetry;
        this.dbCircuitBreaker = dbCircuitBreaker;
    }

    @Around("execution(public * com.pismochallenge.api.service.*.*(..))")
    public Object applyResilience(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.debug("[RESILIENCE] Applying retry + circuit-breaker to {}", method);

        try {
            return Retry.decorateCheckedSupplier(dbRetry,
                    CircuitBreaker.decorateCheckedSupplier(dbCircuitBreaker,
                            pjp::proceed
                    )
            ).get();
        } catch (Throwable t) {
            log.error("[RESILIENCE] Final failure for {}: {}", method, t.getMessage());
            throw t;
        }
    }
}
