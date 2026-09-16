package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class EifdTifIncrementRunControllerTest {

    @Test
    void acceptedAndReplayedStartsBothReturnCreatedWithLongRunLocation() {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        EifdTifIncrementRunRequest request = request();
        EifdTifIncrementRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startEifdTifIncrementRun(request))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, false))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, true));
        EifdTifIncrementRunController controller = new EifdTifIncrementRunController(
                useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<EifdTifIncrementRunResponse> accepted =
                controller.startEifdTifIncrementRun(request, Map.of());
        ResponseEntity<EifdTifIncrementRunResponse> replayed =
                controller.startEifdTifIncrementRun(request, Map.of());

        assertEquals(HttpStatus.CREATED, accepted.getStatusCode());
        assertEquals("/api/eifd-tif-increment-runs/2147483648",
                accepted.getHeaders().getLocation().toString());
        assertEquals(HttpStatus.CREATED, replayed.getStatusCode());
        assertEquals(accepted.getHeaders().getLocation(), replayed.getHeaders().getLocation());
    }

    @Test
    void reviewedRequestIsAcceptedOverHttp() throws Exception {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        EifdTifIncrementRunResponse queued = response(2_147_483_648L, "QUEUED");
        when(useCase.startEifdTifIncrementRun(any(EifdTifIncrementRunRequest.class)))
                .thenReturn(new BatchRunStartResult<>(queued.getId(), queued, false));

        mvc(useCase).perform(post("/api/eifd-tif-increment-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("10000")))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/eifd-tif-increment-runs/2147483648"));
    }

    @Test
    void zeroAnnualEqualizationFactorIsRejectedBeforeServiceInvocation() throws Exception {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);

        mvc(useCase).perform(post("/api/eifd-tif-increment-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("00000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "annualEqualizationFactor must be greater than 00000."));

        verifyNoInteractions(useCase);
    }

    @Test
    void missingLongRunIdentifierReturnsApprovedJsonErrorShape() {
        EifdTifIncrementRunUseCase useCase = mock(EifdTifIncrementRunUseCase.class);
        when(useCase.getEifdTifIncrementRun(Long.MAX_VALUE)).thenReturn(Optional.empty());
        EifdTifIncrementRunController controller = new EifdTifIncrementRunController(
                useCase, mock(FactorBatchOutcomeRecorder.class));

        ResponseEntity<?> response = controller.getEifdTifIncrementRun(Long.MAX_VALUE);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        EifdTifIncrementErrorResponse error = assertInstanceOf(
                EifdTifIncrementErrorResponse.class, response.getBody());
        assertEquals("RUN_NOT_FOUND", error.getError());
        assertEquals("No EIFD/TIF increment run was found for id " + Long.MAX_VALUE + ".",
                error.getMessage());
    }

    private static MockMvc mvc(EifdTifIncrementRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(new EifdTifIncrementRunController(
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
                """.formatted(annualEqualizationFactor);
    }

    private EifdTifIncrementRunRequest request() {
        EifdTifIncrementRunRequest request = new EifdTifIncrementRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime("12:00:00");
        request.setIdempotencyKey("controller-test");
        request.setReassessmentControl("260181202526");
        request.setProcessingYear("26");
        request.setReportingYear("2026");
        request.setAnnualEqualizationFactor("10000");
        return request;
    }

    private EifdTifIncrementRunResponse response(long id, String status) {
        EifdTifIncrementRunResponse response = new EifdTifIncrementRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(LocalDate.of(2025, 9, 15));
        response.setBusinessTime("12:00:00");
        response.setOutputs(new ArrayList<>());
        response.setMessages(new ArrayList<>());
        return response;
    }
}
