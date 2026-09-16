package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PropertyTaxExemptionsHttpContractTest {

    private static final String VALID_REQUEST = """
            {
              "businessDate": "2025-09-15",
              "businessTime": "12:00:00",
              "idempotencyKey": "home852",
              "homeownerProcessingVariant": "ENUMERATED"
            }
            """;

    @Test
    void unsupportedVariantIsRejectedBeforeServiceInvocation() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        String invalidRequest = VALID_REQUEST.replace("ENUMERATED", "ASHMA850");

        mvc(useCase).perform(post("/api/property-tax-exemptions-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "homeownerProcessingVariant must be ENUMERATED or BROAD."));

        verifyNoInteractions(useCase);
    }

    @Test
    void idempotencyConflictIsHttp409() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        when(useCase.startPropertyTaxExemptionsRun(any(PropertyTaxExemptionsRunRequest.class)))
                .thenThrow(new BatchRunIdempotencyConflictException(
                        "The idempotency key is already associated with different request parameters."));

        mvc(useCase).perform(post("/api/property-tax-exemptions-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY_CONFLICT"));
    }

    @Test
    void createdRunCarriesLocation() throws Exception {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        PropertyTaxExemptionsRunResponse response = new PropertyTaxExemptionsRunResponse();
        response.setId(2_147_483_648L);
        response.setStatus("QUEUED");
        response.setBusinessDate(java.time.LocalDate.of(2025, 9, 15));
        response.setBusinessTime("12:00:00");
        response.setHomeownerProcessingVariant("ENUMERATED");
        when(useCase.startPropertyTaxExemptionsRun(any(PropertyTaxExemptionsRunRequest.class)))
                .thenReturn(response);

        mvc(useCase).perform(post("/api/property-tax-exemptions-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/property-tax-exemptions-runs/2147483648"));
    }

    private static MockMvc mvc(PropertyTaxExemptionsRunUseCase useCase) {
        return MockMvcBuilders.standaloneSetup(new PropertyTaxExemptionsRunController(
                        useCase,
                        mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                        mock(FactorBatchOutcomeRecorder.class)))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }
}
