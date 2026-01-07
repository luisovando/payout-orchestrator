package com.luisovando.payout_service.infrastructure.web.payout;

import java.util.UUID;

public record CreatePayoutResponse(
        UUID payoutId,
        String status
) {
}
