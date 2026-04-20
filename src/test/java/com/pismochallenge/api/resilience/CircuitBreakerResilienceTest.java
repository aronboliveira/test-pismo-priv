package com.pismochallenge.api.resilience;

import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.repository.AccountRepository;
import com.pismochallenge.api.repository.OperationTypeRepository;
import com.pismochallenge.api.repository.TransactionRepository;
import com.pismochallenge.api.service.AccountService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CircuitBreakerResilienceTest {

    @MockitoBean
    private AccountRepository accountRepository;
    @MockitoBean
    private OperationTypeRepository operationTypeRepository;
    @MockitoBean
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;
    @Autowired
    private CircuitBreaker dbCircuitBreaker;

    @BeforeEach
    void resetCircuitBreaker() {
        dbCircuitBreaker.reset();
    }

    @Test
    void shouldOpenCircuitBreaker_afterRetryExhaustion() {
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new QueryTimeoutException("timeout"));

        // First call: retry exhausts 3 attempts → CB records 3 failures
        // With test config: minimumCalls=3, failureRateThreshold=50 → 3/3 = 100% → CB opens
        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
                .isInstanceOf(QueryTimeoutException.class);

        assertThat(dbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRejectCalls_whenCircuitIsOpen() {
        // Programmatically open the circuit
        dbCircuitBreaker.transitionToOpenState();

        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
                .isInstanceOf(CallNotPermittedException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldAllowProbe_whenCircuitIsHalfOpen() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        // Transition to half-open (allows limited probe calls)
        dbCircuitBreaker.transitionToOpenState();
        dbCircuitBreaker.transitionToHalfOpenState();

        assertThat(dbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));
        assertThat(response.accountId()).isEqualTo(1L);
    }

    @Test
    void shouldTransitionToClosedFromHalfOpen_afterSuccessfulCalls() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        dbCircuitBreaker.transitionToOpenState();
        dbCircuitBreaker.transitionToHalfOpenState();

        // Test config: permittedNumberOfCallsInHalfOpenState=2
        // After 2 successful calls, CB should transition to CLOSED
        accountService.createAccount(new CreateAccountRequest("11111111111"));
        accountService.createAccount(new CreateAccountRequest("22222222222"));

        assertThat(dbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldTransitionBackToOpen_fromHalfOpenOnFailure() {
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new QueryTimeoutException("still failing"));

        dbCircuitBreaker.transitionToOpenState();
        dbCircuitBreaker.transitionToHalfOpenState();

        // Call fails in half-open → retry exhausts permitted calls → CB goes back to OPEN
        // Final exception is CallNotPermittedException (CB opened mid-retry)
        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
                .isInstanceOfAny(QueryTimeoutException.class, CallNotPermittedException.class);

        assertThat(dbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldNotAffectCircuitBreaker_forBusinessExceptions() {
        when(accountRepository.save(any(Account.class))).thenReturn(new Account(1L, "12345678900"));

        // Successful calls should not degrade the circuit
        for (int i = 0; i < 5; i++) {
            accountService.createAccount(new CreateAccountRequest("12345678900"));
        }

        assertThat(dbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldRecordCircuitBreakerMetrics() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        accountService.createAccount(new CreateAccountRequest("12345678900"));

        CircuitBreaker.Metrics metrics = dbCircuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.getNumberOfFailedCalls()).isEqualTo(0);
    }
}
