package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementMessage;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementOutput;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunResponse;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/// Reserves, executes, and observes increment runs through the shared batch-run coordinator.
///
/// The coordinator persists each snapshot and arbitrates replay keys across restarts. Active work
/// does not resume after a process restart. Each accepted run uses one asynchronous execution.
@Service
public final class EifdTifIncrementRunService implements EifdTifIncrementRunUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EifdTifIncrementRunService.class);
    private final EifdTifIncrementProcessor processor;
    private final EifdTifIncrementKernel kernel;
    private final EifdTifIncrementOutcomeProjector outcomeProjector;
    private final BatchRunCoordinator<EifdTifIncrementRunResponse> coordinator;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    /// Creates a service that shares the configured admission executor and durable run store.
    public EifdTifIncrementRunService(
            EifdTifIncrementProcessor processor,
            EifdTifIncrementKernel kernel,
            EifdTifIncrementOutcomeProjector outcomeProjector,
            @Qualifier("batchRunExecutor") Executor executor,
            FactorBatchOutcomeRecorder outcomeRecorder,
            BatchRunStore batchRunStore,
            BatchRunLifecycle batchRunLifecycle) {
        this.processor = Objects.requireNonNull(processor, "processor");
        this.kernel = Objects.requireNonNull(kernel, "kernel");
        this.outcomeProjector = Objects.requireNonNull(outcomeProjector, "outcomeProjector");
        this.coordinator =
                new BatchRunCoordinator<>(
                        "eifd-tif-increment",
                        EifdTifIncrementRunResponse.class,
                        batchRunStore,
                        executor,
                        batchRunLifecycle);
        this.outcomeRecorder = Objects.requireNonNull(outcomeRecorder, "outcomeRecorder");
    }

    /// Replays the durable snapshot for the generated run identity without starting more work.
    @Override
    public Result<EifdTifIncrementRunResponse, BatchRunFailure> getEifdTifIncrementRun(Long id) {
        return coordinator.find(id);
    }

    @Override
    public Result<BatchRunStart<EifdTifIncrementRunResponse>, BatchRunFailure>
            startEifdTifIncrementRun(EifdTifIncrementRunRequest request) {
        return validatedRequest(request)
                .flatMap(
                        pinned ->
                                coordinator.start(
                                        Objects.requireNonNull(pinned.idempotencyKey()),
                                        fingerprint(pinned),
                                        id -> snapshot(id, "QUEUED", pinned, null),
                                        id -> admissionRejected(id, pinned),
                                        id -> snapshot(id, "FAILED", pinned, operationalFailure()),
                                        id -> execute(id, pinned)));
    }

    /// Creates the durable terminal snapshot used when admission rejects queued work.
    ///
    /// The initial launch still returns the typed capacity failure. An exact replay observes this
    /// same failed resource and does not submit another worker.
    private EifdTifIncrementRunResponse admissionRejected(
            long id, EifdTifIncrementRunRequest request) {
        EifdTifIncrementProcessResult rejected =
                new EifdTifIncrementProcessResult(
                        false,
                        16,
                        0,
                        0,
                        0,
                        0,
                        List.of(),
                        List.of(
                                new EifdTifIncrementProcessResult.Message(
                                        "ERROR", "Batch admission capacity is exhausted.", null)));
        return snapshot(id, "FAILED", request, rejected);
    }

    /// Builds a safe failure result when execution stops outside the expected rule outcomes.
    private EifdTifIncrementProcessResult operationalFailure() {
        EifdTifIncrementProcessResult initial =
                new EifdTifIncrementProcessResult(
                        false,
                        16,
                        0,
                        0,
                        0,
                        0,
                        List.of(),
                        List.of(
                                new EifdTifIncrementProcessResult.Message(
                                        "ERROR", "The increment batch worker failed.", null)));
        return new EifdTifIncrementProcessResult(
                initial.completed(),
                initial.returnCode(),
                initial.recordsRead(),
                initial.recordsWritten(),
                initial.recordsUpdated(),
                initial.recordsRejected(),
                initial.outputs(),
                initial.messages(),
                outcomeProjector.failed(initial));
    }

    /// Executes the reserved run and publishes its terminal snapshot.
    ///
    /// Expected rule failures arrive in the process result. An unexpected runtime failure publishes
    /// return code 16 without internal diagnostics. Completed earlier steps remain committed.
    private void execute(long id, EifdTifIncrementRunRequest request) {
        if (!coordinator.running(id, snapshot(id, "RUNNING", request, null))) {
            return;
        }
        try {
            EifdTifIncrementProcessResult result = processor.process(request);
            String status = result.completed() ? "COMPLETED" : "FAILED";
            boolean published =
                    status.equals("COMPLETED")
                            ? coordinator.complete(id, snapshot(id, status, request, result))
                            : coordinator.fail(id, snapshot(id, status, request, result));
            var outcome = result.outcome();
            if (published && outcome != null) {
                outcomeRecorder.completed("eifd-tif-increment", id, status, outcome);
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Increment batch run {} failed", id, exception);
            EifdTifIncrementProcessResult failed = operationalFailure();
            if (coordinator.fail(id, snapshot(id, "FAILED", request, failed))) {
                outcomeRecorder.completed(
                        "eifd-tif-increment",
                        id,
                        "FAILED",
                        Objects.requireNonNull(failed.outcome()));
            }
        }
    }

    /// Creates one immutable durable snapshot from the current process result.
    private EifdTifIncrementRunResponse snapshot(
            long id,
            String status,
            EifdTifIncrementRunRequest request,
            @Nullable EifdTifIncrementProcessResult result) {
        List<EifdTifIncrementOutput> outputs =
                result == null
                        ? List.of()
                        : result.outputs().stream()
                                .map(
                                        source ->
                                                new EifdTifIncrementOutput(
                                                        source.generation(),
                                                        source.name(),
                                                        source.recordCount()))
                                .toList();
        List<EifdTifIncrementMessage> messages =
                result == null
                        ? List.of()
                        : result.messages().stream()
                                .map(
                                        source ->
                                                new EifdTifIncrementMessage(
                                                        source.ruleId(),
                                                        source.severity(),
                                                        source.text()))
                                .toList();
        return new EifdTifIncrementRunResponse(
                Objects.requireNonNull(request.businessDate()),
                Objects.requireNonNull(request.businessTime()),
                id,
                messages,
                outputs,
                result == null ? null : result.recordsRead(),
                result == null ? null : result.recordsRejected(),
                result == null ? null : result.recordsUpdated(),
                result == null ? null : result.recordsWritten(),
                result == null ? null : result.returnCode(),
                status);
    }

    /// Validates controls before a run reservation can become durable.
    private Result<EifdTifIncrementRunRequest, BatchRunFailure> validatedRequest(
            @Nullable EifdTifIncrementRunRequest request) {
        if (request == null) {
            return invalid("request body is required");
        }
        if (request.businessDate() == null) {
            return invalid("businessDate is required");
        }
        if (blank(request.businessTime())) {
            return invalid("businessTime is required");
        }
        if (!Objects.requireNonNull(request.businessTime())
                .matches("^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")) {
            return invalid("businessTime must use 24-hour HH:mm:ss format");
        }
        if (blank(request.idempotencyKey())) {
            return invalid("idempotencyKey is required");
        }
        if (blank(request.reportingYear())
                || !Objects.requireNonNull(request.reportingYear()).matches("^[0-9]{4}$")) {
            return invalid("reportingYear must contain exactly four digits");
        }
        try {
            kernel.validateControls(
                    request.reassessmentControl(),
                    request.processingYear(),
                    request.annualEqualizationFactor());
        } catch (EifdTifIncrementKernel.RuleViolation violation) {
            return invalid(violation.getMessage());
        }
        return new Result.Ok<>(request);
    }

    /// Creates the typed invalid-request branch without reserving a run.
    private static <T> Result<T, BatchRunFailure> invalid(String message) {
        return new Result.Err<>(new BatchRunFailure.InvalidRequest(message));
    }

    /// Builds the stable fingerprint that distinguishes conflicting uses of one replay key.
    private String fingerprint(EifdTifIncrementRunRequest request) {
        return String.join(
                "\u001f",
                Objects.requireNonNull(request.businessDate()).toString(),
                Objects.requireNonNull(request.businessTime()),
                Objects.requireNonNull(request.reassessmentControl()),
                Objects.requireNonNull(request.processingYear()),
                Objects.requireNonNull(request.reportingYear()),
                Objects.requireNonNull(request.annualEqualizationFactor()));
    }

    /// Reports whether an optional launch string is absent or blank.
    private boolean blank(@Nullable String value) {
        return value == null || value.isBlank();
    }
}
