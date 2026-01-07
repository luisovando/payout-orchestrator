package com.luisovando.payout_service.infrastructure.web.payout;

import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutCommand;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutResult;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/payouts")
public class PayoutController {
    private final CreatePayoutUseCase createPayoutUseCase;

    public PayoutController(CreatePayoutUseCase createPayoutUseCase) {
        this.createPayoutUseCase = createPayoutUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatePayoutResponse> create(@Valid @RequestBody CreatePayoutRequest request) {
        CreatePayoutCommand command = new CreatePayoutCommand(
                request.companyId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey()
        );

        CreatePayoutResult result = createPayoutUseCase.execute(command);

        URI location = URI.create("/payouts/" + result.payoutId());

        return ResponseEntity.created(location)
                .body(new CreatePayoutResponse(result.payoutId(), result.status()));
    }
}
