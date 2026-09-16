package org.cookcounty.tax.application.service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.DatasetOp;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.cookcounty.tax.application.service.TaxRateInputKernel.AgencyAssessment;
import org.cookcounty.tax.application.service.TaxRateInputKernel.DividedValue;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Posting;
import org.cookcounty.tax.application.service.TaxRateInputKernel.Result;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.TaxRateInputPreparationRunRequest;
import org.springframework.stereotype.Component;

/** Projects the completed tax-rate pipeline from modern execution state. */
@Component
public final class TaxRateInputFactorOutcomeProjector {

    private static final String AGENCY_DSN = "output/CLRTM752-AGCYMSTR.dat";
    private static final String REPORT_DSN = "output/CLRTM752-PRINTFLE.dat";
    private static final int REPORT_WIDTH = 133;
    private static final DateTimeFormatter REPORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public Outcome project(TaxRateInputPreparationRunRequest request, Result result) {
        List<String> agencyRecords = result.agencyAssessments().stream()
                .map(TaxRateInputFactorOutcomeProjector::agencyRecord)
                .toList();
        List<String> reportRecords = reportRecords(request, result);

        List<String> clrtm751Messages = clrtm751Messages(request, result);
        List<String> clrtm752Messages = clrtm752Messages(result);
        int clrtm751ReturnCode = stageReturnCode(result, "clrtm751-");
        int clrtm752ReturnCode = clrtm751ReturnCode == 0 ? result.returnCode() : 0;
        List<Step> steps = List.of(
                step(
                        "S001",
                        "CLRTM751",
                        clrtm751ReturnCode,
                        false,
                        clrtm751Messages,
                        List.of(
                                new DatasetOp("READ", "PS.EQUALVAL",
                                        result.divisionStamping().equalizedValueRecordsRead()),
                                new DatasetOp("READ", "PS.DIVSION",
                                        result.divisionStamping().divisionRecordsRead()),
                                new DatasetOp("WRITE", "SPOOL.HANDOFF.CLRTM751-OUTPUT",
                                        result.divisionStamping().outputRecordsWritten()))),
                step(
                        "S002",
                        "CLRTM752",
                        clrtm752ReturnCode,
                        clrtm751ReturnCode == TaxRateInputKernel.ERROR_RETURN_CODE,
                        clrtm752Messages,
                        List.of(
                                new DatasetOp("READ", "SPOOL.HANDOFF.CLRTM751-OUTPUT",
                                        result.agencyAttachment().assessmentRecordsRead()),
                                new DatasetOp("READ", "TXCDFILE",
                                        result.agencyAttachment().assessmentRecordsRead()),
                                new DatasetOp("CATALOG", "SPOOL.OUT.CLRTM752-AGCYMSTR",
                                        result.agencyAttachment().assessmentRecordsWritten()),
                                new DatasetOp("CATALOG", "SPOOL.OUT.CLRTM752-PRINTFLE",
                                        reportRecords.size()))));

        Map<String, List<String>> outputs = Map.of();
        if (result.returnCode() != 0) {
            Map<String, List<String>> failureOutputs = new LinkedHashMap<>();
            failureOutputs.put(AGENCY_DSN, agencyRecords);
            failureOutputs.put(REPORT_DSN, reportRecords);
            outputs = Map.copyOf(failureOutputs);
        }

        List<String> displays = new ArrayList<>(clrtm751Messages.size() + clrtm752Messages.size());
        displays.addAll(clrtm751Messages);
        displays.addAll(clrtm752Messages);

        List<Cataloged> cataloged = List.of(
                cataloged(AGENCY_DSN, agencyRecords),
                cataloged(REPORT_DSN, reportRecords));
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

    public Outcome workerFailure(RuntimeException failure) {
        String text = failure.getMessage() == null
                ? "Tax-rate input preparation worker failed."
                : failure.getMessage();
        List<String> messages = List.of(text);
        return new Outcome(
                TaxRateInputKernel.ERROR_RETURN_CODE,
                List.of(new Step(
                        "S001", "CLRTM751", TaxRateInputKernel.ERROR_RETURN_CODE, false,
                        completionCode(TaxRateInputKernel.ERROR_RETURN_CODE), messages, List.of())),
                Map.of(),
                messages,
                Map.of(),
                List.of(),
                new FactorBatchOutcomeRecorder.Abend("WORKER_FAILURE", "CLRTM751", "S001"),
                false,
                false);
    }

    private static List<String> clrtm751Messages(
            TaxRateInputPreparationRunRequest request, Result result) {
        return List.of(
                "PROGRAM CLRTM751 DATE AND TIME OF RUN =  "
                        + request.getBusinessDate().format(REPORT_DATE)
                        + "   " + request.getBusinessTime().substring(0, 5),
                "TOTAL MASTER RECORDS READ          "
                        + result.divisionStamping().equalizedValueRecordsRead(),
                "TOTAL DIVISION RECORDS READ        "
                        + result.divisionStamping().divisionRecordsRead(),
                "TOTAL MASTER RECORDS WRITTEN       "
                        + result.divisionStamping().outputRecordsWritten(),
                "TOTAL MASTER RECORDS UPTD/DIVN NO  "
                        + result.divisionStamping().recordsStamped(),
                "TOTAL DIVISION RECORDS UNMATCHED   "
                        + result.divisionStamping().divisionRecordsUnmatched());
    }

    private static List<String> clrtm752Messages(Result result) {
        return List.of(
                "   TOTAL ASSESSMENT MASTER RECORDS READ              "
                        + result.agencyAttachment().assessmentRecordsRead(),
                "   TOTAL ASSESSMENT MASTER RECORDS WRITTEN            "
                        + result.agencyAttachment().assessmentRecordsWritten(),
                "   TOTAL ASSESSMENT MASTER RECORDS UNMATCHED         "
                        + result.agencyAttachment().assessmentRecordsUnmatched());
    }

    private static List<String> reportRecords(
            TaxRateInputPreparationRunRequest request, Result result) {
        List<String> records = new ArrayList<>();
        records.add(pad(""));
        for (DividedValue value : result.unmatchedDividedValues()) {
            records.add(pad(String.format(
                    "   %03d   %018d   %s %s   NO MATCHING TAX CODE MASTER RECORD",
                    value.volume(), value.property(), value.taxType(), value.taxCode())));
        }
        records.add(pad(
                " CLRTM752                                        OFFICE OF THE COOK COUNTY CLERK                                        PAGE     1"));
        records.add(pad(" " + request.getBusinessDate().format(REPORT_DATE)
                + "                                DISCONNECT/ANNEXATION AGENCY UPDATE ERROR REPORT"));
        records.add(pad("   VOL    PERMANENT INDEX     TAX    TAX"));
        records.add(pad("               NUMBER        TYPE   CODE"));
        records.add(pad(""));
        records.add(pad("   TOTAL ASSESSMENT MASTER RECORDS READ              "
                + result.agencyAttachment().assessmentRecordsRead()));
        records.add(pad("   TOTAL ASSESSMENT MASTER RECORDS WRITTEN            "
                + result.agencyAttachment().assessmentRecordsWritten()));
        records.add(pad("   TOTAL ASSESSMENT MASTER RECORDS UNMATCHED         "
                + result.agencyAttachment().assessmentRecordsUnmatched()));
        return List.copyOf(records);
    }


    private static String agencyRecord(AgencyAssessment value) {
        return String.format(
                "%014d|%02d|%03d|%018d|%s|%s|%d|%d|%s|%s",
                value.divisionNumber(), value.town(), value.volume(), value.property(),
                value.taxType(), value.taxCode(), value.assessedValue(), value.equalizedValue(),
                value.rate().toPlainString(), String.join(",", value.agencies()));
    }

    private static Cataloged cataloged(String dsn, List<String> records) {
        List<String> encoded = records.stream()
                .map(record -> Base64.getEncoder().encodeToString(
                        record.getBytes(StandardCharsets.US_ASCII)))
                .toList();
        return new Cataloged(dsn, 1, records.size(), records, encoded);
    }

    private static Map<String, Object> datasetDiffs(List<Posting> postings) {
        if (postings.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> persisted = postings.stream()
                .map(posting -> Map.<String, Object>of(
                        "taxCode", posting.taxCode(),
                        "agencyNumber", posting.agencyNumber(),
                        "operation", posting.operation().name(),
                        "disconnectedValue", posting.disconnectedValue(),
                        "annexedValue", posting.annexedValue()))
                .toList();
        return Map.of("frozenAgencyAdjustments", persisted);
    }

    private static int stageReturnCode(Result result, String rulePrefix) {
        return result.messages().stream()
                .anyMatch(message -> message.severity().equals("ERROR")
                        && message.ruleId().startsWith(rulePrefix))
                ? TaxRateInputKernel.ERROR_RETURN_CODE
                : 0;
    }

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

    private static String completionCode(int returnCode) {
        return "CC " + String.format("%04d", returnCode);
    }

    private static String pad(String value) {
        if (value.length() > REPORT_WIDTH) {
            throw new IllegalArgumentException("CLRTM752 report record exceeds 133 characters");
        }
        return value + " ".repeat(REPORT_WIDTH - value.length());
    }
}
