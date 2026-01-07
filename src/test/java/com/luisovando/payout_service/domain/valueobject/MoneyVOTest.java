package com.luisovando.payout_service.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class MoneyVOTest {

    @Test
    void shouldCreateMoneyWithValidValues() {
        MoneyVO money = new MoneyVO(new BigDecimal("100.50"), new CurrencyVO("USD"));

        assertThat(money.amount()).isEqualByComparingTo("100.50");
        assertThat(money.currency().value()).isEqualTo("USD");
    }

    @Test
    void shouldNormalizeCurrency() {
        MoneyVO money = MoneyVO.of(new BigDecimal("100.50"), " usd ");

        assertThat(money.currency().value()).isEqualTo("USD");
        assertThat(money.amount()).isEqualByComparingTo("100.50");
    }

    @Test
    void shouldThrowWhenAmountIsZeroOrNegative() {
        assertThatThrownBy(() -> new MoneyVO(BigDecimal.ZERO, new CurrencyVO("USD")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than 0");

        assertThatThrownBy(() -> new MoneyVO(new BigDecimal("-1"), new CurrencyVO("USD")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than 0");
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> new MoneyVO(null, new CurrencyVO("USD")))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("amount is required");
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        assertThatThrownBy(() -> new MoneyVO(new BigDecimal("100.50"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currency is required");
    }

    @Test
    void shouldCreateMoneyUsingFactoryMethod() {
        MoneyVO money = MoneyVO.of(new BigDecimal("100.50"), "USD");

        assertThat(money.amount()).isEqualByComparingTo("100.50");
        assertThat(money.currency().value()).isEqualTo("USD");
    }
}