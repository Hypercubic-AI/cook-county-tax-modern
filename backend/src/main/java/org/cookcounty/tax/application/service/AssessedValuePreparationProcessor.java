package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Runs overall-class selection, valuation bucketing, and Type-5 conversion in source order.
///
/// One transaction owns repository changes for the complete invocation. The processor preserves the
/// accepted partial-output behavior for ordering failures. It does not resume an interrupted
/// invocation from an intermediate stage.
@Component
public class AssessedValuePreparationProcessor {
    private static final int REPORT_WIDTH = 133;
    // The overall-class and valuation-bucketing reports retain YY/MM/CC and YYYYMMCC date layouts,
    // where CC is the century rather than the day of month.

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final AssessedValueOverallClassKernel overallClassKernel;
    private final AssessedValueBucketingKernel bucketingKernel;
    private final AssessedValueType5Kernel type5Kernel;

    /// Creates the ordered processor from its repositories and parcel-local policies.
    ///
    /// @param parcelRepository parcel persistence in source ingestion order
    /// @param detailRepository detail persistence in source occurrence order
    /// @param overallClassKernel overall-class decision policy
    /// @param bucketingKernel valuation-bucket policy
    /// @param type5Kernel Type-5 conversion and revaluation policy
    public AssessedValuePreparationProcessor(
            AssessmentParcelRepository parcelRepository,
            AssessmentDetailRepository detailRepository,
            AssessedValueOverallClassKernel overallClassKernel,
            AssessedValueBucketingKernel bucketingKernel,
            AssessedValueType5Kernel type5Kernel) {
        this.parcelRepository = parcelRepository;
        this.detailRepository = detailRepository;
        this.overallClassKernel = overallClassKernel;
        this.bucketingKernel = bucketingKernel;
        this.type5Kernel = type5Kernel;
    }

    /// Runs all three stages and returns their typed outputs and recoverable observations.
    ///
    /// @param businessDate report date used by the two source-compatible report layouts
    /// @param businessTime accepted run control retained for the external contract
    /// @param processYear two-digit year used by Type-5 report conversion
    /// @return immutable stage outcomes and output descriptors
    @Transactional
    public ProcessResult process(LocalDate businessDate, String businessTime, String processYear) {
        List<AssessmentParcel> parcels = new ArrayList<>(parcelRepository.findAllInInputOrder());
        Map<ParcelKey, List<AssessmentDetail>> detailsByParcel =
                groupDetails(detailRepository.findAllInInputOrder());
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        List<OutputRecord> outputs = new ArrayList<>();
        List<StageOutcome> stages = new ArrayList<>();

        StageOutcome overall = runOverallClass(parcels, detailsByParcel, messages, businessDate);
        stages.add(overall);
        outputs.add(
                OutputRecord.text("OVERALL_CLASS_REPORT", "text/plain", overall.reportRecords()));

        StageOutcome bucketing = runBucketing(parcels, detailsByParcel, messages, businessDate);
        stages.add(bucketing);
        outputs.add(
                OutputRecord.text(
                        "VALUATION_BREAKDOWN_REPORT", "text/plain", bucketing.reportRecords()));
        if (overall.failed() || bucketing.failed()) {
            stages.add(StageOutcome.notStarted("TYPE5_CONVERSION"));
            int publishedMasterRows = bucketing.recordsWritten();
            outputs.add(
                    new OutputRecord(
                            "ASSESSMENT_MASTER", "application/octet-stream", publishedMasterRows));
            return finish(true, overall.failed() ? 16 : 0, true, outputs, messages, stages);
        }

        int reportYear = type5Kernel.reportYear(processYear);
        StageOutcome conversion = runType5(parcels, detailsByParcel, messages, reportYear);
        stages.add(conversion);
        outputs.add(
                new OutputRecord(
                        "ASSESSMENT_MASTER",
                        "application/octet-stream",
                        conversion.recordsWritten()));
        if (conversion.reportRows() > 0) {
            outputs.add(
                    OutputRecord.text(
                            "TYPE5_ERROR_REPORT", "text/plain", conversion.reportRecords()));
        }
        return finish(false, 0, false, outputs, messages, stages);
    }

    private StageOutcome runOverallClass(
            List<AssessmentParcel> parcels,
            Map<ParcelKey, List<AssessmentDetail>> detailsByParcel,
            List<AssessedValueRuleMessage> messages,
            LocalDate businessDate) {
        int updated = 0;
        int rejected = 0;
        int reportRows = 0;
        List<OverallReportRow> report = new ArrayList<>();
        AssessmentParcel previous = null;
        for (int index = 0; index < parcels.size(); index++) {
            AssessmentParcel parcel = parcels.get(index);
            if (previous != null
                    && AssessedValueRuleSupport.compareOverallOrder(parcel, previous) < 0) {
                rejected++;
                messages.add(
                        new AssessedValueRuleMessage(
                                "ERROR",
                                "OVERALL_CLASS_OUT_OF_SEQUENCE",
                                "asrea018-001",
                                "The overall-class input key is lower than the preceding key.",
                                AssessedValueRuleSupport.recordKey(parcel)));
            }
            int oldClass = AssessedValueRuleSupport.orZero(parcel.overallClass());
            AssessedValueOverallClassKernel.Result result =
                    overallClassKernel.prepare(parcel, detailsFor(parcel, detailsByParcel));
            AssessmentParcel prepared = parcelRepository.save(result.parcel());
            parcels.set(index, prepared);
            messages.addAll(result.messages());
            reportRows += result.reportRows();
            if (result.reportRows() > 0) {
                report.add(
                        new OverallReportRow(
                                Math.toIntExact(
                                        AssessedValueRuleSupport.sourceInteger(
                                                prepared.volumeNumber())),
                                AssessedValueRuleSupport.sourceInteger(prepared.parcelNumber()),
                                prepared.taxType(),
                                oldClass,
                                AssessedValueRuleSupport.orZero(prepared.overallClass())));
            }
            if (result.changed()) {
                updated++;
            }
            previous = prepared;
        }
        return new StageOutcome(
                "OVERALL_CLASS",
                rejected == 0 ? "COMPLETED" : "FAILED",
                rejected == 0 ? 0 : 16,
                parcels.size(),
                parcels.size(),
                updated,
                rejected,
                !parcels.isEmpty(),
                rejected > 0,
                reportRows,
                Map.of("reported", (long) reportRows),
                overallReportRecords(businessDate, report));
    }

    private StageOutcome runBucketing(
            List<AssessmentParcel> parcels,
            Map<ParcelKey, List<AssessmentDetail>> detailsByParcel,
            List<AssessedValueRuleMessage> messages,
            LocalDate businessDate) {
        int read = 0;
        int written = 0;
        int updated = 0;
        int rejected = 0;
        BigDecimal farm = BigDecimal.ZERO;
        BigDecimal homeowner = BigDecimal.ZERO;
        BigDecimal nonHomeowner = BigDecimal.ZERO;
        Map<Integer, TownAccumulator> towns = new LinkedHashMap<>();
        AssessmentParcel previous = null;
        for (int index = 0; index < parcels.size(); index++) {
            AssessmentParcel parcel = parcels.get(index);
            read++;
            if (previous != null
                    && AssessedValueRuleSupport.compareBucketingOrder(parcel, previous) < 0) {
                rejected = 1;
                messages.add(
                        new AssessedValueRuleMessage(
                                "ERROR",
                                "VALUATION_BUCKET_OUT_OF_SEQUENCE",
                                "asrea151-001",
                                "The valuation-bucketing input key is lower than the preceding"
                                        + " key.",
                                AssessedValueRuleSupport.recordKey(parcel)));
                break;
            }
            AssessedValueBucketingKernel.Result result =
                    bucketingKernel.bucket(parcel, detailsFor(parcel, detailsByParcel));
            AssessmentParcel bucketed = parcelRepository.save(result.parcel());
            parcels.set(index, bucketed);
            messages.addAll(result.messages());
            farm = farm.add(result.farmValue());
            homeowner = homeowner.add(result.homeownerValue());
            nonHomeowner = nonHomeowner.add(result.nonHomeownerValue());
            int town = AssessedValueRuleSupport.township(bucketed);
            towns.computeIfAbsent(town, ignored -> new TownAccumulator(townName(town))).add(result);
            if (result.changed()) {
                updated++;
            }
            written++;
            previous = bucketed;
        }
        List<String> report =
                breakdownReportRecords(
                        businessDate, towns.values(), farm, homeowner, nonHomeowner, written);
        return new StageOutcome(
                "VALUATION_BUCKETING",
                rejected == 0 ? "COMPLETED" : "FAILED",
                0,
                read,
                written,
                updated,
                rejected,
                written > 0,
                rejected > 0,
                report.size(),
                Map.of(
                        "farm", farm,
                        "homeowner", homeowner,
                        "nonHomeowner", nonHomeowner),
                report);
    }

    private StageOutcome runType5(
            List<AssessmentParcel> parcels,
            Map<ParcelKey, List<AssessmentDetail>> detailsByParcel,
            List<AssessedValueRuleMessage> messages,
            int reportYear) {
        int updated = 0;
        int reportRows = 0;
        long type5Hits = 0L;
        long zeroType5 = 0L;
        List<String> report = new ArrayList<>();
        for (int parcelIndex = 0; parcelIndex < parcels.size(); parcelIndex++) {
            AssessmentParcel parcel = parcels.get(parcelIndex);
            List<AssessmentDetail> details = detailsFor(parcel, detailsByParcel);
            long parcelHits =
                    details.stream().filter(detail -> "5".equals(detail.detailType())).count();
            type5Hits += parcelHits;
            AssessedValueType5Kernel.Result result =
                    type5Kernel.convert(parcel, details, reportYear);
            messages.addAll(result.messages());
            reportRows += result.conversionErrorRows();
            zeroType5 += result.conversionErrorRows();
            result.messages().stream()
                    .filter(message -> "asrea178-003".equals(message.ruleId()))
                    .map(message -> pad(message.message()))
                    .forEach(report::add);
            if (result.detailsChanged()) {
                List<AssessmentDetail> savedDetails = new ArrayList<>(result.details().size());
                for (AssessmentDetail detail : result.details()) {
                    savedDetails.add(detailRepository.save(detail));
                }
                detailsByParcel.put(
                        new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber()),
                        List.copyOf(savedDetails));
            }
            if (result.parcelChanged() || result.detailsChanged()) {
                parcels.set(parcelIndex, parcelRepository.save(result.parcel()));
                updated++;
            }
        }
        return new StageOutcome(
                "TYPE5_CONVERSION",
                "COMPLETED",
                0,
                parcels.size(),
                parcels.size(),
                updated,
                0,
                !parcels.isEmpty(),
                false,
                reportRows,
                Map.of(
                        "type5Hits", type5Hits,
                        "zeroType5", zeroType5,
                        "convertedType5", type5Hits),
                List.copyOf(report));
    }

    private static List<String> overallReportRecords(
            LocalDate businessDate, List<OverallReportRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<String> records = new ArrayList<>(rows.size() + 4);
        records.add(
                pad(
                        "   "
                                + overallReportDate(businessDate)
                                + "                                        OFFICE OF THE COOK"
                                + " COUNTY ASSESSOR                                    PAGE    "
                                + " 1"));
        records.add(
                pad(
                        "   ASREA018                                         "
                                + "RECOMPUTED OVER-ALL CLASS REPORT"));
        records.add(
                pad(
                        "                               VOLUME          PROPERTY"
                                + "        TAX         OLD         NEW"));
        records.add(
                pad(
                        "                                                NUMBER"
                                + "         TYPE        CLASS       CLASS"));
        for (OverallReportRow row : rows) {
            String property = String.format("%014d", Math.abs(row.parcelNumber()));
            property =
                    property.substring(0, 2)
                            + "-"
                            + property.substring(2, 4)
                            + "-"
                            + property.substring(4, 7)
                            + "-"
                            + property.substring(7, 10)
                            + "-"
                            + property.substring(10);
            records.add(
                    pad(
                            String.format(
                                    "                                 %03d      %s     %s"
                                            + "         %04d        %04d",
                                    row.volume(),
                                    property,
                                    character(row.taxType()),
                                    row.oldClass(),
                                    row.newClass())));
        }
        return List.copyOf(records);
    }

    private static List<String> breakdownReportRecords(
            LocalDate businessDate,
            Iterable<TownAccumulator> towns,
            BigDecimal farm,
            BigDecimal homeowner,
            BigDecimal nonHomeowner,
            int recordsWritten) {
        List<String> records = new ArrayList<>();
        records.add(pad(""));
        records.add(
                pad(
                        "   "
                                + breakdownReportDate(businessDate)
                                + "                                        OFFICE OF THE COOK"
                                + " COUNTY ASSESSOR"));
        records.add(
                pad(
                        "   ASREA151                                       "
                                + "ASSESSED VALUATION BREAKDOWN REPORT"));
        records.add(
                pad(
                        "                        FARM              HOMEOWNER"
                                + "        NON-HOMEOWNER              TOTAL               TOTAL"));
        records.add(
                pad(
                        "   TOWN NAME         VALUATION            VALUATION"
                                + "            VALUATION            VALUATION            PARCELS"));
        records.add(pad(""));
        for (TownAccumulator town : towns) {
            records.add(
                    breakdownLine(
                            town.name, town.farm, town.homeowner, town.nonHomeowner, town.records));
        }
        records.add(breakdownLine("", farm, homeowner, nonHomeowner, recordsWritten));
        return List.copyOf(records);
    }

    private static String overallReportDate(LocalDate businessDate) {
        String year = String.format("%04d", businessDate.getYear());
        return year.substring(2)
                + "/"
                + String.format("%02d", businessDate.getMonthValue())
                + "/"
                + year.substring(0, 2);
    }

    private static String breakdownReportDate(LocalDate businessDate) {
        String year = String.format("%04d", businessDate.getYear());
        return year + String.format("%02d", businessDate.getMonthValue()) + year.substring(0, 2);
    }

    private static String breakdownLine(
            String town,
            BigDecimal farm,
            BigDecimal homeowner,
            BigDecimal nonHomeowner,
            int parcels) {
        return pad(
                "   "
                        + String.format("%-13s", town)
                        + reportNumber(farm)
                        + "      "
                        + reportNumber(homeowner)
                        + "      "
                        + reportNumber(nonHomeowner)
                        + "      "
                        + reportNumber(farm.add(homeowner).add(nonHomeowner))
                        + "   "
                        + reportNumber(BigDecimal.valueOf(parcels)));
    }

    /// Renders an exact whole-unit amount with the report's comma grouping and minimum field width.
    private static String reportNumber(BigDecimal value) {
        return String.format(java.util.Locale.US, "%,15d", value.toBigIntegerExact());
    }

    private static String townName(int town) {
        return switch (town) {
            case 10 -> "BARRINGTON";
            case 11 -> "BERWYN";
            case 12 -> "BLOOM";
            case 13 -> "BREMEN";
            case 14 -> "CALUMET";
            case 15 -> "CICERO";
            case 16 -> "ELK GROVE";
            case 17 -> "EVANSTON";
            case 18 -> "HANOVER";
            case 19 -> "LEMONT";
            case 20 -> "LEYDEN";
            case 21 -> "LYONS";
            case 22 -> "MAINE";
            case 23 -> "NEW TRIER";
            case 24 -> "NILES";
            case 25 -> "NORTHFIELD";
            case 26 -> "NORWOOD PARK";
            case 27 -> "OAK PARK";
            case 28 -> "ORLAND";
            case 29 -> "PALATINE";
            case 30 -> "PALOS";
            case 31 -> "PROVISO";
            case 32 -> "RICH";
            case 33 -> "RIVER FOREST";
            case 34 -> "RIVERSIDE";
            case 35 -> "SCHAUMBURG";
            case 36 -> "STICKNEY";
            case 37 -> "THORNTON";
            case 38 -> "WHEELING";
            case 39 -> "WORTH";
            case 70 -> "HYDE PARK";
            case 71 -> "JEFFERSON";
            case 72 -> "LAKE";
            case 73 -> "LAKE VIEW";
            case 74 -> "NORTH";
            case 75 -> "ROGERS PARK";
            case 76 -> "SOUTH";
            case 77 -> "WEST";
            default -> "TOWN " + town;
        };
    }

    private static String character(@Nullable String value) {
        return value == null || value.isEmpty() ? " " : value.substring(0, 1);
    }

    private static String pad(String value) {
        if (value.length() > REPORT_WIDTH) {
            throw new IllegalArgumentException("assessed report record exceeds 133 characters");
        }
        return value + " ".repeat(REPORT_WIDTH - value.length());
    }

    private static ProcessResult finish(
            boolean failed,
            int returnCode,
            boolean partialOutput,
            List<OutputRecord> outputs,
            List<AssessedValueRuleMessage> messages,
            List<StageOutcome> stages) {
        int recordsRead = stages.stream().mapToInt(StageOutcome::recordsRead).sum();
        int recordsWritten = stages.stream().mapToInt(StageOutcome::recordsWritten).sum();
        int recordsUpdated = stages.stream().mapToInt(StageOutcome::recordsUpdated).sum();
        int recordsRejected = stages.stream().mapToInt(StageOutcome::recordsRejected).sum();
        return new ProcessResult(
                failed,
                returnCode,
                recordsRead,
                recordsWritten,
                recordsUpdated,
                recordsRejected,
                partialOutput,
                List.copyOf(outputs),
                List.copyOf(messages),
                List.copyOf(stages));
    }

    private static Map<ParcelKey, List<AssessmentDetail>> groupDetails(
            List<AssessmentDetail> details) {
        Map<ParcelKey, List<AssessmentDetail>> result = new HashMap<>();
        for (AssessmentDetail detail : details) {
            result.computeIfAbsent(
                            new ParcelKey(detail.parcelVolumeNumber(), detail.parcelNumber()),
                            unused -> new ArrayList<>())
                    .add(detail);
        }
        Comparator<AssessmentDetail> byOccurrence =
                Comparator.comparing(AssessmentDetail::occurrenceNumber);
        result.values().forEach(parcelDetails -> parcelDetails.sort(byOccurrence));
        return result;
    }

    private static List<AssessmentDetail> detailsFor(
            AssessmentParcel parcel, Map<ParcelKey, List<AssessmentDetail>> detailsByParcel) {
        return detailsByParcel.getOrDefault(
                new ParcelKey(parcel.volumeNumber(), parcel.parcelNumber()), List.of());
    }

    /// Complete immutable outcome of one assessed-value processor invocation.
    ///
    /// @param failed whether any stage produced a terminal semantic failure
    /// @param returnCode overall batch completion code
    /// @param recordsRead sum of records read by started stages
    /// @param recordsWritten sum of records written by started stages
    /// @param recordsUpdated sum of records whose business state changed
    /// @param recordsRejected sum of recoverable rejected records
    /// @param partialOutput whether an ordering failure limited publication
    /// @param outputs ordered output descriptors and optional record evidence
    /// @param messages ordered recoverable rule observations
    /// @param stages ordered stage outcomes
    public record ProcessResult(
            boolean failed,
            int returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            boolean partialOutput,
            List<OutputRecord> outputs,
            List<AssessedValueRuleMessage> messages,
            List<StageOutcome> stages) {

        /// Defensively copies all returned collections.
        public ProcessResult {
            outputs = List.copyOf(outputs);
            messages = List.copyOf(messages);
            stages = List.copyOf(stages);
        }
    }

    /// One processor artifact with optional text or Base64 record evidence.
    ///
    /// @param kind stable business role of the artifact
    /// @param mediaType media type required to read its records
    /// @param recordCount number of published records
    /// @param recordData text display records
    /// @param recordDataBase64 Base64 records used for binary-safe comparator projection
    public record OutputRecord(
            String kind,
            String mediaType,
            int recordCount,
            List<String> recordData,
            List<String> recordDataBase64) {

        /// Creates an output descriptor when record bytes are read from persisted state later.
        ///
        /// @param kind stable business role of the artifact
        /// @param mediaType artifact media type
        /// @param recordCount number of published records
        public OutputRecord(String kind, String mediaType, int recordCount) {
            this(kind, mediaType, recordCount, List.of(), List.of());
        }

        /// Defensively copies both record evidence collections.
        public OutputRecord {
            recordData = List.copyOf(recordData);
            recordDataBase64 = List.copyOf(recordDataBase64);
        }

        static OutputRecord text(String kind, String mediaType, List<String> records) {
            List<String> encoded =
                    records.stream()
                            .map(
                                    record ->
                                            Base64.getEncoder()
                                                    .encodeToString(
                                                            record.getBytes(
                                                                    StandardCharsets.UTF_8)))
                            .toList();
            return new OutputRecord(kind, mediaType, records.size(), records, encoded);
        }

        static OutputRecord binary(String kind, List<byte[]> records) {
            List<String> display =
                    records.stream()
                            .map(record -> new String(record, StandardCharsets.UTF_8))
                            .toList();
            List<String> encoded =
                    records.stream()
                            .map(record -> Base64.getEncoder().encodeToString(record))
                            .toList();
            return new OutputRecord(
                    kind, "application/octet-stream", records.size(), display, encoded);
        }
    }

    /// Observable outcome of one ordered assessed-value stage.
    ///
    /// @param stage stable stage name
    /// @param status `COMPLETED`, `FAILED`, or `NOT_STARTED`
    /// @param returnCode stage completion code, or `null` before the stage starts
    /// @param recordsRead source records inspected
    /// @param recordsWritten records published to the handoff
    /// @param recordsUpdated records whose business state changed
    /// @param recordsRejected records that caused recoverable failure
    /// @param outputPublished whether the stage published at least one output record
    /// @param partialOutput whether the stage limited or continued output after failure
    /// @param reportRows number of report records
    /// @param metrics exact whole-dollar `BigDecimal` totals or integral stage counts
    /// @param reportRecords fixed-width report lines
    public record StageOutcome(
            String stage,
            String status,
            @Nullable Integer returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            boolean outputPublished,
            boolean partialOutput,
            int reportRows,
            Map<String, Number> metrics,
            List<String> reportRecords) {

        /// Creates a stage outcome with no named metrics or report record evidence.
        public StageOutcome(
                String stage,
                String status,
                @Nullable Integer returnCode,
                int recordsRead,
                int recordsWritten,
                int recordsUpdated,
                int recordsRejected,
                boolean outputPublished,
                boolean partialOutput,
                int reportRows) {
            this(
                    stage,
                    status,
                    returnCode,
                    recordsRead,
                    recordsWritten,
                    recordsUpdated,
                    recordsRejected,
                    outputPublished,
                    partialOutput,
                    reportRows,
                    Map.of(),
                    List.of());
        }

        /// Defensively copies metrics and fixed-width report records.
        public StageOutcome {
            metrics = Map.copyOf(metrics);
            reportRecords = List.copyOf(reportRecords);
        }

        static StageOutcome notStarted(String stage) {
            return new StageOutcome(
                    stage, "NOT_STARTED", null, 0, 0, 0, 0, false, false, 0, Map.of(), List.of());
        }

        boolean failed() {
            return "FAILED".equals(status);
        }
    }

    private record OverallReportRow(
            int volume, long parcelNumber, String taxType, int oldClass, int newClass) {}

    private static final class TownAccumulator {
        private final String name;
        private BigDecimal farm = BigDecimal.ZERO;
        private BigDecimal homeowner = BigDecimal.ZERO;
        private BigDecimal nonHomeowner = BigDecimal.ZERO;
        private int records;

        private TownAccumulator(String name) {
            this.name = name;
        }

        private void add(AssessedValueBucketingKernel.Result result) {
            farm = farm.add(result.farmValue());
            homeowner = homeowner.add(result.homeownerValue());
            nonHomeowner = nonHomeowner.add(result.nonHomeownerValue());
            records++;
        }
    }

    private record ParcelKey(String volumeNumber, String parcelNumber) {}
}
