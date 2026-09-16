package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class PropertyTaxExemptionsRunControllerTest {

    @Test
    void createdAndReplayedRunsExposeTheirCanonicalLocation() {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        PropertyTaxExemptionsRunRequest request = new PropertyTaxExemptionsRunRequest();
        PropertyTaxExemptionsRunResponse response = new PropertyTaxExemptionsRunResponse();
        response.setId(2_147_483_648L);
        response.setHomeownerProcessingVariant("ENUMERATED");
        when(useCase.startPropertyTaxExemptionsRun(request)).thenReturn(response);
        PropertyTaxExemptionsFactorOutcomeProjector projector =
                mock(PropertyTaxExemptionsFactorOutcomeProjector.class);
        FactorBatchOutcomeRecorder recorder = mock(FactorBatchOutcomeRecorder.class);
        when(projector.scenarioId(HomeownerVariant.ENUMERATED))
                .thenReturn("homeowner-enumerated-variant");
        PropertyTaxExemptionsRunController controller =
                new PropertyTaxExemptionsRunController(useCase, projector, recorder);

        Map<String, String> headers = Map.of("X-Factor-Scenario-Id",
                "homeowner-enumerated-variant");
        var result = controller.startPropertyTaxExemptionsRun(request, headers);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString(
                "/api/property-tax-exemptions-runs/2147483648");
        assertThat(result.getBody()).isSameAs(response);
        verify(recorder).accepted(
                "homeowner-enumerated-variant", 2_147_483_648L, request, headers);
    }

    @Test
    void missingLongRunIdReturnsApprovedJsonErrorShape() {
        PropertyTaxExemptionsRunUseCase useCase = mock(PropertyTaxExemptionsRunUseCase.class);
        when(useCase.getPropertyTaxExemptionsRun(3_000_000_000L)).thenReturn(Optional.empty());
        PropertyTaxExemptionsRunController controller =
                new PropertyTaxExemptionsRunController(
                        useCase,
                        mock(PropertyTaxExemptionsFactorOutcomeProjector.class),
                        mock(FactorBatchOutcomeRecorder.class));
        var result = controller.getPropertyTaxExemptionsRun(3_000_000_000L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isInstanceOfSatisfying(
                PropertyTaxExemptionsErrorResponse.class,
                error -> {
                    assertThat(error.getError()).isEqualTo("RUN_NOT_FOUND");
                    assertThat(error.getMessage()).contains("3000000000");
                });
    }
}
