package org.cookcounty.tax.application.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/// Applies source-order, attachment, comparison, and frozen-agency posting rules.
///
/// Assessed and equalized values use exact decimals in whole-value units. Stamping and attachment
/// preserve the source scale. Grouping uses exact addition without rounding. Posting rounds to
/// scale zero with [`RoundingMode.HALF_UP`]. The kernel returns typed rule messages and retained
/// partial output instead of throwing for expected business failures.
@Component
public final class TaxRateInputKernel {

    /// Return code for an expected ordering, width, capacity, calculation, or posting failure.
    public static final int ERROR_RETURN_CODE = 16;

    /// Number of maintained agency slots that grouping and posting logic can inspect.
    public static final int AGENCY_SLOT_LIMIT = 40;

    /// Maximum number of annex and disconnect segments in one comparison handoff.
    public static final int SEGMENT_LIMIT = 50;

    /// Largest nonnegative value that fits the downstream fourteen-digit identifier field.
    public static final long MAX_FOURTEEN_DIGIT_VALUE = 99_999_999_999_999L;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    /// Runs every preparation stage in order and stops new stages after a typed failure.
    ///
    /// Successful store mutations remain represented in the result when a later post fails.
    ///
    /// @param input immutable source-order and maintained reference inputs
    /// @param frozenAgencyStore persistent posting boundary
    /// @return immutable output, diagnostics, and operation totals
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
                    input.priorAgencyAssessments(),
                    state.agencyAssessments,
                    state.comparisons,
                    frozenAgencyStore,
                    state);
        }
        return state.result();
    }

    /// Runs only the posting stage against an already prepared comparison handoff.
    ///
    /// @param prior source-ordered prior-year assessment records
    /// @param current source-ordered current-year assessment records
    /// @param comparisons prepared division comparisons
    /// @param frozenAgencyStore persistent posting boundary
    /// @return immutable posting output, diagnostics, and operation totals
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

    /// Stamps matched divisions and applies the property fallback in source order.
    private void stampDivisions(
            List<EqualizedValue> equalizedValues, List<Division> divisions, State state) {
        Map<ParcelKey, Division> divisionByParcel = new java.util.HashMap<>();
        ParcelKey previousDivisionKey = null;
        for (Division division : divisions) {
            state.divisionRecordsRead++;
            ParcelKey key = new ParcelKey(division.volume(), division.property());
            if (previousDivisionKey != null && key.compareTo(previousDivisionKey) <= 0) {
                state.fail(
                        "DIVISION_INPUT_OUT_OF_SEQUENCE",
                        "Division input must be strictly increasing by volume and property.",
                        "clrtm751-001");
                return;
            }
            if (!fitsFourteenDigits(division.divisionNumber())) {
                state.fail(
                        "DIVISION_NUMBER_TOO_WIDE",
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
                state.fail(
                        "EQUALIZED_VALUE_INPUT_OUT_OF_SEQUENCE",
                        "Equalized-value input must be nondecreasing by volume and property.",
                        "clrtm751-001");
                return;
            }
            Division division = divisionByParcel.get(key);
            long divisionNumber = division == null ? value.property() : division.divisionNumber();
            if (!fitsFourteenDigits(divisionNumber)) {
                state.fail(
                        "DIVISION_NUMBER_TOO_WIDE",
                        "Property number cannot be preserved in the 14-digit division field.",
                        "clrtm751-003");
                return;
            }
            state.dividedValues.add(
                    new DividedValue(
                            value.town(),
                            value.volume(),
                            value.property(),
                            value.taxType(),
                            value.taxCode(),
                            divisionNumber,
                            value.assessedValue(),
                            value.equalizedValue()));
            state.outputRecordsWritten++;
            state.recordsStamped++;
            previousEqualizedKey = key;
        }
    }

    /// Attaches maintained rates and agency slots without calculating a new rate.
    private void attachAgencies(
            List<DividedValue> dividedValues, Map<String, TaxCode> taxCodes, State state) {
        DividedKey previous = null;
        for (DividedValue value : dividedValues) {
            state.assessmentRecordsRead++;
            DividedKey key =
                    new DividedKey(value.town(), value.volume(), value.property(), value.taxType());
            if (previous != null && key.compareTo(previous) <= 0) {
                state.fail(
                        "DIVIDED_VALUE_INPUT_OUT_OF_SEQUENCE",
                        "Divided equalized-value input must be strictly increasing by town, volume,"
                                + " property, and tax type.",
                        "clrtm752-001");
                return;
            }
            TaxCode taxCode = taxCodes.get(value.taxCode());
            if (taxCode == null) {
                state.assessmentRecordsUnmatched++;
                state.unmatchedDividedValues.add(value);
                state.messages.add(
                        new Message(
                                "WARNING",
                                "UNMATCHED_TAX_CODE",
                                "No tax-code master match for volume "
                                        + value.volume()
                                        + ", property "
                                        + value.property()
                                        + ", tax type "
                                        + value.taxType()
                                        + ", tax code "
                                        + value.taxCode()
                                        + ".",
                                "clrtm752-003"));
            } else {
                state.agencyAssessments.add(
                        new AgencyAssessment(
                                value.divisionNumber(),
                                value.town(),
                                value.volume(),
                                value.property(),
                                value.taxType(),
                                value.taxCode(),
                                value.assessedValue(),
                                value.equalizedValue(),
                                taxCode.rate(),
                                copyAgencySlots(taxCode.agencies())));
                state.assessmentRecordsWritten++;
            }
            previous = key;
        }
    }

    /// Compares division groups and retains a completed group if a late ordering error appears.
    private void compareAgencies(
            List<AgencyAssessment> prior, List<AgencyAssessment> current, State state) {
        int priorIndex = 0;
        int currentIndex = 0;
        long lastPriorDivision = Long.MIN_VALUE;
        long lastCurrentDivision = Long.MIN_VALUE;

        while (priorIndex < prior.size()
                && currentIndex < current.size()
                && state.returnCode == 0) {
            AgencyAssessment priorRecord = prior.get(priorIndex);
            AgencyAssessment currentRecord = current.get(currentIndex);
            if (!fitsFourteenDigits(priorRecord.divisionNumber())
                    || !fitsFourteenDigits(currentRecord.divisionNumber())) {
                state.fail(
                        "DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (priorRecord.divisionNumber() < currentRecord.divisionNumber()) {
                state.priorRecordsRead++;
                if (priorRecord.divisionNumber() < lastPriorDivision) {
                    state.fail(
                            "PRIOR_AGENCY_INPUT_OUT_OF_SEQUENCE",
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
                    state.fail(
                            "CURRENT_AGENCY_INPUT_OUT_OF_SEQUENCE",
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
            Group priorGroup =
                    loadGroup(prior, priorIndex, division, lastPriorDivision, true, state);
            priorIndex = priorGroup.nextIndex();
            lastPriorDivision = priorGroup.lastDivision();
            Group currentGroup =
                    loadGroup(current, currentIndex, division, lastCurrentDivision, false, state);
            currentIndex = currentGroup.nextIndex();
            lastCurrentDivision = currentGroup.lastDivision();

            List<Segment> segments = differences(priorGroup.agencies(), currentGroup.agencies());
            if (segments.size() > SEGMENT_LIMIT) {
                state.fail(
                        "ANNEX_DISCONNECT_CAPACITY_EXCEEDED",
                        "A matched division produced more than 50 annex/disconnect segments.",
                        "clrtm753-006");
                return;
            }
            state.comparisons.add(
                    new Comparison(
                            division,
                            priorGroup.totalEqualizedValue(),
                            currentGroup.totalEqualizedValue(),
                            ONE_HUNDRED,
                            segments));
            state.comparisonRecordsWritten++;
            state.disconnectSegments +=
                    (int) segments.stream().filter(segment -> segment.type() == 'D').count();
            state.annexSegments +=
                    (int) segments.stream().filter(segment -> segment.type() == 'A').count();
            // A descending record discovered while loading a matched group does not suppress this
            // output.
            if (priorGroup.orderingError() || currentGroup.orderingError()) {
                state.fail(
                        "AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Prior- and current-year agency inputs must be nondecreasing by division.",
                        "clrtm753-001");
            }
        }

        while (priorIndex < prior.size() && state.returnCode == 0) {
            AgencyAssessment record = prior.get(priorIndex++);
            state.priorRecordsRead++;
            if (!fitsFourteenDigits(record.divisionNumber())) {
                state.fail(
                        "DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (record.divisionNumber() < lastPriorDivision) {
                state.fail(
                        "PRIOR_AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Prior-year agency input must be nondecreasing by division.",
                        "clrtm753-001");
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
                state.fail(
                        "DIVISION_NUMBER_TOO_WIDE",
                        "Comparison division cannot be represented in the 14-digit handoff field.",
                        "clrtm753-006");
                return;
            }
            if (record.divisionNumber() < lastCurrentDivision) {
                state.fail(
                        "CURRENT_AGENCY_INPUT_OUT_OF_SEQUENCE",
                        "Current-year agency input must be nondecreasing by division.",
                        "clrtm753-001");
                return;
            }
            lastCurrentDivision = record.divisionNumber();
            state.currentOnlyDivisions++;
            state.messages.add(unmatchedDivision(record.divisionNumber(), false));
        }
    }

    /// Totals one division and deduplicates at most forty nonzero agency slots.
    private Group loadGroup(
            List<AgencyAssessment> records,
            int start,
            long division,
            long previousDivision,
            boolean prior,
            State state) {
        int index = start;
        BigDecimal total = BigDecimal.ZERO;
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
            total = total.add(record.equalizedValue());
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

    /// Posts comparison segments against prior and current assessment streams.
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
            List<AgencyAssessment> priorGroup =
                    priorCursor.takeThrough(comparison.divisionNumber());
            List<AgencyAssessment> currentGroup =
                    currentCursor.takeThrough(comparison.divisionNumber());
            List<String> disconnectAgencies = classifiedAgencies(comparison.segments(), 'D');
            List<String> annexAgencies = classifiedAgencies(comparison.segments(), 'A');

            postGroup(priorGroup, disconnectAgencies, comparison, false, store, state);
            postGroup(currentGroup, annexAgencies, comparison, true, store, state);
            if (priorCursor.orderingError || currentCursor.orderingError) {
                state.fail(
                        "POSTING_INPUT_OUT_OF_SEQUENCE",
                        "Posting inputs must be nondecreasing by division.",
                        "clrtm755-006");
            }
        }
    }

    /// Applies every matching slot occurrence, including repeated agency slots.
    ///
    /// The calculation multiplies a whole-unit equalized value by the exact percentage. It then
    /// rounds to scale zero with [`RoundingMode.HALF_UP`] before the store receives the amount.
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
                BigDecimal postedValue;
                try {
                    postedValue =
                            record.equalizedValue()
                                    .multiply(comparison.percentChange())
                                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                } catch (RuntimeException exception) {
                    state.fail(
                            "POSTING_VALUE_ERROR",
                            "Posted value cannot be rounded to a whole-unit amount: "
                                    + exception.getMessage(),
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
                    state.postings.add(
                            new Posting(
                                    comparison.divisionNumber(),
                                    record.property(),
                                    record.taxCode(),
                                    agency,
                                    comparison.percentChange(),
                                    record.equalizedValue(),
                                    annex ? BigDecimal.ZERO : postedValue,
                                    annex ? postedValue : BigDecimal.ZERO,
                                    operation));
                } catch (RuntimeException exception) {
                    state.fail(
                            "FROZEN_AGENCY_PERSISTENCE_ERROR",
                            "Frozen-agency posting failed: " + exception.getMessage(),
                            annex ? "clrtm755-005" : "clrtm755-004");
                }
            }
        }
    }

    /// Returns at most forty agencies with the requested segment classification.
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

    /// Preserves prior-only agencies before current-only agencies in the comparison result.
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

    /// Loads unique nonzero agencies until the forty-slot boundary.
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

    /// Returns exactly forty non-null slots and pads missing positions with zero identifiers.
    private static List<String> copyAgencySlots(List<String> slots) {
        List<String> result = new ArrayList<>(AGENCY_SLOT_LIMIT);
        for (int index = 0; index < AGENCY_SLOT_LIMIT; index++) {
            String agency = index < slots.size() ? slots.get(index) : "000000000";
            result.add(isZeroAgency(agency) ? "000000000" : agency);
        }
        return List.copyOf(result);
    }

    /// Returns populated agency slots before the first blank or zero identifier.
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

    /// Identifies a blank or all-zero agency slot.
    private static boolean isZeroAgency(String agency) {
        return agency.isBlank() || agency.chars().allMatch(character -> character == '0');
    }

    /// Checks the unsigned width required by the fixed-width division handoff.
    private static boolean fitsFourteenDigits(long value) {
        return value >= 0 && value <= MAX_FOURTEEN_DIGIT_VALUE;
    }

    /// Creates the warning for a division present in only one comparison year.
    private static Message unmatchedDivision(long division, boolean priorOnly) {
        return new Message(
                "WARNING",
                priorOnly ? "NO_CURRENT_DIVISION_MATCH" : "NO_PRIOR_DIVISION_MATCH",
                "Division "
                        + division
                        + (priorOnly ? " has no current-year match." : " has no prior-year match."),
                "clrtm753-004");
    }

    /// Persists one whole-unit frozen-agency posting.
    ///
    /// The amount has scale zero because the kernel rounds it with [`RoundingMode.HALF_UP`] before
    /// this boundary. The implementation returns whether it inserted a new composite key or
    /// replaced an existing snapshot. Expected posting failures can throw an infrastructure
    /// exception. The kernel turns that exception into a typed error result.
    public interface FrozenAgencyStore {
        /// Applies one scale-zero annex or disconnect amount.
        ///
        /// @param taxCode five-character tax-code identity
        /// @param agencyNumber agency identity within the tax code
        /// @param value exact scale-zero amount after half-up rounding
        /// @param annex true for annexed value or false for disconnected value
        /// @return persistent insert or replacement operation
        Operation post(String taxCode, String agencyNumber, BigDecimal value, boolean annex);
    }

    /// Persistent operation performed for one frozen-agency posting.
    public enum Operation {
        /// Replaced an existing composite key.
        REWRITE,
        /// Inserted a new composite key.
        INSERT
    }

    /// Complete immutable input for a full preparation run.
    ///
    /// @param equalizedValues source-order parcel values
    /// @param divisions source-order parcel-to-division associations
    /// @param taxCodes maintained rate and agency data keyed by five-character tax code
    /// @param priorAgencyAssessments source-order prior-year agency assessments
    public record Input(
            List<EqualizedValue> equalizedValues,
            List<Division> divisions,
            Map<String, TaxCode> taxCodes,
            List<AgencyAssessment> priorAgencyAssessments) {
        /// Copies each collection so later caller changes cannot alter processing input.
        public Input {
            equalizedValues = List.copyOf(equalizedValues);
            divisions = List.copyOf(divisions);
            taxCodes = Map.copyOf(taxCodes);
            priorAgencyAssessments = List.copyOf(priorAgencyAssessments);
        }
    }

    /// Source parcel value used by the division-stamping stage.
    ///
    /// @param town town prefix derived from the five-character tax code
    /// @param volume volume portion of the parcel key
    /// @param property parcel portion of the key
    /// @param taxType tax-type ordering code
    /// @param taxCode five-character tax-code identity
    /// @param assessedValue exact assessed value in whole units. The source scale is preserved.
    /// @param equalizedValue exact equalized value in whole units. The source scale is preserved.
    public record EqualizedValue(
            int town,
            int volume,
            long property,
            String taxType,
            String taxCode,
            BigDecimal assessedValue,
            BigDecimal equalizedValue) {
        /// Requires both exact monetary values while preserving their supplied scales.
        public EqualizedValue {
            Objects.requireNonNull(assessedValue, "assessedValue");
            Objects.requireNonNull(equalizedValue, "equalizedValue");
        }
    }

    /// Parcel-to-division association used during stamping.
    ///
    /// @param volume volume portion of the parcel key
    /// @param property parcel portion of the key
    /// @param divisionNumber division identity written to the handoff
    public record Division(int volume, long property, long divisionNumber) {}

    /// Maintained rate and agency slots for one tax code.
    ///
    /// The rate is carried unchanged. The kernel does not calculate it.
    ///
    /// @param taxCode five-character tax-code identity
    /// @param rate exact maintained composite rate
    /// @param agencies ordered agency identifiers. Only the first forty can be carried.
    public record TaxCode(String taxCode, BigDecimal rate, List<String> agencies) {
        /// Copies the agency slot sequence and rejects missing required values.
        public TaxCode {
            Objects.requireNonNull(taxCode, "taxCode");
            Objects.requireNonNull(rate, "rate");
            agencies = List.copyOf(agencies);
        }
    }

    /// Division-aware parcel value produced by the stamping stage.
    ///
    /// @param town town prefix derived from the tax code
    /// @param volume volume portion of the parcel key
    /// @param property parcel portion of the key
    /// @param taxType tax-type ordering code
    /// @param taxCode five-character tax-code identity
    /// @param divisionNumber matched division or property fallback
    /// @param assessedValue exact assessed value in whole units, carried with its source scale
    /// @param equalizedValue exact equalized value in whole units, carried with its source scale
    public record DividedValue(
            int town,
            int volume,
            long property,
            String taxType,
            String taxCode,
            long divisionNumber,
            BigDecimal assessedValue,
            BigDecimal equalizedValue) {
        /// Requires both exact monetary values while preserving their supplied scales.
        public DividedValue {
            Objects.requireNonNull(assessedValue, "assessedValue");
            Objects.requireNonNull(equalizedValue, "equalizedValue");
        }
    }

    /// Parcel assessment with its maintained rate and exactly forty agency slots.
    ///
    /// @param divisionNumber fourteen-digit handoff division identity
    /// @param town town prefix derived from the tax code
    /// @param volume volume portion of the parcel key
    /// @param property parcel portion of the key
    /// @param taxType tax-type ordering code
    /// @param taxCode five-character tax-code identity
    /// @param assessedValue exact assessed value in whole units, carried with its source scale
    /// @param equalizedValue exact equalized value in whole units, carried with its source scale
    /// @param rate exact maintained rate carried without calculation
    /// @param agencies immutable forty-slot agency table
    public record AgencyAssessment(
            long divisionNumber,
            int town,
            int volume,
            long property,
            String taxType,
            String taxCode,
            BigDecimal assessedValue,
            BigDecimal equalizedValue,
            BigDecimal rate,
            List<String> agencies) {
        /// Requires exact monetary values and copies the fixed-slot agency table.
        public AgencyAssessment {
            Objects.requireNonNull(assessedValue, "assessedValue");
            Objects.requireNonNull(equalizedValue, "equalizedValue");
            Objects.requireNonNull(rate, "rate");
            agencies = List.copyOf(agencies);
        }
    }

    /// One annex or disconnect agency difference.
    ///
    /// @param agency agency identity
    /// @param type `A` for annex or `D` for disconnect. Posting ignores other values.
    public record Segment(String agency, char type) {}

    /// Prior-year and current-year totals and agency differences for one division.
    ///
    /// @param divisionNumber matched division identity
    /// @param priorTotalEqualizedValue exact prior-year total. Addition preserves operand scale.
    /// @param currentTotalEqualizedValue exact current-year total. Addition preserves operand
    ///   scale.
    /// @param percentChange exact posting percentage. Current evidence carries 100.0.
    /// @param segments ordered disconnect segments followed by annex segments
    public record Comparison(
            long divisionNumber,
            BigDecimal priorTotalEqualizedValue,
            BigDecimal currentTotalEqualizedValue,
            BigDecimal percentChange,
            List<Segment> segments) {
        /// Requires exact monetary values and copies the ordered segment sequence.
        public Comparison {
            Objects.requireNonNull(priorTotalEqualizedValue, "priorTotalEqualizedValue");
            Objects.requireNonNull(currentTotalEqualizedValue, "currentTotalEqualizedValue");
            Objects.requireNonNull(percentChange, "percentChange");
            segments = List.copyOf(segments);
        }
    }

    /// One successful persistent posting operation.
    ///
    /// @param divisionNumber comparison division identity
    /// @param property parcel identity that supplied the posted value
    /// @param taxCode five-character tax-code identity
    /// @param agencyNumber posted agency identity
    /// @param percentChange exact percentage applied before scale-zero half-up rounding
    /// @param sourceEqualizedValue exact source amount in whole equalized-value units
    /// @param disconnectedValue scale-zero posted disconnect amount, otherwise exact zero
    /// @param annexedValue scale-zero posted annex amount, otherwise exact zero
    /// @param operation insert or replacement of the composite persistent key
    public record Posting(
            long divisionNumber,
            long property,
            String taxCode,
            String agencyNumber,
            BigDecimal percentChange,
            BigDecimal sourceEqualizedValue,
            BigDecimal disconnectedValue,
            BigDecimal annexedValue,
            Operation operation) {
        /// Requires every exact monetary value in the successful posting result.
        public Posting {
            Objects.requireNonNull(percentChange, "percentChange");
            Objects.requireNonNull(sourceEqualizedValue, "sourceEqualizedValue");
            Objects.requireNonNull(disconnectedValue, "disconnectedValue");
            Objects.requireNonNull(annexedValue, "annexedValue");
        }
    }

    /// Ordered typed diagnostic from an expected business failure or warning.
    ///
    /// @param severity `WARNING` or `ERROR`
    /// @param code stable machine-readable diagnostic code
    /// @param text safe caller-readable context
    /// @param ruleId accepted business-rule identity
    public record Message(String severity, String code, String text, String ruleId) {}

    /// Operation totals for division stamping.
    ///
    /// @param equalizedValueRecordsRead parcel values examined
    /// @param divisionRecordsRead division associations examined
    /// @param outputRecordsWritten divided values produced
    /// @param recordsStamped divided values assigned a matched division or fallback
    /// @param divisionRecordsUnmatched displayed total that remains zero
    public record DivisionStampingCounts(
            int equalizedValueRecordsRead,
            int divisionRecordsRead,
            int outputRecordsWritten,
            int recordsStamped,
            int divisionRecordsUnmatched) {}

    /// Operation totals for tax-code attachment.
    ///
    /// @param assessmentRecordsRead divided values examined
    /// @param assessmentRecordsWritten agency assessments produced
    /// @param assessmentRecordsUnmatched missing tax-code matches
    /// @param normalCompletionBalanced whether written plus unmatched equals read
    public record AgencyAttachmentCounts(
            int assessmentRecordsRead,
            int assessmentRecordsWritten,
            int assessmentRecordsUnmatched,
            boolean normalCompletionBalanced) {}

    /// Operation totals for prior-year and current-year comparison.
    ///
    /// @param priorRecordsRead prior-year assessments examined
    /// @param currentRecordsRead current-year assessments examined
    /// @param comparisonRecordsWritten matched-division comparisons produced
    /// @param disconnectSegments prior-only agency segments produced
    /// @param annexSegments current-only agency segments produced
    /// @param priorOnlyDivisions prior-year occurrences without current-year matches
    /// @param currentOnlyDivisions current-year occurrences without prior-year matches
    public record AgencyComparisonCounts(
            int priorRecordsRead,
            int currentRecordsRead,
            int comparisonRecordsWritten,
            int disconnectSegments,
            int annexSegments,
            int priorOnlyDivisions,
            int currentOnlyDivisions) {}

    /// Operation totals for persistent posting.
    ///
    /// @param priorRecordsRead prior-year assessments examined
    /// @param currentRecordsRead current-year assessments examined
    /// @param comparisonRecordsRead comparisons examined
    /// @param rewriteOperations successful replacements, including repeated composite keys
    /// @param insertOperations successful first writes
    public record FrozenAgencyPostingCounts(
            int priorRecordsRead,
            int currentRecordsRead,
            int comparisonRecordsRead,
            int rewriteOperations,
            int insertOperations) {}

    /// Complete immutable outcome of a full or posting-only run.
    ///
    /// @param returnCode zero for normal completion or 16 for an expected failure
    /// @param dividedValues retained division-stamped output
    /// @param unmatchedDividedValues values omitted for missing tax-code matches
    /// @param agencyAssessments retained attachment output
    /// @param comparisons retained comparison output
    /// @param postings successful persistent posting operations
    /// @param messages ordered warnings and typed errors
    /// @param divisionStamping stamping totals
    /// @param agencyAttachment attachment totals
    /// @param agencyComparison comparison totals
    /// @param frozenAgencyPosting posting totals
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
        /// Copies every output sequence while preserving its processing order.
        public Result {
            dividedValues = List.copyOf(dividedValues);
            unmatchedDividedValues = List.copyOf(unmatchedDividedValues);
            agencyAssessments = List.copyOf(agencyAssessments);
            comparisons = List.copyOf(comparisons);
            postings = List.copyOf(postings);
            messages = List.copyOf(messages);
        }
    }

    /// Comparison key for division stamping.
    private record ParcelKey(int volume, long property) implements Comparable<ParcelKey> {
        @Override
        public int compareTo(ParcelKey other) {
            int volumeComparison = Integer.compare(volume, other.volume);
            return volumeComparison != 0
                    ? volumeComparison
                    : Long.compare(property, other.property);
        }
    }

    /// Strict attachment-order key.
    private record DividedKey(int town, int volume, long property, String taxType)
            implements Comparable<DividedKey> {
        private static final Comparator<DividedKey> COMPARATOR =
                Comparator.comparingInt(DividedKey::town)
                        .thenComparingInt(DividedKey::volume)
                        .thenComparingLong(DividedKey::property)
                        .thenComparing(DividedKey::taxType);

        @Override
        public int compareTo(DividedKey other) {
            return COMPARATOR.compare(this, other);
        }
    }

    /// Completed division-group totals plus the next unread position.
    ///
    /// The total uses exact decimal addition. Java preserves the larger operand scale at each
    /// addition, and this grouping step performs no rounding.
    private record Group(
            int nextIndex,
            long lastDivision,
            BigDecimal totalEqualizedValue,
            List<String> agencies,
            boolean orderingError) {}

    /// Consumes one ordered posting stream while retaining late ordering-error state.
    private static final class PostingCursor {
        private final List<AgencyAssessment> records;
        private final boolean prior;
        private final State state;
        private int index;
        private long lastDivision = Long.MIN_VALUE;
        private boolean orderingError;

        /// Creates a cursor for either the prior-year or current-year operation counter.
        private PostingCursor(List<AgencyAssessment> records, boolean prior, State state) {
            this.records = records;
            this.prior = prior;
            this.state = state;
        }

        /// Consumes records through a division and returns only records for that exact division.
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

    /// Mutable execution accumulator confined to one kernel invocation.
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

        /// Records a typed expected failure without discarding earlier output or mutations.
        private void fail(String code, String text, String ruleId) {
            returnCode = ERROR_RETURN_CODE;
            messages.add(new Message("ERROR", code, text, ruleId));
        }

        /// Freezes all sequences and operation totals into the public result.
        private Result result() {
            return new Result(
                    returnCode,
                    dividedValues,
                    unmatchedDividedValues,
                    agencyAssessments,
                    comparisons,
                    postings,
                    messages,
                    new DivisionStampingCounts(
                            equalizedValueRecordsRead,
                            divisionRecordsRead,
                            outputRecordsWritten,
                            recordsStamped,
                            0),
                    new AgencyAttachmentCounts(
                            assessmentRecordsRead,
                            assessmentRecordsWritten,
                            assessmentRecordsUnmatched,
                            assessmentRecordsWritten + assessmentRecordsUnmatched
                                    == assessmentRecordsRead),
                    new AgencyComparisonCounts(
                            priorRecordsRead,
                            currentRecordsRead,
                            comparisonRecordsWritten,
                            disconnectSegments,
                            annexSegments,
                            priorOnlyDivisions,
                            currentOnlyDivisions),
                    new FrozenAgencyPostingCounts(
                            postingPriorRecordsRead,
                            postingCurrentRecordsRead,
                            comparisonRecordsRead,
                            rewriteOperations,
                            insertOperations));
        }
    }
}
