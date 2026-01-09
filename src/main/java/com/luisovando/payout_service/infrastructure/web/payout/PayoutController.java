package com.luisovando.payout_service.infrastructure.web.payout;

import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutCommand;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutResult;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutUseCase;
import com.luisovando.payout_service.domain.valueobject.MoneyVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
                MoneyVO.of(request.amount(), request.currency()),
                request.idempotencyKey()
        );

        CreatePayoutResult result = createPayoutUseCase.execute(command);

        URI location = URI.create("/payouts/" + result.payoutId());

        if (!result.created()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CreatePayoutResponse(result.payoutId(), result.status()));
        }

        return ResponseEntity.created(location).location(location)
                .body(new CreatePayoutResponse(result.payoutId(), result.status()));
    }
}
