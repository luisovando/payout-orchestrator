package com.luisovando.payout_service.infrastructure.web.payout;

import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutCommand;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutResult;
import com.luisovando.payout_service.application.usecase.createpayout.CreatePayoutUseCase;
import com.luisovando.payout_service.domain.exceptions.IdempotencyConflictException;
import com.luisovando.payout_service.infrastructure.web.error.ApiExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayoutController.class)
@Import(ApiExceptionHandler.class)
public class PayoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CreatePayoutUseCase createPayoutUseCase;

    @AfterEach
    void tearDown() {
        reset(createPayoutUseCase);
    }

    @Test
    void shouldReturn201WhenPayoutCreated() throws Exception {
        UUID payoutId = UUID.randomUUID();

        when(createPayoutUseCase.execute(any(CreatePayoutCommand.class)))
                .thenReturn(new CreatePayoutResult(payoutId, "CREATED", true));

        String body = """
                {
                    "companyId": "11111111-1111-1111-1111-111111111111",
                    "amount": "1000.50",
                    "currency": "USD",
                    "idempotencyKey": "test-key-1"
                }
                """;

        mockMvc.perform(
                        post("/payouts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payoutId").value(payoutId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(createPayoutUseCase, times(1)).execute(any(CreatePayoutCommand.class));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        String body = """
                {
                    "companyId": "11111111-1111-1111-1111-111111111111",
                    "amount": 1000.50,
                    "currency": "MX",
                    "idempotencyKey": "test-key-1"
                }
                """;
        mockMvc.perform(post("/payouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createPayoutUseCase);
    }

    @Test
    void shouldReturn400WhenRequestBodyInvalid() throws Exception {
        mockMvc.perform(post("/payouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "11111111-1111-1111-1111-111111111111",
                                  "amount": "1000.50",
                                  "currency": "MX",
                                  "idempotencyKey": "test-key-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_INVALID"));

        verifyNoInteractions(createPayoutUseCase);
    }

    @Test
    void shouldReturn400WhenUseCaseThrowsIllegalArgument() throws Exception {
        when(createPayoutUseCase.execute(any(CreatePayoutCommand.class)))
                .thenThrow(new IllegalArgumentException("company is blocked"));

        mockMvc.perform(post("/payouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "11111111-1111-1111-1111-111111111111",
                                  "amount": "1000.50",
                                  "currency": "CAD",
                                  "idempotencyKey": "test-key-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(createPayoutUseCase, times(1)).execute(any(CreatePayoutCommand.class));
    }

    @Test
    void shouldReturn200WhenIdempotentReplay() throws Exception {
        UUID existingPayoutId = UUID.randomUUID();
        CreatePayoutResult existingResult = new CreatePayoutResult(existingPayoutId, "PROCESSING", false);
        
        when(createPayoutUseCase.execute(any(CreatePayoutCommand.class)))
                .thenReturn(existingResult);

        mockMvc.perform(post("/payouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "11111111-1111-1111-1111-111111111111",
                                  "amount": "1000.50",
                                  "currency": "USD",
                                  "idempotencyKey": "test-key-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payoutId").value(existingPayoutId.toString()))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(createPayoutUseCase, times(1)).execute(any(CreatePayoutCommand.class));
    }

    @Test
    void shouldReturn201WhenNewPayoutCreated() throws Exception {
        UUID newPayoutId = UUID.randomUUID();
        CreatePayoutResult newResult = new CreatePayoutResult(newPayoutId, "CREATED", true);
        
        when(createPayoutUseCase.execute(any(CreatePayoutCommand.class)))
                .thenReturn(newResult);

        mockMvc.perform(post("/payouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyId": "11111111-1111-1111-1111-111111111111",
                                  "amount": "1000.50",
                                  "currency": "USD",
                                  "idempotencyKey": "test-key-1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payoutId").value(newPayoutId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(header().string("Location", "/payouts/" + newPayoutId.toString()));

        verify(createPayoutUseCase, times(1)).execute(any(CreatePayoutCommand.class));
    }

    @Test
    void shouldReturn409WhenIdempotencyConflict() throws Exception {
        when(createPayoutUseCase.execute(any(CreatePayoutCommand.class)))
                .thenThrow(new IdempotencyConflictException("idempotency key already used"));

        mockMvc.perform(post("/payouts")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                    "companyId": "11111111-1111-1111-1111-111111111111",
                    "amount": "1000.50",
                    "currency": "USD",
                    "idempotencyKey": "test-key-1"
                }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CreatePayoutUseCase createPayoutUseCase() {
            return mock(CreatePayoutUseCase.class);
        }
    }
}
