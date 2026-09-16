package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationMessage;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationOutput;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunResponse;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationStageResult;
import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

/// Reserves, executes, and observes durable assessed-value preparation runs.
///
/// The coordinator persists run identity before the bounded executor starts work. Exact replays use
/// the stored snapshot. A worker failure records a terminal result. An interrupted worker does not
/// resume from an intermediate assessed-value stage.
@Service
public final class AssessedValuePreparationRunService
        implements AssessedValuePreparationRunUseCase {

    /// Keeps worker stack traces in server diagnostics, separate from safe public responses.
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AssessedValuePreparationRunService.class);

    private final AssessedValuePreparationProcessor processor;
    private final AssessedValuePreparationFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final BatchRunCoordinator<AssessedValuePreparationRunResponse> runs;

    /// Creates the durable run service and its qualified execution path.
    ///
    /// @param processor transactional assessed-value workflow
    /// @param outcomeProjector comparator projection for completed work
    /// @param outcomeRecorder accepted and completed comparator evidence store
    /// @param batchRunStore persisted run identity and snapshots
    /// @param executor bounded executor dedicated to batch runs
    /// @param batchRunLifecycle durable owner and lease manager
    public AssessedValuePreparationRunService(
            AssessedValuePreparationProcessor processor,
            AssessedValuePreparationFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder,
            BatchRunStore batchRunStore,
            @Qualifier("batchRunExecutor") Executor executor,
            BatchRunLifecycle batchRunLifecycle) {
        this.processor = processor;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
        this.runs =
                new BatchRunCoordinator<>(
                        "assessed-value-preparation",
                        AssessedValuePreparationRunResponse.class,
                        batchRunStore,
                        executor,
                        batchRunLifecycle);
    }

    /// Returns the latest persisted snapshot or a typed missing-run failure.
    @Override
    public Result<AssessedValuePreparationRunResponse, BatchRunFailure>
            getAssessedValuePreparationRun(Long id) {
        return runs.find(id);
    }

    /// Validates controls and reserves a new run or returns an exact replay.
    @Override
    public Result<BatchRunStart<AssessedValuePreparationRunResponse>, BatchRunFailure>
            startAssessedValuePreparationRun(AssessedValuePreparationRunRequest request) {
        return validatedControls(request)
                .flatMap(
                        controls ->
                                runs.start(
                                        controls.idempotencyKey(),
                                        controls.fingerprint(),
                                        id -> snapshot(id, "QUEUED", controls, null),
                                        id -> admissionFailure(id, controls),
                                        id -> operationalFailure(id, controls),
                                        id -> execute(id, controls)));
    }

    /// Executes one reserved run and records its terminal comparator outcome.
    private void execute(long id, Controls controls) {
        if (!runs.running(id, snapshot(id, "RUNNING", controls, null))) {
            return;
        }
        ProcessResult result;
        try {
            result =
                    processor.process(
                            controls.businessDate(),
                            controls.businessTime(),
                            controls.processYear());
        } catch (RuntimeException exception) {
            LOGGER.error("Assessed-value preparation run {} failed", id, exception);
            if (runs.fail(id, operationalFailure(id, controls))) {
                outcomeRecorder.completed(
                        "valuation-preparation",
                        id,
                        "FAILED",
                        outcomeProjector.workerFailure(exception));
            }
            return;
        }
        String status = result.failed() ? "FAILED" : "COMPLETED";
        if (status.equals("COMPLETED")
                ? runs.complete(id, snapshot(id, status, controls, result))
                : runs.fail(id, snapshot(id, status, controls, result))) {
            outcomeRecorder.completed(
                    "valuation-preparation", id, status, outcomeProjector.project(result));
        }
    }

    /// Converts validated request components to non-null internal controls.
    private static Result<Controls, BatchRunFailure> validatedControls(
            AssessedValuePreparationRunRequest request) {
        LocalDate businessDate = request.businessDate();
        if (businessDate == null) {
            return invalid("businessDate is required");
        }
        String businessTime = request.businessTime();
        if (businessTime == null
                || !businessTime.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]")) {
            return invalid("businessTime must use HH:mm:ss");
        }
        String idempotencyKey = request.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return invalid("idempotencyKey is required");
        }
        if (idempotencyKey.length() > 128) {
            return invalid("idempotencyKey must not exceed 128 characters");
        }
        String processYear = request.processYear();
        if (processYear == null || !processYear.matches("[0-9]{2}")) {
            return invalid("processYear must contain exactly two decimal digits.");
        }
        return new Result.Ok<>(
                new Controls(businessDate, businessTime, idempotencyKey, processYear));
    }

    /// Creates a typed request failure without dispatching work.
    private static <T> Result<T, BatchRunFailure> invalid(String message) {
        return new Result.Err<>(new BatchRunFailure.InvalidRequest(message));
    }

    /// Builds an immutable queued, running, or terminal run snapshot.
    private static AssessedValuePreparationRunResponse snapshot(
            long id, String status, Controls controls, @Nullable ProcessResult result) {
        if (result == null) {
            return new AssessedValuePreparationRunResponse(
                    controls.businessDate(),
                    controls.businessTime(),
                    id,
                    List.of(),
                    List.of(),
                    null,
                    controls.processYear(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    status);
        }
        return new AssessedValuePreparationRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                id,
                mapMessages(result.messages()),
                mapOutputs(id, result.outputs(), result.partialOutput()),
                result.partialOutput(),
                controls.processYear(),
                result.recordsRead(),
                result.recordsRejected(),
                result.recordsUpdated(),
                result.recordsWritten(),
                result.returnCode(),
                mapStages(result.stages()),
                status);
    }

    /// Maps output evidence without exposing raw fixed-width record data.
    private static List<AssessedValuePreparationOutput> mapOutputs(
            long id, List<OutputRecord> source, boolean partial) {
        return java.util.stream.IntStream.range(0, source.size())
                .mapToObj(
                        index -> {
                            OutputRecord record = source.get(index);
                            return new AssessedValuePreparationOutput(
                                    "avpout_" + id + "_" + String.format("%02d", index + 1),
                                    record.kind(),
                                    record.mediaType(),
                                    partial,
                                    record.recordCount());
                        })
                .toList();
    }

    /// Maps internal rule observations to the stable API shape.
    private static List<AssessedValuePreparationMessage> mapMessages(
            List<AssessedValueRuleMessage> source) {
        return source.stream()
                .map(
                        record ->
                                new AssessedValuePreparationMessage(
                                        record.code(),
                                        record.message(),
                                        record.recordKey(),
                                        record.ruleId(),
                                        record.severity()))
                .toList();
    }

    /// Maps ordered stage evidence to the stable API shape.
    private static List<AssessedValuePreparationStageResult> mapStages(List<StageOutcome> source) {
        return source.stream()
                .map(
                        outcome ->
                                new AssessedValuePreparationStageResult(
                                        outcome.outputPublished(),
                                        outcome.partialOutput(),
                                        outcome.recordsRead(),
                                        outcome.recordsRejected(),
                                        outcome.recordsUpdated(),
                                        outcome.recordsWritten(),
                                        outcome.returnCode(),
                                        outcome.stage(),
                                        outcome.status()))
                .toList();
    }

    /// Builds a terminal failure snapshot after an unexpected worker exception.
    private static AssessedValuePreparationRunResponse operationalFailure(
            long id, Controls controls) {
        String text = "The assessed-value preparation worker failed.";
        return new AssessedValuePreparationRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                id,
                List.of(
                        new AssessedValuePreparationMessage(
                                "WORKER_FAILED", text, null, null, "ERROR")),
                List.of(),
                false,
                controls.processYear(),
                0,
                0,
                0,
                0,
                16,
                List.of(),
                "FAILED");
    }

    /// Builds the durable terminal snapshot used when the bounded executor rejects admission.
    private static AssessedValuePreparationRunResponse admissionFailure(
            long id, Controls controls) {
        return new AssessedValuePreparationRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                id,
                List.of(
                        new AssessedValuePreparationMessage(
                                "BATCH_CAPACITY_EXHAUSTED",
                                "The assessed-value preparation run was rejected because batch"
                                        + " capacity is exhausted.",
                                null,
                                null,
                                "ERROR")),
                List.of(),
                false,
                controls.processYear(),
                0,
                0,
                0,
                0,
                16,
                List.of(),
                "FAILED");
    }

    /// Validated controls used for persisted identity and worker dispatch.
    private record Controls(
            LocalDate businessDate,
            String businessTime,
            String idempotencyKey,
            String processYear) {

        /// Returns the stable semantic request fingerprint excluding the idempotency key.
        private String fingerprint() {
            return businessDate + "\u001f" + businessTime + "\u001f" + processYear;
        }
    }
}
