package org.cookcounty.tax.infrastructure.adapter.in.rest;

import java.net.URI;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsErrorResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/property-tax-exemptions-runs")
public class PropertyTaxExemptionsRunController {

    private final PropertyTaxExemptionsRunUseCase useCase;
    private final PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    public PropertyTaxExemptionsRunController(
            PropertyTaxExemptionsRunUseCase useCase,
            PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyTaxExemptionsRun(
            @PathVariable @Positive Long id) {
        return useCase.getPropertyTaxExemptionsRun(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(notFound(id)));
    }

    @PostMapping
    public ResponseEntity<PropertyTaxExemptionsRunResponse> startPropertyTaxExemptionsRun(
            @Valid @RequestBody PropertyTaxExemptionsRunRequest request,
            @RequestHeader Map<String, String> headers) {
        PropertyTaxExemptionsRunResponse result =
                useCase.startPropertyTaxExemptionsRun(request);
        HomeownerVariant variant =
                HomeownerVariant.valueOf(result.getHomeownerProcessingVariant());
        outcomeRecorder.accepted(
                outcomeProjector.scenarioId(variant), result.getId(), request, headers);
        URI location = URI.create("/api/property-tax-exemptions-runs/" + result.getId());
        return ResponseEntity.created(location).body(result);
    }

    private static PropertyTaxExemptionsErrorResponse notFound(Long id) {
        PropertyTaxExemptionsErrorResponse error = new PropertyTaxExemptionsErrorResponse();
        error.setError("RUN_NOT_FOUND");
        error.setMessage("No property-tax-exemptions run was found for id " + id);
        return error;
    }
}
