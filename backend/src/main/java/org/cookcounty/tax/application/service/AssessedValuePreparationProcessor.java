package org.cookcounty.tax.application.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Executes the three accepted assessed-value preparation stages against the live repositories. */
@Component
public class AssessedValuePreparationProcessor {
    private static final int REPORT_WIDTH = 133;
    // ASREA018/151 expose the source YY/MM/CC and YYYYMMCC report-date layouts,
    // where CC is the century rather than the day of month.

    private final AssessmentParcelRepository parcelRepository;
    private final AssessmentDetailRepository detailRepository;
    private final AssessedValueOverallClassKernel overallClassKernel;
    private final AssessedValueBucketingKernel bucketingKernel;
    private final AssessedValueType5Kernel type5Kernel;

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

    @Transactional
    public ProcessResult process(LocalDate businessDate, String businessTime, String processYear) {
        List<AssessmentParcel> parcels = parcelRepository.findAllInInputOrder();
        Map<ParcelKey, List<AssessmentDetail>> detailsByParcel = groupDetails(
                detailRepository.findAllInInputOrder());
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        List<OutputRecord> outputs = new ArrayList<>();
        List<StageOutcome> stages = new ArrayList<>();

        StageOutcome overall = runOverallClass(
                parcels, detailsByParcel, messages, businessDate);
        stages.add(overall);
        outputs.add(OutputRecord.text(
                "OVERALL_CLASS_REPORT", "text/plain", overall.reportRecords()));

        StageOutcome bucketing = runBucketing(
                parcels, detailsByParcel, messages, businessDate);
        stages.add(bucketing);
        outputs.add(OutputRecord.text(
                "VALUATION_BREAKDOWN_REPORT", "text/plain", bucketing.reportRecords()));
        if (overall.failed() || bucketing.failed()) {
            stages.add(StageOutcome.notStarted("TYPE5_CONVERSION"));
            int publishedMasterRows = bucketing.recordsWritten();
            outputs.add(new OutputRecord(
                    "ASSESSMENT_MASTER", "application/octet-stream", publishedMasterRows));
            return finish(
                    true, overall.failed() ? 16 : 0, true, outputs, messages, stages);
        }

        int reportYear = type5Kernel.reportYear(processYear);
        StageOutcome conversion = runType5(parcels, detailsByParcel, messages, reportYear);
        stages.add(conversion);
        outputs.add(new OutputRecord(
                "ASSESSMENT_MASTER", "application/octet-stream", conversion.recordsWritten()));
        if (conversion.reportRows() > 0) {
            outputs.add(OutputRecord.text(
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
        for (AssessmentParcel parcel : parcels) {
            if (previous != null && AssessedValueRuleSupport.compareOverallOrder(parcel, previous) < 0) {
                rejected++;
                messages.add(new AssessedValueRuleMessage(
                        "ERROR", "OVERALL_CLASS_OUT_OF_SEQUENCE", "asrea018-001",
                        "The overall-class input key is lower than the preceding key.",
                        AssessedValueRuleSupport.recordKey(parcel)));
            }
            int oldClass = AssessedValueRuleSupport.orZero(parcel.getOverallClass());
            AssessedValueOverallClassKernel.Result result = overallClassKernel.prepare(
                    parcel, detailsFor(parcel, detailsByParcel));
            messages.addAll(result.messages());
            reportRows += result.reportRows();
            if (result.reportRows() > 0) {
                report.add(new OverallReportRow(
                        AssessedValueRuleSupport.orZero(parcel.getVolumeNumber()),
                        AssessedValueRuleSupport.orZero(parcel.getParcelNumber()),
                        parcel.getTaxType(),
                        oldClass,
                        AssessedValueRuleSupport.orZero(parcel.getOverallClass())));
            }
            if (result.changed()) {
                updated++;
            }
            parcelRepository.save(parcel);
            previous = parcel;
        }
        return new StageOutcome(
                "OVERALL_CLASS", rejected == 0 ? "COMPLETED" : "FAILED", rejected == 0 ? 0 : 16,
                parcels.size(), parcels.size(), updated, rejected, !parcels.isEmpty(), rejected > 0,
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
        long farm = 0L;
        long homeowner = 0L;
        long nonHomeowner = 0L;
        Map<Integer, TownAccumulator> towns = new LinkedHashMap<>();
        AssessmentParcel previous = null;
        for (AssessmentParcel parcel : parcels) {
            read++;
            if (previous != null && AssessedValueRuleSupport.compareBucketingOrder(parcel, previous) < 0) {
                rejected = 1;
                messages.add(new AssessedValueRuleMessage(
                        "ERROR", "VALUATION_BUCKET_OUT_OF_SEQUENCE", "asrea151-001",
                        "The valuation-bucketing input key is lower than the preceding key.",
                        AssessedValueRuleSupport.recordKey(parcel)));
                break;
            }
            AssessedValueBucketingKernel.Result result = bucketingKernel.bucket(
                    parcel, detailsFor(parcel, detailsByParcel));
            messages.addAll(result.messages());
            farm += result.farmValue();
            homeowner += result.homeownerValue();
            nonHomeowner += result.nonHomeownerValue();
            int town = AssessedValueRuleSupport.orZero(parcel.getTaxCode()) / 1_000;
            towns.computeIfAbsent(town, ignored -> new TownAccumulator(townName(town)))
                    .add(result);
            if (result.changed()) {
                updated++;
            }
            parcelRepository.save(parcel);
            written++;
            previous = parcel;
        }
        List<String> report = breakdownReportRecords(
                businessDate, towns.values(), farm, homeowner, nonHomeowner, written);
        return new StageOutcome(
                "VALUATION_BUCKETING", rejected == 0 ? "COMPLETED" : "FAILED", 0,
                read, written, updated, rejected, written > 0, rejected > 0, report.size(),
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
        for (AssessmentParcel parcel : parcels) {
            List<AssessmentDetail> details = detailsFor(parcel, detailsByParcel);
            long parcelHits = details.stream()
                    .filter(detail -> "5".equals(detail.getDetailType()))
                    .count();
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
                for (AssessmentDetail detail : details) {
                    detailRepository.save(detail);
                }
            }
            if (result.parcelChanged() || result.detailsChanged()) {
                parcelRepository.save(parcel);
                updated++;
            }
        }
        return new StageOutcome(
                "TYPE5_CONVERSION", "COMPLETED", 0,
                parcels.size(), parcels.size(), updated, 0, !parcels.isEmpty(), false, reportRows,
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
        records.add(pad("   " + overallReportDate(businessDate)
                + "                                        OFFICE OF THE COOK COUNTY ASSESSOR"
                + "                                    PAGE     1"));
        records.add(pad("   ASREA018                                         "
                + "RECOMPUTED OVER-ALL CLASS REPORT"));
        records.add(pad("                               VOLUME          PROPERTY"
                + "        TAX         OLD         NEW"));
        records.add(pad("                                                NUMBER"
                + "         TYPE        CLASS       CLASS"));
        for (OverallReportRow row : rows) {
            String property = String.format("%014d", Math.abs(row.parcelNumber()));
            property = property.substring(0, 2) + "-" + property.substring(2, 4)
                    + "-" + property.substring(4, 7) + "-" + property.substring(7, 10)
                    + "-" + property.substring(10);
            records.add(pad(String.format(
                    "                                 %03d      %s     %s"
                            + "         %04d        %04d",
                    row.volume(), property, character(row.taxType()),
                    row.oldClass(), row.newClass())));
        }
        return List.copyOf(records);
    }

    private static List<String> breakdownReportRecords(
            LocalDate businessDate,
            Iterable<TownAccumulator> towns,
            long farm,
            long homeowner,
            long nonHomeowner,
            int recordsWritten) {
        List<String> records = new ArrayList<>();
        records.add(pad(""));
        records.add(pad("   " + breakdownReportDate(businessDate)
                + "                                        OFFICE OF THE COOK COUNTY ASSESSOR"));
        records.add(pad("   ASREA151                                       "
                + "ASSESSED VALUATION BREAKDOWN REPORT"));
        records.add(pad("                        FARM              HOMEOWNER"
                + "        NON-HOMEOWNER              TOTAL               TOTAL"));
        records.add(pad("   TOWN NAME         VALUATION            VALUATION"
                + "            VALUATION            VALUATION            PARCELS"));
        records.add(pad(""));
        for (TownAccumulator town : towns) {
            records.add(breakdownLine(
                    town.name, town.farm, town.homeowner, town.nonHomeowner, town.records));
        }
        records.add(breakdownLine(
                "", farm, homeowner, nonHomeowner, recordsWritten));
        return List.copyOf(records);
    }
    private static String overallReportDate(LocalDate businessDate) {
        String year = String.format("%04d", businessDate.getYear());
        return year.substring(2)
                + "/" + String.format("%02d", businessDate.getMonthValue())
                + "/" + year.substring(0, 2);
    }

    private static String breakdownReportDate(LocalDate businessDate) {
        String year = String.format("%04d", businessDate.getYear());
        return year + String.format("%02d", businessDate.getMonthValue())
                + year.substring(0, 2);
    }


    private static String breakdownLine(
            String town, long farm, long homeowner, long nonHomeowner, int parcels) {
        return pad("   " + String.format("%-13s", town)
                + reportNumber(farm) + "      "
                + reportNumber(homeowner) + "      "
                + reportNumber(nonHomeowner) + "      "
                + reportNumber(farm + homeowner + nonHomeowner) + "   "
                + reportNumber(parcels));
    }

    private static String reportNumber(long value) {
        String digits = Long.toString(Math.abs(value));
        StringBuilder grouped = new StringBuilder(digits.length() + digits.length() / 3);
        for (int index = 0; index < digits.length(); index++) {
            if (index > 0 && (digits.length() - index) % 3 == 0) {
                grouped.append(',');
            }
            grouped.append(digits.charAt(index));
        }
        if (value < 0) {
            grouped.insert(0, '-');
        }
        return " ".repeat(Math.max(0, 15 - grouped.length())) + grouped;
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

    private static String character(String value) {
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
                failed, returnCode, recordsRead, recordsWritten, recordsUpdated, recordsRejected,
                partialOutput, List.copyOf(outputs), List.copyOf(messages), List.copyOf(stages));
    }

    private static Map<ParcelKey, List<AssessmentDetail>> groupDetails(List<AssessmentDetail> details) {
        Map<ParcelKey, List<AssessmentDetail>> result = new HashMap<>();
        for (AssessmentDetail detail : details) {
            result.computeIfAbsent(
                            new ParcelKey(detail.getParcelVolumeNumber(), detail.getParcelNumber()),
                            unused -> new ArrayList<>())
                    .add(detail);
        }
        Comparator<AssessmentDetail> byOccurrence = Comparator.comparing(
                AssessmentDetail::getOccurrenceNumber,
                Comparator.nullsLast(Comparator.naturalOrder()));
        result.values().forEach(parcelDetails -> parcelDetails.sort(byOccurrence));
        return result;
    }

    private static List<AssessmentDetail> detailsFor(
            AssessmentParcel parcel,
            Map<ParcelKey, List<AssessmentDetail>> detailsByParcel) {
        return detailsByParcel.getOrDefault(
                new ParcelKey(parcel.getVolumeNumber(), parcel.getParcelNumber()), List.of());
    }

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

        public ProcessResult {
            outputs = List.copyOf(outputs);
            messages = List.copyOf(messages);
            stages = List.copyOf(stages);
        }
    }

    public record OutputRecord(
            String kind,
            String mediaType,
            int recordCount,
            List<String> recordData,
            List<String> recordDataBase64) {

        public OutputRecord(String kind, String mediaType, int recordCount) {
            this(kind, mediaType, recordCount, List.of(), List.of());
        }

        public OutputRecord {
            recordData = List.copyOf(recordData);
            recordDataBase64 = List.copyOf(recordDataBase64);
        }

        static OutputRecord text(String kind, String mediaType, List<String> records) {
            List<String> encoded = records.stream()
                    .map(record -> Base64.getEncoder().encodeToString(
                            record.getBytes(StandardCharsets.UTF_8)))
                    .toList();
            return new OutputRecord(kind, mediaType, records.size(), records, encoded);
        }

        static OutputRecord binary(String kind, List<byte[]> records) {
            List<String> display = records.stream()
                    .map(record -> new String(record, StandardCharsets.UTF_8))
                    .toList();
            List<String> encoded = records.stream()
                    .map(record -> Base64.getEncoder().encodeToString(record))
                    .toList();
            return new OutputRecord(
                    kind, "application/octet-stream", records.size(), display, encoded);
        }
    }

    public record StageOutcome(
            String stage,
            String status,
            Integer returnCode,
            int recordsRead,
            int recordsWritten,
            int recordsUpdated,
            int recordsRejected,
            boolean outputPublished,
            boolean partialOutput,
            int reportRows,
            Map<String, Long> metrics,
            List<String> reportRecords) {

        public StageOutcome(
                String stage,
                String status,
                Integer returnCode,
                int recordsRead,
                int recordsWritten,
                int recordsUpdated,
                int recordsRejected,
                boolean outputPublished,
                boolean partialOutput,
                int reportRows) {
            this(
                    stage, status, returnCode, recordsRead, recordsWritten, recordsUpdated,
                    recordsRejected, outputPublished, partialOutput, reportRows,
                    Map.of(), List.of());
        }

        public StageOutcome {
            metrics = Map.copyOf(metrics);
            reportRecords = List.copyOf(reportRecords);
        }

        static StageOutcome notStarted(String stage) {
            return new StageOutcome(
                    stage, "NOT_STARTED", null, 0, 0, 0, 0, false, false, 0,
                    Map.of(), List.of());
        }

        boolean failed() {
            return "FAILED".equals(status);
        }
    }

    private record OverallReportRow(
            int volume, long parcelNumber, String taxType, int oldClass, int newClass) {}

    private static final class TownAccumulator {
        private final String name;
        private long farm;
        private long homeowner;
        private long nonHomeowner;
        private int records;

        private TownAccumulator(String name) {
            this.name = name;
        }

        private void add(AssessedValueBucketingKernel.Result result) {
            farm += result.farmValue();
            homeowner += result.homeownerValue();
            nonHomeowner += result.nonHomeownerValue();
            records++;
        }
    }

    private record ParcelKey(Integer volumeNumber, Long parcelNumber) {}
}
