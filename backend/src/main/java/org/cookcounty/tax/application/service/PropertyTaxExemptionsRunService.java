package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Message;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.OutputRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Reconciliation;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Rejection;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RuleDisposition;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsMessage;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsOutput;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsReconciliationOutcome;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRejectionOutcome;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRuleOutcome;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.domain.contract.dto.PropertyTaxExemptionsRunResponse;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;

/// Owns durable run reservation, replay detection, asynchronous execution, and final snapshots.
@Service
public class PropertyTaxExemptionsRunService implements PropertyTaxExemptionsRunUseCase {

    /// Keeps worker stack traces in server diagnostics, separate from safe public responses.
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PropertyTaxExemptionsRunService.class);

    private final PropertyTaxExemptionsProcessor processor;
    private final PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final BatchRunCoordinator<PropertyTaxExemptionsRunResponse> runs;

    /// Creates the service with durable storage and the bounded batch executor.
    public PropertyTaxExemptionsRunService(
            PropertyTaxExemptionsProcessor processor,
            PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder,
            BatchRunStore batchRunStore,
            @Qualifier("batchRunExecutor") Executor executor,
            BatchRunLifecycle batchRunLifecycle) {
        this.processor = processor;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
        this.runs =
                new BatchRunCoordinator<>(
                        "property-tax-exemptions",
                        PropertyTaxExemptionsRunResponse.class,
                        batchRunStore,
                        executor,
                        batchRunLifecycle);
    }

    /// Returns the durable snapshot for one run identity.
    @Override
    public Result<PropertyTaxExemptionsRunResponse, BatchRunFailure> getPropertyTaxExemptionsRun(
            Long id) {
        return runs.find(id);
    }

    /// Reserves or replays a run, then admits new work to asynchronous execution.
    @Override
    public Result<BatchRunStart<PropertyTaxExemptionsRunResponse>, BatchRunFailure>
            startPropertyTaxExemptionsRun(PropertyTaxExemptionsRunRequest request) {
        return validatedControls(request)
                .flatMap(
                        controls ->
                                runs.start(
                                        controls.idempotencyKey(),
                                        controls.fingerprint(),
                                        id -> baseSnapshot(id, "QUEUED", controls),
                                        id -> admissionRejectedSnapshot(id, controls),
                                        id -> failedSnapshot(id, controls),
                                        id -> execute(id, controls)));
    }

    private void execute(long id, Controls controls) {
        if (!runs.running(id, baseSnapshot(id, "RUNNING", controls))) {
            return;
        }
        String scenarioId = outcomeProjector.scenarioId(controls.variant());
        try {
            ProcessResult result =
                    processor.process(
                            controls.businessDate(), controls.businessTime(), controls.variant());
            PropertyTaxExemptionsRunResponse completed = completedSnapshot(id, controls, result);
            if (runs.complete(id, completed)) {
                outcomeRecorder.completed(
                        scenarioId,
                        id,
                        completed.status(),
                        outcomeProjector.project(controls.variant(), result));
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Property-tax exemption run {} failed", id, exception);
            PropertyTaxExemptionsRunResponse failed = failedSnapshot(id, controls);
            if (runs.fail(id, failed)) {
                outcomeRecorder.completed(
                        scenarioId, id, failed.status(), outcomeProjector.failed(exception));
            }
        }
    }

    private static Result<Controls, BatchRunFailure> validatedControls(
            PropertyTaxExemptionsRunRequest request) {
        var businessDate = request.businessDate();
        if (businessDate == null) {
            return invalid("businessDate is required");
        }
        var businessTime = request.businessTime();
        if (businessTime == null
                || !businessTime.matches("^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$")) {
            return invalid("businessTime must use HH:mm:ss");
        }
        var idempotencyKey = request.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return invalid("idempotencyKey is required");
        }
        if (idempotencyKey.length() > 128) {
            return invalid("idempotencyKey must not exceed 128 characters");
        }

        var requestedVariant = request.homeownerProcessingVariant();
        if (requestedVariant == null) {
            return invalid("homeownerProcessingVariant must be ENUMERATED or BROAD");
        }
        HomeownerVariant variant;
        try {
            variant = HomeownerVariant.valueOf(requestedVariant);
        } catch (IllegalArgumentException exception) {
            return invalid("homeownerProcessingVariant must be ENUMERATED or BROAD");
        }
        return new Result.Ok<>(new Controls(businessDate, businessTime, idempotencyKey, variant));
    }

    private static <T> Result<T, BatchRunFailure> invalid(String message) {
        return new Result.Err<>(new BatchRunFailure.InvalidRequest(message));
    }

    private static PropertyTaxExemptionsRunResponse baseSnapshot(
            long id, String status, Controls controls) {
        return new PropertyTaxExemptionsRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                controls.variant().name(),
                id,
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
                status);
    }

    private static PropertyTaxExemptionsRunResponse completedSnapshot(
            long id, Controls controls, ProcessResult result) {
        return new PropertyTaxExemptionsRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                controls.variant().name(),
                id,
                mapMessages(result.messages()),
                mapOutputs(result.outputs()),
                mapReconciliations(result.reconciliations()),
                (long) result.recordsRead(),
                (long) result.recordsRejected(),
                (long) result.recordsUpdated(),
                (long) result.recordsWritten(),
                mapRejections(result.rejections()),
                result.returnCode(),
                mapRuleOutcomes(result.ruleOutcomes()),
                "COMPLETED");
    }

    private static PropertyTaxExemptionsRunResponse failedSnapshot(long id, Controls controls) {
        return terminalFailureSnapshot(id, controls, "The property-tax-exemptions worker failed.");
    }

    private static PropertyTaxExemptionsRunResponse admissionRejectedSnapshot(
            long id, Controls controls) {
        return terminalFailureSnapshot(
                id,
                controls,
                "The service could not start the run because batch capacity was exhausted.");
    }

    private static PropertyTaxExemptionsRunResponse terminalFailureSnapshot(
            long id, Controls controls, String failureMessage) {
        var output =
                new PropertyTaxExemptionsOutput(
                        "DATASET", "ASREA859 HOMEOUT", "WITHHELD", 0L, List.of("asrea859-001"));
        var message = new PropertyTaxExemptionsMessage(failureMessage, null, "ERROR");
        var reconciliation =
                new PropertyTaxExemptionsReconciliationOutcome(
                        "The logical output was withheld after an operational failure.",
                        "PROPERTY TAX EXEMPTIONS RUN",
                        0L,
                        0L,
                        0L,
                        0L,
                        List.of("asrea859-001"),
                        "FAILED");
        return new PropertyTaxExemptionsRunResponse(
                controls.businessDate(),
                controls.businessTime(),
                controls.variant().name(),
                id,
                List.of(message),
                List.of(output),
                List.of(reconciliation),
                0L,
                0L,
                0L,
                0L,
                List.of(),
                16,
                List.of(),
                "FAILED");
    }

    private static List<PropertyTaxExemptionsOutput> mapOutputs(List<OutputRecord> source) {
        return source.stream()
                .map(
                        record ->
                                new PropertyTaxExemptionsOutput(
                                        record.kind(),
                                        record.name(),
                                        record.publicationStatus(),
                                        record.recordCount(),
                                        record.ruleIds()))
                .toList();
    }

    private static List<PropertyTaxExemptionsMessage> mapMessages(List<Message> source) {
        return source.stream()
                .map(
                        record ->
                                new PropertyTaxExemptionsMessage(
                                        record.message(), record.ruleId(), record.severity()))
                .toList();
    }

    private static List<PropertyTaxExemptionsRejectionOutcome> mapRejections(
            List<Rejection> source) {
        return source.stream()
                .map(
                        record ->
                                new PropertyTaxExemptionsRejectionOutcome(
                                        record.message(),
                                        record.outcome(),
                                        record.recordKey(),
                                        record.ruleId(),
                                        record.source()))
                .toList();
    }

    private static List<PropertyTaxExemptionsReconciliationOutcome> mapReconciliations(
            List<Reconciliation> source) {
        return source.stream()
                .map(
                        record ->
                                new PropertyTaxExemptionsReconciliationOutcome(
                                        record.message(),
                                        record.name(),
                                        record.recordsMatched(),
                                        record.recordsRead(),
                                        record.recordsRejected(),
                                        record.recordsWritten(),
                                        record.ruleIds(),
                                        record.status()))
                .toList();
    }

    private static List<PropertyTaxExemptionsRuleOutcome> mapRuleOutcomes(
            List<RuleDisposition> source) {
        return source.stream()
                .map(
                        record ->
                                new PropertyTaxExemptionsRuleOutcome(
                                        record.message(),
                                        record.outcome(),
                                        record.recordsAffected(),
                                        record.ruleId()))
                .toList();
    }

    private record Controls(
            LocalDate businessDate,
            String businessTime,
            String idempotencyKey,
            HomeownerVariant variant) {

        String fingerprint() {
            return businessDate + "\u001f" + businessTime + "\u001f" + variant;
        }
    }
}
