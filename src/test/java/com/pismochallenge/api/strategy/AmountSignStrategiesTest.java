package com.pismochallenge.api.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class AmountSignStrategiesTest {

    private final DebitAmountSignStrategy debit = new DebitAmountSignStrategy();
    private final CreditAmountSignStrategy credit = new CreditAmountSignStrategy();

    @Test
    void debitStrategy_shouldApplyToDebitTypesOnly() {
        assertThat(debit.appliesTo(1)).isTrue();
        assertThat(debit.appliesTo(2)).isTrue();
        assertThat(debit.appliesTo(3)).isTrue();
        assertThat(debit.appliesTo(4)).isFalse();
        assertThat(debit.appliesTo(0)).isFalse();
        assertThat(debit.appliesTo(99)).isFalse();
    }

    @Test
    void debitStrategy_shouldNegate() {
        assertThat(debit.apply(new BigDecimal("50.00"))).isEqualByComparingTo(new BigDecimal("-50.00"));
        assertThat(debit.apply(new BigDecimal("-30.00"))).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void creditStrategy_shouldApplyOnlyToPayment() {
        assertThat(credit.appliesTo(4)).isTrue();
        assertThat(credit.appliesTo(1)).isFalse();
        assertThat(credit.appliesTo(2)).isFalse();
        assertThat(credit.appliesTo(3)).isFalse();
        assertThat(credit.appliesTo(0)).isFalse();
    }

    @Test
    void creditStrategy_shouldReturnAmountUnchanged() {
        BigDecimal amount = new BigDecimal("75.25");
        assertThat(credit.apply(amount)).isSameAs(amount);
    }
}
