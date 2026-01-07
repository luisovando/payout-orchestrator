package com.luisovando.payout_service.infrastructure.web.error;

import java.time.Instant;

public record ApiErrorResponse(
        String code,
        String message,
        Instant timestamp
) {
}
