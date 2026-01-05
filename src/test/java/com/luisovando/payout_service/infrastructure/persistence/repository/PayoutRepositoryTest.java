package com.luisovando.payout_service.infrastructure.persistence.repository;

import com.luisovando.payout_service.infrastructure.persistence.entity.PayoutEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PayoutRepositoryTest {
    @Autowired
    private PayoutRepository payoutRepository;

    @Test
    void shouldSaveAndFindPayoutByCompanyAndIdempotencyKey() {

        UUID companyId = UUID.randomUUID();

        PayoutEntity payoutEntity = PayoutEntity.createNew(
                companyId,
                new BigDecimal("100.00"),
                "USD",
                "CREATED",
                "IDEMPOTENCY-123");

        payoutRepository.save(payoutEntity);

        Optional<PayoutEntity> result =
                payoutRepository.findByCompanyIdAndIdempotencyKey(
                        companyId,
                        "IDEMPOTENCY-123"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(payoutEntity.getId());
    }
}
