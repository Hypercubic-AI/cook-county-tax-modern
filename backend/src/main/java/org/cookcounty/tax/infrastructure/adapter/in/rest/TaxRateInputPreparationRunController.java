package org.cookcounty.tax.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationApiResponse;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationErrorResponse;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunResponse;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/// Translates tax-rate input preparation HTTP requests and typed application outcomes.
///
/// The controller starts asynchronous work and returns its resource location. It does not own the
/// processing transaction or restart behavior.
@RestController
@RequestMapping("/api/tax-rate-input-preparation-runs")
public final class TaxRateInputPreparationRunController {
    private static final String RESOURCE_PATH = "/api/tax-rate-input-preparation-runs/";
    private static final String FACTOR_SCENARIO_ID = "clerk-agency-attachment";

    private final TaxRateInputPreparationRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    /// Creates the HTTP adapter for the run use case and accepted-request recorder.
    public TaxRateInputPreparationRunController(
            TaxRateInputPreparationRunUseCase useCase, FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }

    /// Returns the latest run snapshot or a typed 404 error body.
    @GetMapping("/{id}")
    public ResponseEntity<TaxRateInputPreparationApiResponse> getTaxRateInputPreparationRun(
            @PathVariable Long id) {
        return switch (useCase.getTaxRateInputPreparationRun(id)) {
            case Result.Ok(var response) -> ResponseEntity.ok(response);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Starts a structurally valid request or returns its typed expected failure.
    ///
    /// The response includes a resource location for new and exact replay outcomes.
    @PostMapping
    public ResponseEntity<TaxRateInputPreparationApiResponse> startTaxRateInputPreparationRun(
            @Valid @RequestBody TaxRateInputPreparationRunRequest request,
            @RequestHeader HttpHeaders requestHeaders) {
        return switch (useCase.startTaxRateInputPreparationRun(request)) {
            case Result.Ok(var start) -> accepted(start, request, requestHeaders);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Records accepted request provenance and returns the run resource.
    private ResponseEntity<TaxRateInputPreparationApiResponse> accepted(
            BatchRunStart<TaxRateInputPreparationRunResponse> start,
            TaxRateInputPreparationRunRequest request,
            HttpHeaders requestHeaders) {
        outcomeRecorder.accepted(
                FACTOR_SCENARIO_ID, start.id(), request, flattened(requestHeaders));
        URI location = URI.create(RESOURCE_PATH + start.id());
        return ResponseEntity.created(location).body(start.response());
    }

    /// Exhaustively maps each expected failure to its documented HTTP status and code.
    private static ResponseEntity<TaxRateInputPreparationApiResponse> failure(
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
                            "No run exists for the supplied identifier");
            case BatchRunFailure.AdmissionRejected(var message) ->
                    error(HttpStatus.SERVICE_UNAVAILABLE, "BATCH_CAPACITY_EXHAUSTED", message);
        };
    }

    /// Creates the common safe error body without business-rule context.
    private static ResponseEntity<TaxRateInputPreparationApiResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new TaxRateInputPreparationErrorResponse(code, message, null));
    }

    /// Copies request headers into an immutable single-value provenance map.
    private static Map<String, String> flattened(HttpHeaders headers) {
        Map<String, String> flattened = new LinkedHashMap<>();
        headers.forEach((name, values) -> flattened.put(name, String.join(",", values)));
        return Map.copyOf(flattened);
    }
}
