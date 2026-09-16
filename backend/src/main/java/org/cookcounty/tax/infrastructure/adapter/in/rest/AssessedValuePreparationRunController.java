package org.cookcounty.tax.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationApiResponse;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationErrorResponse;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunResponse;
import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
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

/// Starts durable assessed-value preparation runs and returns their latest snapshots.
///
/// Expected validation, replay-conflict, lookup, and admission failures use the shared typed result
/// channel. The controller records accepted request headers for comparator evidence.
@Validated
@RestController
@RequestMapping("/api/assessed-value-preparation-runs")
public class AssessedValuePreparationRunController {

    private static final String RESOURCE_PATH = "/api/assessed-value-preparation-runs/";
    private final AssessedValuePreparationRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    /// Creates the inbound adapter for assessed-value run commands and queries.
    ///
    /// @param useCase durable assessed-value run boundary
    /// @param outcomeRecorder comparator evidence recorder for accepted requests
    public AssessedValuePreparationRunController(
            AssessedValuePreparationRunUseCase useCase,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }

    /// Returns the latest snapshot for one positive run identity.
    ///
    /// @param id generated run identity
    /// @return the snapshot or a typed `404` error body
    @GetMapping("/{id}")
    public ResponseEntity<AssessedValuePreparationApiResponse> getAssessedValuePreparationRun(
            @PathVariable @Positive Long id) {
        return switch (useCase.getAssessedValuePreparationRun(id)) {
            case Result.Ok(var response) -> ResponseEntity.ok(response);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Validates controls and reserves a run before asynchronous execution.
    ///
    /// @param request nullable input components validated as one structural request
    /// @param headers request headers retained for comparator evidence
    /// @return `201` with the queued snapshot or a typed expected error
    @PostMapping
    public ResponseEntity<AssessedValuePreparationApiResponse> startAssessedValuePreparationRun(
            @Valid @RequestBody AssessedValuePreparationRunRequest request,
            @RequestHeader Map<String, String> headers) {
        return switch (useCase.startAssessedValuePreparationRun(request)) {
            case Result.Ok(var start) -> accepted(start, request, headers);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Records comparator acceptance and creates the run resource response.
    private ResponseEntity<AssessedValuePreparationApiResponse> accepted(
            BatchRunStart<AssessedValuePreparationRunResponse> start,
            AssessedValuePreparationRunRequest request,
            Map<String, String> headers) {
        outcomeRecorder.accepted("valuation-preparation", start.id(), request, headers);
        URI location = URI.create(RESOURCE_PATH + start.id());
        return ResponseEntity.created(location).body(start.response());
    }

    /// Maps every recoverable run failure to its documented HTTP status.
    private static ResponseEntity<AssessedValuePreparationApiResponse> failure(
            BatchRunFailure failure) {
        return switch (failure) {
            case BatchRunFailure.InvalidRequest(var message) ->
                    error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
            case BatchRunFailure.IdempotencyConflict(var message) ->
                    error(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", message);
            case BatchRunFailure.RunNotFound(var ignored) ->
                    error(
                            HttpStatus.NOT_FOUND,
                            "RUN_NOT_FOUND",
                            "No assessed-value preparation run exists for the supplied id.");
            case BatchRunFailure.AdmissionRejected(var message) ->
                    error(HttpStatus.SERVICE_UNAVAILABLE, "BATCH_CAPACITY_EXHAUSTED", message);
        };
    }

    /// Creates the shared assessed-value error body without internal diagnostics.
    private static ResponseEntity<AssessedValuePreparationApiResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new AssessedValuePreparationErrorResponse(code, message, null));
    }
}
