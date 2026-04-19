package com.pismochallenge.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotNull(message = "account_id is required")
    Long accountId,

    @NotNull(message = "operation_type_id is required")
    Integer operationTypeId,

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    BigDecimal amount
) {}
