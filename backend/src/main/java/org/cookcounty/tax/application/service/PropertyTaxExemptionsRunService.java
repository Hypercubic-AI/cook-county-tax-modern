package org.cookcounty.tax.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Message;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.OutputRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Reconciliation;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.Rejection;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RuleDisposition;
import org.cookcounty.tax.domain.port.in.PropertyTaxExemptionsRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsMessage;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsOutput;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsReconciliationOutcome;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRejectionOutcome;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRuleOutcome;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.PropertyTaxExemptionsRunResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PropertyTaxExemptionsRunService implements PropertyTaxExemptionsRunUseCase {

    private final PropertyTaxExemptionsProcessor processor;
    private final PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final BatchRunCoordinator<PropertyTaxExemptionsRunResponse> runs;

    public PropertyTaxExemptionsRunService(
            PropertyTaxExemptionsProcessor processor,
            PropertyTaxExemptionsFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder,
            @Qualifier("batchRunExecutor") Executor executor) {
        this.processor = processor;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
        this.runs = new BatchRunCoordinator<>(executor);
    }

    @Override
    public Optional<PropertyTaxExemptionsRunResponse> getPropertyTaxExemptionsRun(Long id) {
        return id == null ? Optional.empty() : runs.find(id);
    }

    @Override
    public PropertyTaxExemptionsRunResponse startPropertyTaxExemptionsRun(
            PropertyTaxExemptionsRunRequest request) {
        Controls controls = validatedControls(request);
        return runs.start(
                        controls.idempotencyKey(),
                        controls.fingerprint(),
                        id -> baseSnapshot(id, "QUEUED", controls),
                        id -> execute(id, controls))
                .response();
    }

    private void execute(long id, Controls controls) {
        runs.replace(id, baseSnapshot(id, "RUNNING", controls));
        String scenarioId = outcomeProjector.scenarioId(controls.variant());
        try {
            ProcessResult result = processor.process(
                    controls.businessDate(), controls.businessTime(), controls.variant());
            PropertyTaxExemptionsRunResponse completed =
                    completedSnapshot(id, controls, result);
            runs.replace(id, completed);
            outcomeRecorder.completed(
                    scenarioId, id, completed.getStatus(), outcomeProjector.project(
                            controls.variant(), result));
        } catch (RuntimeException exception) {
            PropertyTaxExemptionsRunResponse failed =
                    failedSnapshot(id, controls, exception);
            runs.replace(id, failed);
            outcomeRecorder.completed(
                    scenarioId, id, failed.getStatus(), outcomeProjector.failed(exception));
        }
    }

    private static Controls validatedControls(PropertyTaxExemptionsRunRequest request) {
        if (request == null) {
            throw new BatchRunInvalidRequestException("request is required");
        }
        if (request.getBusinessDate() == null) {
            throw new BatchRunInvalidRequestException("businessDate is required");
        }
        String businessTime = request.getBusinessTime();
        if (businessTime == null
                || !businessTime.matches("^(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$")) {
            throw new BatchRunInvalidRequestException("businessTime must use HH:mm:ss");
        }
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BatchRunInvalidRequestException("idempotencyKey is required");
        }
        if (idempotencyKey.length() > 128) {
            throw new BatchRunInvalidRequestException(
                    "idempotencyKey must not exceed 128 characters");
        }

        HomeownerVariant variant;
        try {
            variant = HomeownerVariant.valueOf(request.getHomeownerProcessingVariant());
        } catch (NullPointerException | IllegalArgumentException exception) {
            throw new BatchRunInvalidRequestException(
                    "homeownerProcessingVariant must be ENUMERATED or BROAD");
        }
        return new Controls(
                request.getBusinessDate(), businessTime, idempotencyKey, variant);
    }

    private static PropertyTaxExemptionsRunResponse baseSnapshot(
            long id,
            String status,
            Controls controls) {
        PropertyTaxExemptionsRunResponse response = new PropertyTaxExemptionsRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(controls.businessDate());
        response.setBusinessTime(controls.businessTime());
        response.setHomeownerProcessingVariant(controls.variant().name());
        return response;
    }

    private static PropertyTaxExemptionsRunResponse completedSnapshot(
            long id,
            Controls controls,
            ProcessResult result) {
        PropertyTaxExemptionsRunResponse response = baseSnapshot(id, "COMPLETED", controls);
        response.setReturnCode(result.returnCode());
        response.setRecordsRead((long) result.recordsRead());
        response.setRecordsWritten((long) result.recordsWritten());
        response.setRecordsUpdated((long) result.recordsUpdated());
        response.setRecordsRejected((long) result.recordsRejected());
        response.setOutputs(mapOutputs(result.outputs()));
        response.setMessages(mapMessages(result.messages()));
        response.setRejections(mapRejections(result.rejections()));
        response.setReconciliations(mapReconciliations(result.reconciliations()));
        response.setRuleOutcomes(mapRuleOutcomes(result.ruleOutcomes()));
        return response;
    }

    private static PropertyTaxExemptionsRunResponse failedSnapshot(
            long id,
            Controls controls,
            RuntimeException exception) {
        PropertyTaxExemptionsRunResponse response = baseSnapshot(id, "FAILED", controls);
        response.setReturnCode(16);
        response.setRecordsRead(0L);
        response.setRecordsWritten(0L);
        response.setRecordsUpdated(0L);
        response.setRecordsRejected(0L);

        PropertyTaxExemptionsOutput output = new PropertyTaxExemptionsOutput();
        output.setName("ASREA859 HOMEOUT");
        output.setKind("DATASET");
        output.setRecordCount(0L);
        output.setPublicationStatus("WITHHELD");
        output.setRuleIds(List.of("asrea859-001"));
        response.setOutputs(List.of(output));

        PropertyTaxExemptionsMessage message = new PropertyTaxExemptionsMessage();
        message.setSeverity("ERROR");
        message.setMessage(exception.getMessage() == null
                ? "The property-tax-exemptions worker failed."
                : exception.getMessage());
        response.setMessages(List.of(message));

        PropertyTaxExemptionsReconciliationOutcome reconciliation =
                new PropertyTaxExemptionsReconciliationOutcome();
        reconciliation.setName("PROPERTY TAX EXEMPTIONS RUN");
        reconciliation.setStatus("FAILED");
        reconciliation.setRecordsRead(0L);
        reconciliation.setRecordsMatched(0L);
        reconciliation.setRecordsWritten(0L);
        reconciliation.setRecordsRejected(0L);
        reconciliation.setRuleIds(List.of("asrea859-001"));
        reconciliation.setMessage("The logical output was withheld after an operational failure.");
        response.setReconciliations(List.of(reconciliation));
        response.setRejections(List.of());
        response.setRuleOutcomes(List.of());
        return response;
    }

    private static List<PropertyTaxExemptionsOutput> mapOutputs(List<OutputRecord> source) {
        List<PropertyTaxExemptionsOutput> result = new ArrayList<>(source.size());
        for (OutputRecord record : source) {
            PropertyTaxExemptionsOutput output = new PropertyTaxExemptionsOutput();
            output.setName(record.name());
            output.setKind(record.kind());
            output.setRecordCount(record.recordCount());
            output.setPublicationStatus(record.publicationStatus());
            output.setRuleIds(record.ruleIds());
            result.add(output);
        }
        return List.copyOf(result);
    }

    private static List<PropertyTaxExemptionsMessage> mapMessages(List<Message> source) {
        List<PropertyTaxExemptionsMessage> result = new ArrayList<>(source.size());
        for (Message record : source) {
            PropertyTaxExemptionsMessage message = new PropertyTaxExemptionsMessage();
            message.setSeverity(record.severity());
            message.setMessage(record.message());
            message.setRuleId(record.ruleId());
            result.add(message);
        }
        return List.copyOf(result);
    }

    private static List<PropertyTaxExemptionsRejectionOutcome> mapRejections(
            List<Rejection> source) {
        List<PropertyTaxExemptionsRejectionOutcome> result = new ArrayList<>(source.size());
        for (Rejection record : source) {
            PropertyTaxExemptionsRejectionOutcome rejection =
                    new PropertyTaxExemptionsRejectionOutcome();
            rejection.setSource(record.source());
            rejection.setRecordKey(record.recordKey());
            rejection.setOutcome(record.outcome());
            rejection.setMessage(record.message());
            rejection.setRuleId(record.ruleId());
            result.add(rejection);
        }
        return List.copyOf(result);
    }

    private static List<PropertyTaxExemptionsReconciliationOutcome> mapReconciliations(
            List<Reconciliation> source) {
        List<PropertyTaxExemptionsReconciliationOutcome> result =
                new ArrayList<>(source.size());
        for (Reconciliation record : source) {
            PropertyTaxExemptionsReconciliationOutcome reconciliation =
                    new PropertyTaxExemptionsReconciliationOutcome();
            reconciliation.setName(record.name());
            reconciliation.setStatus(record.status());
            reconciliation.setRecordsRead(record.recordsRead());
            reconciliation.setRecordsMatched(record.recordsMatched());
            reconciliation.setRecordsWritten(record.recordsWritten());
            reconciliation.setRecordsRejected(record.recordsRejected());
            reconciliation.setRuleIds(record.ruleIds());
            reconciliation.setMessage(record.message());
            result.add(reconciliation);
        }
        return List.copyOf(result);
    }

    private static List<PropertyTaxExemptionsRuleOutcome> mapRuleOutcomes(
            List<RuleDisposition> source) {
        List<PropertyTaxExemptionsRuleOutcome> result = new ArrayList<>(source.size());
        for (RuleDisposition record : source) {
            PropertyTaxExemptionsRuleOutcome outcome = new PropertyTaxExemptionsRuleOutcome();
            outcome.setRuleId(record.ruleId());
            outcome.setOutcome(record.outcome());
            outcome.setRecordsAffected(record.recordsAffected());
            outcome.setMessage(record.message());
            result.add(outcome);
        }
        return List.copyOf(result);
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
