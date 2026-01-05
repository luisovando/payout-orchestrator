package com.luisovando.payout_service.infrastructure.persistence.repository;

import com.luisovando.payout_service.infrastructure.persistence.entity.PayoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<PayoutEntity, UUID> {
    Optional<PayoutEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);
}
