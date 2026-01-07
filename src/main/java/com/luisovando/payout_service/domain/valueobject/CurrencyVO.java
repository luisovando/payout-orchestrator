package com.luisovando.payout_service.domain.valueobject;

import java.util.Objects;


/**
 * Immutable value object representing a currency according to ISO-4217 standard.
 *
 * <p>A currency is a 3-letter alphabetic code as defined in ISO 4217.
 *
 * <p>Provides a safe way to handle currency values, validating their format and
 * ensuring immutability.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
 */
public record CurrencyVO(String value) {
    /**
     * Creates a new {@link CurrencyVO} instance.
     *
     * @throws NullPointerException if currency is null
     * @throws IllegalArgumentException if currency is blank or not a valid ISO-4217 code
     */
    public CurrencyVO {
        Objects.requireNonNull(value, "currency is required");

        String normalized = value.trim().toUpperCase();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }

        if (normalized.length() != 3) {
            throw new IllegalArgumentException("currency must be ISO-4217 (3 chars)");
        }

        try {
            java.util.Currency.getInstance(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("currency must be a valid ISO-4217 code");
        }

        value = normalized;
    }

    /**
     * Creates a new {@link CurrencyVO} instance.
     *
     * @param raw the raw currency value
     * @return the new {@link CurrencyVO} instance
     * @throws NullPointerException if currency is null
     * @throws IllegalArgumentException if currency is blank or not a valid ISO-4217 code
     */
    public static CurrencyVO of(String raw) {
        return new CurrencyVO(raw);
    }
}