package com.luisovando.payout_service.application.usecase.createpayout;

import com.luisovando.payout_service.infrastructure.persistence.entity.PayoutEntity;
import com.luisovando.payout_service.infrastructure.persistence.repository.PayoutRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CreatePayoutUseCase {
    private final PayoutRepository payoutRepository;
    private static final String INITIAL_STATUS = "CREATED";

    public CreatePayoutUseCase(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    private void validate(CreatePayoutCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.amount(), "amount is required");
        Objects.requireNonNull(command.currency(), "currency is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        if (command.currency().isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }

        if (command.currency().trim().toUpperCase().length() != 3) {
            throw new IllegalArgumentException("currency must be ISO-4217 (3 chars)");
        }

        if (command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }

        if (command.idempotencyKey().length() > 128) {
            throw new IllegalArgumentException("idempotencyKey max length is 128");
        }
    }

    /**
     * Executes the payout creation use case.
     */
    public CreatePayoutResult execute(CreatePayoutCommand command) {
        this.validate(command);

        Optional<PayoutEntity> payout = this.payoutRepository.findByCompanyIdAndIdempotencyKey(command.companyId(), command.idempotencyKey());

        if (payout.isPresent()) {
            return new CreatePayoutResult(payout.get().getId(), payout.get().getStatus());
        }

        PayoutEntity newPayout = PayoutEntity.createNew(
                command.companyId(),
                command.amount(),
                command.currency(),
                INITIAL_STATUS,
                command.idempotencyKey()
        );

        PayoutEntity saved = this.payoutRepository.save(newPayout);

        return new CreatePayoutResult(saved.getId(), saved.getStatus());
    }
}
