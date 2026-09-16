package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static com.google.common.truth.Truth.assertThat;

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

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationErrorResponse;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunResponse;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

class TaxRateInputPreparationRunControllerTest {

    @Test
    void acceptedAndReplayedStartsPreserveCreatedStatusAndLocation() {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        TaxRateInputPreparationRunRequest request = request();
        TaxRateInputPreparationRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startTaxRateInputPreparationRun(request))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, false)))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, true)));
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        TaxRateInputPreparationRunController controller =
                new TaxRateInputPreparationRunController(useCase, recorder);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("X-Factor-Scenario-Id", "clerk-agency-attachment");
        requestHeaders.add("X-Factor-Probe", "first");
        requestHeaders.add("X-Factor-Probe", "second");

        ResponseEntity<?> accepted =
                controller.startTaxRateInputPreparationRun(request, requestHeaders);
        ResponseEntity<?> replayed =
                controller.startTaxRateInputPreparationRun(request, requestHeaders);
        var acceptedLocation =
                requireNonNull(accepted.getHeaders().getLocation(), "accepted Location");
        var replayedLocation =
                requireNonNull(replayed.getHeaders().getLocation(), "replayed Location");

        assertThat(accepted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(acceptedLocation.toString())
                .isEqualTo("/api/tax-rate-input-preparation-runs/2147483648");
        assertThat(replayed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replayedLocation).isEqualTo(acceptedLocation);
        verify(recorder, times(2))
                .accepted(
                        eq("clerk-agency-attachment"),
                        eq(queued.id()),
                        eq(request),
                        argThat(
                                headers ->
                                        "clerk-agency-attachment"
                                                        .equals(headers.get("X-Factor-Scenario-Id"))
                                                && "first,second"
                                                        .equals(headers.get("X-Factor-Probe"))));
    }

    @Test
    void reviewedRequestIsAcceptedOverHttp() throws Exception {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        TaxRateInputPreparationRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startTaxRateInputPreparationRun(any(TaxRateInputPreparationRunRequest.class)))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, false)));

        mvc(useCase)
                .perform(
                        post("/api/tax-rate-input-preparation-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "businessDate":"2025-09-15",
                                          "businessTime":"12:00:00",
                                          "idempotencyKey":"reviewed-tax-rate-input"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        "/api/tax-rate-input-preparation-runs/2147483648"));
    }

    @Test
    void invalidBusinessTimeIsRejectedBeforeServiceInvocation() throws Exception {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);

        mvc(useCase)
                .perform(
                        post("/api/tax-rate-input-preparation-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "businessDate":"2025-09-15",
                                          "businessTime":"24:00:00",
                                          "idempotencyKey":"invalid-tax-rate-input"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));

        verifyNoInteractions(useCase);
    }

    @Test
    void missingLongRunIdentifierReturnsApprovedJsonErrorShape() {
        TaxRateInputPreparationRunUseCase useCase = mock(TaxRateInputPreparationRunUseCase.class);
        when(useCase.getTaxRateInputPreparationRun(Long.MAX_VALUE))
                .thenReturn(new Result.Err<>(new BatchRunFailure.RunNotFound(Long.MAX_VALUE)));
        TaxRateInputPreparationRunController controller =
                new TaxRateInputPreparationRunController(
                        useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<?> response = controller.getTaxRateInputPreparationRun(Long.MAX_VALUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Object body = requireNonNull(response.getBody(), "not-found response body");
        assertThat(body).isInstanceOf(TaxRateInputPreparationErrorResponse.class);
        TaxRateInputPreparationErrorResponse error = (TaxRateInputPreparationErrorResponse) body;
        assertThat(error.error()).isEqualTo("RUN_NOT_FOUND");
    }

    private static MockMvc mvc(TaxRateInputPreparationRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(
                        new TaxRateInputPreparationRunController(
                                useCase, mock(FactorBatchOutcomeRecorder.class)))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }

    private static TaxRateInputPreparationRunRequest request() {
        return new TaxRateInputPreparationRunRequest(
                LocalDate.of(2025, 9, 15), "12:00:00", "controller-test");
    }

    private static TaxRateInputPreparationRunResponse response(long id, String status) {
        return new TaxRateInputPreparationRunResponse(
                LocalDate.of(2025, 9, 15),
                "12:00:00",
                id,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                status);
    }
}
