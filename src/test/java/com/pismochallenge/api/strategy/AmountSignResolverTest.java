package com.pismochallenge.api.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AmountSignResolverTest {

    private final AmountSignResolver resolver = new AmountSignResolver(
            List.of(new DebitAmountSignStrategy(), new CreditAmountSignStrategy()));

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void normalize_shouldNegateAmount_forDebitOperationTypes(int operationTypeId) {
        BigDecimal result = resolver.normalize(operationTypeId, new BigDecimal("100.00"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("-100.00"));
    }

    @Test
    void normalize_shouldKeepAmountPositive_forPaymentOperationType() {
        BigDecimal result = resolver.normalize(4, new BigDecimal("123.45"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void normalize_shouldThrowIllegalState_whenNoStrategyMatches() {
        assertThatThrownBy(() -> resolver.normalize(999, BigDecimal.TEN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("999");
    }

    @Test
    void normalize_shouldHandleZero() {
        assertThat(resolver.normalize(1, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resolver.normalize(4, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void normalize_shouldPreserveScale_forDebitOperation() {
        BigDecimal input = new BigDecimal("99.9999");
        BigDecimal result = resolver.normalize(2, input);

        assertThat(result).isEqualByComparingTo(new BigDecimal("-99.9999"));
        assertThat(result.scale()).isEqualTo(input.scale());
    }
}
