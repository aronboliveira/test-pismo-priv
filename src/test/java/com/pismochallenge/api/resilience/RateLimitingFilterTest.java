package com.pismochallenge.api.resilience;

import com.pismochallenge.api.config.RateLimitingFilter;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private RateLimiter rateLimiter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();
        rateLimiter = RateLimiter.of("testRateLimiter", config);
        filter = new RateLimitingFilter(rateLimiter);
    }

    @Test
    void shouldAllowRequests_withinLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, filterChain);
            assertThat(response.getStatus()).isNotEqualTo(429);
        }
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, filterChain);
        }

        // Next request should be rate limited
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/accounts/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Too Many Requests");
        verify(filterChain, times(5)).doFilter(any(), any()); // Only 5 passed through
    }

    @Test
    void shouldReturn429WithErrorBody_containingCorrectStructure() throws Exception {
        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            filter.doFilter(new MockHttpServletRequest("GET", "/accounts/1"),
                    new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/transactions"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        String body = response.getContentAsString();
        assertThat(body).contains("429");
        assertThat(body).contains("Too Many Requests");
        assertThat(body).contains("Rate limit exceeded");
    }

    @Test
    void shouldNotFilter_actuatorEndpoints() throws Exception {
        // Exhaust the limit on API endpoints
        for (int i = 0; i < 5; i++) {
            filter.doFilter(new MockHttpServletRequest("GET", "/accounts/1"),
                    new MockHttpServletResponse(), filterChain);
        }

        // Actuator should bypass the filter entirely
        MockHttpServletRequest actuatorRequest = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse actuatorResponse = new MockHttpServletResponse();
        filter.doFilter(actuatorRequest, actuatorResponse, filterChain);

        assertThat(actuatorResponse.getStatus()).isNotEqualTo(429);
    }

    @Test
    void shouldNotFilter_swaggerEndpoints() throws Exception {
        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            filter.doFilter(new MockHttpServletRequest("GET", "/accounts/1"),
                    new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletRequest swaggerRequest = new MockHttpServletRequest("GET", "/swagger-ui.html");
        MockHttpServletResponse swaggerResponse = new MockHttpServletResponse();
        filter.doFilter(swaggerRequest, swaggerResponse, filterChain);

        assertThat(swaggerResponse.getStatus()).isNotEqualTo(429);
    }

    @Test
    void shouldNotFilter_openApiEndpoints() throws Exception {
        // Exhaust the limit
        for (int i = 0; i < 5; i++) {
            filter.doFilter(new MockHttpServletRequest("GET", "/accounts/1"),
                    new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletRequest apiDocsRequest = new MockHttpServletRequest("GET", "/v3/api-docs");
        MockHttpServletResponse apiDocsResponse = new MockHttpServletResponse();
        filter.doFilter(apiDocsRequest, apiDocsResponse, filterChain);

        assertThat(apiDocsResponse.getStatus()).isNotEqualTo(429);
    }

    @Test
    void shouldApplyRateLimit_acrossDifferentEndpoints() throws Exception {
        // Rate limiter is global — different endpoints share the same quota
        for (int i = 0; i < 3; i++) {
            filter.doFilter(new MockHttpServletRequest("GET", "/accounts/1"),
                    new MockHttpServletResponse(), filterChain);
        }
        for (int i = 0; i < 2; i++) {
            filter.doFilter(new MockHttpServletRequest("POST", "/transactions"),
                    new MockHttpServletResponse(), filterChain);
        }

        // Limit reached (5 total), next should be rejected regardless of endpoint
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST", "/accounts"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
    }
}
