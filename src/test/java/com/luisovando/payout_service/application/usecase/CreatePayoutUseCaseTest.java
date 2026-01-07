package com.luisovando.payout_service.application.usecase;

import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutCommand;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutResult;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutUseCase;
import com.luisovando.payout_service.domain.valueobject.MoneyVO;
import com.luisovando.payout_service.infrastructure.persistence.entity.PayoutEntity;
import com.luisovando.payout_service.infrastructure.persistence.repository.PayoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatePayoutUseCaseTest {
    @Mock
    PayoutRepository payoutRepository;
    @InjectMocks
    CreatePayoutUseCase useCase;

    @Captor
    private ArgumentCaptor<PayoutEntity> payoutCaptor;

    private UUID companyId;
    private String idempotencyKey;
    private CreatePayoutCommand command;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        idempotencyKey = "test-key-abd123";
        command = new CreatePayoutCommand(
                companyId,
                MoneyVO.of(new BigDecimal("1000.50"), " usd "),
                idempotencyKey
        );
    }

    /**
     * Test case to verify that when a payout does not exist
     * with the given company id and idempotency key, a new payout
     * record is created in the database.
     * <p>
     * The test uses the following Mockito methods:
     * - when: to specify the behavior of a method when it is called
     * - thenAnswer: to specify the return value of a method when it is called
     * - thenReturn: to specify the return value of a method when it is called
     * - verify: to verify that a method was called with the given arguments
     * - capture: to capture the argument of a method and assert its value
     */
    @Test
    void shouldCreateANewPayoutWhenNotExists() {
        when(payoutRepository.save(any(PayoutEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(payoutRepository.findByCompanyIdAndIdempotencyKey(
                eq(command.companyId()),
                eq(command.idempotencyKey())
        )).thenReturn(Optional.empty());

        CreatePayoutResult result = useCase.execute(command);

        verify(payoutRepository).save(payoutCaptor.capture());
        verify(payoutRepository).findByCompanyIdAndIdempotencyKey(eq(companyId), eq(idempotencyKey));
        verify(payoutRepository).save(any(PayoutEntity.class));

        PayoutEntity savedPayout = payoutCaptor.getValue();
        assertThat(savedPayout.getCompanyId()).isEqualTo(companyId);
        assertThat(savedPayout.getAmount()).isEqualByComparingTo("1000.50");
        assertThat(savedPayout.getCurrency()).isEqualTo("USD");
        assertThat(savedPayout.getStatus()).isEqualTo("CREATED");
        assertThat(savedPayout.getIdempotencyKey()).isEqualTo(idempotencyKey);

        assertThat(result.payoutId()).isNotNull();
        assertThat(result.status()).isEqualTo("CREATED");

        verifyNoMoreInteractions(payoutRepository);
    }


    /**
     * Test case to verify that when a payout does exist
     * with the given company id and idempotency key, the use case
     * should return the existing payout without creating a new one.
     * <p>
     * The test uses the following Mockito methods:
     * - when: to specify the behavior of a method when it is called
     * - thenReturn: to specify the return value of a method when it is called
     * - verify: to verify that a method was called with the given arguments
     * - never: to verify that a method was never called
     * - times: to verify that a method was called a certain number of times
     */
    @Test
    void shouldNotCreateANewPayoutWhenExists() {
        PayoutEntity existing = PayoutEntity.createNew(
                companyId,
                new BigDecimal("1000.50"),
                "USD",
                "CREATED",
                idempotencyKey
        );

        when(payoutRepository.findByCompanyIdAndIdempotencyKey(
                eq(command.companyId()),
                eq(command.idempotencyKey())
        )).thenReturn(Optional.of(existing));

        CreatePayoutResult result = useCase.execute(command);

        verify(payoutRepository, never()).save(any(PayoutEntity.class));
        verify(payoutRepository, times(1)).findByCompanyIdAndIdempotencyKey(eq(companyId), eq(idempotencyKey));

        assertThat(result.payoutId()).isEqualTo(existing.getId());
        assertThat(result.status()).isEqualTo(existing.getStatus());

        verifyNoMoreInteractions(payoutRepository);
    }

    /**
     * Test case to verify that when a payout exists with the given company id and idempotency key,
     * the use case should return the existing payout without creating a new one.
     * <p>
     * The test uses the following Mockito methods:
     * - when: to specify the behavior of a method when it is called
     * - thenReturn: to specify the return value of a method when it is called
     * - verify: to verify that a method was called with the given arguments
     * - never: to verify that a method was never called
     */
    @Test
    void shouldReturnExistingPayoutWhenIdempotencyKeyExists() {
        PayoutEntity existing = PayoutEntity.createNew(
                companyId,
                new BigDecimal("1000.50"),
                "USD",
                "PROCESSING",
                idempotencyKey
        );

        when(payoutRepository.findByCompanyIdAndIdempotencyKey(eq(companyId), eq(idempotencyKey)))
                .thenReturn(Optional.of(existing));

        CreatePayoutResult result = useCase.execute(command);

        verify(payoutRepository, never()).save(any(PayoutEntity.class));
        verify(payoutRepository).findByCompanyIdAndIdempotencyKey(eq(companyId), eq(idempotencyKey));

        assertThat(result.payoutId()).isEqualTo(existing.getId());
        assertThat(result.status()).isEqualTo(existing.getStatus());

        verifyNoMoreInteractions(payoutRepository);
    }

    @Test
    void shouldThrowWhenCurrencyIsInvalid() {
        CreatePayoutCommand invalidCommand = new CreatePayoutCommand(
                companyId,
                MoneyVO.of(new BigDecimal("100.50"), " cad "),
                idempotencyKey
        );

        assertThatThrownBy(() -> useCase.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency not supported");

        verifyNoInteractions(payoutRepository);
    }

    @Test
    void shouldThrowWhenIdempotencyKeyIsBlank() {
        CreatePayoutCommand invalidCommand = new CreatePayoutCommand(
                companyId,
                MoneyVO.of(new BigDecimal("1000.50"), "USD"),
                ""
        );

        assertThatThrownBy(() -> useCase.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey must not be blank");

        verifyNoInteractions(payoutRepository);
    }
}