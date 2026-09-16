package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.DatasetOp;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.cookcounty.tax.application.service.TaxRateInputKernel.AgencyAssessment;
import org.cookcounty.tax.application.service.TaxRateInputKernel.DividedValue;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Posting;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Projects completed processing state into the external batch comparison contract.
///
/// The report output uses 133-character US-ASCII records. Projection has no persistence effects.
@Component
public final class TaxRateInputFactorOutcomeProjector {

    /// External agency-assessment output identity required by the comparison boundary.
    private static final String AGENCY_OUTPUT_DATASET = "output/CLRTM752-AGCYMSTR.dat";

    /// External fixed-width report identity required by the comparison boundary.
    private static final String REPORT_OUTPUT_DATASET = "output/CLRTM752-PRINTFLE.dat";

    /// Exact character width of each report record before US-ASCII encoding.
    private static final int REPORT_WIDTH = 133;

    /// Date layout required by the external report header.
    private static final DateTimeFormatter REPORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /// Projects one completed result using the run's fixed business date and time.
    ///
    /// @param businessDate date printed in external report headers
    /// @param businessTime 24-hour time whose hour and minute are printed in stage messages
    /// @param result immutable processing result, including retained partial output
    /// @return complete external outcome with fixed-width cataloged records
    public Outcome project(LocalDate businessDate, String businessTime, Result result) {
        List<String> agencyRecords =
                result.agencyAssessments().stream()
                        .map(TaxRateInputFactorOutcomeProjector::agencyRecord)
                        .toList();
        List<String> reportRecords = reportRecords(businessDate, result);

        List<String> divisionStampingMessages =
                divisionStampingMessages(businessDate, businessTime, result);
        List<String> agencyAttachmentMessages = agencyAttachmentMessages(result);
        int divisionStampingReturnCode = stageReturnCode(result, "clrtm751-");
        int agencyAttachmentReturnCode = divisionStampingReturnCode == 0 ? result.returnCode() : 0;
        List<Step> steps =
                List.of(
                        step(
                                "S001",
                                "CLRTM751",
                                divisionStampingReturnCode,
                                false,
                                divisionStampingMessages,
                                List.of(
                                        new DatasetOp(
                                                "READ",
                                                "PS.EQUALVAL",
                                                result.divisionStamping()
                                                        .equalizedValueRecordsRead()),
                                        new DatasetOp(
                                                "READ",
                                                "PS.DIVSION",
                                                result.divisionStamping().divisionRecordsRead()),
                                        new DatasetOp(
                                                "WRITE",
                                                "SPOOL.HANDOFF.CLRTM751-OUTPUT",
                                                result.divisionStamping().outputRecordsWritten()))),
                        step(
                                "S002",
                                "CLRTM752",
                                agencyAttachmentReturnCode,
                                divisionStampingReturnCode == TaxRateInputKernel.ERROR_RETURN_CODE,
                                agencyAttachmentMessages,
                                List.of(
                                        new DatasetOp(
                                                "READ",
                                                "SPOOL.HANDOFF.CLRTM751-OUTPUT",
                                                result.agencyAttachment().assessmentRecordsRead()),
                                        new DatasetOp(
                                                "READ",
                                                "TXCDFILE",
                                                result.agencyAttachment().assessmentRecordsRead()),
                                        new DatasetOp(
                                                "CATALOG",
                                                "SPOOL.OUT.CLRTM752-AGCYMSTR",
                                                result.agencyAttachment()
                                                        .assessmentRecordsWritten()),
                                        new DatasetOp(
                                                "CATALOG",
                                                "SPOOL.OUT.CLRTM752-PRINTFLE",
                                                reportRecords.size()))));

        Map<String, List<String>> outputs = Map.of();
        if (result.returnCode() != 0) {
            Map<String, List<String>> failureOutputs = new LinkedHashMap<>();
            failureOutputs.put(AGENCY_OUTPUT_DATASET, agencyRecords);
            failureOutputs.put(REPORT_OUTPUT_DATASET, reportRecords);
            outputs = Map.copyOf(failureOutputs);
        }

        List<String> displays =
                new ArrayList<>(divisionStampingMessages.size() + agencyAttachmentMessages.size());
        displays.addAll(divisionStampingMessages);
        displays.addAll(agencyAttachmentMessages);

        List<Cataloged> cataloged =
                List.of(
                        cataloged(AGENCY_OUTPUT_DATASET, agencyRecords),
                        cataloged(REPORT_OUTPUT_DATASET, reportRecords));
        return new Outcome(
                result.returnCode(),
                steps,
                outputs,
                List.copyOf(displays),
                datasetDiffs(result.postings()),
                cataloged,
                null,
                false,
                false);
    }

    /// Projects an unexpected worker exception into a safe external abend outcome.
    public Outcome workerFailure(RuntimeException failure) {
        var failureMessage = failure.getMessage();
        String text =
                failureMessage == null
                        ? "Tax-rate input preparation worker failed."
                        : failureMessage;
        List<String> messages = List.of(text);
        return new Outcome(
                TaxRateInputKernel.ERROR_RETURN_CODE,
                List.of(
                        new Step(
                                "S001",
                                "CLRTM751",
                                TaxRateInputKernel.ERROR_RETURN_CODE,
                                false,
                                completionCode(TaxRateInputKernel.ERROR_RETURN_CODE),
                                messages,
                                List.of())),
                Map.of(),
                messages,
                Map.of(),
                List.of(),
                new FactorBatchOutcomeRecorder.Abend("WORKER_FAILURE", "CLRTM751", "S001"),
                false,
                false);
    }

    /// Returns the external division-stamping display records in their required order.
    private static List<String> divisionStampingMessages(
            LocalDate businessDate, String businessTime, Result result) {
        return List.of(
                "PROGRAM CLRTM751 DATE AND TIME OF RUN =  "
                        + businessDate.format(REPORT_DATE)
                        + "   "
                        + businessTime.substring(0, 5),
                "TOTAL MASTER RECORDS READ          "
                        + result.divisionStamping().equalizedValueRecordsRead(),
                "TOTAL DIVISION RECORDS READ        "
                        + result.divisionStamping().divisionRecordsRead(),
                "TOTAL MASTER RECORDS WRITTEN       "
                        + result.divisionStamping().outputRecordsWritten(),
                "TOTAL MASTER RECORDS UPTD/DIVN NO  " + result.divisionStamping().recordsStamped(),
                "TOTAL DIVISION RECORDS UNMATCHED   "
                        + result.divisionStamping().divisionRecordsUnmatched());
    }

    /// Returns the external agency-attachment totals in their required order.
    private static List<String> agencyAttachmentMessages(Result result) {
        return List.of(
                "   TOTAL ASSESSMENT MASTER RECORDS READ              "
                        + result.agencyAttachment().assessmentRecordsRead(),
                "   TOTAL ASSESSMENT MASTER RECORDS WRITTEN            "
                        + result.agencyAttachment().assessmentRecordsWritten(),
                "   TOTAL ASSESSMENT MASTER RECORDS UNMATCHED         "
                        + result.agencyAttachment().assessmentRecordsUnmatched());
    }

    /// Builds immutable 133-character attachment report records.
    private static List<String> reportRecords(LocalDate businessDate, Result result) {
        List<String> records = new ArrayList<>();
        records.add(pad(""));
        for (DividedValue value : result.unmatchedDividedValues()) {
            records.add(
                    pad(
                            String.format(
                                    "   %03d   %018d   %s %s   NO MATCHING TAX CODE MASTER RECORD",
                                    value.volume(),
                                    value.property(),
                                    value.taxType(),
                                    value.taxCode())));
        }
        records.add(
                pad(
                        " CLRTM752                                        OFFICE OF THE COOK COUNTY"
                                + " CLERK                                        PAGE     1"));
        records.add(
                pad(
                        " "
                                + businessDate.format(REPORT_DATE)
                                + "                                DISCONNECT/ANNEXATION AGENCY"
                                + " UPDATE ERROR REPORT"));
        records.add(pad("   VOL    PERMANENT INDEX     TAX    TAX"));
        records.add(pad("               NUMBER        TYPE   CODE"));
        records.add(pad(""));
        records.add(
                pad(
                        "   TOTAL ASSESSMENT MASTER RECORDS READ              "
                                + result.agencyAttachment().assessmentRecordsRead()));
        records.add(
                pad(
                        "   TOTAL ASSESSMENT MASTER RECORDS WRITTEN            "
                                + result.agencyAttachment().assessmentRecordsWritten()));
        records.add(
                pad(
                        "   TOTAL ASSESSMENT MASTER RECORDS UNMATCHED         "
                                + result.agencyAttachment().assessmentRecordsUnmatched()));
        return List.copyOf(records);
    }

    /// Encodes one agency assessment as the external delimited handoff record.
    ///
    /// The boundary requires integer text for monetary fields. Exact integer conversion rejects a
    /// fractional value instead of truncating or rounding it.
    private static String agencyRecord(AgencyAssessment value) {
        return String.format(
                "%014d|%02d|%03d|%018d|%s|%s|%s|%s|%s|%s",
                value.divisionNumber(),
                value.town(),
                value.volume(),
                value.property(),
                value.taxType(),
                value.taxCode(),
                wholeUnitInteger(value.assessedValue()),
                wholeUnitInteger(value.equalizedValue()),
                value.rate().toPlainString(),
                String.join(",", value.agencies()));
    }

    /// Converts an exact whole-unit monetary value only at the fixed-format comparison boundary.
    ///
    /// @param value exact monetary amount that must have no fractional part
    /// @return exact integer representation for the legacy output field
    /// @throws ArithmeticException if the value has a nonzero fractional part
    private static BigInteger wholeUnitInteger(BigDecimal value) {
        return value.toBigIntegerExact();
    }

    /// Encodes records as US-ASCII while preserving their original text form.
    private static Cataloged cataloged(String datasetName, List<String> records) {
        List<String> encoded =
                records.stream()
                        .map(
                                record ->
                                        Base64.getEncoder()
                                                .encodeToString(
                                                        record.getBytes(StandardCharsets.US_ASCII)))
                        .toList();
        return new Cataloged(datasetName, 1, records.size(), records, encoded);
    }

    /// Projects successful posting mutations in operation order.
    private static Map<String, Object> datasetDiffs(List<Posting> postings) {
        if (postings.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> persisted =
                postings.stream()
                        .map(
                                posting ->
                                        Map.<String, Object>of(
                                                "taxCode", posting.taxCode(),
                                                "agencyNumber", posting.agencyNumber(),
                                                "operation", posting.operation().name(),
                                                "disconnectedValue", posting.disconnectedValue(),
                                                "annexedValue", posting.annexedValue()))
                        .toList();
        return Map.of("frozenAgencyAdjustments", persisted);
    }

    /// Derives a stage return code from ordered rule-linked error messages.
    private static int stageReturnCode(Result result, String rulePrefix) {
        return result.messages().stream()
                        .anyMatch(
                                message ->
                                        message.severity().equals("ERROR")
                                                && message.ruleId().startsWith(rulePrefix))
                ? TaxRateInputKernel.ERROR_RETURN_CODE
                : 0;
    }

    /// Creates one external step while preserving skipped and error diagnostics.
    private static Step step(
            String name,
            String program,
            int returnCode,
            boolean skipped,
            List<String> messages,
            List<DatasetOp> datasetOps) {
        if (returnCode == 0 && !skipped) {
            return new Step(name, program, returnCode, false, "", List.of(), List.of());
        }
        return new Step(
                name,
                program,
                returnCode,
                skipped,
                completionCode(returnCode),
                messages,
                datasetOps);
    }

    /// Formats the external four-digit completion code.
    private static String completionCode(int returnCode) {
        return "CC " + String.format("%04d", returnCode);
    }

    /// Pads one record to exactly 133 characters without truncation.
    private static String pad(String value) {
        if (value.length() > REPORT_WIDTH) {
            throw new IllegalArgumentException("Report record exceeds 133 characters");
        }
        return value + " ".repeat(REPORT_WIDTH - value.length());
    }
}
