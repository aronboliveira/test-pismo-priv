package com.pismochallenge.api.strategy;

import java.math.BigDecimal;

/**
 * Sign-normalization rule for transaction amounts, dispatched per operation type.
 *
 * <p>Implementations encapsulate the invariant that the API contract requires the
 * client to send a positive amount, while the persisted value carries the sign
 * dictated by the operation type (debit operations are stored as negative;
 * credit operations as positive).
 *
 * <p>Adding a new operation type is open-closed: register a new strategy bean
 * declaring the operation type IDs it covers; no existing component changes.
 */
public interface AmountSignStrategy {

    boolean appliesTo(int operationTypeId);

    BigDecimal apply(BigDecimal positiveAmount);
}
