package org.cookcounty.tax.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementApiResponse;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementErrorResponse;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunResponse;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
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
import java.util.Map;

/// HTTP boundary for starting and observing durable increment runs.
@RestController
@RequestMapping("/api/eifd-tif-increment-runs")
public final class EifdTifIncrementRunController {

    private static final String RESOURCE_PATH = "/api/eifd-tif-increment-runs/";
    private final EifdTifIncrementRunUseCase useCase;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    /// Connects the typed run contract to the HTTP and comparator boundaries.
    public EifdTifIncrementRunController(
            EifdTifIncrementRunUseCase useCase, FactorBatchOutcomeRecorder outcomeRecorder) {
        this.useCase = useCase;
        this.outcomeRecorder = outcomeRecorder;
    }

    /// Returns the current run snapshot, or a typed not-found response.
    @GetMapping("/{id}")
    public ResponseEntity<EifdTifIncrementApiResponse> getEifdTifIncrementRun(
            @PathVariable Long id) {
        return switch (useCase.getEifdTifIncrementRun(id)) {
            case Result.Ok(var response) -> ResponseEntity.ok(response);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Reserves asynchronous work or returns a typed validation, replay, or capacity failure.
    @PostMapping
    public ResponseEntity<EifdTifIncrementApiResponse> startEifdTifIncrementRun(
            @Valid @RequestBody EifdTifIncrementRunRequest request,
            @RequestHeader Map<String, String> headers) {
        return switch (useCase.startEifdTifIncrementRun(request)) {
            case Result.Ok(var start) -> accepted(start, request, headers);
            case Result.Err(var failure) -> failure(failure);
        };
    }

    /// Records the accepted request and returns its durable resource location.
    private ResponseEntity<EifdTifIncrementApiResponse> accepted(
            BatchRunStart<EifdTifIncrementRunResponse> start,
            EifdTifIncrementRunRequest request,
            Map<String, String> headers) {
        outcomeRecorder.accepted("eifd-tif-increment", start.id(), request, headers);
        URI location = URI.create(RESOURCE_PATH + start.id());
        return ResponseEntity.created(location).body(start.response());
    }

    /// Maps every expected batch-run failure to its reviewed HTTP status and body.
    private static ResponseEntity<EifdTifIncrementApiResponse> failure(BatchRunFailure failure) {
        return switch (failure) {
            case BatchRunFailure.InvalidRequest(var message) ->
                    error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
            case BatchRunFailure.IdempotencyConflict(var message) ->
                    error(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", message);
            case BatchRunFailure.RunNotFound(var id) ->
                    error(
                            HttpStatus.NOT_FOUND,
                            "RUN_NOT_FOUND",
                            "No EIFD/TIF increment run was found for id " + id + ".");
            case BatchRunFailure.AdmissionRejected(var message) ->
                    error(HttpStatus.SERVICE_UNAVAILABLE, "BATCH_CAPACITY_EXHAUSTED", message);
        };
    }

    /// Creates a safe typed error body without internal diagnostics.
    private static ResponseEntity<EifdTifIncrementApiResponse> error(
            HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new EifdTifIncrementErrorResponse(code, message, null));
    }
}
