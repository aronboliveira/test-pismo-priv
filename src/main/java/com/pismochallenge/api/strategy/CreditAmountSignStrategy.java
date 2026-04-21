package com.pismochallenge.api.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class CreditAmountSignStrategy implements AmountSignStrategy {

    static final Set<Integer> CREDIT_OPERATION_TYPE_IDS = Set.of(4);

    @Override
    public boolean appliesTo(int operationTypeId) {
        return CREDIT_OPERATION_TYPE_IDS.contains(operationTypeId);
    }

    @Override
    public BigDecimal apply(BigDecimal positiveAmount) {
        return positiveAmount;
    }
}
