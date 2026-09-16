package org.cookcounty.tax.application.service;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.DatasetOp;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelSourceRecordRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Projects assessed-value stage evidence into the comparator's ordered batch outcome.
///
/// The assessment-master artifact overlays modern parcel and detail fields on each preserved
/// 122-byte source prefix. Missing prefixes and invalid prefix lengths fail projection instead of
/// publishing a partial binary record.
@Component
public final class AssessedValuePreparationFactorOutcomeProjector {

    private static final String OVERALL_REPORT_DSN = "output/ASREA018-PRINTFL.dat";
    private static final String BREAKDOWN_REPORT_DSN = "output/ASREA151-PRINTFLE.dat";
    private static final String MASTER_DSN = "output/ASREA178-MOUT.dat";
    private static final String TYPE5_REPORT_DSN = "output/ASREA178-PRINT.dat";

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final AssessmentParcelSourceRecordRepository sourceRecordRepository;

    /// Creates the projector with repositories for final persisted state and preserved prefixes.
    ///
    /// @param parcelRepository processed parcels in source order
    /// @param detailRepository processed details in source order
    /// @param sourceRecordRepository preserved fixed-width parcel prefixes
    public AssessedValuePreparationFactorOutcomeProjector(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            AssessmentParcelSourceRecordRepository sourceRecordRepository) {
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.sourceRecordRepository = sourceRecordRepository;
    }

    /// Creates the complete comparator outcome for one finished processor result.
    ///
    /// @param result immutable stage and output evidence
    /// @return ordered steps, dataset effects, displays, and cataloged outputs
    public Outcome project(ProcessResult result) {
        StageOutcome overall = stage(result, 0, "OVERALL_CLASS");
        StageOutcome bucketing = stage(result, 1, "VALUATION_BUCKETING");
        StageOutcome conversion = stage(result, 2, "TYPE5_CONVERSION");

        OutputRecord overallReport = output(result, "OVERALL_CLASS_REPORT");
        OutputRecord breakdownReport = output(result, "VALUATION_BREAKDOWN_REPORT");
        OutputRecord master = masterOutput(result);
        OutputRecord type5Report = output(result, "TYPE5_ERROR_REPORT");

        List<String> overallMessages = overallMessages(overall);
        List<String> bucketingMessages = bucketingMessages(bucketing);
        List<String> conversionMessages = conversionMessages(conversion);
        List<Step> steps =
                List.of(
                        step(
                                "S001",
                                "ASREA018",
                                overall,
                                overallMessages,
                                List.of(
                                        new DatasetOp("READ", "UT-S-MASTIN", overall.recordsRead()),
                                        new DatasetOp(
                                                "WRITE", "UT-S-MASTOUT", overall.recordsWritten()),
                                        new DatasetOp(
                                                "WRITE",
                                                "UT-S-PRINTFL",
                                                overallReport.recordCount()))),
                        step(
                                "S002",
                                "ASREA151",
                                bucketing,
                                bucketingMessages,
                                List.of(
                                        new DatasetOp(
                                                "READ", "UT-S-MASTERIN", bucketing.recordsRead()),
                                        new DatasetOp(
                                                "WRITE",
                                                "UT-S-MASTROUT",
                                                bucketing.recordsWritten()),
                                        new DatasetOp(
                                                "WRITE",
                                                "UT-S-PRINTFLE",
                                                breakdownReport.recordCount()))),
                        step(
                                "S003",
                                "ASREA178",
                                conversion,
                                conversionMessages,
                                List.of(
                                        new DatasetOp("READ", "UT-S-MIN", conversion.recordsRead()),
                                        new DatasetOp("WRITE", "UT-S-MOUT", master.recordCount()),
                                        new DatasetOp(
                                                "WRITE",
                                                "UT-S-PRINT",
                                                type5Report.recordCount()))));

        Map<String, List<String>> outputs = new LinkedHashMap<>();
        outputs.put("UT-S-PRINTFL", overallReport.recordData());
        outputs.put("UT-S-PRINTFLE", breakdownReport.recordData());
        outputs.put("UT-S-MOUT", master.recordData());
        outputs.put("UT-S-PRINT", type5Report.recordData());

        List<String> displays = new ArrayList<>();
        displays.addAll(overallMessages);
        displays.addAll(bucketingMessages);
        displays.addAll(conversionMessages);

        return new Outcome(
                result.returnCode(),
                steps,
                result.failed() ? Map.copyOf(outputs) : Map.of(),
                List.copyOf(displays),
                Map.of(),
                List.of(
                        cataloged(OVERALL_REPORT_DSN, overallReport),
                        cataloged(BREAKDOWN_REPORT_DSN, breakdownReport),
                        cataloged(MASTER_DSN, master),
                        cataloged(TYPE5_REPORT_DSN, type5Report)),
                null,
                false,
                false);
    }

    /// Creates the terminal comparator outcome for an unexpected worker failure.
    ///
    /// @param failure unexpected worker exception
    /// @return safe failed outcome that retains no internal stack details
    public Outcome workerFailure(RuntimeException failure) {
        String failureMessage = failure.getMessage();
        String text =
                failureMessage == null
                        ? "The assessed-value preparation worker failed."
                        : failureMessage;
        List<String> messages = List.of(text);
        return new Outcome(
                16,
                List.of(
                        new Step(
                                "S001",
                                "ASREA018",
                                16,
                                false,
                                completionCode(16),
                                messages,
                                List.of())),
                Map.of(),
                messages,
                Map.of(),
                List.of(),
                new FactorBatchOutcomeRecorder.Abend("WORKER_FAILURE", "ASREA018", "S001"),
                false,
                true);
    }

    private static List<String> overallMessages(StageOutcome stage) {
        return List.of(
                "TOTAL MASTER RECORDS READ     " + stage.recordsRead(),
                "TOTAL MASTER RECORDS UPDATED  " + metric(stage, "reported"),
                "TOTAL MASTER RECORDS WRITTEN  " + stage.recordsWritten());
    }

    private static List<String> bucketingMessages(StageOutcome stage) {
        BigDecimal farm = valuationMetric(stage, "farm");
        BigDecimal homeowner = valuationMetric(stage, "homeowner");
        BigDecimal nonHomeowner = valuationMetric(stage, "nonHomeowner");
        return List.of(
                "TOTAL MASTER RECORDS READ -  " + stage.recordsRead(),
                "TOTAL MASTER RECORDS WRITTEN -  " + stage.recordsWritten(),
                "TOTAL FARM-VALUE -  " + farm,
                "TOTAL HOME OWNER VALUE -  " + homeowner,
                "TOTAL NON-HOME-OWNER VALUE -  " + nonHomeowner,
                "TOTAL GRAND-TOTAL             " + farm.add(homeowner).add(nonHomeowner),
                "TOTAL PARCELS                 " + stage.recordsWritten());
    }

    private static List<String> conversionMessages(StageOutcome stage) {
        return List.of(
                "*************************",
                "      ASREA178 EOJ       ",
                "   MASTERS               ",
                "               READ       " + stage.recordsRead(),
                "               WRITTEN    " + stage.recordsWritten(),
                "                         ",
                "   TYP-5                 ",
                "               HITS 5         " + metric(stage, "type5Hits"),
                "               ZERO 5         " + metric(stage, "zeroType5"),
                "               CONVERTED 5    " + metric(stage, "convertedType5"),
                "                         ",
                "*************************");
    }

    /// Rebuilds each published master record from persisted post-processing state.
    ///
    /// The first 122 bytes come from the preserved source envelope. The projector overlays changed
    /// parcel fields and writes up to 350 ordered 53-byte detail occurrences.
    ///
    /// @throws IllegalStateException if a published parcel has no exact 122-byte source envelope
    private OutputRecord masterOutput(ProcessResult result) {
        OutputRecord declared = output(result, "ASSESSMENT_MASTER");
        if (declared.recordCount() == 0) {
            return OutputRecord.binary("ASSESSMENT_MASTER", List.of());
        }
        List<AssessmentParcel> parcels = parcelRepository.findAllInInputOrder();
        if (parcels.size() < declared.recordCount()) {
            throw new IllegalStateException(
                    "persisted assessed parcels are fewer than the published master count");
        }
        Map<String, byte[]> sourceRecords = new HashMap<>();
        for (AssessmentParcelSourceRecord source : sourceRecordRepository.findAllInSourceOrder()) {
            byte[] bytes = Base64.getDecoder().decode(source.sourceRecordBase64());
            if (bytes.length != 122) {
                throw new IllegalStateException(
                        "preserved assessed source record must contain 122 bytes");
            }
            sourceRecords.put(source.parcelNumber(), bytes);
        }
        Map<ParcelKey, List<AssessmentDetail>> details = new HashMap<>();
        for (AssessmentDetail detail : detailRepository.findAllInInputOrder()) {
            details.computeIfAbsent(
                            new ParcelKey(detail.parcelVolumeNumber(), detail.parcelNumber()),
                            ignored -> new ArrayList<>())
                    .add(detail);
        }
        details.values()
                .forEach(
                        records ->
                                records.sort(
                                        java.util.Comparator.comparing(
                                                AssessmentDetail::occurrenceNumber)));

        List<byte[]> records = new ArrayList<>(declared.recordCount());
        for (int index = 0; index < declared.recordCount(); index++) {
            AssessmentParcel parcel = parcels.get(index);
            byte[] source = sourceRecords.get(parcel.parcelNumber());
            if (source == null) {
                throw new IllegalStateException(
                        "no preserved assessed source record for parcel " + parcel.parcelNumber());
            }
            records.add(
                    encodeMaster(
                            source,
                            parcel,
                            details.getOrDefault(
                                    new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber()),
                                    List.of())));
        }
        return OutputRecord.binary("ASSESSMENT_MASTER", records);
    }

    /// Encodes one 18,706-byte assessment-master record without changing the source envelope.
    ///
    /// Space-filled detail positions remain unchanged when the parcel has no persisted details.
    /// Numeric values use their declared packed or zoned source representation.
    private static byte[] encodeMaster(
            byte[] source, AssessmentParcel parcel, List<AssessmentDetail> details) {
        byte[] record = new byte[122 + 34 + (350 * 53)];
        Arrays.fill(record, (byte) ' ');
        System.arraycopy(source, 0, record, 0, source.length);
        writeCharacter(record, 0, parcel.assessmentStatus());
        writePacked(record, 1, 2, AssessedValueRuleSupport.sourceInteger(parcel.volumeNumber()));
        writePacked(record, 3, 8, AssessedValueRuleSupport.sourceInteger(parcel.parcelNumber()));
        writeCharacter(record, 11, parcel.taxType());
        writePacked(record, 13, 3, AssessedValueRuleSupport.sourceInteger(parcel.taxCode()));
        writeCharacter(record, 16, parcel.parcelStatus());
        writePacked(record, 17, 2, AssessedValueRuleSupport.orZero(parcel.overallClass()));
        writeCharacter(record, 37, parcel.clerkMajorClass());
        BigDecimal[] values = {
            AssessedValueRuleSupport.orZero(parcel.priorLandValue()),
            AssessedValueRuleSupport.orZero(parcel.priorImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.priorTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.currentLandValue()),
            AssessedValueRuleSupport.orZero(parcel.currentImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.currentTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.proposedLandValue()),
            AssessedValueRuleSupport.orZero(parcel.proposedImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.proposedTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.farmValue()),
            AssessedValueRuleSupport.orZero(parcel.combinedHomeownerNonHomeownerValue()),
            AssessedValueRuleSupport.orZero(parcel.archivedPreConversionProposedTotal())
        };
        for (int index = 0; index < values.length; index++) {
            writePacked(record, 56 + (index * 5), 5, values[index].longValueExact());
        }
        writeZoned(record, 118, 1, AssessedValueRuleSupport.orZero(parcel.salesSegmentCount()));
        writeZoned(record, 119, 3, details.size());
        for (int index = 0; index < Math.min(details.size(), 350); index++) {
            encodeDetail(record, 156 + (index * 53), details.get(index));
        }
        return record;
    }

    private static void encodeDetail(byte[] target, int offset, AssessmentDetail detail) {
        writePacked(target, offset, 2, AssessedValueRuleSupport.orZero(detail.multicode()));
        writeCharacter(target, offset + 2, detail.detailType());
        writeCharacter(target, offset + 3, detail.detailCode());
        writeCharacter(
                target,
                offset + 4,
                detail.decimalScale() == null ? null : Integer.toString(detail.decimalScale()));
        writeString(target, offset + 5, 2, detail.unitMeasure());
        writePacked(
                target, offset + 7, 2, AssessedValueRuleSupport.orZero(detail.assessmentClass()));
        if ("1".equals(detail.detailType())) {
            writePacked(
                    target, offset + 10, 4, AssessedValueRuleSupport.orZero(detail.frontFootage()));
            writePacked(target, offset + 14, 3, AssessedValueRuleSupport.orZero(detail.depth()));
            writePacked(target, offset + 17, 4, scaled(detail.unitPrice(), 2));
            writePacked(target, offset + 21, 3, scaled(detail.depthFactor(), 3));
            writePacked(target, offset + 24, 3, scaled(detail.cornerFactor(), 4));
            writePacked(target, offset + 27, 3, scaled(detail.extraCornerFactor(), 5));
            writePacked(target, offset + 30, 4, scaled(detail.percentAssessed(), 5));
            writePacked(target, offset + 34, 2, scaled(detail.landConditionFactor(), 1));
        } else {
            writeString(target, offset + 9, 2, detail.supplementalDetailCode());
            writePacked(target, offset + 11, 4, AssessedValueRuleSupport.orZero(detail.area()));
            writePacked(target, offset + 15, 4, scaled(detail.unitPrice(), 2));
            writePacked(
                    target,
                    offset + 19,
                    5,
                    AssessedValueRuleSupport.orZero(detail.reproductionCost()).longValueExact());
            writePacked(target, offset + 24, 2, AssessedValueRuleSupport.orZero(detail.age()));
            writePacked(target, offset + 26, 2, scaled(detail.conditionFactor(), 1));
            writePacked(target, offset + 28, 4, scaled(detail.percentAssessed(), 5));
            writePacked(
                    target,
                    offset + 32,
                    2,
                    AssessedValueRuleSupport.orZero(detail.improvementYear()));
            writePacked(
                    target,
                    offset + 41,
                    8,
                    detail.keyParcelNumber() == null
                            ? 0L
                            : AssessedValueRuleSupport.sourceInteger(detail.keyParcelNumber()));
            writeCharacter(target, offset + 49, detail.splitCode());
        }
        writePacked(target, offset + 36, 5, detail.valuation().longValueExact());
    }

    private static long scaled(@Nullable BigDecimal value, int scale) {
        return value == null ? 0L : value.movePointRight(scale).longValue();
    }

    private static void writePacked(byte[] target, int offset, int length, long value) {
        boolean negative = value < 0;
        String digits = Long.toString(Math.abs(value));
        int digitCount = (length * 2) - 1;
        if (digits.length() > digitCount) {
            throw new IllegalArgumentException("value exceeds assessed master packed field");
        }
        digits = "0".repeat(digitCount - digits.length()) + digits;
        for (int index = 0; index < length; index++) {
            int high = digits.charAt(index * 2) - '0';
            int low =
                    index == length - 1
                            ? (negative ? 0x0d : 0x0c)
                            : digits.charAt((index * 2) + 1) - '0';
            target[offset + index] = (byte) ((high << 4) | low);
        }
    }

    private static void writeZoned(byte[] target, int offset, int length, int value) {
        String format = "%0" + length + "d";
        String digits = String.format(format, Math.abs(value));
        writeString(target, offset, length, digits);
        int last = target[offset + length - 1] - '0';
        target[offset + length - 1] =
                (byte) ((value < 0 ? "}JKLMNOPQR" : "{ABCDEFGHI").charAt(last));
    }

    private static void writeCharacter(byte[] target, int offset, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            target[offset] = (byte) value.charAt(0);
        }
    }

    private static void writeString(byte[] target, int offset, int length, @Nullable String value) {
        if (value == null) {
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, target, offset, Math.min(length, bytes.length));
    }

    private record ParcelKey(String volumeNumber, String parcelNumber) {}

    private static StageOutcome stage(ProcessResult result, int index, String name) {
        if (index < result.stages().size()) {
            return result.stages().get(index);
        }
        return new StageOutcome(name, "NOT_STARTED", null, 0, 0, 0, 0, false, false, 0);
    }

    private static OutputRecord output(ProcessResult result, String kind) {
        for (OutputRecord candidate : result.outputs()) {
            if (kind.equals(candidate.kind())) {
                return candidate;
            }
        }
        return new OutputRecord(kind, "application/octet-stream", 0);
    }

    private static Cataloged cataloged(String dsn, OutputRecord output) {
        return new Cataloged(
                dsn, 1, output.recordCount(), output.recordData(), output.recordDataBase64());
    }

    /// Returns an integral stage count, or zero when that stage did not produce the metric.
    private static long metric(StageOutcome stage, String name) {
        return stage.metrics().getOrDefault(name, 0L).longValue();
    }

    /// Returns an exact monetary total, or zero for a stage that did not produce that total.
    private static BigDecimal valuationMetric(StageOutcome stage, String name) {
        return (BigDecimal) stage.metrics().getOrDefault(name, BigDecimal.ZERO);
    }

    private static Step step(
            String name,
            String program,
            StageOutcome stage,
            List<String> messages,
            List<DatasetOp> datasetOps) {
        if ("COMPLETED".equals(stage.status())) {
            return new Step(name, program, returnCode(stage), false, null, List.of(), List.of());
        }
        return new Step(
                name,
                program,
                returnCode(stage),
                skipped(stage),
                completionCode(stage),
                messages,
                datasetOps);
    }

    private static boolean skipped(StageOutcome stage) {
        return "NOT_STARTED".equals(stage.status());
    }

    private static int returnCode(StageOutcome stage) {
        return stage.returnCode() == null ? 0 : stage.returnCode();
    }

    private static @Nullable String completionCode(StageOutcome stage) {
        return skipped(stage) ? null : completionCode(returnCode(stage));
    }

    private static String completionCode(int returnCode) {
        return "CC " + String.format("%04d", returnCode);
    }
}
