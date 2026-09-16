package org.cookcounty.tax.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.port.in.EifdTifIncrementRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementMessage;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementOutput;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public final class EifdTifIncrementRunService implements EifdTifIncrementRunUseCase {

    private final EifdTifIncrementProcessor processor;
    private final EifdTifIncrementKernel kernel = new EifdTifIncrementKernel();
    private final BatchRunCoordinator<EifdTifIncrementRunResponse> coordinator;
    private final FactorBatchOutcomeRecorder outcomeRecorder;

    public EifdTifIncrementRunService(
            EifdTifIncrementProcessor processor,
            @Qualifier("batchRunExecutor") Executor executor,
            FactorBatchOutcomeRecorder outcomeRecorder) {
        this.processor = Objects.requireNonNull(processor, "processor");
        this.coordinator = new BatchRunCoordinator<>(executor);
        this.outcomeRecorder = Objects.requireNonNull(outcomeRecorder, "outcomeRecorder");
    }

    @Override
    public Optional<EifdTifIncrementRunResponse> getEifdTifIncrementRun(Long id) {
        return id == null ? Optional.empty() : coordinator.find(id);
    }

    @Override
    public BatchRunStartResult<EifdTifIncrementRunResponse> startEifdTifIncrementRun(
            EifdTifIncrementRunRequest request) {
        validateRequiredControls(request);
        try {
            kernel.validateControls(request.getReassessmentControl(), request.getProcessingYear(),
                    request.getAnnualEqualizationFactor());
        } catch (EifdTifIncrementKernel.RuleViolation violation) {
            throw new BatchRunInvalidRequestException(violation.getMessage());
        }
        EifdTifIncrementRunRequest pinned = copy(request);
        return coordinator.start(
                request.getIdempotencyKey(),
                fingerprint(request),
                id -> snapshot(id, "QUEUED", pinned, null),
                id -> execute(id, pinned));
    }

    private void execute(long id, EifdTifIncrementRunRequest request) {
        coordinator.replace(id, snapshot(id, "RUNNING", request, null));
        try {
            EifdTifIncrementProcessResult result = processor.process(request);
            String status = result.completed() ? "COMPLETED" : "FAILED";
            coordinator.replace(id, snapshot(id, status, request, result));
            outcomeRecorder.completed(
                    "eifd-tif-increment", id, status, result.outcome());
        } catch (RuntimeException exception) {
            EifdTifIncrementProcessResult failed = new EifdTifIncrementProcessResult(
                    false, 16, 0, 0, 0, 0, List.of(),
                    List.of(new EifdTifIncrementProcessResult.Message(
                            "ERROR", "EIFD/TIF increment batch failed.", null)));
            failed = new EifdTifIncrementProcessResult(
                    failed.completed(), failed.returnCode(), failed.recordsRead(),
                    failed.recordsWritten(), failed.recordsUpdated(), failed.recordsRejected(),
                    failed.outputs(), failed.messages(),
                    new EifdTifIncrementOutcomeProjector().failed(failed));
            coordinator.replace(id, snapshot(id, "FAILED", request, failed));
            outcomeRecorder.completed(
                    "eifd-tif-increment", id, "FAILED", failed.outcome());
        }
    }

    private EifdTifIncrementRunResponse snapshot(
            long id,
            String status,
            EifdTifIncrementRunRequest request,
            EifdTifIncrementProcessResult result) {
        EifdTifIncrementRunResponse response = new EifdTifIncrementRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(request.getBusinessDate());
        response.setBusinessTime(request.getBusinessTime());
        response.setOutputs(new ArrayList<>());
        response.setMessages(new ArrayList<>());
        if (result != null) {
            response.setReturnCode(result.returnCode());
            response.setRecordsRead(result.recordsRead());
            response.setRecordsWritten(result.recordsWritten());
            response.setRecordsUpdated(result.recordsUpdated());
            response.setRecordsRejected(result.recordsRejected());
            for (EifdTifIncrementProcessResult.Output source : result.outputs()) {
                EifdTifIncrementOutput output = new EifdTifIncrementOutput();
                output.setName(source.name());
                output.setRecordCount(source.recordCount());
                output.setGeneration(source.generation());
                response.getOutputs().add(output);
            }
            for (EifdTifIncrementProcessResult.Message source : result.messages()) {
                EifdTifIncrementMessage message = new EifdTifIncrementMessage();
                message.setSeverity(source.severity());
                message.setText(source.text());
                message.setRuleId(source.ruleId());
                response.getMessages().add(message);
            }
        }
        return response;
    }

    private void validateRequiredControls(EifdTifIncrementRunRequest request) {
        if (request == null) {
            throw new BatchRunInvalidRequestException("request body is required");
        }
        if (request.getBusinessDate() == null) {
            throw new BatchRunInvalidRequestException("businessDate is required");
        }
        if (blank(request.getBusinessTime())) {
            throw new BatchRunInvalidRequestException("businessTime is required");
        }
        if (!request.getBusinessTime().matches(
                "^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")) {
            throw new BatchRunInvalidRequestException(
                    "businessTime must use 24-hour HH:mm:ss format");
        }
        if (blank(request.getIdempotencyKey())) {
            throw new BatchRunInvalidRequestException("idempotencyKey is required");
        }
        if (blank(request.getReportingYear())
                || !request.getReportingYear().matches("^[0-9]{4}$")) {
            throw new BatchRunInvalidRequestException(
                    "reportingYear must contain exactly four digits");
        }
    }

    private String fingerprint(EifdTifIncrementRunRequest request) {
        return String.join("\u001f",
                request.getBusinessDate().toString(),
                request.getBusinessTime(),
                request.getReassessmentControl(),
                request.getProcessingYear(),
                request.getReportingYear(),
                request.getAnnualEqualizationFactor());
    }

    private EifdTifIncrementRunRequest copy(EifdTifIncrementRunRequest source) {
        EifdTifIncrementRunRequest target = new EifdTifIncrementRunRequest();
        target.setBusinessDate(source.getBusinessDate());
        target.setBusinessTime(source.getBusinessTime());
        target.setIdempotencyKey(source.getIdempotencyKey());
        target.setReassessmentControl(source.getReassessmentControl());
        target.setProcessingYear(source.getProcessingYear());
        target.setReportingYear(source.getReportingYear());
        target.setAnnualEqualizationFactor(source.getAnnualEqualizationFactor());
        return target;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
