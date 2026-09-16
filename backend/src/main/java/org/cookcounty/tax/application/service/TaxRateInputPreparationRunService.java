package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationAgencyAttachmentCounts;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationAgencyComparisonCounts;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationDivisionStampingCounts;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationFrozenAgencyPostingCounts;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationMessage;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationOutput;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationReconciliation;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.TaxRateInputPreparationRunResponse;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/// Reserves, executes, and publishes durable tax-rate input preparation snapshots.
///
/// Exact request replays return the existing run without another execution. Active work uses the
/// shared repository state. Successful posting mutations remain when a later operation fails.
@Service
public final class TaxRateInputPreparationRunService implements TaxRateInputPreparationRunUseCase {
    /// Keeps worker stack traces in server diagnostics, separate from safe public responses.
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TaxRateInputPreparationRunService.class);

    private static final String TIME_PATTERN = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
    private static final String FACTOR_SCENARIO_ID = "clerk-agency-attachment";

    private final TaxRateInputProcessor processor;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final TaxRateInputFactorOutcomeProjector outcomeProjector;
    private final BatchRunCoordinator<TaxRateInputPreparationRunResponse> coordinator;

    /// Creates the service with the shared executor, persistent run store, and outcome recorder.
    public TaxRateInputPreparationRunService(
            TaxRateInputProcessor processor,
            @Qualifier("batchRunExecutor") Executor executor,
            FactorBatchOutcomeRecorder outcomeRecorder,
            TaxRateInputFactorOutcomeProjector outcomeProjector,
            BatchRunStore batchRunStore,
            BatchRunLifecycle batchRunLifecycle) {
        this.processor = processor;
        this.coordinator =
                new BatchRunCoordinator<>(
                        "tax-rate-input-preparation",
                        TaxRateInputPreparationRunResponse.class,
                        batchRunStore,
                        executor,
                        batchRunLifecycle);
        this.outcomeRecorder = outcomeRecorder;
        this.outcomeProjector = outcomeProjector;
    }

    /// Returns the latest durable snapshot or a typed missing-run failure.
    @Override
    public org.cookcounty.tax.domain.contract.Result<
                    TaxRateInputPreparationRunResponse, BatchRunFailure>
            getTaxRateInputPreparationRun(Long id) {
        return coordinator.find(id);
    }

    /// Reserves one asynchronous run or returns the exact replay or a typed expected failure.
    ///
    /// A replay makes no new submission. Reusing a key with different business-clock values returns
    /// an idempotency conflict.
    @Override
    public org.cookcounty.tax.domain.contract.Result<
                    BatchRunStart<TaxRateInputPreparationRunResponse>, BatchRunFailure>
            startTaxRateInputPreparationRun(TaxRateInputPreparationRunRequest request) {
        return validatedRequest(request)
                .flatMap(
                        controls -> {
                            String fingerprint =
                                    controls.businessDate() + "|" + controls.businessTime();
                            return coordinator.start(
                                    controls.idempotencyKey(),
                                    fingerprint,
                                    id -> baseSnapshot(id, "QUEUED", controls),
                                    id -> admissionRejectedSnapshot(id, controls),
                                    id -> workerFailureSnapshot(id, controls),
                                    id -> execute(id, controls));
                        });
    }

    /// Executes against shared repository state and publishes one terminal snapshot.
    private void execute(long id, ValidatedRequest request) {
        if (!coordinator.running(id, baseSnapshot(id, "RUNNING", request))) {
            return;
        }
        TaxRateInputPreparationRunResponse response;
        Outcome outcome;
        try {
            Result result = processor.process();
            response = terminalSnapshot(id, request, result);
            outcome =
                    outcomeProjector.project(
                            request.businessDate(), request.businessTime(), result);
        } catch (RuntimeException failure) {
            LOGGER.error("Tax-rate input preparation run {} failed", id, failure);
            response = workerFailureSnapshot(id, request);
            outcome = outcomeProjector.workerFailure(failure);
        }
        boolean published =
                response.status().equals("COMPLETED")
                        ? coordinator.complete(id, response)
                        : coordinator.fail(id, response);
        if (published) {
            outcomeRecorder.completed(FACTOR_SCENARIO_ID, id, response.status(), outcome);
        }
    }

    /// Combines stage operation totals without replacing the stage-level reconciliation contract.
    private static TaxRateInputPreparationRunResponse terminalSnapshot(
            long id, ValidatedRequest request, Result result) {
        int recordsRead =
                result.divisionStamping().equalizedValueRecordsRead()
                        + result.divisionStamping().divisionRecordsRead()
                        + result.agencyAttachment().assessmentRecordsRead()
                        + result.agencyComparison().priorRecordsRead()
                        + result.agencyComparison().currentRecordsRead()
                        + result.frozenAgencyPosting().priorRecordsRead()
                        + result.frozenAgencyPosting().currentRecordsRead()
                        + result.frozenAgencyPosting().comparisonRecordsRead();
        int recordsWritten =
                result.divisionStamping().outputRecordsWritten()
                        + result.agencyAttachment().assessmentRecordsWritten()
                        + result.agencyComparison().comparisonRecordsWritten()
                        + result.frozenAgencyPosting().insertOperations();
        int recordsUpdated =
                result.divisionStamping().recordsStamped()
                        + result.frozenAgencyPosting().rewriteOperations();
        int recordsRejected =
                result.agencyAttachment().assessmentRecordsUnmatched()
                        + result.agencyComparison().priorOnlyDivisions()
                        + result.agencyComparison().currentOnlyDivisions();
        return new TaxRateInputPreparationRunResponse(
                request.businessDate(),
                request.businessTime(),
                id,
                result.messages().stream().map(TaxRateInputPreparationRunService::message).toList(),
                outputs(result),
                reconciliation(result),
                recordsRead,
                recordsRejected,
                recordsUpdated,
                recordsWritten,
                result.returnCode(),
                result.returnCode() == 0 ? "COMPLETED" : "FAILED");
    }

    /// Creates the durable terminal snapshot used when admission rejects queued work.
    ///
    /// The initial start returns a typed capacity failure. An exact replay observes this failed
    /// snapshot and does not submit another worker.
    private static TaxRateInputPreparationRunResponse admissionRejectedSnapshot(
            long id, ValidatedRequest request) {
        return new TaxRateInputPreparationRunResponse(
                request.businessDate(),
                request.businessTime(),
                id,
                List.of(
                        new TaxRateInputPreparationMessage(
                                "BATCH_CAPACITY_EXHAUSTED",
                                null,
                                "ERROR",
                                "Batch admission capacity is exhausted.")),
                List.of(),
                null,
                0,
                0,
                0,
                0,
                TaxRateInputKernel.ERROR_RETURN_CODE,
                "FAILED");
    }

    /// Publishes a safe terminal diagnostic when execution fails outside typed kernel outcomes.
    private static TaxRateInputPreparationRunResponse workerFailureSnapshot(
            long id, ValidatedRequest request) {
        String text = "The tax-rate input preparation worker failed.";
        return new TaxRateInputPreparationRunResponse(
                request.businessDate(),
                request.businessTime(),
                id,
                List.of(new TaxRateInputPreparationMessage("WORKER_FAILURE", null, "ERROR", text)),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                TaxRateInputKernel.ERROR_RETURN_CODE,
                "FAILED");
    }

    /// Creates a queued or active snapshot before stage totals are available.
    private static TaxRateInputPreparationRunResponse baseSnapshot(
            long id, String status, ValidatedRequest request) {
        return new TaxRateInputPreparationRunResponse(
                request.businessDate(),
                request.businessTime(),
                id,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                status);
    }

    /// Returns logical output counts in processing-stage order.
    private static List<TaxRateInputPreparationOutput> outputs(Result result) {
        List<TaxRateInputPreparationOutput> outputs = new ArrayList<>();
        outputs.add(output("dividedValue", "DIVIDED_VALUE", result.dividedValues().size()));
        outputs.add(
                output("agencyAssessment", "AGENCY_ASSESSMENT", result.agencyAssessments().size()));
        outputs.add(output("annexDisconnect", "ANNEX_DISCONNECT", result.comparisons().size()));
        outputs.add(output("frozenAgency", "FROZEN_AGENCY", result.postings().size()));
        outputs.add(
                output(
                        "attachmentReport",
                        "REPORT",
                        9 + result.agencyAttachment().assessmentRecordsUnmatched()));
        return List.copyOf(outputs);
    }

    /// Creates an available server-managed logical output.
    private static TaxRateInputPreparationOutput output(String name, String kind, int count) {
        return new TaxRateInputPreparationOutput(true, kind, name, count);
    }

    /// Preserves the kernel diagnostic order and optional rule identity.
    private static TaxRateInputPreparationMessage message(TaxRateInputKernel.Message source) {
        return new TaxRateInputPreparationMessage(
                source.code(), source.ruleId(), source.severity(), source.text());
    }

    /// Maps every stage counter into the public reconciliation shape.
    private static TaxRateInputPreparationReconciliation reconciliation(Result result) {
        var attachment =
                new TaxRateInputPreparationAgencyAttachmentCounts(
                        result.agencyAttachment().assessmentRecordsRead(),
                        result.agencyAttachment().assessmentRecordsUnmatched(),
                        result.agencyAttachment().assessmentRecordsWritten(),
                        result.agencyAttachment().normalCompletionBalanced());
        var comparison =
                new TaxRateInputPreparationAgencyComparisonCounts(
                        result.agencyComparison().annexSegments(),
                        result.agencyComparison().comparisonRecordsWritten(),
                        result.agencyComparison().currentOnlyDivisions(),
                        result.agencyComparison().currentRecordsRead(),
                        result.agencyComparison().disconnectSegments(),
                        result.agencyComparison().priorOnlyDivisions(),
                        result.agencyComparison().priorRecordsRead());
        var division =
                new TaxRateInputPreparationDivisionStampingCounts(
                        result.divisionStamping().divisionRecordsRead(),
                        result.divisionStamping().divisionRecordsUnmatched(),
                        result.divisionStamping().equalizedValueRecordsRead(),
                        result.divisionStamping().outputRecordsWritten(),
                        result.divisionStamping().recordsStamped());
        var posting =
                new TaxRateInputPreparationFrozenAgencyPostingCounts(
                        result.frozenAgencyPosting().comparisonRecordsRead(),
                        result.frozenAgencyPosting().currentRecordsRead(),
                        result.frozenAgencyPosting().insertOperations(),
                        result.frozenAgencyPosting().priorRecordsRead(),
                        result.frozenAgencyPosting().rewriteOperations());
        return new TaxRateInputPreparationReconciliation(attachment, comparison, division, posting);
    }

    /// Converts structurally valid nullable request components into a non-null internal contract.
    private static org.cookcounty.tax.domain.contract.Result<ValidatedRequest, BatchRunFailure>
            validatedRequest(TaxRateInputPreparationRunRequest request) {
        var businessDate = request.businessDate();
        var businessTime = request.businessTime();
        var idempotencyKey = request.idempotencyKey();
        if (businessDate == null) {
            return invalid("businessDate is required");
        }
        if (businessTime == null || !businessTime.matches(TIME_PATTERN)) {
            return invalid("businessTime must use 24-hour HH:mm:ss format");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return invalid("idempotencyKey is required");
        }
        if (idempotencyKey.length() > 128) {
            return invalid("idempotencyKey must contain at most 128 characters");
        }
        return new org.cookcounty.tax.domain.contract.Result.Ok<>(
                new ValidatedRequest(businessDate, businessTime, idempotencyKey));
    }

    /// Creates the shared typed invalid-request alternative.
    private static <T> org.cookcounty.tax.domain.contract.Result<T, BatchRunFailure> invalid(
            String message) {
        return new org.cookcounty.tax.domain.contract.Result.Err<>(
                new BatchRunFailure.InvalidRequest(message));
    }

    /// Holds the non-null controls used after structural validation.
    private record ValidatedRequest(
            LocalDate businessDate, String businessTime, String idempotencyKey) {}
}
