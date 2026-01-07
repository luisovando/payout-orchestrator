package com.luisovando.payout_service.application.usecase.createpayout;

import com.luisovando.payout_service.domain.valueobject.MoneyVO;
import com.luisovando.payout_service.infrastructure.persistence.entity.PayoutEntity;
import com.luisovando.payout_service.infrastructure.persistence.repository.PayoutRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CreatePayoutUseCase {
    private final PayoutRepository payoutRepository;
    private static final String INITIAL_STATUS = "CREATED";
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "MXN", "EUR");

    public CreatePayoutUseCase(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    private void validate(CreatePayoutCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.money(), "money is required");
        Objects.requireNonNull(command.idempotencyKey(), "idempotencyKey is required");

        if (command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }

        if (!SUPPORTED_CURRENCIES.contains(command.money().currency().value())) {
            throw new IllegalArgumentException("currency not supported");
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
                command.money().amount(),
                command.money().currency().value(),
                INITIAL_STATUS,
                command.idempotencyKey()
        );

        PayoutEntity saved = this.payoutRepository.save(newPayout);

        return new CreatePayoutResult(saved.getId(), saved.getStatus());
    }
}
