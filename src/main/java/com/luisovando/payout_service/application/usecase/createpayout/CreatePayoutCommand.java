package com.luisovando.payout_service.application.usecase.createpayout;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePayoutCommand(
        UUID companyId,
        BigDecimal amount,
        String currency,
        String idempotencyKey
) {
}
