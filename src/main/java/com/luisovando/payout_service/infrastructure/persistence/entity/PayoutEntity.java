package com.luisovando.payout_service.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class that represents a payout record in the database.
 *
 * @since 1.0.0
 */
@Entity
@Table(name = "payouts")
public class PayoutEntity {
    protected PayoutEntity() {
    }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public static PayoutEntity createNew (
            UUID companyId,
            BigDecimal amount,
            String currency,
            String status,
            String idempotencyKey) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");

        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be ISO-4217 (3 chars)");
        }

        if (status.length() > 32) {
            throw new IllegalArgumentException("status max length is 32");
        }

        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException("idempotencyKey max length is 128");
        }

        PayoutEntity entity = new PayoutEntity();
        entity.id = UUID.randomUUID();
        entity.companyId = companyId;
        entity.amount = amount;
        entity.currency = currency;
        entity.status = status;
        entity.idempotencyKey = idempotencyKey;

        return  entity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}