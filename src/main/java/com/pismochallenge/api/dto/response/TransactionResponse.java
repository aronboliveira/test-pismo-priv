package com.pismochallenge.api.dto.response;

import java.math.BigDecimal;

public record TransactionResponse(
    Long transactionId,
    Long accountId,
    Integer operationTypeId,
    BigDecimal amount
) {}
