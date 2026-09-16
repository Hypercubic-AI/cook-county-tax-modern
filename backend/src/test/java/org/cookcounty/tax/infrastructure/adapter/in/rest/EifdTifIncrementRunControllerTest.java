package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementErrorResponse;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunResponse;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

class EifdTifIncrementRunControllerTest {

    @Test
    void acceptedAndReplayedStartsReturnCreatedWithLongRunLocation() {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        EifdTifIncrementRunRequest request = request();
        EifdTifIncrementRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startEifdTifIncrementRun(request))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, false)))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, true)));
        EifdTifIncrementRunController controller =
                new EifdTifIncrementRunController(useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<?> accepted = controller.startEifdTifIncrementRun(request, Map.of());
        ResponseEntity<?> replayed = controller.startEifdTifIncrementRun(request, Map.of());
        var acceptedLocation =
                requireNonNull(accepted.getHeaders().getLocation(), "accepted Location");
        var replayedLocation =
                requireNonNull(replayed.getHeaders().getLocation(), "replayed Location");

        assertThat(accepted.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(acceptedLocation.toString())
                .isEqualTo("/api/eifd-tif-increment-runs/2147483648");
        assertThat(replayed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replayedLocation).isEqualTo(acceptedLocation);
    }

    @Test
    void reviewedRequestIsAcceptedOverHttp() throws Exception {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        EifdTifIncrementRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startEifdTifIncrementRun(any(EifdTifIncrementRunRequest.class)))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(queued.id(), queued, false)));

        mvc(useCase)
                .perform(
                        post("/api/eifd-tif-increment-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson("10000")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/eifd-tif-increment-runs/2147483648"));
    }

    @Test
    void zeroAnnualEqualizationFactorIsRejectedBeforeServiceInvocation() throws Exception {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);

        mvc(useCase)
                .perform(
                        post("/api/eifd-tif-increment-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validRequestJson("00000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));

        verifyNoInteractions(useCase);
    }

    @Test
    void missingLongRunIdentifierReturnsApprovedJsonErrorShape() {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        when(useCase.getEifdTifIncrementRun(Long.MAX_VALUE))
                .thenReturn(new Result.Err<>(new BatchRunFailure.RunNotFound(Long.MAX_VALUE)));
        EifdTifIncrementRunController controller =
                new EifdTifIncrementRunController(useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<?> response = controller.getEifdTifIncrementRun(Long.MAX_VALUE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Object body = requireNonNull(response.getBody(), "not-found response body");
        assertThat(body).isInstanceOf(EifdTifIncrementErrorResponse.class);
        EifdTifIncrementErrorResponse error = (EifdTifIncrementErrorResponse) body;
        assertThat(error.error()).isEqualTo("RUN_NOT_FOUND");
    }

    private static MockMvc mvc(EifdTifIncrementRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(
                        new EifdTifIncrementRunController(
                                useCase, mock(FactorBatchOutcomeRecorder.class)))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }

    private static String validRequestJson(String annualEqualizationFactor) {
        return """
        {
          "businessDate":"2025-09-15",
          "businessTime":"12:00:00",
          "idempotencyKey":"reviewed-eifd",
          "reassessmentControl":"260181202526",
          "processingYear":"26",
          "reportingYear":"2026",
          "annualEqualizationFactor":"%s"
        }
        """
                .formatted(annualEqualizationFactor);
    }

    private EifdTifIncrementRunRequest request() {
        return new EifdTifIncrementRunRequest(
                "10000",
                LocalDate.of(2025, 9, 15),
                "12:00:00",
                "controller-test",
                "26",
                "260181202526",
                "2026");
    }

    private EifdTifIncrementRunResponse response(long id, String status) {
        return new EifdTifIncrementRunResponse(
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
                status);
    }
}
