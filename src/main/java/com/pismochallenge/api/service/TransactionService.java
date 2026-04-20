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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OperationTypeRepository operationTypeRepository;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction: account_id={}, operation_type_id={}, amount={}",
                request.accountId(), request.operationTypeId(), request.amount());

        Account account = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Account not found with id: " + request.accountId()));

        OperationType operationType = operationTypeRepository.findById(request.operationTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Operation type not found with id: " + request.operationTypeId()));

        // Sign rule: types 1-3 (debit) are negative, type 4 (payment) is positive
        BigDecimal amount = request.amount();
        if (operationType.getOperationTypeId() <= 3) {
            amount = amount.negate();
        }

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setOperationType(operationType);
        transaction.setAmount(amount);
        transaction.setEventDate(OffsetDateTime.now());

        transaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", transaction.getTransactionId());

        return toResponse(transaction);
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getTransactionId(),
            transaction.getAccount().getAccountId(),
            transaction.getOperationType().getOperationTypeId(),
            transaction.getAmount()
        );
    }
}
