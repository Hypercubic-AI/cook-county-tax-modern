package org.cookcounty.tax.application.batch;

import java.util.List;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;

public record EifdTifIncrementProcessResult(
        boolean completed,
        int returnCode,
        int recordsRead,
        int recordsWritten,
        int recordsUpdated,
        int recordsRejected,
        List<Output> outputs,
        List<Message> messages,
        List<Asrea740ReportFact> asrea740ReportFacts,
        FactorBatchOutcomeRecorder.Outcome outcome) {

    public EifdTifIncrementProcessResult {
        outputs = List.copyOf(outputs);
        messages = List.copyOf(messages);
        asrea740ReportFacts = List.copyOf(asrea740ReportFacts);
    }

    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages) {
        this(completed, returnCode, recordsRead, recordsWritten, recordsUpdated,
                recordsRejected, outputs, messages, List.of(), null);
    }

    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages,
            FactorBatchOutcomeRecorder.Outcome outcome) {
        this(completed, returnCode, recordsRead, recordsWritten, recordsUpdated,
                recordsRejected, outputs, messages, List.of(), outcome);
    }

    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages,
            List<Asrea740ReportFact> asrea740ReportFacts) {
        this(completed, returnCode, recordsRead, recordsWritten, recordsUpdated,
                recordsRejected, outputs, messages, asrea740ReportFacts, null);
    }

    public record Output(String name, int recordCount, Integer generation) {}

    public record Message(String severity, String text, String ruleId) {}

    public record Asrea740ReportFact(
            String divisionNumber,
            long priorLandValue,
            long priorImprovementValue,
            long priorTotalValue,
            long priorParcelCount,
            long currentLandValue,
            long currentImprovementValue,
            long currentTotalValue,
            long currentParcelCount,
            long proposedImprovementValue,
            long proposedExpired288Value,
            long proposedCurrent288Value,
            long proposedTotalValue,
            long proposedActualValue,
            long changeActionPriorLandValue,
            long changeActionPriorImprovementValue,
            long changeActionPriorTotalValue,
            long changeActionPriorParcelCount,
            long changeActionCurrentLandValue,
            long changeActionCurrentImprovementValue,
            long changeActionCurrentTotalValue,
            long changeActionCurrentParcelCount,
            long noChangeActionPriorLandValue,
            long noChangeActionPriorImprovementValue,
            long noChangeActionPriorTotalValue,
            long noChangeActionPriorParcelCount,
            long noChangeActionCurrentLandValue,
            long noChangeActionCurrentImprovementValue,
            long noChangeActionCurrentTotalValue,
            long noChangeActionCurrentParcelCount) {}
}
