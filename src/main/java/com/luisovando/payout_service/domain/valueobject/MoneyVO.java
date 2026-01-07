package com.luisovando.payout_service.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public record MoneyVO(BigDecimal amount, CurrencyVO currency) {
    public MoneyVO {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
    }

    public static MoneyVO of(BigDecimal amount, String currency) {
        return new MoneyVO(amount, CurrencyVO.of(currency));
    }
}
