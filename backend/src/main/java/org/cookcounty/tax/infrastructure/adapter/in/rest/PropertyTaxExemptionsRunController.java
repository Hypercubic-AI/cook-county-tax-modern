package org.cookcounty.tax.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsFactorOutcomeProjector;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsApiResponse;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsErrorResponse;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/// Exposes durable property-tax-exemption run creation and observation.
///
/// The controller validates structural input before reservation. It maps each typed failure to its
/// documented HTTP status and records accepted requests for comparator correlation.
@Validated
@RestController
@RequestMapping("/api/property-tax-exemptions-runs")
public class PropertyTaxExemptionsRunController {

    private static final String RESOURCE_PATH = "/api/property-tax-exemptions-runs/";
    private final PropertyTaxExemptionsRunUseCase useCase;
    private final PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    /// Creates the HTTP adapter for the run use case and comparator recorder.
    public PropertyTaxExemptionsRunController(
            PropertyTaxExemptionsRunUseCase useCase,
            PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
    }

    /// Returns the latest immutable snapshot for a positive generated run identity.
    @GetMapping("/{id}")
    public ResponseEntity<PropertyTaxExemptionsApiResponse> getPropertyTaxExemptionsRun(
            @PathVariable @Positive Long id) {
        return switch (useCase.getPropertyTaxExemptionsRun(id)) {
            case Result.Ok(var response) -> ResponseEntity.ok(response);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Reserves or replays one run and returns its canonical resource location.
    @PostMapping
    public ResponseEntity<PropertyTaxExemptionsApiResponse> startPropertyTaxExemptionsRun(
            @Valid @RequestBody PropertyTaxExemptionsRunRequest request,
            @RequestHeader Map<String, String> headers) {
        return switch (useCase.startPropertyTaxExemptionsRun(request)) {
            case Result.Ok(var start) -> accepted(start, request, headers);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    private ResponseEntity<PropertyTaxExemptionsApiResponse> accepted(
            BatchRunStart<PropertyTaxExemptionsRunResponse> start,
            PropertyTaxExemptionsRunRequest request,
            Map<String, String> headers) {
        HomeownerVariant variant =
                HomeownerVariant.valueOf(start.response().homeownerProcessingVariant());
        outcomeRecorder.accepted(
                outcomeProjector.scenarioId(variant), start.id(), request, headers);
        URI location = URI.create(RESOURCE_PATH + start.id());
        return ResponseEntity.created(location).body(start.response());
    }

    private static ResponseEntity<PropertyTaxExemptionsApiResponse> failure(
            BatchRunFailure failure) {
        return switch (failure) {
            case BatchRunFailure.InvalidRequest(var message) ->
                    error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
            case BatchRunFailure.IdempotencyConflict(var message) ->
                    error(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", message);
            case BatchRunFailure.RunNotFound(var id) ->
                    error(
                            HttpStatus.NOT_FOUND,
                            "RUN_NOT_FOUND",
                            "No property-tax-exemptions run was found for id " + id);
            case BatchRunFailure.AdmissionRejected(var message) ->
                    error(HttpStatus.SERVICE_UNAVAILABLE, "BATCH_CAPACITY_EXHAUSTED", message);
        };
    }

    private static ResponseEntity<PropertyTaxExemptionsApiResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new PropertyTaxExemptionsErrorResponse(code, message, null));
    }
}
