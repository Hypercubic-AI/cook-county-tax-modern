
package org.cookcounty.tax.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.cookcounty.tax.application.batch.BatchRunStartResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.domain.port.in.TaxRateInputPreparationRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationAgencyAttachmentCounts;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationAgencyComparisonCounts;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationDivisionStampingCounts;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationFrozenAgencyPostingCounts;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationMessage;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationOutput;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationReconciliation;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TaxRateInputPreparationRunService implements TaxRateInputPreparationRunUseCase {

    private static final String TIME_PATTERN = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";
    private static final String FACTOR_SCENARIO_ID = "clerk-agency-attachment";
    private final TaxRateInputProcessor processor;
    private final FactorBatchOutcomeRecorder outcomeRecorder;
    private final TaxRateInputFactorOutcomeProjector outcomeProjector;
    private final BatchRunCoordinator<TaxRateInputPreparationRunResponse> coordinator;

    public TaxRateInputPreparationRunService(
            TaxRateInputProcessor processor,
            @Qualifier("batchRunExecutor") Executor executor,
            FactorBatchOutcomeRecorder outcomeRecorder,
            TaxRateInputFactorOutcomeProjector outcomeProjector) {
        this.processor = processor;
        this.coordinator = new BatchRunCoordinator<>(executor);
        this.outcomeRecorder = outcomeRecorder;
        this.outcomeProjector = outcomeProjector;
    }

    @Override
    public Optional<TaxRateInputPreparationRunResponse> getTaxRateInputPreparationRun(Long id) {
        return id == null ? Optional.empty() : coordinator.find(id);
    }

    @Override
    public BatchRunStartResult<TaxRateInputPreparationRunResponse> startTaxRateInputPreparationRun(
            TaxRateInputPreparationRunRequest request) {
        validate(request);
        TaxRateInputPreparationRunRequest controls = copyOf(request);
        String key = controls.getIdempotencyKey();
        String fingerprint = controls.getBusinessDate() + "|" + controls.getBusinessTime();
        return coordinator.start(
                key,
                fingerprint,
                id -> baseSnapshot(id, "QUEUED", controls),
                id -> execute(id, controls));
    }

    private void execute(long id, TaxRateInputPreparationRunRequest request) {
        coordinator.replace(id, baseSnapshot(id, "RUNNING", request));
        TaxRateInputPreparationRunResponse response;
        Outcome outcome;
        try {
            Result result = processor.process();
            response = terminalSnapshot(id, request, result);
            outcome = outcomeProjector.project(request, result);
        } catch (RuntimeException failure) {
            response = workerFailureSnapshot(id, request, failure);
            outcome = outcomeProjector.workerFailure(failure);
        }
        coordinator.replace(id, response);
        outcomeRecorder.completed(FACTOR_SCENARIO_ID, id, response.getStatus(), outcome);
    }

    private static TaxRateInputPreparationRunResponse terminalSnapshot(
            long id, TaxRateInputPreparationRunRequest request, Result result) {
        TaxRateInputPreparationRunResponse response = baseSnapshot(
                id, result.returnCode() == 0 ? "COMPLETED" : "FAILED", request);
        response.setReturnCode(result.returnCode());

        int recordsRead = result.divisionStamping().equalizedValueRecordsRead()
                + result.divisionStamping().divisionRecordsRead()
                + result.agencyAttachment().assessmentRecordsRead()
                + result.agencyComparison().priorRecordsRead()
                + result.agencyComparison().currentRecordsRead()
                + result.frozenAgencyPosting().priorRecordsRead()
                + result.frozenAgencyPosting().currentRecordsRead()
                + result.frozenAgencyPosting().comparisonRecordsRead();
        int recordsWritten = result.divisionStamping().outputRecordsWritten()
                + result.agencyAttachment().assessmentRecordsWritten()
                + result.agencyComparison().comparisonRecordsWritten()
                + result.frozenAgencyPosting().insertOperations();
        int recordsUpdated = result.divisionStamping().recordsStamped()
                + result.frozenAgencyPosting().rewriteOperations();
        int recordsRejected = result.agencyAttachment().assessmentRecordsUnmatched()
                + result.agencyComparison().priorOnlyDivisions()
                + result.agencyComparison().currentOnlyDivisions();
        response.setRecordsRead(recordsRead);
        response.setRecordsWritten(recordsWritten);
        response.setRecordsUpdated(recordsUpdated);
        response.setRecordsRejected(recordsRejected);
        response.setMessages(result.messages().stream().map(TaxRateInputPreparationRunService::message).toList());
        response.setOutputs(outputs(result));
        response.setReconciliation(reconciliation(result));
        return response;
    }

    private static TaxRateInputPreparationRunResponse workerFailureSnapshot(
            long id, TaxRateInputPreparationRunRequest request, RuntimeException exception) {
        TaxRateInputPreparationRunResponse response = baseSnapshot(id, "FAILED", request);
        response.setReturnCode(TaxRateInputKernel.ERROR_RETURN_CODE);
        TaxRateInputPreparationMessage message = new TaxRateInputPreparationMessage();
        message.setSeverity("ERROR");
        message.setCode("WORKER_FAILURE");
        message.setText(exception.getMessage() == null
                ? "Tax-rate input preparation worker failed."
                : exception.getMessage());
        response.setMessages(List.of(message));
        return response;
    }

    private static TaxRateInputPreparationRunResponse baseSnapshot(
            long id, String status, TaxRateInputPreparationRunRequest request) {
        TaxRateInputPreparationRunResponse response = new TaxRateInputPreparationRunResponse();
        response.setId(id);
        response.setStatus(status);
        response.setBusinessDate(request.getBusinessDate());
        response.setBusinessTime(request.getBusinessTime());
        response.setOutputs(List.of());
        response.setMessages(List.of());
        return response;
    }

    private static List<TaxRateInputPreparationOutput> outputs(Result result) {
        List<TaxRateInputPreparationOutput> outputs = new ArrayList<>();
        outputs.add(output("dividedValue", "DIVIDED_VALUE", result.dividedValues().size()));
        outputs.add(output("agencyAssessment", "AGENCY_ASSESSMENT", result.agencyAssessments().size()));
        outputs.add(output("annexDisconnect", "ANNEX_DISCONNECT", result.comparisons().size()));
        outputs.add(output("frozenAgency", "FROZEN_AGENCY", result.postings().size()));
        outputs.add(output(
                "attachmentReport",
                "REPORT",
                9 + result.agencyAttachment().assessmentRecordsUnmatched()));
        return List.copyOf(outputs);
    }

    private static TaxRateInputPreparationOutput output(String name, String kind, int count) {
        TaxRateInputPreparationOutput output = new TaxRateInputPreparationOutput();
        output.setName(name);
        output.setKind(kind);
        output.setRecordCount(count);
        output.setAvailable(true);
        return output;
    }

    private static TaxRateInputPreparationMessage message(TaxRateInputKernel.Message source) {
        TaxRateInputPreparationMessage message = new TaxRateInputPreparationMessage();
        message.setSeverity(source.severity());
        message.setCode(source.code());
        message.setText(source.text());
        message.setRuleId(source.ruleId());
        return message;
    }

    private static TaxRateInputPreparationReconciliation reconciliation(Result result) {
        TaxRateInputPreparationReconciliation reconciliation =
                new TaxRateInputPreparationReconciliation();

        TaxRateInputPreparationDivisionStampingCounts division =
                new TaxRateInputPreparationDivisionStampingCounts();
        division.setEqualizedValueRecordsRead(
                result.divisionStamping().equalizedValueRecordsRead());
        division.setDivisionRecordsRead(result.divisionStamping().divisionRecordsRead());
        division.setOutputRecordsWritten(result.divisionStamping().outputRecordsWritten());
        division.setRecordsStamped(result.divisionStamping().recordsStamped());
        division.setDivisionRecordsUnmatched(
                result.divisionStamping().divisionRecordsUnmatched());
        reconciliation.setDivisionStamping(division);

        TaxRateInputPreparationAgencyAttachmentCounts attachment =
                new TaxRateInputPreparationAgencyAttachmentCounts();
        attachment.setAssessmentRecordsRead(result.agencyAttachment().assessmentRecordsRead());
        attachment.setAssessmentRecordsWritten(
                result.agencyAttachment().assessmentRecordsWritten());
        attachment.setAssessmentRecordsUnmatched(
                result.agencyAttachment().assessmentRecordsUnmatched());
        attachment.setNormalCompletionBalanced(
                result.agencyAttachment().normalCompletionBalanced());
        reconciliation.setAgencyAttachment(attachment);

        TaxRateInputPreparationAgencyComparisonCounts comparison =
                new TaxRateInputPreparationAgencyComparisonCounts();
        comparison.setPriorRecordsRead(result.agencyComparison().priorRecordsRead());
        comparison.setCurrentRecordsRead(result.agencyComparison().currentRecordsRead());
        comparison.setComparisonRecordsWritten(
                result.agencyComparison().comparisonRecordsWritten());
        comparison.setDisconnectSegments(result.agencyComparison().disconnectSegments());
        comparison.setAnnexSegments(result.agencyComparison().annexSegments());
        comparison.setPriorOnlyDivisions(result.agencyComparison().priorOnlyDivisions());
        comparison.setCurrentOnlyDivisions(result.agencyComparison().currentOnlyDivisions());
        reconciliation.setAgencyComparison(comparison);

        TaxRateInputPreparationFrozenAgencyPostingCounts posting =
                new TaxRateInputPreparationFrozenAgencyPostingCounts();
        posting.setPriorRecordsRead(result.frozenAgencyPosting().priorRecordsRead());
        posting.setCurrentRecordsRead(result.frozenAgencyPosting().currentRecordsRead());
        posting.setComparisonRecordsRead(
                result.frozenAgencyPosting().comparisonRecordsRead());
        posting.setRewriteOperations(result.frozenAgencyPosting().rewriteOperations());
        posting.setInsertOperations(result.frozenAgencyPosting().insertOperations());
        reconciliation.setFrozenAgencyPosting(posting);
        return reconciliation;
    }

    private static TaxRateInputPreparationRunRequest copyOf(
            TaxRateInputPreparationRunRequest request) {
        TaxRateInputPreparationRunRequest copy = new TaxRateInputPreparationRunRequest();
        copy.setBusinessDate(request.getBusinessDate());
        copy.setBusinessTime(request.getBusinessTime());
        copy.setIdempotencyKey(request.getIdempotencyKey());
        return copy;
    }

    private static void validate(TaxRateInputPreparationRunRequest request) {
        if (request == null) {
            throw new BatchRunInvalidRequestException("request is required");
        }
        if (request.getBusinessDate() == null) {
            throw new BatchRunInvalidRequestException("businessDate is required");
        }
        if (request.getBusinessTime() == null || !request.getBusinessTime().matches(TIME_PATTERN)) {
            throw new BatchRunInvalidRequestException(
                    "businessTime must use 24-hour HH:mm:ss format");
        }
        if (request.getIdempotencyKey() == null
                || request.getIdempotencyKey().isBlank()) {
            throw new BatchRunInvalidRequestException("idempotencyKey is required");
        }
        if (request.getIdempotencyKey().length() > 128) {
            throw new BatchRunInvalidRequestException(
                    "idempotencyKey must contain at most 128 characters");
        }
    }
}
