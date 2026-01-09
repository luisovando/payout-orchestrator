package com.luisovando.payout_service.application.usecase.createpayout;

import java.util.UUID;

public record CreatePayoutResult(
        UUID payoutId,
        String status,
        boolean created
) {
}
