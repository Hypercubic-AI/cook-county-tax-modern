package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/** Rule kernel for the CLRTM751/752/753/755 tax-rate input preparation pipeline. */
public final class TaxRateInputKernel {

    public static final int ERROR_RETURN_CODE = 16;
    public static final int AGENCY_SLOT_LIMIT = 40;
    public static final int SEGMENT_LIMIT = 50;
    public static final long MAX_FOURTEEN_DIGIT_VALUE = 99_999_999_999_999L;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    public Result process(Input input, FrozenAgencyStore frozenAgencyStore) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(frozenAgencyStore, "frozenAgencyStore");
        State state = new State();

        stampDivisions(input.equalizedValues(), input.divisions(), state);
        if (state.returnCode == 0) {
            attachAgencies(state.dividedValues, input.taxCodes(), state);
        }
        if (state.returnCode == 0) {
            compareAgencies(input.priorAgencyAssessments(), state.agencyAssessments, state);
        }
        if (state.returnCode == 0) {
            postFrozenAgencyAdjustments(
                    input.priorAgencyAssessments(), state.agencyAssessments, state.comparisons,
                    frozenAgencyStore, state);
        }
        return state.result();
    }

    /** Runs the CLRTM755 stage against an already prepared comparison handoff. */
    public Result postPrepared(
            List<AgencyAssessment> prior,
            List<AgencyAssessment> current,
            List<Comparison> comparisons,
            FrozenAgencyStore frozenAgencyStore) {
        Objects.requireNonNull(prior, "prior");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(comparisons, "comparisons");
        Objects.requireNonNull(frozenAgencyStore, "frozenAgencyStore");
        State state = new State();
        state.comparisons.addAll(comparisons);
        postFrozenAgencyAdjustments(prior, current, comparisons, frozenAgencyStore, state);
        return state.result();
    }

    private void stampDivisions(
            List<EqualizedValue> equalizedValues,
            List<Division> divisions,
            State state) {
        Map<ParcelKey, Division> divisionByParcel = new java.util.HashMap<>();
        ParcelKey previousDivisionKey = null;
        for (Division division : divisions) {
            state.divisionRecordsRead++;
            ParcelKey key = new ParcelKey(division.volume(), division.property());
            if (previousDivisionKey != null && key.compareTo(previousDivisionKey) <= 0) {
                state.fail("DIVISION_INPUT_OUT_OF_SEQUENCE",
                        "Division input must be strictly increasing by volume and property.",
                        "clrtm751-001");
                return;
            }
            if (!fitsFourteenDigits(division.divisionNumber())) {
                state.fail("DIVISION_NUMBER_TOO_WIDE",
                        "Division number cannot be represented in the 14-digit handoff field.",
                        "clrtm751-003");
                return;
            }
            divisionByParcel.put(key, division);
            previousDivisionKey = key;
        }

        ParcelKey previousEqualizedKey = null;
        for (EqualizedValue value : equalizedValues) {
            state.equalizedValueRecordsRead++;
            ParcelKey key = new ParcelKey(value.volume(), value.property());
            if (previousEqualizedKey != null && key.compareTo(previousEqualizedKey) < 0) {
                state.fail("EQUALIZED_VALUE_INPUT_OUT_OF_SEQUENCE",
                        "Equalized-value input must be nondecreasing by volume and property.",
                        "clrtm751-001");
                return;
            }
            Division division = divisionByParcel.get(key);
            long divisionNumber = division == null ? value.property() : division.divisionNumber();
            if (!fitsFourteenDigits(divisionNumber)) {
                state.fail("DIVISION_NUMBER_TOO_WIDE",
                        "Property number cannot be preserved in the 14-digit division field.",
                        "clrtm751-003");
                return;
            }
            state.dividedValues.add(new DividedValue(
                    value.town(), value.volume(), value.property(), value.taxType(), value.taxCode(),
                    divisionNumber, value.assessedValue(), value.equalizedValue()));
            state.outputRecordsWritten++;
            state.recordsStamped++;
            previousEqualizedKey = key;
        }
    }

    private void attachAgencies(
            List<DividedValue> dividedValues,
            Map<String, TaxCode> taxCodes,
            State state) {
        DividedKey previous = null;
        for (DividedValue value : dividedValues) {
            state.assessmentRecordsRead++;
            DividedKey key = new DividedKey(
                    value.town(), value.volume(), value.property(), value.taxType());
            if (previous != null && key.compareTo(previous) <= 0) {
                state.fail("DIVIDED_VALUE_INPUT_OUT_OF_SEQUENCE",
                        "Divided equalized-value input must be strictly increasing by town, volume, property, and tax type.",
                        "clrtm752-001");
                return;
            }
            TaxCode taxCode = taxCodes.get(value.taxCode());
            if (taxCode == null) {
                state.assessmentRecordsUnmatched++;
                state.unmatchedDividedValues.add(value);
                state.messages.add(new Message(
                        "WARNING", "UNMATCHED_TAX_CODE",
                        "No tax-code master match for volume " + value.volume()
                                + ", property " + value.property()
                                + ", tax type " + value.taxType()
                                + ", tax code " + value.taxCode() + ".",
                        "clrtm752-003"));
            } else {
                state.agencyAssessments.add(new AgencyAssessment(
                        value.divisionNumber(), value.town(), value.volume(), value.property(),
                        value.taxType(), value.taxCode(), value.assessedValue(), value.equalizedValue(),
                        taxCode.rate(), copyAgencySlots(taxCode.agencies())));
                state.assessmentRecordsWritten++;
            }
            previous = key;
        }
    }

    private void compareAgencies(
            List<AgencyAssessment> prior,
            List<AgencyAssessment> current,
            State state) {
        int priorIndex = 0;
        int currentIndex = 0;
        long lastPriorDivision = Long.MIN_VALUE;
        long lastCurrentDivision = Long.MIN_VALUE;

        while (priorIndex < prior.size() && currentIndex < current.size() && state.returnCode == 0) {
            AgencyAssessment priorRecord = prior.get(priorIndex);
            AgencyAssessment currentRecord = current.get(currentIndex);
            if (!fitsFourteenDigits(priorRecord.divisionNumber())
                    || !fitsFourteenDigits(currentRecord.divisionNumber())) {
                state.fail("DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (priorRecord.divisionNumber() < currentRecord.divisionNumber()) {
                state.priorRecordsRead++;
                if (priorRecord.divisionNumber() < lastPriorDivision) {
                    state.fail("PRIOR_AGENCY_INPUT_OUT_OF_SEQUENCE",
                            "Prior-year agency input must be nondecreasing by division.",
                            "clrtm753-001");
                    return;
                }
                lastPriorDivision = priorRecord.divisionNumber();
                state.priorOnlyDivisions++;
                state.messages.add(unmatchedDivision(priorRecord.divisionNumber(), true));
                priorIndex++;
                continue;
            }
            if (currentRecord.divisionNumber() < priorRecord.divisionNumber()) {
                state.currentRecordsRead++;
                if (currentRecord.divisionNumber() < lastCurrentDivision) {
                    state.fail("CURRENT_AGENCY_INPUT_OUT_OF_SEQUENCE",
                            "Current-year agency input must be nondecreasing by division.",
                            "clrtm753-001");
                    return;
                }
                lastCurrentDivision = currentRecord.divisionNumber();
                state.currentOnlyDivisions++;
                state.messages.add(unmatchedDivision(currentRecord.divisionNumber(), false));
                currentIndex++;
                continue;
            }

            long division = priorRecord.divisionNumber();
            Group priorGroup = loadGroup(prior, priorIndex, division, lastPriorDivision, true, state);
            priorIndex = priorGroup.nextIndex();
            lastPriorDivision = priorGroup.lastDivision();
            Group currentGroup = loadGroup(current, currentIndex, division, lastCurrentDivision, false, state);
            currentIndex = currentGroup.nextIndex();
            lastCurrentDivision = currentGroup.lastDivision();

            List<Segment> segments = differences(priorGroup.agencies(), currentGroup.agencies());
            if (segments.size() > SEGMENT_LIMIT) {
                state.fail("ANNEX_DISCONNECT_CAPACITY_EXCEEDED",
                        "A matched division produced more than 50 annex/disconnect segments.",
                        "clrtm753-006");
                return;
            }
            state.comparisons.add(new Comparison(
                    division, priorGroup.totalEqualizedValue(), currentGroup.totalEqualizedValue(),
                    ONE_HUNDRED, segments));
            state.comparisonRecordsWritten++;
            state.disconnectSegments += (int) segments.stream().filter(segment -> segment.type() == 'D').count();
            state.annexSegments += (int) segments.stream().filter(segment -> segment.type() == 'A').count();
            // A descending record discovered while loading a matched group does not suppress this output.
            if (priorGroup.orderingError() || currentGroup.orderingError()) {
                state.fail("AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Prior- and current-year agency inputs must be nondecreasing by division.",
                        "clrtm753-001");
            }
        }

        while (priorIndex < prior.size() && state.returnCode == 0) {
            AgencyAssessment record = prior.get(priorIndex++);
            state.priorRecordsRead++;
            if (!fitsFourteenDigits(record.divisionNumber())) {
                state.fail("DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (record.divisionNumber() < lastPriorDivision) {
                state.fail("PRIOR_AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Prior-year agency input must be nondecreasing by division.", "clrtm753-001");
                return;
            }
            lastPriorDivision = record.divisionNumber();
            state.priorOnlyDivisions++;
            state.messages.add(unmatchedDivision(record.divisionNumber(), true));
        }
        while (currentIndex < current.size() && state.returnCode == 0) {
            AgencyAssessment record = current.get(currentIndex++);
            state.currentRecordsRead++;
            if (!fitsFourteenDigits(record.divisionNumber())) {
                state.fail("DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (record.divisionNumber() < lastCurrentDivision) {
                state.fail("CURRENT_AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Current-year agency input must be nondecreasing by division.", "clrtm753-001");
                return;
            }
            lastCurrentDivision = record.divisionNumber();
            state.currentOnlyDivisions++;
            state.messages.add(unmatchedDivision(record.divisionNumber(), false));
        }
    }

    private Group loadGroup(
            List<AgencyAssessment> records,
            int start,
            long division,
            long previousDivision,
            boolean prior,
            State state) {
        int index = start;
        long total = 0;
        long lastDivision = previousDivision;
        boolean orderingError = false;
        LinkedHashSet<String> agencies = new LinkedHashSet<>();
        while (index < records.size() && records.get(index).divisionNumber() == division) {
            AgencyAssessment record = records.get(index++);
            if (prior) {
                state.priorRecordsRead++;
            } else {
                state.currentRecordsRead++;
            }
            if (record.divisionNumber() < lastDivision) {
                orderingError = true;
            }
            lastDivision = record.divisionNumber();
            total += record.equalizedValue();
            loadUniqueAgencies(record.agencies(), agencies);
        }
        if (index < records.size() && records.get(index).divisionNumber() < lastDivision) {
            AgencyAssessment descending = records.get(index++);
            if (prior) {
                state.priorRecordsRead++;
            } else {
                state.currentRecordsRead++;
            }
            orderingError = true;
            lastDivision = descending.divisionNumber();
        }
        return new Group(index, lastDivision, total, List.copyOf(agencies), orderingError);
    }

    private void postFrozenAgencyAdjustments(
            List<AgencyAssessment> prior,
            List<AgencyAssessment> current,
            List<Comparison> comparisons,
            FrozenAgencyStore store,
            State state) {
        PostingCursor priorCursor = new PostingCursor(prior, true, state);
        PostingCursor currentCursor = new PostingCursor(current, false, state);
        for (Comparison comparison : comparisons) {
            if (state.returnCode != 0) {
                break;
            }
            state.comparisonRecordsRead++;
            List<AgencyAssessment> priorGroup = priorCursor.takeThrough(comparison.divisionNumber());
            List<AgencyAssessment> currentGroup = currentCursor.takeThrough(comparison.divisionNumber());
            List<String> disconnectAgencies = classifiedAgencies(comparison.segments(), 'D');
            List<String> annexAgencies = classifiedAgencies(comparison.segments(), 'A');

            postGroup(priorGroup, disconnectAgencies, comparison, false, store, state);
            postGroup(currentGroup, annexAgencies, comparison, true, store, state);
            if (priorCursor.orderingError || currentCursor.orderingError) {
                state.fail("POSTING_INPUT_OUT_OF_SEQUENCE",
                        "Posting inputs must be nondecreasing by division.", "clrtm755-006");
            }
        }
    }

    private void postGroup(
            List<AgencyAssessment> records,
            List<String> affectedAgencies,
            Comparison comparison,
            boolean annex,
            FrozenAgencyStore store,
            State state) {
        if (affectedAgencies.isEmpty()) {
            return;
        }
        for (AgencyAssessment record : records) {
            for (String agency : scanAgencySlots(record.agencies())) {
                if (!affectedAgencies.contains(agency)) {
                    continue;
                }
                long postedValue;
                try {
                    postedValue = BigDecimal.valueOf(record.equalizedValue())
                            .multiply(comparison.percentChange())
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                            .longValueExact();
                } catch (RuntimeException exception) {
                    state.fail("POSTING_VALUE_ERROR",
                            "Posted value cannot be represented as an integer: " + exception.getMessage(),
                            "clrtm755-003");
                    continue;
                }
                try {
                    Operation operation = store.post(record.taxCode(), agency, postedValue, annex);
                    if (operation == Operation.REWRITE) {
                        state.rewriteOperations++;
                    } else {
                        state.insertOperations++;
                    }
                    state.postings.add(new Posting(
                            comparison.divisionNumber(), record.property(), record.taxCode(), agency,
                            comparison.percentChange(), record.equalizedValue(),
                            annex ? 0 : postedValue, annex ? postedValue : 0, operation));
                } catch (RuntimeException exception) {
                    state.fail("FROZEN_AGENCY_PERSISTENCE_ERROR",
                            "Frozen-agency posting failed: " + exception.getMessage(),
                            annex ? "clrtm755-005" : "clrtm755-004");
                }
            }
        }
    }

    private static List<String> classifiedAgencies(List<Segment> segments, char type) {
        List<String> classified = new ArrayList<>();
        for (int index = 0; index < Math.min(segments.size(), SEGMENT_LIMIT); index++) {
            Segment segment = segments.get(index);
            if (segment.type() == type) {
                classified.add(segment.agency());
            }
        }
        return List.copyOf(classified.subList(0, Math.min(classified.size(), AGENCY_SLOT_LIMIT)));
    }

    private static List<Segment> differences(List<String> prior, List<String> current) {
        Set<String> priorSet = new LinkedHashSet<>(prior);
        Set<String> currentSet = new LinkedHashSet<>(current);
        List<Segment> segments = new ArrayList<>();
        for (String agency : priorSet) {
            if (!currentSet.contains(agency)) {
                segments.add(new Segment(agency, 'D'));
            }
        }
        for (String agency : currentSet) {
            if (!priorSet.contains(agency)) {
                segments.add(new Segment(agency, 'A'));
            }
        }
        return List.copyOf(segments);
    }

    private static void loadUniqueAgencies(List<String> slots, LinkedHashSet<String> agencies) {
        int scanned = 0;
        for (String agency : slots) {
            if (scanned++ == AGENCY_SLOT_LIMIT || isZeroAgency(agency)) {
                break;
            }
            if (agencies.size() == AGENCY_SLOT_LIMIT) {
                break;
            }
            agencies.add(agency);
        }
    }

    private static List<String> copyAgencySlots(List<String> slots) {
        List<String> result = new ArrayList<>(AGENCY_SLOT_LIMIT);
        for (int index = 0; index < AGENCY_SLOT_LIMIT; index++) {
            String agency = index < slots.size() ? slots.get(index) : null;
            result.add(agency == null ? "000000000" : agency);
        }
        return List.copyOf(result);
    }

    private static List<String> scanAgencySlots(List<String> slots) {
        List<String> result = new ArrayList<>();
        for (int index = 0; index < Math.min(slots.size(), AGENCY_SLOT_LIMIT); index++) {
            String agency = slots.get(index);
            if (isZeroAgency(agency)) {
                break;
            }
            result.add(agency);
        }
        return List.copyOf(result);
    }

    private static boolean isZeroAgency(String agency) {
        return agency == null || agency.isBlank() || agency.chars().allMatch(character -> character == '0');
    }

    private static boolean fitsFourteenDigits(long value) {
        return value >= 0 && value <= MAX_FOURTEEN_DIGIT_VALUE;
    }

    private static Message unmatchedDivision(long division, boolean priorOnly) {
        return new Message(
                "WARNING",
                priorOnly ? "NO_CURRENT_DIVISION_MATCH" : "NO_PRIOR_DIVISION_MATCH",
                "Division " + division + (priorOnly
                        ? " has no current-year match."
                        : " has no prior-year match."),
                "clrtm753-004");
    }

    public interface FrozenAgencyStore {
        Operation post(String taxCode, String agencyNumber, long value, boolean annex);
    }

    public enum Operation { REWRITE, INSERT }

    public record Input(
            List<EqualizedValue> equalizedValues,
            List<Division> divisions,
            Map<String, TaxCode> taxCodes,
            List<AgencyAssessment> priorAgencyAssessments) {
        public Input {
            equalizedValues = List.copyOf(equalizedValues);
            divisions = List.copyOf(divisions);
            taxCodes = Map.copyOf(taxCodes);
            priorAgencyAssessments = List.copyOf(priorAgencyAssessments);
        }
    }

    public record EqualizedValue(
            int town, int volume, long property, String taxType, String taxCode,
            long assessedValue, long equalizedValue) {}

    public record Division(int volume, long property, long divisionNumber) {}

    public record TaxCode(String taxCode, BigDecimal rate, List<String> agencies) {
        public TaxCode {
            Objects.requireNonNull(taxCode, "taxCode");
            Objects.requireNonNull(rate, "rate");
            agencies = List.copyOf(agencies);
        }
    }

    public record DividedValue(
            int town, int volume, long property, String taxType, String taxCode,
            long divisionNumber, long assessedValue, long equalizedValue) {}

    public record AgencyAssessment(
            long divisionNumber, int town, int volume, long property, String taxType,
            String taxCode, long assessedValue, long equalizedValue, BigDecimal rate,
            List<String> agencies) {
        public AgencyAssessment {
            agencies = List.copyOf(agencies);
        }
    }

    public record Segment(String agency, char type) {}

    public record Comparison(
            long divisionNumber, long priorTotalEqualizedValue, long currentTotalEqualizedValue,
            BigDecimal percentChange, List<Segment> segments) {
        public Comparison {
            segments = List.copyOf(segments);
        }
    }

    public record Posting(
            long divisionNumber, long property, String taxCode, String agencyNumber,
            BigDecimal percentChange, long sourceEqualizedValue, long disconnectedValue,
            long annexedValue, Operation operation) {}

    public record Message(String severity, String code, String text, String ruleId) {}

    public record DivisionStampingCounts(
            int equalizedValueRecordsRead, int divisionRecordsRead, int outputRecordsWritten,
            int recordsStamped, int divisionRecordsUnmatched) {}

    public record AgencyAttachmentCounts(
            int assessmentRecordsRead, int assessmentRecordsWritten,
            int assessmentRecordsUnmatched, boolean normalCompletionBalanced) {}

    public record AgencyComparisonCounts(
            int priorRecordsRead, int currentRecordsRead, int comparisonRecordsWritten,
            int disconnectSegments, int annexSegments, int priorOnlyDivisions,
            int currentOnlyDivisions) {}

    public record FrozenAgencyPostingCounts(
            int priorRecordsRead, int currentRecordsRead, int comparisonRecordsRead,
            int rewriteOperations, int insertOperations) {}

    public record Result(
            int returnCode,
            List<DividedValue> dividedValues,
            List<DividedValue> unmatchedDividedValues,
            List<AgencyAssessment> agencyAssessments,
            List<Comparison> comparisons,
            List<Posting> postings,
            List<Message> messages,
            DivisionStampingCounts divisionStamping,
            AgencyAttachmentCounts agencyAttachment,
            AgencyComparisonCounts agencyComparison,
            FrozenAgencyPostingCounts frozenAgencyPosting) {
        public Result {
            dividedValues = List.copyOf(dividedValues);
            unmatchedDividedValues = List.copyOf(unmatchedDividedValues);
            agencyAssessments = List.copyOf(agencyAssessments);
            comparisons = List.copyOf(comparisons);
            postings = List.copyOf(postings);
            messages = List.copyOf(messages);
        }
    }

    private record ParcelKey(int volume, long property) implements Comparable<ParcelKey> {
        @Override
        public int compareTo(ParcelKey other) {
            int volumeComparison = Integer.compare(volume, other.volume);
            return volumeComparison != 0 ? volumeComparison : Long.compare(property, other.property);
        }
    }

    private record DividedKey(int town, int volume, long property, String taxType)
            implements Comparable<DividedKey> {
        private static final Comparator<DividedKey> COMPARATOR = Comparator
                .comparingInt(DividedKey::town)
                .thenComparingInt(DividedKey::volume)
                .thenComparingLong(DividedKey::property)
                .thenComparing(DividedKey::taxType);

        @Override
        public int compareTo(DividedKey other) {
            return COMPARATOR.compare(this, other);
        }
    }

    private record Group(
            int nextIndex, long lastDivision, long totalEqualizedValue,
            List<String> agencies, boolean orderingError) {}

    private static final class PostingCursor {
        private final List<AgencyAssessment> records;
        private final boolean prior;
        private final State state;
        private int index;
        private long lastDivision = Long.MIN_VALUE;
        private boolean orderingError;

        private PostingCursor(List<AgencyAssessment> records, boolean prior, State state) {
            this.records = records;
            this.prior = prior;
            this.state = state;
        }

        private List<AgencyAssessment> takeThrough(long division) {
            List<AgencyAssessment> matching = new ArrayList<>();
            while (index < records.size() && records.get(index).divisionNumber() <= division) {
                AgencyAssessment record = records.get(index++);
                if (prior) {
                    state.postingPriorRecordsRead++;
                } else {
                    state.postingCurrentRecordsRead++;
                }
                if (record.divisionNumber() < lastDivision) {
                    orderingError = true;
                }
                lastDivision = record.divisionNumber();
                if (record.divisionNumber() == division) {
                    matching.add(record);
                }
            }
            return matching;
        }
    }

    private static final class State {
        private int returnCode;
        private final List<DividedValue> dividedValues = new ArrayList<>();
        private final List<DividedValue> unmatchedDividedValues = new ArrayList<>();
        private final List<AgencyAssessment> agencyAssessments = new ArrayList<>();
        private final List<Comparison> comparisons = new ArrayList<>();
        private final List<Posting> postings = new ArrayList<>();
        private final List<Message> messages = new ArrayList<>();
        private int equalizedValueRecordsRead;
        private int divisionRecordsRead;
        private int outputRecordsWritten;
        private int recordsStamped;
        private int assessmentRecordsRead;
        private int assessmentRecordsWritten;
        private int assessmentRecordsUnmatched;
        private int priorRecordsRead;
        private int currentRecordsRead;
        private int comparisonRecordsWritten;
        private int disconnectSegments;
        private int annexSegments;
        private int priorOnlyDivisions;
        private int currentOnlyDivisions;
        private int postingPriorRecordsRead;
        private int postingCurrentRecordsRead;
        private int comparisonRecordsRead;
        private int rewriteOperations;
        private int insertOperations;

        private void fail(String code, String text, String ruleId) {
            returnCode = ERROR_RETURN_CODE;
            messages.add(new Message("ERROR", code, text, ruleId));
        }

        private Result result() {
            return new Result(
                    returnCode, dividedValues, unmatchedDividedValues, agencyAssessments,
                    comparisons, postings, messages,
                    new DivisionStampingCounts(equalizedValueRecordsRead, divisionRecordsRead,
                            outputRecordsWritten, recordsStamped, 0),
                    new AgencyAttachmentCounts(assessmentRecordsRead, assessmentRecordsWritten,
                            assessmentRecordsUnmatched,
                            assessmentRecordsWritten + assessmentRecordsUnmatched
                                    == assessmentRecordsRead),
                    new AgencyComparisonCounts(priorRecordsRead, currentRecordsRead,
                            comparisonRecordsWritten, disconnectSegments, annexSegments,
                            priorOnlyDivisions, currentOnlyDivisions),
                    new FrozenAgencyPostingCounts(postingPriorRecordsRead, postingCurrentRecordsRead,
                            comparisonRecordsRead, rewriteOperations, insertOperations));
        }
    }
}
