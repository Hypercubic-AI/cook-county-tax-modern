package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PropertyTaxExemptionsHttpContractTest {

    private static final String VALID_REQUEST =
            """
            {
              "businessDate": "2025-09-15",
              "businessTime": "12:00:00",
              "idempotencyKey": "homeowner-eligibility",
              "homeownerProcessingVariant": "ENUMERATED"
            }
            """;

    @Test
    void unsupportedVariantIsRejectedBeforeServiceInvocation() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        String invalidRequest = VALID_REQUEST.replace("ENUMERATED", "UNSUPPORTED");

        mvc(useCase)
                .perform(
                        post("/api/property-tax-exemptions-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));

        verifyNoInteractions(useCase);
    }

    @Test
    void idempotencyConflictIsHttp409() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        when(useCase.startPropertyTaxExemptionsRun(any(PropertyTaxExemptionsRunRequest.class)))
                .thenReturn(
                        new Result.Err<>(
                                new BatchRunFailure.IdempotencyConflict(
                                        "The idempotency key is already associated with different"
                                                + " request parameters.")));

        mvc(useCase)
                .perform(
                        post("/api/property-tax-exemptions-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY_CONFLICT"));
    }

    @Test
    void admissionRejectionIsHttp503WithoutAReservedRun() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        when(useCase.startPropertyTaxExemptionsRun(any(PropertyTaxExemptionsRunRequest.class)))
                .thenReturn(
                        new Result.Err<>(
                                new BatchRunFailure.AdmissionRejected(
                                        "The service cannot accept another batch run at this"
                                                + " time.")));

        mvc(useCase)
                .perform(
                        post("/api/property-tax-exemptions-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("BATCH_CAPACITY_EXHAUSTED"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "The service cannot accept another batch run at this"
                                                + " time."));
    }

    @Test
    void createdRunCarriesLocation() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        PropertyTaxExemptionsRunResponse response =
                new PropertyTaxExemptionsRunResponse(
                        java.time.LocalDate.of(2025, 9, 15),
                        "12:00:00",
                        "ENUMERATED",
                        2_147_483_648L,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "QUEUED");
        when(useCase.startPropertyTaxExemptionsRun(any(PropertyTaxExemptionsRunRequest.class)))
                .thenReturn(new Result.Ok<>(new BatchRunStart<>(response.id(), response, false)));

        mvc(useCase)
                .perform(
                        post("/api/property-tax-exemptions-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        "/api/property-tax-exemptions-runs/2147483648"));
    }

    private static MockMvc mvc(PropertyTaxExemptionsRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(
                        new PropertyTaxExemptionsRunController(
                                useCase,
                                mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                                mock(FactorBatchOutcomeRecorder.class)))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }
}
