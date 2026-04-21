package com.pismochallenge.api.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AmountSignResolver {

    private final List<AmountSignStrategy> strategies;

    public AmountSignResolver(List<AmountSignStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public BigDecimal normalize(int operationTypeId, BigDecimal positiveAmount) {
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(operationTypeId))
                .findFirst()
                .map(strategy -> strategy.apply(positiveAmount))
                .orElseThrow(() -> new IllegalStateException(
                        "No AmountSignStrategy registered for operation type id: " + operationTypeId));
    }
}
