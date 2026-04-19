package com.pismochallenge.api.service;

import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Criando conta com document_number: {}", request.documentNumber());
        Account account = new Account();
        account.setDocumentNumber(request.documentNumber());
        account = accountRepository.save(account);
        log.info("Conta criada com ID: {}", account.getAccountId());
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        log.info("Buscando conta com ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        return toResponse(account);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getAccountId(), account.getDocumentNumber());
    }
}
