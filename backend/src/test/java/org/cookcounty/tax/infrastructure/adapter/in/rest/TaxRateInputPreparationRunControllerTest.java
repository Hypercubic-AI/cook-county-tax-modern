package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunResponse;
import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TaxRateInputPreparationRunControllerTest {

    @Test
    void acceptedAndReplayedStartsBothReturnCreatedWithRunLocation() {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        TaxRateInputPreparationRunRequest request = request();
        TaxRateInputPreparationRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startTaxRateInputPreparationRun(request))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, false))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, true));
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        TaxRateInputPreparationRunController controller =
                new TaxRateInputPreparationRunController(useCase, recorder);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-Factor-Scenario-Id", "clerk-agency-attachment");
        requestHeaders.add("X-Factor-Probe", "first");
        requestHeaders.add("X-Factor-Probe", "second");

        ResponseEntity<TaxRateInputPreparationRunResponse> accepted =
                controller.startTaxRateInputPreparationRun(request, requestHeaders);
        ResponseEntity<TaxRateInputPreparationRunResponse> replayed =
                controller.startTaxRateInputPreparationRun(request, requestHeaders);

        assertEquals(HttpStatus.CREATED, accepted.getStatusCode());
        assertEquals(
                "/api/tax-rate-input-preparation-runs/2147483648",
                accepted.getHeaders().getLocation().toString());
        assertEquals(HttpStatus.CREATED, replayed.getStatusCode());
        assertEquals(accepted.getHeaders().getLocation(), replayed.getHeaders().getLocation());
        verify(recorder, times(2)).accepted(
                eq("clerk-agency-attachment"),
                eq(queued.getId()),
                eq(request),
                argThat(headers -> "clerk-agency-attachment".equals(
                                headers.get("X-Factor-Scenario-Id"))
                        && "first,second".equals(headers.get("X-Factor-Probe"))));
    }

    @Test
    void reviewedRequestIsAcceptedOverHttp() throws Exception {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        TaxRateInputPreparationRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startTaxRateInputPreparationRun(any(TaxRateInputPreparationRunRequest.class)))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, false));

        mvc(useCase).perform(post("/api/tax-rate-input-preparation-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessDate":"2025-09-15",
                                  "businessTime":"12:00:00",
                                  "idempotencyKey":"reviewed-tax-rate-input"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/tax-rate-input-preparation-runs/2147483648"));
    }

    @Test
    void invalidBusinessTimeIsRejectedBeforeServiceInvocation() throws Exception {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);

        mvc(useCase).perform(post("/api/tax-rate-input-preparation-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessDate":"2025-09-15",
                                  "businessTime":"24:00:00",
                                  "idempotencyKey":"invalid-tax-rate-input"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "businessTime must use HH:mm:ss in 24-hour time."));

        verifyNoInteractions(useCase);
    }

    @Test
    void missingLongRunIdentifierReturnsApprovedJsonErrorShape() {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        when(useCase.getTaxRateInputPreparationRun(Long.MAX_VALUE)).thenReturn(Optional.empty());
        TaxRateInputPreparationRunController controller =
                new TaxRateInputPreparationRunController(
                        useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<?> response = controller.getTaxRateInputPreparationRun(Long.MAX_VALUE);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        TaxRateInputPreparationErrorResponse error = assertInstanceOf(
                TaxRateInputPreparationErrorResponse.class, response.getBody());
        assertEquals("RUN_NOT_FOUND", error.getError());
        assertEquals("No run exists for the supplied identifier", error.getMessage());
    }

    private static MockMvc mvc(TaxRateInputPreparationRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(new TaxRateInputPreparationRunController(
                        useCase, mock(FactorBatchOutcomeRecorder.class)))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }

    private static TaxRateInputPreparationRunRequest request() {
        TaxRateInputPreparationRunRequest request = new TaxRateInputPreparationRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime("12:00:00");
        request.setIdempotencyKey("controller-test");
        return request;
    }

    private static TaxRateInputPreparationRunResponse response(long id, String status) {
        TaxRateInputPreparationRunResponse response = new TaxRateInputPreparationRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(LocalDate.of(2025, 9, 15));
        response.setBusinessTime("12:00:00");
        response.setOutputs(List.of());
        response.setMessages(List.of());
        return response;
    }
}
