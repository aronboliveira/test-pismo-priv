package com.pismochallenge.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "resilience")
public class ResilienceProperties {

    private final Retry retry = new Retry();
    private final CircuitBreaker circuitBreaker = new CircuitBreaker();
    private final RateLimiter rateLimiter = new RateLimiter();

    public Retry getRetry() {
        return retry;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialIntervalMs = 200;
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalMs() {
            return initialIntervalMs;
        }

        public void setInitialIntervalMs(long initialIntervalMs) {
            this.initialIntervalMs = initialIntervalMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class CircuitBreaker {
        private float failureRateThreshold = 50;
        private long waitDurationOpenS = 30;
        private int slidingWindowSize = 10;
        private int minimumCalls = 5;
        private int permittedHalfOpenCalls = 3;

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public long getWaitDurationOpenS() {
            return waitDurationOpenS;
        }

        public void setWaitDurationOpenS(long waitDurationOpenS) {
            this.waitDurationOpenS = waitDurationOpenS;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumCalls() {
            return minimumCalls;
        }

        public void setMinimumCalls(int minimumCalls) {
            this.minimumCalls = minimumCalls;
        }

        public int getPermittedHalfOpenCalls() {
            return permittedHalfOpenCalls;
        }

        public void setPermittedHalfOpenCalls(int permittedHalfOpenCalls) {
            this.permittedHalfOpenCalls = permittedHalfOpenCalls;
        }
    }

    public static class RateLimiter {
        private int limitPerPeriod = 100;
        private long periodMs = 1000;
        private long timeoutMs = 500;

        public int getLimitPerPeriod() {
            return limitPerPeriod;
        }

        public void setLimitPerPeriod(int limitPerPeriod) {
            this.limitPerPeriod = limitPerPeriod;
        }

        public long getPeriodMs() {
            return periodMs;
        }

        public void setPeriodMs(long periodMs) {
            this.periodMs = periodMs;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}