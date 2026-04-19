package com.pismochallenge.api.service;

import com.pismochallenge.api.dto.request.CreateAccountRequest;
import com.pismochallenge.api.dto.response.AccountResponse;
import com.pismochallenge.api.entity.Account;
import com.pismochallenge.api.exception.ResourceNotFoundException;
import com.pismochallenge.api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldReturnAccountResponse() {
        Account saved = new Account(1L, "12345678900");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.createAccount(new CreateAccountRequest("12345678900"));

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccount_shouldReturnAccountResponse_whenExists() {
        Account account = new Account(1L, "12345678900");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(1L);

        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.documentNumber()).isEqualTo("12345678900");
    }

    @Test
    void getAccount_shouldThrowException_whenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Account not found");
    }
}
