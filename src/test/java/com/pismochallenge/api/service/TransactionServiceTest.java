package com.pismochallenge.api.service;

import com.pismochallenge.api.dto.request.CreateTransactionRequest;
import com.pismochallenge.api.dto.response.TransactionResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.entity.OperationType;
import com.pismochallenge.api.entity.Transaction;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import com.pismochallenge.api.repository.OperationTypeRepository;
import com.pismochallenge.api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationTypeRepository operationTypeRepository;

    @InjectMocks
    private TransactionService transactionService;

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void createTransaction_shouldNegateAmount_forDebitOperations(int opTypeId) {
        Account account = new Account(1L, "12345678900");
        OperationType opType = new OperationType(opTypeId, "DEBIT_TYPE");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(opTypeId)).thenReturn(Optional.of(opType));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setTransactionId(1L);
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(1L, opTypeId, new BigDecimal("100.00"));
        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.amount()).isNegative();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("-100.00"));
    }

    @Test
    void createTransaction_shouldKeepPositiveAmount_forPayment() {
        Account account = new Account(1L, "12345678900");
        OperationType opType = new OperationType(4, "PAYMENT");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(4)).thenReturn(Optional.of(opType));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setTransactionId(1L);
            return t;
        });

        CreateTransactionRequest request = new CreateTransactionRequest(1L, 4, new BigDecimal("123.45"));
        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response.amount()).isPositive();
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void createTransaction_shouldThrow_whenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(99L, 1, new BigDecimal("50.00"));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Account not found");
    }

    @Test
    void createTransaction_shouldThrow_whenOperationTypeNotFound() {
        Account account = new Account(1L, "12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationTypeRepository.findById(99)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(1L, 99, new BigDecimal("50.00"));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Operation type not found");
    }
}
