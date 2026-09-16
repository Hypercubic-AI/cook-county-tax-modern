package org.cookcounty.tax.application.batch;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/// Immutable aggregate returned after the ordered increment steps stop or complete.
///
/// Counts include partial products retained by an evidence-backed failure path. The comparator
/// outcome is absent until the service projects the final observable.
///
/// @param completed whether all nine steps completed
/// @param returnCode zero for completion or the terminal batch failure code
/// @param recordsRead aggregate source and transient records read
/// @param recordsWritten aggregate transient and persisted records written
/// @param recordsUpdated shared-live records replaced by committed steps
/// @param recordsRejected records omitted or rejected by the reviewed rules
/// @param outputs logical outputs available at the stopping point
/// @param messages ordered caller-visible observations
/// @param frozenValuationReportFacts complete whole-unit snapshots used by comparator projection
/// @param outcome final comparator observable when projection is available
public record EifdTifIncrementProcessResult(
        boolean completed,
        int returnCode,
        int recordsRead,
        int recordsWritten,
        int recordsUpdated,
        int recordsRejected,
        List<Output> outputs,
        List<Message> messages,
        List<FrozenValuationReportFact> frozenValuationReportFacts,
        @Nullable Outcome outcome) {

    /// Copies outputs, messages, and frozen-valuation snapshots at the terminal boundary.
    ///
    /// The snapshots use whole valuation units and retain source order. Construction performs no
    /// persistence. Earlier committed step effects remain visible when a later step fails.
    public EifdTifIncrementProcessResult {
        outputs = List.copyOf(outputs);
        messages = List.copyOf(messages);
        frozenValuationReportFacts = List.copyOf(frozenValuationReportFacts);
    }

    /// Creates an unprojected result without frozen-valuation comparator facts.
    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages) {
        this(
                completed,
                returnCode,
                recordsRead,
                recordsWritten,
                recordsUpdated,
                recordsRejected,
                outputs,
                messages,
                List.of(),
                null);
    }

    /// Creates a projected result without frozen-valuation comparator facts.
    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages,
            Outcome outcome) {
        this(
                completed,
                returnCode,
                recordsRead,
                recordsWritten,
                recordsUpdated,
                recordsRejected,
                outputs,
                messages,
                List.of(),
                outcome);
    }

    /// Creates an unprojected result with complete frozen-valuation comparator snapshots.
    ///
    /// @param frozenValuationReportFacts source-ordered snapshots in whole valuation units
    public EifdTifIncrementProcessResult(
            boolean completed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            List<Output> outputs,
            List<Message> messages,
            List<FrozenValuationReportFact> frozenValuationReportFacts) {
        this(
                completed,
                returnCode,
                recordsRead,
                recordsWritten,
                recordsUpdated,
                recordsRejected,
                outputs,
                messages,
                frozenValuationReportFacts,
                null);
    }

    /// Logical output metadata. Generation is absent for outputs without generation ownership.
    public record Output(String name, int recordCount, @Nullable Integer generation) {}

    /// Ordered process observation. Rule identity is absent for general status information.
    public record Message(String severity, String text, @Nullable String ruleId) {}

    /// Complete scale-zero frozen-valuation snapshot used by reconciliation projection.
    ///
    /// Values follow source field order. Monetary fields use exact `BigDecimal` values. Parcel
    /// counts remain integral. This immutable fact does not write or update frozen valuations.
    public record FrozenValuationReportFact(
            String divisionNumber,
            BigDecimal priorLandValue,
            BigDecimal priorImprovementValue,
            BigDecimal priorTotalValue,
            long priorParcelCount,
            BigDecimal currentLandValue,
            BigDecimal currentImprovementValue,
            BigDecimal currentTotalValue,
            long currentParcelCount,
            BigDecimal proposedImprovementValue,
            BigDecimal proposedExpired288Value,
            BigDecimal proposedCurrent288Value,
            BigDecimal proposedTotalValue,
            BigDecimal proposedActualValue,
            BigDecimal changeActionPriorLandValue,
            BigDecimal changeActionPriorImprovementValue,
            BigDecimal changeActionPriorTotalValue,
            long changeActionPriorParcelCount,
            BigDecimal changeActionCurrentLandValue,
            BigDecimal changeActionCurrentImprovementValue,
            BigDecimal changeActionCurrentTotalValue,
            long changeActionCurrentParcelCount,
            BigDecimal noChangeActionPriorLandValue,
            BigDecimal noChangeActionPriorImprovementValue,
            BigDecimal noChangeActionPriorTotalValue,
            long noChangeActionPriorParcelCount,
            BigDecimal noChangeActionCurrentLandValue,
            BigDecimal noChangeActionCurrentImprovementValue,
            BigDecimal noChangeActionCurrentTotalValue,
            long noChangeActionCurrentParcelCount) {
        /// Requires all values and normalizes exact monetary components to scale zero.
        public FrozenValuationReportFact {
            Objects.requireNonNull(divisionNumber, "divisionNumber");
            priorLandValue = wholeUnits(priorLandValue, "priorLandValue");
            priorImprovementValue = wholeUnits(priorImprovementValue, "priorImprovementValue");
            priorTotalValue = wholeUnits(priorTotalValue, "priorTotalValue");
            currentLandValue = wholeUnits(currentLandValue, "currentLandValue");
            currentImprovementValue =
                    wholeUnits(currentImprovementValue, "currentImprovementValue");
            currentTotalValue = wholeUnits(currentTotalValue, "currentTotalValue");
            proposedImprovementValue =
                    wholeUnits(proposedImprovementValue, "proposedImprovementValue");
            proposedExpired288Value =
                    wholeUnits(proposedExpired288Value, "proposedExpired288Value");
            proposedCurrent288Value =
                    wholeUnits(proposedCurrent288Value, "proposedCurrent288Value");
            proposedTotalValue = wholeUnits(proposedTotalValue, "proposedTotalValue");
            proposedActualValue = wholeUnits(proposedActualValue, "proposedActualValue");
            changeActionPriorLandValue =
                    wholeUnits(changeActionPriorLandValue, "changeActionPriorLandValue");
            changeActionPriorImprovementValue =
                    wholeUnits(
                            changeActionPriorImprovementValue, "changeActionPriorImprovementValue");
            changeActionPriorTotalValue =
                    wholeUnits(changeActionPriorTotalValue, "changeActionPriorTotalValue");
            changeActionCurrentLandValue =
                    wholeUnits(changeActionCurrentLandValue, "changeActionCurrentLandValue");
            changeActionCurrentImprovementValue =
                    wholeUnits(
                            changeActionCurrentImprovementValue,
                            "changeActionCurrentImprovementValue");
            changeActionCurrentTotalValue =
                    wholeUnits(changeActionCurrentTotalValue, "changeActionCurrentTotalValue");
            noChangeActionPriorLandValue =
                    wholeUnits(noChangeActionPriorLandValue, "noChangeActionPriorLandValue");
            noChangeActionPriorImprovementValue =
                    wholeUnits(
                            noChangeActionPriorImprovementValue,
                            "noChangeActionPriorImprovementValue");
            noChangeActionPriorTotalValue =
                    wholeUnits(noChangeActionPriorTotalValue, "noChangeActionPriorTotalValue");
            noChangeActionCurrentLandValue =
                    wholeUnits(noChangeActionCurrentLandValue, "noChangeActionCurrentLandValue");
            noChangeActionCurrentImprovementValue =
                    wholeUnits(
                            noChangeActionCurrentImprovementValue,
                            "noChangeActionCurrentImprovementValue");
            noChangeActionCurrentTotalValue =
                    wholeUnits(noChangeActionCurrentTotalValue, "noChangeActionCurrentTotalValue");
        }

        /// Normalizes one required exact whole-unit value without rounding.
        private static BigDecimal wholeUnits(BigDecimal value, String name) {
            return Objects.requireNonNull(value, name).setScale(0, RoundingMode.UNNECESSARY);
        }
    }
}
