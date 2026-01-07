package com.luisovando.payout_service.application.usecase.createpayout;

import com.luisovando.payout_service.domain.valueobject.MoneyVO;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePayoutCommand(
        UUID companyId,
        MoneyVO money,
        String idempotencyKey
) {
}
