package org.cookcounty.tax.application.service;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.BatchEvidence;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.EligibilityPrintRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.HomeoutRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RenewalErrorRecord;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RenewalPrintRecord;
import org.springframework.stereotype.Component;

/** Projects the executed homeowner renewal path into Factor's batch-observable contract. */
@Component
public class PropertyTaxExemptionsFactorOutcomeProjector {

    static final String ENUMERATED_SCENARIO = "homeowner-enumerated-variant";
    static final String BROAD_SCENARIO = "homeowner-broad-variant";

    private static final int REPORT_WIDTH = 133;

    public String scenarioId(HomeownerVariant variant) {
        return variant == HomeownerVariant.ENUMERATED
                ? ENUMERATED_SCENARIO : BROAD_SCENARIO;
    }

    public Outcome project(HomeownerVariant variant, ProcessResult result) {
        BatchEvidence evidence = result.batchEvidence();
        String variantProgram = variant == HomeownerVariant.ENUMERATED
                ? "ASREA852" : "ASREA853";

        List<String> renewalErrors = renewalErrorReport(evidence);
        List<String> renewalPrint = renewalPrintReport(evidence);
        List<String> eligibilityPrint = eligibilityReport(evidence);
        List<String> homeout = homeoutRecords(evidence);
        List<String> rollForwardPrint = rollForwardReport(evidence);

        List<Cataloged> cataloged = List.of(
                cataloged("output/ASREA841-ERRPRINT.dat", renewalErrors),
                cataloged("output/ASREA841-PRINTOUT.dat", renewalPrint),
                cataloged("output/" + variantProgram + "-PRNTOUT.dat", eligibilityPrint),
                cataloged("output/ASREA859-HOMEOUT--ASHMOWFD01.dat", homeout),
                cataloged("output/ASREA859-PRNTOUT.dat", rollForwardPrint));

        List<Step> steps = List.of(
                step("S001", "ASREA841"),
                step("S002", "ASREA847"),
                step("S003", variantProgram),
                step("S004", "ASREA859"));

        return new Outcome(
                result.returnCode(),
                steps,
                Map.of(),
                evidence.batchDisplays(),
                Map.of(),
                cataloged,
                null,
                false,
                false);
    }

    public Outcome failed(RuntimeException exception) {
        String message = exception.getMessage() == null
                ? "The property-tax-exemptions worker failed."
                : exception.getMessage();
        return new Outcome(
                16,
                List.of(),
                Map.of(),
                List.of(message),
                Map.of(),
                List.of(),
                new org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Abend(
                        "MODERN_PROCESSING_FAILURE", null, null),
                false,
                true);
    }

    private static Step step(String name, String program) {
        return new Step(name, program, 0, false, null, List.of(), List.of());
    }

    private static Cataloged cataloged(String dsn, List<String> records) {
        List<String> immutableRecords = List.copyOf(records);
        List<String> encoded = immutableRecords.stream()
                .map(record -> Base64.getEncoder().encodeToString(
                        record.getBytes(StandardCharsets.UTF_8)))
                .toList();
        return new Cataloged(dsn, 1, immutableRecords.size(), immutableRecords, encoded);
    }

    private static List<String> renewalErrorReport(BatchEvidence evidence) {
        List<String> result = new ArrayList<>();
        for (RenewalErrorRecord record : evidence.renewalErrorRecords()) {
            result.add(fixed(String.format(
                    "   %018d        %-5s", record.propertyNumber(), record.batchNumber())));
        }
        addRenewalTotals(result, evidence);
        return List.copyOf(result);
    }

    private static List<String> renewalPrintReport(BatchEvidence evidence) {
        List<String> result = new ArrayList<>();
        for (RenewalPrintRecord record : evidence.renewalPrintRecords()) {
            result.add(fixed(String.format(
                    Locale.US,
                    " %03d  %018d   %05d   %04d    %04d   %8s   %05d   %,11d   %-5s",
                    record.volumeNumber(),
                    record.propertyNumber(),
                    record.taxCode(),
                    record.assessmentClass(),
                    1900 + record.applicationYear(),
                    record.proration().setScale(6, RoundingMode.UNNECESSARY).toPlainString(),
                    record.cooperativeQuantity(),
                    record.assessedValue(),
                    record.batchNumber())));
        }
        addRenewalTotals(result, evidence);
        return List.copyOf(result);
    }

    private static void addRenewalTotals(List<String> result, BatchEvidence evidence) {
        int updated = evidence.renewalPrintRecords().size();
        int rejected = evidence.renewalErrorRecords().size();
        result.add(countLine(" TOTAL RENEWAL RECORDS READ", updated + rejected, 43));
        result.add(countLine(" TOTAL RENEWAL RECORDS UPDATED", updated, 43));
        result.add(countLine(" TOTAL RENEWAL RECORDS REJECTED", rejected, 43));
        result.add(countLine(" TOTAL HOMEOWNER RECORDS READ", evidence.homeownerRecords(), 43));
        result.add(countLine(" TOTAL HOMEOWNER RECORDS WRITTEN", evidence.homeownerRecords(), 43));
        result.add(countLine(" TOTAL HOMEOWNER RECORDS UPDATED", updated, 43));
    }

    private static List<String> eligibilityReport(BatchEvidence evidence) {
        List<String> result = new ArrayList<>();
        for (EligibilityPrintRecord record : evidence.ineligibleRecords()) {
            result.add(fixed(String.format(
                    "   %03d  %014d   %05d       %03d       %03d     PARCEL IS NON-RESIDENTIAL",
                    record.volumeNumber(),
                    record.propertyNumber(),
                    record.taxCode(),
                    record.overallClass(),
                    record.overallClass())));
        }
        result.add(countLine("   TOTAL MASTER RECORDS READ", evidence.assessmentRecords(), 48));
        result.add(countLine("   TOTAL HOMEOWNERS RECORDS READ", evidence.homeownerRecords(), 48));
        result.add(countLine("   TOTAL HOMEOWNERS RECORDS WRITTEN", evidence.eligibleRecords(), 48));
        return List.copyOf(result);
    }

    private static List<String> homeoutRecords(BatchEvidence evidence) {
        return evidence.homeoutRecords().stream()
                .map(PropertyTaxExemptionsFactorOutcomeProjector::homeoutRecord)
                .toList();
    }

    private static String homeoutRecord(HomeoutRecord record) {
        return fixed(String.format(
                " %03d  %014d   %05d   %01d   %03d   %011d   %8s   %01d",
                record.volumeNumber(),
                record.propertyNumber(),
                record.taxCode(),
                record.taxType(),
                record.assessmentClass(),
                record.assessedValue(),
                record.proration().setScale(6, RoundingMode.HALF_UP).toPlainString(),
                record.responseStatus()));
    }

    private static List<String> rollForwardReport(BatchEvidence evidence) {
        return List.of(
                countLine("  TOTAL MASTER RECORDS READ", evidence.assessmentRecords(), 43),
                countLine("  TOTAL HOMEOWNERS RECORDS READ", evidence.eligibleRecords(), 43),
                countLine("  TOTAL HOMEOWNERS RECORDS WRITTEN", evidence.homeoutRecords().size(), 43));
    }

    private static String countLine(String label, int count, int fieldEnd) {
        String value = Integer.toString(count);
        int spaces = fieldEnd - label.length() - value.length();
        if (spaces < 1) {
            throw new IllegalArgumentException("Count does not fit report field: " + label);
        }
        return fixed(label + " ".repeat(spaces) + value);
    }

    private static String fixed(String value) {
        if (value.length() > REPORT_WIDTH) {
            throw new IllegalArgumentException("Report record exceeds " + REPORT_WIDTH + " bytes");
        }
        return value + " ".repeat(REPORT_WIDTH - value.length());
    }
}
