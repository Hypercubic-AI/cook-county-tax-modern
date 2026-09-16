package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.Objects.requireNonNull;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsErrorResponse;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Map;

class PropertyTaxExemptionsRunControllerTest {

    @ParameterizedTest(name = "{0} start returns {2}")
    @CsvSource({"new, false, CREATED", "replay, true, CREATED"})
    void startPreservesCreatedStatusForNewAndReplayedRuns(
            String caseName, boolean replayed, HttpStatus expectedStatus) {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        PropertyTaxExemptionsRunRequest request =
                new PropertyTaxExemptionsRunRequest(
                        LocalDate.of(2025, 9, 15), "12:00:00", "ENUMERATED", "controller-key");
        PropertyTaxExemptionsRunResponse response =
                new PropertyTaxExemptionsRunResponse(
                        LocalDate.of(2025, 9, 15),
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
        when(useCase.startPropertyTaxExemptionsRun(request))
                .thenReturn(
                        new Result.Ok<>(new BatchRunStart<>(response.id(), response, replayed)));
        PropertyTaxExemptionsFactorOutcomeProjector projector =
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class);
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        when(projector.scenarioId(HomeownerVariant.ENUMERATED))
                .thenReturn("homeowner-enumerated-variant");
        PropertyTaxExemptionsRunController controller =
                new PropertyTaxExemptionsRunController(useCase, projector, recorder);

        Map<String, String> headers =
                Map.of("X-Factor-Scenario-Id", "homeowner-enumerated-variant");
        var result = controller.startPropertyTaxExemptionsRun(request, headers);
        var location = requireNonNull(result.getHeaders().getLocation(), "run Location");

        assertThat(result.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(location.toString()).isEqualTo("/api/property-tax-exemptions-runs/2147483648");
        assertThat(result.getBody()).isSameInstanceAs(response);
        verify(recorder).accepted("homeowner-enumerated-variant", 2_147_483_648L, request, headers);
    }

    @Test
    void missingLongRunIdReturnsApprovedJsonErrorShape() {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        when(useCase.getPropertyTaxExemptionsRun(3_000_000_000L))
                .thenReturn(new Result.Err<>(new BatchRunFailure.RunNotFound(3_000_000_000L)));
        PropertyTaxExemptionsRunController controller =
                new PropertyTaxExemptionsRunController(
                        useCase,
                        mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                        mock(FactorBatchOutcomeRecorder.class));
        var result = controller.getPropertyTaxExemptionsRun(3_000_000_000L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Object body = requireNonNull(result.getBody(), "not-found response body");
        assertThat(body).isInstanceOf(PropertyTaxExemptionsErrorResponse.class);
        PropertyTaxExemptionsErrorResponse error = (PropertyTaxExemptionsErrorResponse) body;
        assertThat(error.error()).isEqualTo("RUN_NOT_FOUND");
    }
}
