package org.cookcounty.tax.application.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Component;

/** Projects the completed VALPREP workflow from modern processor evidence. */
@Component
public final class AssessedValuePreparationFactorOutcomeProjector {

    private static final String OVERALL_REPORT_DSN = "output/ASREA018-PRINTFL.dat";
    private static final String BREAKDOWN_REPORT_DSN = "output/ASREA151-PRINTFLE.dat";
    private static final String MASTER_DSN = "output/ASREA178-MOUT.dat";
    private static final String TYPE5_REPORT_DSN = "output/ASREA178-PRINT.dat";

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final AssessmentParcelSourceRecordRepository sourceRecordRepository;

    public AssessedValuePreparationFactorOutcomeProjector(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            AssessmentParcelSourceRecordRepository sourceRecordRepository) {
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.sourceRecordRepository = sourceRecordRepository;
    }

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
        List<Step> steps = List.of(
                step(
                        "S001", "ASREA018", overall, overallMessages,
                        List.of(
                                new DatasetOp("READ", "UT-S-MASTIN", overall.recordsRead()),
                                new DatasetOp("WRITE", "UT-S-MASTOUT", overall.recordsWritten()),
                                new DatasetOp("WRITE", "UT-S-PRINTFL", overallReport.recordCount()))),
                step(
                        "S002", "ASREA151", bucketing, bucketingMessages,
                        List.of(
                                new DatasetOp("READ", "UT-S-MASTERIN", bucketing.recordsRead()),
                                new DatasetOp("WRITE", "UT-S-MASTROUT", bucketing.recordsWritten()),
                                new DatasetOp("WRITE", "UT-S-PRINTFLE", breakdownReport.recordCount()))),
                step(
                        "S003", "ASREA178", conversion, conversionMessages,
                        List.of(
                                new DatasetOp("READ", "UT-S-MIN", conversion.recordsRead()),
                                new DatasetOp("WRITE", "UT-S-MOUT", master.recordCount()),
                                new DatasetOp("WRITE", "UT-S-PRINT", type5Report.recordCount()))));

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

    public Outcome workerFailure(RuntimeException failure) {
        String text = failure.getMessage() == null
                ? "The assessed-value preparation worker failed."
                : failure.getMessage();
        List<String> messages = List.of(text);
        return new Outcome(
                16,
                List.of(new Step(
                        "S001", "ASREA018", 16, false, completionCode(16),
                        messages, List.of())),
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
        long farm = metric(stage, "farm");
        long homeowner = metric(stage, "homeowner");
        long nonHomeowner = metric(stage, "nonHomeowner");
        return List.of(
                "TOTAL MASTER RECORDS READ -  " + stage.recordsRead(),
                "TOTAL MASTER RECORDS WRITTEN -  " + stage.recordsWritten(),
                "TOTAL FARM-VALUE -  " + farm,
                "TOTAL HOME OWNER VALUE -  " + homeowner,
                "TOTAL NON-HOME-OWNER VALUE -  " + nonHomeowner,
                "TOTAL GRAND-TOTAL             " + (farm + homeowner + nonHomeowner),
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
        Map<Long, byte[]> sourceRecords = new HashMap<>();
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
                            new ParcelKey(detail.getParcelVolumeNumber(), detail.getParcelNumber()),
                            ignored -> new ArrayList<>())
                    .add(detail);
        }
        details.values().forEach(records -> records.sort(
                java.util.Comparator.comparing(
                        AssessmentDetail::getOccurrenceNumber,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))));

        List<byte[]> records = new ArrayList<>(declared.recordCount());
        for (int index = 0; index < declared.recordCount(); index++) {
            AssessmentParcel parcel = parcels.get(index);
            byte[] source = sourceRecords.get(parcel.getParcelNumber());
            if (source == null) {
                throw new IllegalStateException(
                        "no preserved assessed source record for parcel "
                                + parcel.getParcelNumber());
            }
            records.add(encodeMaster(
                    source,
                    parcel,
                    details.getOrDefault(
                            new ParcelKey(parcel.getVolumeNumber(), parcel.getParcelNumber()),
                            List.of())));
        }
        return OutputRecord.binary("ASSESSMENT_MASTER", records);
    }

    private static byte[] encodeMaster(
            byte[] source, AssessmentParcel parcel, List<AssessmentDetail> details) {
        byte[] record = new byte[122 + 34 + (350 * 53)];
        Arrays.fill(record, (byte) ' ');
        System.arraycopy(source, 0, record, 0, source.length);
        writeCharacter(record, 0, parcel.getAssessmentStatus());
        writePacked(record, 1, 2, AssessedValueRuleSupport.orZero(parcel.getVolumeNumber()));
        writePacked(record, 3, 8, AssessedValueRuleSupport.orZero(parcel.getParcelNumber()));
        writeCharacter(record, 11, parcel.getTaxType());
        writePacked(record, 13, 3, AssessedValueRuleSupport.orZero(parcel.getTaxCode()));
        writeCharacter(record, 16, parcel.getParcelStatus());
        writePacked(record, 17, 2, AssessedValueRuleSupport.orZero(parcel.getOverallClass()));
        writeCharacter(record, 37, parcel.getClerkMajorClass());
        long[] values = {
            AssessedValueRuleSupport.orZero(parcel.getPriorLandValue()),
            AssessedValueRuleSupport.orZero(parcel.getPriorImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.getPriorTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.getCurrentLandValue()),
            AssessedValueRuleSupport.orZero(parcel.getCurrentImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.getCurrentTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.getProposedLandValue()),
            AssessedValueRuleSupport.orZero(parcel.getProposedImprovementValue()),
            AssessedValueRuleSupport.orZero(parcel.getProposedTotalValue()),
            AssessedValueRuleSupport.orZero(parcel.getFarmValue()),
            AssessedValueRuleSupport.orZero(parcel.getCombinedHomeownerNonHomeownerValue()),
            AssessedValueRuleSupport.orZero(parcel.getArchivedPreConversionProposedTotal())
        };
        for (int index = 0; index < values.length; index++) {
            writePacked(record, 56 + (index * 5), 5, values[index]);
        }
        writeZoned(record, 118, 1, AssessedValueRuleSupport.orZero(parcel.getSalesSegmentCount()));
        writeZoned(record, 119, 3, details.size());
        for (int index = 0; index < Math.min(details.size(), 350); index++) {
            encodeDetail(record, 156 + (index * 53), details.get(index));
        }
        return record;
    }

    private static void encodeDetail(byte[] target, int offset, AssessmentDetail detail) {
        writePacked(target, offset, 2, AssessedValueRuleSupport.orZero(detail.getMulticode()));
        writeCharacter(target, offset + 2, detail.getDetailType());
        writeCharacter(target, offset + 3, detail.getDetailCode());
        writeCharacter(target, offset + 4,
                detail.getDecimalScale() == null ? null : Integer.toString(detail.getDecimalScale()));
        writeString(target, offset + 5, 2, detail.getUnitMeasure());
        writePacked(target, offset + 7, 2, AssessedValueRuleSupport.orZero(detail.getAssessmentClass()));
        if ("1".equals(detail.getDetailType())) {
            writePacked(target, offset + 10, 4, AssessedValueRuleSupport.orZero(detail.getFrontFootage()));
            writePacked(target, offset + 14, 3, AssessedValueRuleSupport.orZero(detail.getDepth()));
            writePacked(target, offset + 17, 4, scaled(detail.getUnitPrice(), 2));
            writePacked(target, offset + 21, 3, scaled(detail.getDepthFactor(), 3));
            writePacked(target, offset + 24, 3, scaled(detail.getCornerFactor(), 4));
            writePacked(target, offset + 27, 3, scaled(detail.getExtraCornerFactor(), 5));
            writePacked(target, offset + 30, 4, scaled(detail.getPercentAssessed(), 5));
            writePacked(target, offset + 34, 2, scaled(detail.getLandConditionFactor(), 1));
        } else {
            writeString(target, offset + 9, 2, detail.getCdu());
            writePacked(target, offset + 11, 4, AssessedValueRuleSupport.orZero(detail.getArea()));
            writePacked(target, offset + 15, 4, scaled(detail.getUnitPrice(), 2));
            writePacked(target, offset + 19, 5, AssessedValueRuleSupport.orZero(detail.getReproductionCost()));
            writePacked(target, offset + 24, 2, AssessedValueRuleSupport.orZero(detail.getAge()));
            writePacked(target, offset + 26, 2, scaled(detail.getConditionFactor(), 1));
            writePacked(target, offset + 28, 4, scaled(detail.getPercentAssessed(), 5));
            writePacked(target, offset + 32, 2, AssessedValueRuleSupport.orZero(detail.getImprovementYear()));
            writePacked(target, offset + 41, 8, AssessedValueRuleSupport.orZero(detail.getKeyParcelNumber()));
            writeCharacter(target, offset + 49, detail.getSplitCode());
        }
        writePacked(target, offset + 36, 5, AssessedValueRuleSupport.orZero(detail.getValuation()));
    }

    private static long scaled(java.math.BigDecimal value, int scale) {
        return value == null ? 0L : value.movePointRight(scale).longValue();
    }

    private static void writePacked(
            byte[] target, int offset, int length, long value) {
        boolean negative = value < 0;
        String digits = Long.toString(Math.abs(value));
        int digitCount = (length * 2) - 1;
        if (digits.length() > digitCount) {
            throw new IllegalArgumentException("value exceeds assessed master packed field");
        }
        digits = "0".repeat(digitCount - digits.length()) + digits;
        for (int index = 0; index < length; index++) {
            int high = digits.charAt(index * 2) - '0';
            int low = index == length - 1
                    ? (negative ? 0x0d : 0x0c)
                    : digits.charAt((index * 2) + 1) - '0';
            target[offset + index] = (byte) ((high << 4) | low);
        }
    }

    private static void writeZoned(
            byte[] target, int offset, int length, int value) {
        String digits = String.format("%0" + length + "d", Math.abs(value));
        writeString(target, offset, length, digits);
        int last = target[offset + length - 1] - '0';
        target[offset + length - 1] = (byte) ((value < 0 ? "}JKLMNOPQR" : "{ABCDEFGHI")
                .charAt(last));
    }

    private static void writeCharacter(byte[] target, int offset, String value) {
        if (value != null && !value.isEmpty()) {
            target[offset] = (byte) value.charAt(0);
        }
    }

    private static void writeString(
            byte[] target, int offset, int length, String value) {
        if (value == null) {
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(bytes, 0, target, offset, Math.min(length, bytes.length));
    }

    private record ParcelKey(Integer volumeNumber, Long parcelNumber) {
    }

    private static StageOutcome stage(ProcessResult result, int index, String name) {
        if (index < result.stages().size()) {
            return result.stages().get(index);
        }
        return new StageOutcome(name, "NOT_STARTED", null, 0, 0, 0, 0, false, false, 0);
    }

    private static OutputRecord output(ProcessResult result, String kind) {
        return result.outputs().stream()
                .filter(candidate -> kind.equals(candidate.kind()))
                .findFirst()
                .orElseGet(() -> new OutputRecord(kind, "application/octet-stream", 0));
    }

    private static Cataloged cataloged(String dsn, OutputRecord output) {
        return new Cataloged(
                dsn,
                1,
                output.recordCount(),
                output.recordData(),
                output.recordDataBase64());
    }

    private static long metric(StageOutcome stage, String name) {
        return stage.metrics().getOrDefault(name, 0L);
    }

    private static Step step(
            String name,
            String program,
            StageOutcome stage,
            List<String> messages,
            List<DatasetOp> datasetOps) {
        if ("COMPLETED".equals(stage.status())) {
            return new Step(
                    name, program, returnCode(stage), false, null, List.of(), List.of());
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

    private static String completionCode(StageOutcome stage) {
        return skipped(stage) ? null : completionCode(returnCode(stage));
    }

    private static String completionCode(int returnCode) {
        return "CC " + String.format("%04d", returnCode);
    }
}
