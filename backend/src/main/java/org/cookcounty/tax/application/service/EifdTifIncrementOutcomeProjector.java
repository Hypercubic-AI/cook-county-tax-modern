package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AgencySummary;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AgencySummaryResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.AllocationResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.FrozenAgencyResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.PercentageResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.PostingResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.RollupResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TownReportResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementKernel.TownTotal;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult.FrozenValuationReportFact;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/// Projects the nine ordered step results into the reviewed batch observable.
///
/// Projection does not write business data. It preserves source order, fixed-width encodings,
/// partial outputs, and the exact catalog paths required by comparator evidence.
@Component
public final class EifdTifIncrementOutcomeProjector {

    private static final int PRINT_WIDTH = 133;
    private static final DateTimeFormatter CLERK_DATE = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final String[] FROZEN_GROUPED_FIELDS = {
        "PRIOR-VAL(1)",
        "PRIOR-VAL(2)",
        "PRIOR-VAL(3)",
        "PRIOR-VAL(4)",
        "CURRENT-VAL(1)",
        "CURRENT-VAL(2)",
        "CURRENT-VAL(3)",
        "CURRENT-VAL(4)",
        "PROPOSED-VAL(1)",
        "PROPOSED-VAL(2)",
        "PROPOSED-VAL(3)",
        "PROPOSED-VAL(4)",
        "PROPOSED-VAL(5)",
        "PARWCA-PRIOR-VAL(1)",
        "PARWCA-PRIOR-VAL(2)",
        "PARWCA-PRIOR-VAL(3)",
        "PARWCA-PRIOR-VAL(4)",
        "PARWCA-CURRENT-VAL(1)",
        "PARWCA-CURRENT-VAL(2)",
        "PARWCA-CURRENT-VAL(3)",
        "PARWCA-CURRENT-VAL(4)",
        "PARWOCA-PRIOR-VAL(1)",
        "PARWOCA-PRIOR-VAL(2)",
        "PARWOCA-PRIOR-VAL(3)",
        "PARWOCA-PRIOR-VAL(4)",
        "PARWOCA-CURRENT-VAL(1)",
        "PARWOCA-CURRENT-VAL(2)",
        "PARWOCA-CURRENT-VAL(3)",
        "PARWOCA-CURRENT-VAL(4)"
    };
    private static final String[] FROZEN_NAMED_FIELDS = {
        "PRIOR-LND-VAL",
        "PRIOR-IMP-VAL",
        "PRIOR-TOT-VAL",
        "PRIOR-PCL",
        "CURR-LND-VAL",
        "CURR-IMP-VAL",
        "CURR-TOT-VAL",
        "CURR-PCL",
        "PROP-IMP-VAL",
        "PROP-EXPR-288-VAL",
        "PROP-CURR-288-VAL",
        "PROP-TOT-VAL",
        "PROP-ACT-VAL",
        "CA-PR-LND-VAL",
        "CA-PR-IMP-VAL",
        "CA-PR-TOT-VAL",
        "CA-PR-PCL",
        "CA-CR-LND-VAL",
        "CA-CR-IMP-VAL",
        "CA-CR-TOT-VAL",
        "CA-CR-PCL",
        "WOCA-PR-LND-VAL",
        "WOCA-PR-IMP-VAL",
        "WOCA-PR-TOT-VAL",
        "WOCA-PR-PCL",
        "WOCA-CR-LND-VAL",
        "WOCA-CR-IMP-VAL",
        "WOCA-CR-TOT-VAL",
        "WOCA-CR-PCL"
    };
    private static final String[] FROZEN_PRIOR_DISPLAY_VALUES = {
        "2020202020202", "2020202020202", "202020202"
    };

    /// Projects one completed execution into the exact source-order comparator observable.
    ///
    /// Projection preserves fixed-width report bytes and whole-unit values. It performs no
    /// repository writes. The execution can include effects committed by both whole-step
    /// transactions.
    public Outcome project(Execution execution, EifdTifIncrementProcessResult result) {
        List<String> detail =
                frozenDetail(execution.request(), result.frozenValuationReportFacts());
        List<String> error =
                reconciliation(
                        execution.request(),
                        execution.parcels(),
                        execution.frozen(),
                        "ASREA740-2",
                        "               CREATE FROZEN VALUE FILE -  ERROR REPORT OF REASSESSMENT"
                                + " TOWNSHIP");
        List<String> file =
                reconciliation(
                        execution.request(),
                        execution.parcels(),
                        execution.frozen(),
                        "ASREA740-1",
                        "                  CREATE FROZEN VALUE FILE -  DETAIL OF REASSESSMENT"
                                + " TOWNSHIP");
        List<String> step2Print = step2Print(execution.parcels().size(), 0);
        List<String> step4Print = step4Print(execution.rollups(), execution.percentages());
        List<String> equalValues = equalValues(execution.summaries());
        List<String> agencyPrint =
                agencyPrint(execution.summaries(), execution.frozenAgencies().recordsRead());
        List<String> appended =
                appended(execution.request(), execution.towns(), execution.summaries());
        List<String> townPrint =
                townPrint(execution.request(), execution.towns(), execution.summaries());
        List<String> postingPrint = postingPrint(execution.posting());

        List<Cataloged> cataloged =
                List.of(
                        catalog("output/EIFDTRACE-S001-OUTPUTF.dat", List.of()),
                        catalog("output/EIFDTRACE-S001-PRINTDTL.dat", detail),
                        catalog("output/EIFDTRACE-S001-PRINTERR.dat", error),
                        catalog("output/EIFDTRACE-S001-PRINTFLE.dat", file),
                        catalog("output/EIFDTRACE-S002-PRINTFLE.dat", step2Print),
                        catalog("output/EIFDTRACE-S004-PRINTFLE.dat", step4Print),
                        catalog("output/EIFDTRACE-S007-EQVALFIL.dat", equalValues),
                        catalog("output/EIFDTRACE-S007-PRINTFLE.dat", agencyPrint),
                        catalog("output/EIFDTRACE-S008-APPENDED.dat", appended),
                        catalog("output/EIFDTRACE-S008-PRINTFLE.dat", townPrint),
                        catalog("output/EIFDTRACE-S009-PRINTFLE.dat", postingPrint));

        List<SuccessfulStep> stepEvidence = stepEvidence(execution);
        List<Step> steps =
                stepEvidence.stream()
                        .map(
                                step ->
                                        new Step(
                                                step.name(),
                                                step.program(),
                                                0,
                                                false,
                                                null,
                                                List.of(),
                                                List.of()))
                        .toList();
        List<String> displays =
                stepEvidence.stream().flatMap(step -> step.displays().stream()).toList();

        Map<String, Object> diffs = datasetDiffs(execution);
        return new Outcome(
                result.returnCode(),
                steps,
                Map.of(),
                displays,
                diffs,
                cataloged,
                null,
                false,
                false);
    }

    /// Projects a terminal failure without claiming output from an incomplete step.
    ///
    /// The result can still contain outputs and writes committed by earlier whole-step
    /// transactions. This failure projection performs no persistence.
    public Outcome failed(EifdTifIncrementProcessResult result) {
        List<String> messages =
                result.messages().stream()
                        .map(EifdTifIncrementProcessResult.Message::text)
                        .toList();
        Step failed =
                new Step(
                        "S001",
                        "ASREA740",
                        result.returnCode(),
                        false,
                        "CC %04d".formatted(result.returnCode()),
                        messages,
                        List.of());
        return new Outcome(
                result.returnCode(),
                List.of(failed),
                Map.of(),
                messages,
                Map.of(),
                List.of(),
                null,
                false,
                false);
    }

    /// Builds ordered displays for each completed step from its final counters.
    private List<SuccessfulStep> stepEvidence(Execution x) {
        int frozen = x.frozen().size();
        int selected = x.rollups().recordsRead();
        int taxTotals = x.rollups().taxCodeTotals().size();
        int divisionTotals = x.rollups().divisionTotals().size();
        int percentages = x.percentages().percentages().size();
        int allocations = x.allocations().allocations().size();
        int agencies = x.frozenAgencies().details().size();
        int summaries = x.summaries().summaries().size();
        int towns = x.towns().townTotals().size();
        int posted = x.posting().updated().size();
        return List.of(
                step(
                        "S001",
                        "ASREA740",
                        List.of(
                                "TOTAL MASTER OLD RECORDS READ  " + x.parcels().size(),
                                "TOTAL MASTER NEW RECORDS READ  " + x.parcels().size(),
                                "TOTAL MASTER OLD RECORDS WRITTEN TO OUTPUT REPORT 1  "
                                        + x.parcels().size(),
                                "TOTAL MASTER NEW RECORDS WRITTEN TO OUTPUT REPORT 1  "
                                        + x.parcels().size(),
                                "TOTAL MASTER OLD RECORDS WRITTEN TO OUTPUT REPORT 2  "
                                        + x.parcels().size(),
                                "TOTAL MASTER NEW RECORDS WRITTEN TO OUTPUT REPORT 2  "
                                        + x.parcels().size(),
                                "TOTAL MASTER RECORDS WRITTEN TO OUTPUT REPORT 3  "
                                        + x.parcels().size(),
                                "TOTAL FROZEN VALUE RECORDS UPDATED  " + frozen,
                                "TOTAL FROZEN VALUE RECORDS WRITTEN  0",
                                "TOTAL OUTPUT FILE  RECORDS WRITTEN  0")),
                step(
                        "S002",
                        "ASREA742",
                        List.of(
                                "   TOTAL MASTER RECORDS READ                       "
                                        + field(x.parcels().size(), 2),
                                "   TOTAL MASTER RECORDS WRITTEN                    "
                                        + field(selected, 2),
                                "   TOTAL DETAIL SEGMENTS SELECTED                   0")),
                step(
                        "S003",
                        "ASREA743",
                        List.of(
                                "TOTAL DETAIL RECORDS READ       " + selected,
                                "TOTAL DIVISION RECORDS WRITTEN  " + divisionTotals,
                                "TOTAL TAX CODE RECORDS WRITTEN  " + taxTotals)),
                step(
                        "S004",
                        "ASREA744",
                        List.of(
                                "   TOTAL DIVISION RECORDS READ              " + divisionTotals,
                                "   TOTAL TAX CODE RECORDS READ              " + taxTotals,
                                "   TOTAL TAX CODE RECORDS WRITTEN           " + percentages,
                                "   TOTAL TAX CODE RECORDS UNMATCHED         "
                                        + x.percentages().unmatchedTaxCodeTotals().size())),
                step(
                        "S005",
                        "ASREA745",
                        List.of(
                                "TOTAL FROZEN RECORDS READ       " + percentages,
                                "TOTAL FROZEN RECORDS WRITTEN    " + allocations,
                                "TOTAL FROZEN RECORDS UNMATCHED  "
                                        + x.allocations().unmatchedCount())),
                step(
                        "S006",
                        "ASREA748",
                        List.of(
                                "",
                                "TOTAL RECORDS READ...:  " + x.frozenAgencies().recordsRead(),
                                "TOTAL RECORDS WRITTEN:  " + agencies)),
                step(
                        "S007",
                        "CLRTM749",
                        agencyDisplays(x.summaries(), x.frozenAgencies().recordsRead())),
                step("S008", "CLRTM750", townDisplays(x.towns(), agencies)),
                step(
                        "S009",
                        "CLRTM756",
                        List.of(
                                "PROGRAM CLRTM756",
                                "",
                                "TOTAL NEW PROPERTY RECORDS READ " + x.posting().recordsRead(),
                                "TOTAL SELECTED FUNDS AGENCY EXTENSION RECORDS READ "
                                        + x.posting().recordsRead(),
                                "TOTAL VSAM AGENCY EQ-VALUATION RECORDS UPDATED " + posted)));
    }

    /// Creates one immutable successful-step display snapshot.
    private SuccessfulStep step(String name, String program, List<String> displays) {
        return new SuccessfulStep(name, program, displays);
    }

    private record SuccessfulStep(String name, String program, List<String> displays) {
        private SuccessfulStep {
            displays = List.copyOf(displays);
        }
    }

    /// Builds comparator differences for both shared-live stores after committed updates.
    private Map<String, Object> datasetDiffs(Execution execution) {
        Map<String, AgencyEqualizedValuation> agenciesBefore = new LinkedHashMap<>();
        for (AgencyEqualizedValuation agency : execution.agenciesBefore()) {
            agenciesBefore.put(agency.agencyNumber(), agency);
        }
        Map<String, AgencyEqualizedValuation> agenciesAfter = new LinkedHashMap<>(agenciesBefore);
        agenciesAfter.putAll(execution.posting().updated());

        Map<String, FrozenValuationReportFact> frozenBefore = new LinkedHashMap<>();
        for (FrozenValuationReportFact frozen : execution.frozenBefore()) {
            frozenBefore.put(frozen.divisionNumber(), frozen);
        }
        Map<String, FrozenValuationReportFact> frozenAfter = new LinkedHashMap<>();
        for (FrozenValuation frozen : execution.frozen()) {
            FrozenValuationReportFact snapshot = frozenSnapshot(frozen);
            frozenAfter.put(snapshot.divisionNumber(), snapshot);
        }

        Map<String, Object> datasets = new LinkedHashMap<>();
        Map<String, Object> agencyDiff =
                datasetDiff(agenciesBefore, agenciesAfter, this::agencyFields, 5);
        if (!agencyDiff.isEmpty()) {
            datasets.put("AGYEQVAL", agencyDiff);
        }
        Map<String, Object> frozenDiff =
                datasetDiff(frozenBefore, frozenAfter, this::frozenFields, 8);
        if (!frozenDiff.isEmpty()) {
            datasets.put("FRZVALFL", frozenDiff);
        }
        return Collections.unmodifiableMap(datasets);
    }

    /// Returns changed before-and-after field maps while retaining deterministic key order.
    private <T> Map<String, Object> datasetDiff(
            Map<String, T> before,
            Map<String, T> after,
            java.util.function.Function<T, Map<String, String>> fields,
            int keyBytes) {
        Map<String, Map<String, String>> changed = new TreeMap<>();
        for (Map.Entry<String, T> entry : after.entrySet()) {
            T prior = before.get(entry.getKey());
            if (prior == null) {
                continue;
            }
            Map<String, String> priorFields = fields.apply(prior);
            Map<String, String> currentFields = fields.apply(entry.getValue());
            if (!currentFields.equals(priorFields)) {
                changed.put(packedDecimal(entry.getKey(), keyBytes, 0x0f), currentFields);
            }
        }
        if (changed.isEmpty()) {
            return Map.of();
        }
        return Map.of("changed", Collections.unmodifiableMap(changed));
    }

    /// Encodes every persistent agency valuation field for comparator matching.
    private Map<String, String> agencyFields(AgencyEqualizedValuation value) {
        Map<String, String> fields = new TreeMap<>();
        putAgencyFields(fields, value, '-');
        putAgencyFields(fields, value, '_');
        return Collections.unmodifiableMap(fields);
    }

    /// Writes one complete agency field vocabulary with the requested separator.
    private void putAgencyFields(
            Map<String, String> fields, AgencyEqualizedValuation value, char separator) {
        String prefix = "AEV" + separator;
        put(fields, prefix, separator, "AGCYNO", decimal(value.agencyNumber()));
        put(
                fields,
                prefix,
                separator,
                "ANX-PROP-EQV",
                decimal(numeric(value.annexedPropertyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "CC-AIRPOL",
                decimal(numeric(value.cookCountyAirPollutionValue())));
        put(
                fields,
                prefix,
                separator,
                "CC-RE",
                decimal(numeric(value.cookCountyRealEstateValue())));
        put(fields, prefix, separator, "CC-RR", decimal(numeric(value.cookCountyRailroadValue())));
        put(
                fields,
                prefix,
                separator,
                "CC-USETAX",
                decimal(numeric(value.cookCountyUseTaxValue())));
        put(fields, prefix, separator, "CON-AGCY1", agencyNumber(value.connectingAgency1()));
        put(fields, prefix, separator, "CON-AGCY2", agencyNumber(value.connectingAgency2()));
        put(fields, prefix, separator, "CON-AGCY3", agencyNumber(value.connectingAgency3()));
        put(fields, prefix, separator, "CON-AGCY4", agencyNumber(value.connectingAgency4()));
        put(
                fields,
                prefix,
                separator,
                "DIS-PROP-EQV",
                decimal(numeric(value.disconnectedPropertyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "DIS-TIF-DIF",
                decimal(numeric(value.disconnectedTifDifference())));
        put(
                fields,
                prefix,
                separator,
                "DKB-EQV",
                decimal(numeric(value.deKalbCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "DPG-EQV",
                decimal(numeric(value.duPageCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "GRN-EQV",
                decimal(numeric(value.grundyCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "KND-EQV",
                decimal(numeric(value.kendallCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "KNE-EQV",
                decimal(numeric(value.kaneCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "KNK-EQV",
                decimal(numeric(value.kankakeeCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "LAK-EQV",
                decimal(numeric(value.lakeCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "LIMT-TXRTE-OVRD",
                decimal(unscaled(value.limitingTaxRateOverride(), 6)));
        put(
                fields,
                prefix,
                separator,
                "LSL-EQV",
                decimal(numeric(value.laSalleCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "LVN-EQV",
                decimal(numeric(value.livingstonCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "MCH-EQV",
                decimal(numeric(value.mcHenryCountyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "NEW-PROP-EQV",
                decimal(numeric(value.newPropertyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "OVLP-ANX-PROP-EQV",
                decimal(numeric(value.overlapAnnexedPropertyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "OVLP-DIS-PROP-EQV",
                decimal(numeric(value.overlapDisconnectedPropertyEqualizedValue())));
        put(
                fields,
                prefix,
                separator,
                "OVLP-DIS-TIF-DIF",
                decimal(numeric(value.overlapDisconnectedTifDifference())));
        put(
                fields,
                prefix,
                separator,
                "OVLP-NEW-PROP-EQV",
                decimal(numeric(value.overlapNewPropertyEqualizedValue())));
        put(fields, prefix, separator, "PAR-AGY1", agencyNumber(value.parentAgency1()));
        put(fields, prefix, separator, "PAR-AGY2", agencyNumber(value.parentAgency2()));
        put(fields, prefix, separator, "PAR-AGY3", agencyNumber(value.parentAgency3()));
        put(fields, prefix, separator, "PAR-AGY4", agencyNumber(value.parentAgency4()));
        put(fields, prefix, separator, "PAR-AGY5", agencyNumber(value.parentAgency5()));
        put(fields, prefix, separator, "PCT-BURDEN", decimal(unscaled(value.burdenPercent(), 2)));
        put(fields, prefix, separator, "PREV-TXYR1", decimal(integer(value.previousTaxYear1())));
        put(
                fields,
                prefix,
                separator,
                "PREV-TXYR1-TX-XT",
                decimal(unscaled(value.previousTaxYear1Extension(), 2)));
        put(fields, prefix, separator, "PREV-TXYR2", decimal(integer(value.previousTaxYear2())));
        put(
                fields,
                prefix,
                separator,
                "PREV-TXYR2-TX-XT",
                decimal(unscaled(value.previousTaxYear2Extension(), 2)));
        put(fields, prefix, separator, "PREV-TXYR3", decimal(integer(value.previousTaxYear3())));
        put(
                fields,
                prefix,
                separator,
                "PREV-TXYR3-TX-XT",
                decimal(unscaled(value.previousTaxYear3Extension(), 2)));
        put(fields, prefix, separator, "TAX-YEAR", decimal(integer(value.taxYear())));
        put(
                fields,
                prefix,
                separator,
                "TAXCAP",
                Boolean.TRUE.equals(value.taxCapIndicator()) ? "Y" : "N");
        put(
                fields,
                prefix,
                separator,
                "WIL-EQV",
                decimal(numeric(value.willCountyEqualizedValue())));
    }

    /// Stores one encoded field under the separator form expected by comparator evidence.
    private void put(
            Map<String, String> fields,
            String prefix,
            char separator,
            String suffix,
            String encoded) {
        fields.put(prefix + (separator == '-' ? suffix : suffix.replace('-', '_')), encoded);
    }

    /// Formats the fixed-width agency identity without losing leading zeroes.
    private String agencyNumber(String agencyNumber) {
        return decimal(agencyNumber);
    }

    /// Encodes every signed, whole-unit frozen valuation field for comparator matching.
    private Map<String, String> frozenFields(FrozenValuationReportFact value) {
        Map<String, String> fields = new TreeMap<>();
        putFrozenFields(fields, value, '-');
        putFrozenFields(fields, value, '_');
        return Collections.unmodifiableMap(fields);
    }

    /// Writes one complete frozen valuation vocabulary with the requested source separator.
    private void putFrozenFields(
            Map<String, String> fields, FrozenValuationReportFact value, char separator) {
        String prefix = "FV" + separator;
        put(fields, prefix, separator, "DIVNO", decimal(value.divisionNumber()));

        Number[] values = {
            value.priorLandValue(), value.priorImprovementValue(),
            value.priorTotalValue(), value.priorParcelCount(),
            value.currentLandValue(), value.currentImprovementValue(),
            value.currentTotalValue(), value.currentParcelCount(),
            value.proposedImprovementValue(), value.proposedExpired288Value(),
            value.proposedCurrent288Value(), value.proposedTotalValue(),
            value.proposedActualValue(), value.changeActionPriorLandValue(),
            value.changeActionPriorImprovementValue(), value.changeActionPriorTotalValue(),
            value.changeActionPriorParcelCount(), value.changeActionCurrentLandValue(),
            value.changeActionCurrentImprovementValue(), value.changeActionCurrentTotalValue(),
            value.changeActionCurrentParcelCount(), value.noChangeActionPriorLandValue(),
            value.noChangeActionPriorImprovementValue(), value.noChangeActionPriorTotalValue(),
            value.noChangeActionPriorParcelCount(), value.noChangeActionCurrentLandValue(),
            value.noChangeActionCurrentImprovementValue(), value.noChangeActionCurrentTotalValue(),
            value.noChangeActionCurrentParcelCount()
        };
        for (int index = 0; index < values.length; index++) {
            put(
                    fields,
                    prefix,
                    separator,
                    "VAL(" + (index + 1) + ")",
                    decimal(numeric(values[index])));
            put(
                    fields,
                    prefix,
                    separator,
                    FROZEN_GROUPED_FIELDS[index],
                    index < FROZEN_PRIOR_DISPLAY_VALUES.length
                            ? FROZEN_PRIOR_DISPLAY_VALUES[index]
                            : "");
            put(fields, prefix, separator, FROZEN_NAMED_FIELDS[index], "");
        }
    }

    /// Captures every signed, whole-unit frozen valuation before report projection.
    ///
    /// This conversion preserves exact monetary values and source field order. It has no
    /// persistence effect.
    private FrozenValuationReportFact frozenSnapshot(FrozenValuation value) {
        return new FrozenValuationReportFact(
                value.divisionNumber(),
                amount(value.priorLandValue()),
                amount(value.priorImprovementValue()),
                amount(value.priorTotalValue()),
                count(value.priorParcelCount()),
                amount(value.currentLandValue()),
                amount(value.currentImprovementValue()),
                amount(value.currentTotalValue()),
                count(value.currentParcelCount()),
                amount(value.proposedImprovementValue()),
                amount(value.proposedExpired288Value()),
                amount(value.proposedCurrent288Value()),
                amount(value.proposedTotalValue()),
                amount(value.proposedActualValue()),
                amount(value.changeActionPriorLandValue()),
                amount(value.changeActionPriorImprovementValue()),
                amount(value.changeActionPriorTotalValue()),
                count(value.changeActionPriorParcelCount()),
                amount(value.changeActionCurrentLandValue()),
                amount(value.changeActionCurrentImprovementValue()),
                amount(value.changeActionCurrentTotalValue()),
                count(value.changeActionCurrentParcelCount()),
                amount(value.noChangeActionPriorLandValue()),
                amount(value.noChangeActionPriorImprovementValue()),
                amount(value.noChangeActionPriorTotalValue()),
                count(value.noChangeActionPriorParcelCount()),
                amount(value.noChangeActionCurrentLandValue()),
                amount(value.noChangeActionCurrentImprovementValue()),
                amount(value.noChangeActionCurrentTotalValue()),
                count(value.noChangeActionCurrentParcelCount()));
    }

    /// Encodes decimal digits in the fixed-width packed representation used by the observable.
    private String packedDecimal(@Nullable String decimal, int bytes, int signNibble) {
        String digits = decimal == null || decimal.isBlank() ? "0" : decimal;
        if (!digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException(
                    "packed decimal value must contain only digits: " + digits);
        }
        int digitCount = bytes * 2 - 1;
        if (digits.length() > digitCount) {
            throw new IllegalArgumentException(
                    "packed decimal value exceeds " + bytes + " bytes: " + digits);
        }
        digits = "0".repeat(digitCount - digits.length()) + digits;
        byte[] encoded = new byte[bytes];
        for (int index = 0; index < bytes - 1; index++) {
            encoded[index] =
                    (byte)
                            ((digits.charAt(index * 2) - '0') << 4
                                    | digits.charAt(index * 2 + 1) - '0');
        }
        encoded[bytes - 1] = (byte) ((digits.charAt(digitCount - 1) - '0') << 4 | signNibble);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /// Normalizes a decimal identifier without converting it to a quantity.
    private String decimal(long value) {
        return Long.toString(value);
    }

    /// Normalizes a decimal identifier without converting it to a quantity.
    private String decimal(@Nullable String value) {
        String digits = value == null || value.isBlank() ? "0" : value;
        if (!digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("decimal value must contain only digits: " + digits);
        }
        int firstSignificant = 0;
        while (firstSignificant < digits.length() - 1 && digits.charAt(firstSignificant) == '0') {
            firstSignificant++;
        }
        return digits.substring(firstSignificant);
    }

    /// Returns a decimal amount at its observable fixed scale.
    private long unscaled(@Nullable BigDecimal value, int scale) {
        return value == null
                ? 0L
                : value.setScale(scale, RoundingMode.UNNECESSARY).unscaledValue().longValueExact();
    }

    /// Converts an optional stored integer to the observable zero-filled value.
    private long integer(@Nullable Integer value) {
        return value == null ? 0L : value.longValue();
    }

    /// Converts a stored number to its exact integer representation at the encoding boundary.
    private long numeric(@Nullable Number value) {
        if (value == null) {
            return 0L;
        }
        return value instanceof BigDecimal decimal ? decimal.longValueExact() : value.longValue();
    }

    /// Returns an optional exact monetary value or scale-zero zero.
    private BigDecimal amount(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /// Returns an optional integral source count or zero.
    private long count(@Nullable Long value) {
        return value == null ? 0L : value;
    }

    /// Builds source-ordered agency summary counters and status displays.
    private List<String> agencyDisplays(AgencySummaryResult result, int read) {
        BigDecimal newProperty =
                result.summaries().stream()
                        .map(AgencySummary::newPropertyEqualized)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(
                "",
                "     TOTAL FROZEN RECORDS READ..................:      " + read,
                "     TOTAL AGENCIES PRINTED.....................:      "
                        + result.summaries().size(),
                "     TOTAL NEW PROPERTY EQUALIZED VALUE.........:            "
                        + number(newProperty),
                "     TOTAL ANNEXED PROPERTY EQUALIZED VALUE.....:                  0",
                "     TOTAL DISCONNECTED PROPERTY EQUALIZED VALUE:                  0",
                "     TOTAL RECOVERED TAX INCREMENT VALUE........:                  0",
                "     TOTAL EXPIRED INCENTIVES VALUE.............:                  0");
    }

    /// Builds source-ordered town-report counters and status displays.
    private List<String> townDisplays(TownReportResult result, int read) {
        BigDecimal displayedTotal =
                result.townTotals().stream()
                        .limit(Math.max(0, result.townTotals().size() - 1L))
                        .map(TownTotal::newPropertyEqualized)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(
                "",
                "     TOTAL FROZEN RECORDS READ..................:      " + read,
                "     TOTAL FROZEN RECORDS PRINTED...............:      "
                        + result.townTotals().size(),
                "     TOTAL NEW PROPERTY EQUALIZED VALUE.........:            "
                        + number(displayedTotal),
                "     TOTAL ANNEXEC PROPERTY EQUALIZED VALUE.....:                  0",
                "     TOTAL DISCONNECTED PROPERTY EQUALIZED VALUE:                  0",
                "     TOTAL RECOVERED TAX INCREMENT VALUE........:                  0",
                "     TOTAL EXPIRED INCENTIVES VALUE.............:                  0");
    }

    /// Renders frozen valuation detail pages in source order.
    ///
    /// All valuation and parcel-count inputs are signed whole units. Rendering preserves the
    /// 133-character records and performs no persistence.
    private List<String> frozenDetail(
            EifdTifIncrementRunRequest request, List<FrozenValuationReportFact> values) {
        List<String> lines = new ArrayList<>();
        lines.add(blank(PRINT_WIDTH));
        int page = 1;
        for (FrozenValuationReportFact value : values) {
            lines.add(
                    pad(
                            "  ASREA740-3                                 OFFICE OF THE COOK COUNTY"
                                    + " ASSESSOR                                    PAGE"
                                    + field(page++, 8),
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "  DATE "
                                    + assessorDate(request)
                                    + "            CREATE FROZEN VALUE FILE -  DIVISION NO. SUMMARY"
                                    + " - REASSESSMENT TOWNSHIP",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    pad(
                            "  DIVISION NUMBER       PRIOR LAND VALUE      PRIOR IMPV VALUE      "
                                + " PRIOR TOT VALUE       PRIOR PCL COUNT       CURR LAND COUNT",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            17,
                            value.divisionNumber(),
                            44,
                            value(value.priorLandValue()),
                            65,
                            value(value.priorImprovementValue()),
                            86,
                            value(value.priorTotalValue()),
                            107,
                            value(value.priorParcelCount()),
                            128,
                            value(value.currentLandValue())));
            lines.add(
                    pad(
                            "                         CURR IMPV VALUE      CURR TOTAL VALUE      "
                                + " CURR PCL COUNT       PROP ASSD 1ST IMP      AS 1ST EXPR 288",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            44,
                            value(value.currentImprovementValue()),
                            65,
                            value(value.currentTotalValue()),
                            86,
                            value(value.currentParcelCount()),
                            107,
                            value(value.proposedImprovementValue()),
                            128,
                            value(value.proposedExpired288Value())));
            lines.add(
                    pad(
                            "                         AS 1ST CURR 288      PROP ASSD 1ST TOT    "
                                + " ASSD 1ST ACT TOT      W/CHG ACT PR LAND     W/CHG ACT PR IMPV",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            44,
                            value(value.proposedCurrent288Value()),
                            65,
                            value(value.proposedTotalValue()),
                            86,
                            value(value.proposedActualValue()),
                            107,
                            value(value.changeActionPriorLandValue()),
                            128,
                            value(value.changeActionPriorImprovementValue())));
            lines.add(
                    pad(
                            "                        W/CHG ACT PR TOT      W/CHG ACT PR CNT     "
                                + " W/CHG ACT CR LAND     W/CHG ACT CR IMPV     W/CHG ACT CR TOT",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            44,
                            value(value.changeActionPriorTotalValue()),
                            65,
                            value(value.changeActionPriorParcelCount()),
                            86,
                            value(value.changeActionCurrentLandValue()),
                            107,
                            value(value.changeActionCurrentImprovementValue()),
                            128,
                            value(value.changeActionCurrentTotalValue())));
            lines.add(
                    pad(
                            "                        W/CHG ACT CR CNT      W/O ACT PR LAND      "
                                    + " W/O ACT PR IMPV       W/O ACT PR TOTAL      W/O ACT PR CNT",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            44,
                            value(value.changeActionCurrentParcelCount()),
                            65,
                            value(value.noChangeActionPriorLandValue()),
                            86,
                            value(value.noChangeActionPriorImprovementValue()),
                            107,
                            value(value.noChangeActionPriorTotalValue()),
                            128,
                            value(value.noChangeActionPriorParcelCount())));
            lines.add(
                    pad(
                            "                        W/O ACT CR LAND       W/O ACT CR IMPV      "
                                    + " W/O ACT CR TOT        W/O ACT CR CNT",
                            PRINT_WIDTH));
            lines.add(blank(PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            44,
                            value(value.noChangeActionCurrentLandValue()),
                            65,
                            value(value.noChangeActionCurrentImprovementValue()),
                            86,
                            value(value.noChangeActionCurrentTotalValue()),
                            107,
                            value(value.noChangeActionCurrentParcelCount())));
        }
        return List.copyOf(lines);
    }

    /// Renders the paired prior and current reconciliation report in source order.
    private List<String> reconciliation(
            EifdTifIncrementRunRequest request,
            List<AssessmentParcel> parcels,
            List<FrozenValuation> frozen,
            String program,
            String reportTitle) {
        List<String> lines = new ArrayList<>();
        lines.add(blank(PRINT_WIDTH));
        int count = Math.min(parcels.size(), frozen.size());
        for (int index = 0; index < count; index++) {
            AssessmentParcel parcel = parcels.get(index);
            FrozenValuation division = frozen.get(index);
            lines.add(pad("  PRIOR  RECORD  (MASTER OLD)", PRINT_WIDTH));
            lines.add(
                    pad(
                            "  DIVISION                             P1 - 2-5 OVER 1YR        P2 -"
                                    + " 288 W/PER       P3 - 1 YR OLD          P4 - 1 YR OLD",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "    NUMBER        PROPERTY NUMBER          288 N=CURRYR          "
                                    + " FR/TO YRS               TYPE 2-5              TP 5 ACT",
                            PRINT_WIDTH));
            String propertyNumber = String.format("%018d", Long.parseLong(parcel.parcelNumber()));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            15,
                            division.divisionNumber(),
                            34,
                            propertyNumber,
                            54,
                            "0",
                            77,
                            "0",
                            98,
                            "0",
                            121,
                            "0"));
            lines.add(
                    pad(
                            "                                       P5 - 1 YR OLD             P6 -"
                                    + " DIFF 1 YR      P7 - TOTAL LAND       P8 - TOTAL IMPV",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "                                            TP 5 FULL                "
                                    + " TYPE 5              VALUATION             VALUATION",
                            PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            54,
                            "0",
                            77,
                            "0",
                            98,
                            value(parcel.priorLandValue()),
                            121,
                            value(parcel.priorImprovementValue())));
            lines.add(
                    pad(
                            "                                       P9 - TOTAL                P10 -"
                                    + " TOTAL",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "                                            VALUATION             "
                                    + " PARCEL COUNT",
                            PRINT_WIDTH));
            lines.add(at(PRINT_WIDTH, 54, value(parcel.priorTotalValue()), 77, "1"));
            lines.add(
                    pad(
                            "                            ******  ACCUMULATED A/V NOT EQUAL TO TOTAL"
                                    + " A/V  ******",
                            PRINT_WIDTH));
            lines.add(pad("  CURRENT RECORD (MASTER NEW)", PRINT_WIDTH));
            lines.add(
                    pad(
                            "  DIVISION                             C1 - 2-5 N/FR-TO        C2 -"
                                    + " CUR YR 288       C3 - 1 YR OLD          C4 - TYPE 2-5",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "    NUMBER        PROPERTY NUMBER          288 CR YR-N PER           "
                                    + " W/PER          TYPE 2-5 ACT         FR-TO AGE - ACT",
                            PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            15,
                            division.divisionNumber(),
                            34,
                            propertyNumber,
                            54,
                            "0",
                            77,
                            "0",
                            98,
                            "0",
                            121,
                            "0"));
            lines.add(
                    pad(
                            "                                       C5 - DIFF 1 YR OLD        C6 -"
                                    + " TOTAL LAND     C7 - TOTAL IMPV           C8 - TOTAL",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "                                            C4 - P3                  "
                                    + " VALUATION            VALUATION               VALUATION",
                            PRINT_WIDTH));
            lines.add(
                    at(
                            PRINT_WIDTH,
                            54,
                            "0",
                            77,
                            value(parcel.currentLandValue()),
                            98,
                            value(parcel.currentImprovementValue()),
                            121,
                            value(parcel.currentTotalValue())));
            lines.add(pad("                                       C9 - PARCEL", PRINT_WIDTH));
            lines.add(pad("                                            COUNT", PRINT_WIDTH));
            lines.add(at(PRINT_WIDTH, 54, "1"));
            lines.add(
                    pad(
                            "                            ******  ACCUMULATED A/V NOT EQUAL TO TOTAL"
                                    + " A/V  ******",
                            PRINT_WIDTH));
            if (index + 1 < count) {
                lines.add(
                        pad(
                                "  "
                                        + program
                                        + "                                 OFFICE OF THE COOK"
                                        + " COUNTY ASSESSOR                                    PAGE"
                                        + field(index + 1, 8),
                                PRINT_WIDTH));
                lines.add(pad("  DATE " + assessorDate(request) + reportTitle, PRINT_WIDTH));
            }
        }
        return List.copyOf(lines);
    }

    /// Renders the selection-step page and its reviewed counts.
    private List<String> step2Print(int read, int details) {
        return List.of(
                blank(PRINT_WIDTH),
                pad(
                        "   TOTAL MASTER RECORDS READ                       " + field(read, 2),
                        PRINT_WIDTH),
                pad(
                        "   TOTAL MASTER RECORDS WRITTEN                    " + field(read, 2),
                        PRINT_WIDTH),
                pad(
                        "   TOTAL DETAIL SEGMENTS SELECTED                  " + field(details, 2),
                        PRINT_WIDTH));
    }

    /// Renders percentage allocation rows with seven fractional decimal places.
    private List<String> step4Print(RollupResult rollups, PercentageResult percentages) {
        return List.of(
                blank(PRINT_WIDTH),
                pad(
                        "   TOTAL DIVISION RECORDS READ              "
                                + rollups.divisionTotals().size(),
                        PRINT_WIDTH),
                pad(
                        "   TOTAL TAX CODE RECORDS READ              "
                                + rollups.taxCodeTotals().size(),
                        PRINT_WIDTH),
                pad(
                        "   TOTAL TAX CODE RECORDS WRITTEN           "
                                + percentages.percentages().size(),
                        PRINT_WIDTH),
                pad(
                        "   TOTAL TAX CODE RECORDS UNMATCHED         "
                                + percentages.unmatchedTaxCodeTotals().size(),
                        PRINT_WIDTH));
    }

    /// Encodes machine-readable agency summaries in agency order.
    private List<String> equalValues(AgencySummaryResult result) {
        return result.summaries().stream()
                .map(
                        summary ->
                                String.format("%09d", summary.agency())
                                        + pad(summary.description(), 44)
                                        + zoned(summary.newPropertyEqualized())
                                        + zoned(summary.annexedEqualized())
                                        + zoned(summary.disconnectedEqualized())
                                        + zoned(summary.recoveredTifEqualized())
                                        + zoned(summary.expiredIncentiveEqualized()))
                .toList();
    }

    /// Renders one agency summary group after its reference description resolves.
    private List<String> agencyPrint(AgencySummaryResult result, int read) {
        List<String> lines = new ArrayList<>();
        lines.add(blank(PRINT_WIDTH));
        BigDecimal totalNew = BigDecimal.ZERO;
        for (AgencySummary summary : result.summaries()) {
            lines.add(
                    at(
                            PRINT_WIDTH,
                            12,
                            String.format("%011d", summary.agency()),
                            57,
                            pad(summary.description(), 44),
                            75,
                            number(summary.newPropertyEqualized()),
                            90,
                            "0",
                            105,
                            "0",
                            120,
                            "0",
                            133,
                            "0"));
            totalNew = totalNew.add(summary.newPropertyEqualized());
        }
        lines.add(
                at(
                        PRINT_WIDTH,
                        22,
                        "COUNTY TOTAL",
                        70,
                        number(totalNew),
                        88,
                        "0",
                        103,
                        "0",
                        119,
                        "0",
                        133,
                        "0"));
        lines.add(pad("     TOTAL FROZEN RECORDS READ:      " + read, PRINT_WIDTH));
        lines.add(
                pad(
                        "     TOTAL AGENCIES PRINTED...:      " + result.summaries().size(),
                        PRINT_WIDTH));
        lines.add(
                pad(
                        "     TOTAL EQUAL VAL RECS WRIT:      " + result.summaries().size(),
                        PRINT_WIDTH));
        return List.copyOf(lines);
    }

    /// Renders agency-year append records for every completed town group.
    private List<String> appended(
            EifdTifIncrementRunRequest request,
            TownReportResult result,
            AgencySummaryResult summaries) {
        Map<Long, String> descriptions = descriptions(summaries);
        List<String> lines = new ArrayList<>();
        for (TownTotal town : result.townTotals()) {
            String prefix =
                    String.format("%09d", town.agency())
                            + Objects.requireNonNull(request.reportingYear());
            lines.add(
                    pad(
                            prefix
                                    + "  CLRTM750                              OFFICE OF THE COOK"
                                    + " COUNTY CLERK",
                            150));
            lines.add(pad(prefix, 150));
            lines.add(
                    pad(
                            prefix
                                    + "  "
                                    + Objects.requireNonNull(request.businessDate())
                                            .format(CLERK_DATE)
                                    + "                      PTELL - "
                                    + Objects.requireNonNull(request.reportingYear())
                                    + " NEW PROPERTY, ANNEXED PROPERTY, DISCONNECTED PROPERTY",
                            150));
            lines.add(
                    pad(
                            prefix
                                    + "                                         RECOVERED TAX"
                                    + " INCREMENT REPORT BY TOWN WITHIN AGENCY         EQUALIZATION"
                                    + " FACTOR: "
                                    + factor(request),
                            150));
            lines.add(pad(prefix, 150));
            lines.add(
                    pad(
                            prefix
                                    + "   AGENCY NUMBER: "
                                    + String.format("%011d", town.agency())
                                    + "    AGENCY DESCRIPTION: "
                                    + descriptions.get(town.agency()),
                            150));
            lines.add(pad(prefix, 150));
            lines.add(
                    pad(
                            prefix
                                    + "            TOWN         TOWN                     NEW"
                                    + " PROPERTY    ANNEXED PROPERTY  DISC PROPERTY  RECOVERED TAX "
                                    + "    EXPIRED",
                            150));
            lines.add(
                    pad(
                            prefix
                                    + "           NUMBER        NAME                     EQLZD"
                                    + " VALUE        EQLZD VALUE     EQLZD VALUE  INCREMENT VALUE  "
                                    + " INCENTIVES",
                            150));
            lines.add(pad(prefix + townValues(town), 150));
            lines.add(pad(prefix, 150));
            lines.add(pad(prefix, 150));
            lines.add(pad(prefix + totalValues(town), 150));
        }
        return List.copyOf(lines);
    }

    /// Renders town details and agency totals with final-group output.
    private List<String> townPrint(
            EifdTifIncrementRunRequest request,
            TownReportResult result,
            AgencySummaryResult summaries) {
        Map<Long, String> descriptions = descriptions(summaries);
        List<String> lines = new ArrayList<>();
        lines.add(blank(PRINT_WIDTH));
        int page = 1;
        for (TownTotal town : result.townTotals()) {
            lines.add(
                    pad(
                            "  CLRTM750                              OFFICE OF THE COOK COUNTY"
                                    + " CLERK                                                 PAGE"
                                    + field(page++, 6),
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "  "
                                    + Objects.requireNonNull(request.businessDate())
                                            .format(CLERK_DATE)
                                    + "                      PTELL - "
                                    + Objects.requireNonNull(request.reportingYear())
                                    + " NEW PROPERTY, ANNEXED PROPERTY, DISCONNECTED PROPERTY",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "                                         RECOVERED TAX INCREMENT"
                                    + " REPORT BY TOWN WITHIN AGENCY         EQUALIZATION FACTOR: "
                                    + factor(request),
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "   AGENCY NUMBER: "
                                    + String.format("%011d", town.agency())
                                    + "    AGENCY DESCRIPTION: "
                                    + descriptions.get(town.agency()),
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "            TOWN         TOWN                     NEW PROPERTY   "
                                    + " ANNEXED PROPERTY  DISC PROPERTY  RECOVERED TAX     EXPIRED",
                            PRINT_WIDTH));
            lines.add(
                    pad(
                            "           NUMBER        NAME                     EQLZD VALUE       "
                                    + " EQLZD VALUE     EQLZD VALUE  INCREMENT VALUE   INCENTIVES",
                            PRINT_WIDTH));
            lines.add(townValues(town));
            lines.add(totalValues(town));
        }
        lines.add(
                pad(
                        "     TOTAL FROZEN RECORDS READ...:      " + result.townTotals().size(),
                        PRINT_WIDTH));
        lines.add(
                pad(
                        "     TOTAL AGENCY RECORDS PRINTED:      " + result.townTotals().size(),
                        PRINT_WIDTH));
        return List.copyOf(lines);
    }

    /// Returns the five equalized-value categories for one town group.
    private String townValues(TownTotal town) {
        String townName =
                Objects.requireNonNull(
                        town.townName(), "Town reference is required for a town report row");
        return at(
                PRINT_WIDTH,
                15,
                field(town.town(), 2),
                23 + townName.length(),
                townName,
                61,
                number(town.newPropertyEqualized()),
                79,
                number(town.annexedEqualized()),
                94,
                number(town.disconnectedEqualized()),
                110,
                number(town.recoveredTifEqualized()),
                125,
                number(town.expiredIncentiveEqualized()));
    }

    /// Returns the five equalized-value categories for one agency total.
    private String totalValues(TownTotal town) {
        return at(
                PRINT_WIDTH,
                14,
                "AGENCY TOTAL",
                61,
                number(town.newPropertyEqualized()),
                79,
                number(town.annexedEqualized()),
                94,
                number(town.disconnectedEqualized()),
                110,
                number(town.recoveredTifEqualized()),
                125,
                number(town.expiredIncentiveEqualized()));
    }

    /// Renders the posting result, including unmatched agencies and update counts.
    private List<String> postingPrint(PostingResult posting) {
        return List.of(
                pad(
                        "     TOTAL NUMBER OF CLERK''S NEW PROPERTY RECORDS READ"
                                + field(posting.recordsRead(), 15),
                        PRINT_WIDTH),
                pad(
                        "     TOTAL NUMBER OF CLERK''S NEW PROPERTY RECORDS UNMATCHED"
                                + field(posting.updated().size(), 10),
                        PRINT_WIDTH),
                pad(
                        "     TOTAL NUMBER OF VSAM AGENCY EQ-VALUATION RECORDS UPDATED"
                                + field(posting.updated().size(), 9),
                        PRINT_WIDTH));
    }

    /// Indexes resolved agency descriptions for report headings.
    private Map<Long, String> descriptions(AgencySummaryResult summaries) {
        Map<Long, String> values = new LinkedHashMap<>();
        summaries
                .summaries()
                .forEach(summary -> values.put(summary.agency(), summary.description()));
        return values;
    }

    /// Encodes one logical output as a newline-terminated catalog payload.
    private Cataloged catalog(String dsn, List<String> records) {
        List<String> encoded =
                records.stream()
                        .map(
                                record ->
                                        Base64.getEncoder()
                                                .encodeToString(
                                                        record.getBytes(StandardCharsets.US_ASCII)))
                        .toList();
        return new Cataloged(dsn, 1, records.size(), records, encoded);
    }

    /// Formats the pinned business date in the assessor report layout.
    private String assessorDate(EifdTifIncrementRunRequest request) {
        return "%02d/%02d/%s"
                .formatted(
                        Objects.requireNonNull(request.businessDate()).getYear() % 100,
                        Objects.requireNonNull(request.businessDate()).getMonthValue(),
                        Objects.requireNonNull(request.reassessmentControl()).substring(6, 8));
    }

    /// Formats the implied-scale annual factor for fixed-width report output.
    private String factor(EifdTifIncrementRunRequest request) {
        return String.format(
                "%06d",
                Integer.parseInt(Objects.requireNonNull(request.annualEqualizationFactor())));
    }

    /// Formats a scale-zero monetary value with the source zoned-decimal sign representation.
    private String zoned(BigDecimal value) {
        long exact = value.longValueExact();
        long absolute = Math.abs(exact);
        char[] positive = "{ABCDEFGHI".toCharArray();
        char[] negative = "}JKLMNOPQR".toCharArray();
        char sign = (exact < 0 ? negative : positive)[(int) (absolute % 10)];
        return String.format("%010d", absolute / 10) + sign;
    }

    /// Places alternating end-column and text values in one fixed-width record.
    private String at(int width, Object... placements) {
        char[] line = blank(width).toCharArray();
        for (int index = 0; index < placements.length; index += 2) {
            int end = (Integer) placements[index];
            String text = String.valueOf(placements[index + 1]);
            int start = end - text.length();
            if (start < 0 || end > width) {
                throw new IllegalArgumentException("fixed-width field does not fit");
            }
            text.getChars(0, text.length(), line, start);
        }
        return new String(line);
    }

    /// Formats a whole-number count to its fixed report width.
    private String field(Object value, int width) {
        String format = "%" + width + "s";
        return String.format(format, value);
    }

    /// Formats an optional numeric source value as a report amount.
    private String value(@Nullable Number value) {
        if (value == null) {
            return number(BigDecimal.ZERO);
        }
        return value instanceof BigDecimal decimal
                ? number(decimal)
                : String.format("%,d", value.longValue());
    }

    /// Formats a whole-unit monetary amount at the fixed-width output boundary.
    private String number(BigDecimal value) {
        return String.format("%,d", value.toBigIntegerExact());
    }

    /// Pads or truncates one report line to the fixed print width.
    private String pad(String value, int width) {
        if (value.length() > width) {
            throw new IllegalArgumentException(
                    "record exceeds fixed width " + width + ": " + value);
        }
        return value + " ".repeat(width - value.length());
    }

    /// Creates one blank fixed-width report line.
    private String blank(int width) {
        return " ".repeat(width);
    }

    /// Complete immutable execution facts required to project a successful observable.
    ///
    /// Collections retain the source and group order produced by each step. Agency and frozen
    /// snapshots bracket the two shared-live mutations. The processor commits each mutation in a
    /// separate whole-step transaction, so a later failure can retain an earlier effect.
    public record Execution(
            EifdTifIncrementRunRequest request,
            List<AssessmentParcel> parcels,
            List<FrozenValuationReportFact> frozenBefore,
            List<FrozenValuation> frozen,
            RollupResult rollups,
            PercentageResult percentages,
            AllocationResult allocations,
            FrozenAgencyResult frozenAgencies,
            AgencySummaryResult summaries,
            TownReportResult towns,
            List<AgencyEqualizedValuation> agenciesBefore,
            PostingResult posting) {
        /// Copies source-ordered lists so projection observes one stable completed run.
        ///
        /// Construction performs no repository writes and does not change transaction ownership.
        public Execution {
            parcels = List.copyOf(parcels);
            frozenBefore = List.copyOf(frozenBefore);
            frozen = List.copyOf(frozen);
            agenciesBefore = List.copyOf(agenciesBefore);
        }
    }
}
