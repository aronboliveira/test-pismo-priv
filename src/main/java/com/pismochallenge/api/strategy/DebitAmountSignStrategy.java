package com.pismochallenge.api.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DebitAmountSignStrategy implements AmountSignStrategy {

    static final Set<Integer> DEBIT_OPERATION_TYPE_IDS = Set.of(1, 2, 3);

    @Override
    public boolean appliesTo(int operationTypeId) {
        return DEBIT_OPERATION_TYPE_IDS.contains(operationTypeId);
    }

    @Override
    public BigDecimal apply(BigDecimal positiveAmount) {
        return positiveAmount.negate();
    }
}
