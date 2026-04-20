package com.pismochallenge.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pismochallenge.api.dto.response.ErrorResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (rateLimiter.acquirePermission()) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("[RATE-LIMITER] Request rejected: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(429);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(),
                    new ErrorResponse(429, "Too Many Requests", "Rate limit exceeded. Please try again later."));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }
}
