package com.pismochallenge.api.resilience;

import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.request.CreateTransactionRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.dto.response.TransactionResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.entity.OperationType;
import com.pismochallenge.api.entity.Transaction;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import com.pismochallenge.api.repository.OperationTypeRepository;
import com.pismochallenge.api.repository.TransactionRepository;
import com.pismochallenge.api.service.AccountService;
import com.pismochallenge.api.service.TransactionService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class RetryResilienceTest {

    @MockitoBean
    private AccountRepository accountRepository;
    @MockitoBean
    private OperationTypeRepository operationTypeRepository;
    @MockitoBean
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CircuitBreaker dbCircuitBreaker;

    @BeforeEach
    void resetState() {
        dbCircuitBreaker.reset();
    }

    @Test
    void shouldRetryOnTransientFailure_andSucceedOnSecondAttempt() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new QueryTimeoutException("Connection timeout"))
                .thenReturn(saved);

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void shouldRetryOnTransientResourceException_andSucceed() {
        Account saved = new Account(2L, "99999999999");
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new TransientDataAccessResourceException("JDBC connection reset"))
                .thenThrow(new TransientDataAccessResourceException("JDBC connection reset"))
                .thenReturn(saved);

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("99999999999"));

        assertThat(response.accountId()).isEqualTo(2L);
        verify(accountRepository, times(3)).save(any(Account.class));
    }

    @Test
    void shouldExhaustRetries_afterMaxAttempts() {
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new QueryTimeoutException("Connection timeout"));

        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
                .isInstanceOf(QueryTimeoutException.class);

        verify(accountRepository, times(3)).save(any(Account.class));
    }

    @Test
    void shouldNotRetry_onBusinessException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    void shouldNotRetry_onDataIntegrityViolation() {
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate document_number"));

        assertThatThrownBy(() -> accountService.createAccount(new CreateAccountRequest("12345678900")))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldRetryTransactionCreation_onTransientFailure() {
        Account account = new Account(1L, "12345678900");
        OperationType opType = new OperationType(4, "PAYMENT");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4)).thenReturn(Optional.of(opType));
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new QueryTimeoutException("DB timeout"))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.setTransactionId(1L);
                    return t;
                });

        TransactionResponse response = transactionService.createTransaction(
                new CreateTransactionRequest(1L, 4, new BigDecimal("100.00")));

        assertThat(response.transactionId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void shouldVerifyExponentialBackoff_timing() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new QueryTimeoutException("timeout 1"))
                .thenThrow(new QueryTimeoutException("timeout 2"))
                .thenReturn(saved);

        long startTime = System.currentTimeMillis();
        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));
        long elapsed = System.currentTimeMillis() - startTime;

        long minBackoffMs = 50L + 75L - 45L;
        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(elapsed).isGreaterThanOrEqualTo(minBackoffMs);
        verify(accountRepository, times(3)).save(any(Account.class));
    }
}
