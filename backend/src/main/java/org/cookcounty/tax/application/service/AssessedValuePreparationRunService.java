package org.cookcounty.tax.application.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationMessage;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationOutput;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunResponse;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationStageResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AssessedValuePreparationRunService implements AssessedValuePreparationRunUseCase {

    private final AssessedValuePreparationProcessor processor;
    private final AssessedValuePreparationFactorOutcomeProjector outcomeProjector;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final BatchRunCoordinator<AssessedValuePreparationRunResponse> runs;

    public AssessedValuePreparationRunService(
            AssessedValuePreparationProcessor processor,
            AssessedValuePreparationFactorOutcomeProjector outcomeProjector,
            FactorBatchOutcomeRecorder outcomeRecorder,
            @Qualifier("batchRunExecutor") Executor executor) {
        this.processor = processor;
        this.outcomeProjector = outcomeProjector;
        this.outcomeRecorder = outcomeRecorder;
        this.runs = new BatchRunCoordinator<>(executor);
    }

    @Override
    public Optional<AssessedValuePreparationRunResponse> getAssessedValuePreparationRun(Long id) {
        return id == null ? Optional.empty() : runs.find(id);
    }

    @Override
    public AssessedValuePreparationRunResponse startAssessedValuePreparationRun(
            AssessedValuePreparationRunRequest request) {
        Controls controls = validatedControls(request);
        return runs.start(
                        controls.idempotencyKey(),
                        controls.fingerprint(),
                        id -> snapshot(id, "QUEUED", controls, null),
                        id -> execute(id, controls))
                .response();
    }

    private void execute(long id, Controls controls) {
        runs.replace(id, snapshot(id, "RUNNING", controls, null));
        ProcessResult result;
        try {
            result = processor.process(
                    controls.businessDate(), controls.businessTime(), controls.processYear());
        } catch (RuntimeException exception) {
            runs.replace(id, operationalFailure(id, controls, exception));
            outcomeRecorder.completed(
                    "valuation-preparation", id, "FAILED", outcomeProjector.workerFailure(exception));
            return;
        }
        String status = result.failed() ? "FAILED" : "COMPLETED";
        runs.replace(id, snapshot(id, status, controls, result));
        outcomeRecorder.completed(
                "valuation-preparation", id, status, outcomeProjector.project(result));
    }

    private static Controls validatedControls(AssessedValuePreparationRunRequest request) {
        if (request == null) {
            throw new BatchRunInvalidRequestException("request is required");
        }
        if (request.getBusinessDate() == null) {
            throw new BatchRunInvalidRequestException("businessDate is required");
        }
        String businessTime = request.getBusinessTime();
        if (businessTime == null
                || !businessTime.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]")) {
            throw new BatchRunInvalidRequestException("businessTime must use HH:mm:ss");
        }
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BatchRunInvalidRequestException("idempotencyKey is required");
        }
        if (idempotencyKey.length() > 128) {
            throw new BatchRunInvalidRequestException("idempotencyKey must not exceed 128 characters");
        }
        String processYear = request.getProcessYear();
        if (processYear == null || !processYear.matches("[0-9]{2}")) {
            throw new BatchRunInvalidRequestException(
                    "processYear must contain exactly two decimal digits.");
        }
        return new Controls(request.getBusinessDate(), businessTime, idempotencyKey, processYear);
    }


    private static AssessedValuePreparationRunResponse snapshot(
            long id,
            String status,
            Controls controls,
            ProcessResult result) {
        AssessedValuePreparationRunResponse response = baseSnapshot(id, status, controls);
        if (result == null) {
            return response;
        }
        response.setReturnCode(result.returnCode());
        response.setRecordsRead(result.recordsRead());
        response.setRecordsWritten(result.recordsWritten());
        response.setRecordsUpdated(result.recordsUpdated());
        response.setRecordsRejected(result.recordsRejected());
        response.setPartialOutput(result.partialOutput());
        response.setOutputs(mapOutputs(id, result.outputs(), result.partialOutput()));
        response.setMessages(mapMessages(result.messages()));
        response.setStages(mapStages(result.stages()));
        return response;
    }

    private static AssessedValuePreparationRunResponse baseSnapshot(
            long id, String status, Controls controls) {
        AssessedValuePreparationRunResponse response = new AssessedValuePreparationRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(controls.businessDate());
        response.setBusinessTime(controls.businessTime());
        response.setProcessYear(controls.processYear());
        response.setOutputs(List.of());
        response.setMessages(List.of());
        return response;
    }

    private static List<AssessedValuePreparationOutput> mapOutputs(
            long id, List<OutputRecord> source, boolean partial) {
        List<AssessedValuePreparationOutput> outputs = new ArrayList<>(source.size());
        for (int index = 0; index < source.size(); index++) {
            OutputRecord record = source.get(index);
            AssessedValuePreparationOutput output = new AssessedValuePreparationOutput();
            output.setArtifactId("avpout_" + id + "_" + String.format("%02d", index + 1));
            output.setKind(record.kind());
            output.setMediaType(record.mediaType());
            output.setRecordCount(record.recordCount());
            output.setPartial(partial);
            outputs.add(output);
        }
        return List.copyOf(outputs);
    }

    private static List<AssessedValuePreparationMessage> mapMessages(
            List<AssessedValueRuleMessage> source) {
        List<AssessedValuePreparationMessage> messages = new ArrayList<>(source.size());
        for (AssessedValueRuleMessage record : source) {
            AssessedValuePreparationMessage message = new AssessedValuePreparationMessage();
            message.setSeverity(record.severity());
            message.setCode(record.code());
            message.setRuleId(record.ruleId());
            message.setMessage(record.message());
            message.setRecordKey(record.recordKey());
            messages.add(message);
        }
        return List.copyOf(messages);
    }

    private static List<AssessedValuePreparationStageResult> mapStages(List<StageOutcome> source) {
        List<AssessedValuePreparationStageResult> stages = new ArrayList<>(source.size());
        for (StageOutcome outcome : source) {
            AssessedValuePreparationStageResult stage = new AssessedValuePreparationStageResult();
            stage.setStage(outcome.stage());
            stage.setStatus(outcome.status());
            stage.setReturnCode(outcome.returnCode());
            stage.setRecordsRead(outcome.recordsRead());
            stage.setRecordsWritten(outcome.recordsWritten());
            stage.setRecordsUpdated(outcome.recordsUpdated());
            stage.setRecordsRejected(outcome.recordsRejected());
            stage.setOutputPublished(outcome.outputPublished());
            stage.setPartialOutput(outcome.partialOutput());
            stages.add(stage);
        }
        return List.copyOf(stages);
    }

    private static AssessedValuePreparationRunResponse operationalFailure(
            long id, Controls controls, RuntimeException exception) {
        AssessedValuePreparationRunResponse response = baseSnapshot(id, "FAILED", controls);
        response.setReturnCode(16);
        response.setRecordsRead(0);
        response.setRecordsWritten(0);
        response.setRecordsUpdated(0);
        response.setRecordsRejected(0);
        response.setPartialOutput(false);
        AssessedValuePreparationMessage message = new AssessedValuePreparationMessage();
        message.setSeverity("ERROR");
        message.setCode("WORKER_FAILED");
        message.setMessage(exception.getMessage() == null
                ? "The assessed-value preparation worker failed."
                : exception.getMessage());
        response.setMessages(List.of(message));
        response.setStages(List.of());
        return response;
    }

    private record Controls(
            LocalDate businessDate,
            String businessTime,
            String idempotencyKey,
            String processYear) {

        String fingerprint() {
            return businessDate + "\u001f" + businessTime + "\u001f" + processYear;
        }
    }
}
