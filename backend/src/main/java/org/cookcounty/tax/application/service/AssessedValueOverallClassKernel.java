package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Selects one parcel class from its ordered assessment details.
///
/// Named improvement classes use the accepted fixed precedence. Other classes compare major class,
/// tier, valuation, then minor class. A qualifying improvement can own the immediately following
/// questionnaire occurrence. That companion does not participate in class selection.
@Component
public final class AssessedValueOverallClassKernel {

    private static final List<Integer> NAMED_CLASS_PRECEDENCE =
            List.of(
                    959, 991, 889, 893, 883, 899, 835, 833, 891, 892, 799, 735, 745, 791, 792, 774,
                    772, 679, 663, 638, 666, 673, 689, 693, 683, 589, 593, 583, 599, 535, 591, 592,
                    489, 493, 483, 499, 435, 491, 492, 399, 391, 299);

    private static final Set<Integer> CONTAMINATED_MAJOR_9_TIER_2 = Set.of(901, 990, 997);
    private static final Map<Integer, Integer> OTHER_TIERS = createOtherTiers();

    /// Selects the parcel's overall class without changing the supplied parcel or detail list.
    ///
    /// @param parcel parcel whose prior class is preserved if no recognized improvement wins
    /// @param details occurrences in source order, including questionnaire companions
    /// @return replacement parcel, change count, report count, and recoverable observations
    public Result prepare(AssessmentParcel parcel, List<AssessmentDetail> details) {
        Integer previousClass = parcel.overallClass();
        int highestLandClass = -1;
        boolean hasLand = false;
        boolean hasImprovement = false;
        boolean contaminated = false;
        boolean[] namedClasses = new boolean[NAMED_CLASS_PRECEDENCE.size()];
        Candidate bestOther = null;

        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            int assessmentClass = detail.assessmentClass();
            if ("1".equals(detail.detailType())) {
                hasLand = true;
                if (assessmentClass > highestLandClass) {
                    highestLandClass = assessmentClass;
                }
                continue;
            }
            if (!AssessedValueRuleSupport.isImprovement(detail)) {
                continue;
            }
            hasImprovement = true;
            int namedIndex = NAMED_CLASS_PRECEDENCE.indexOf(assessmentClass);
            if (namedIndex >= 0) {
                namedClasses[namedIndex] = true;
            } else if (OTHER_TIERS.containsKey(assessmentClass)) {
                Candidate candidate =
                        Candidate.from(detail, OTHER_TIERS.getOrDefault(assessmentClass, 0));
                if (bestOther == null || candidate.compareTo(bestOther) > 0) {
                    bestOther = candidate;
                }
            }
            contaminated |= CONTAMINATED_MAJOR_9_TIER_2.contains(assessmentClass);
            if (AssessedValueRuleSupport.isQuestionnaireBearing(detail)) {
                index++;
            }
        }

        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        String recordKey = AssessedValueRuleSupport.recordKey(parcel);
        if (!hasLand) {
            messages.add(
                    new AssessedValueRuleMessage(
                            "WARNING",
                            "NO_LAND_DETAIL",
                            "asrea018-002",
                            "No land detail is present for the assessment parcel.",
                            recordKey));
        }
        if (contaminated) {
            messages.add(
                    new AssessedValueRuleMessage(
                            "WARNING",
                            "MAJOR_9_TIER_2_CONTAMINATION",
                            "asrea018-007",
                            "A major-9 tier-2 class entered the legacy major-8 tier-2 switch.",
                            recordKey));
        }

        Integer selectedClass = firstNamed(namedClasses);
        if (hasImprovement && selectedClass == null && bestOther != null) {
            selectedClass = bestOther.assessmentClass();
        }
        if (!hasImprovement && hasLand) {
            selectedClass = highestLandClass;
        }
        if (hasImprovement && selectedClass == null) {
            messages.add(
                    new AssessedValueRuleMessage(
                            "WARNING",
                            "OVERALL_CLASS_UNRESOLVED",
                            "asrea018-005",
                            "The prior overall class was preserved because no recognized"
                                    + " improvement class won.",
                            recordKey));
        }

        AssessmentParcel prepared =
                selectedClass == null ? parcel : parcel.withOverallClass(selectedClass);
        boolean changed = !previousClass.equals(prepared.overallClass());
        int reportRows = changed || !messages.isEmpty() ? 1 : 0;
        return new Result(prepared, changed, reportRows, List.copyOf(messages));
    }

    /// Returns the first fixed-precedence class found in the parcel.
    private static @Nullable Integer firstNamed(boolean[] found) {
        for (int index = 0; index < found.length; index++) {
            if (found[index]) {
                return NAMED_CLASS_PRECEDENCE.get(index);
            }
        }
        return null;
    }

    /// Builds the accepted tier table for classes outside named precedence.
    private static Map<Integer, Integer> createOtherTiers() {
        Map<Integer, Integer> tiers = new HashMap<>();
        put(tiers, 3, 913, 914, 915, 918, 919, 920, 921, 996);
        put(tiers, 2, 901, 990, 997);
        put(tiers, 3, 832, 831, 830, 829, 828, 827, 826, 823, 817, 816);
        put(tiers, 2, 887, 881, 880);
        put(tiers, 1, 897, 890, 801);
        put(
                tiers, 3, 733, 732, 731, 730, 716, 717, 729, 728, 727, 726, 723, 722, 746, 747, 748,
                752, 753, 756, 757, 758, 760, 761, 762, 764, 767);
        put(tiers, 2, 765, 743);
        put(tiers, 1, 701, 797, 790);
        put(tiers, 3, 670, 671, 677);
        put(tiers, 2, 680, 681, 687);
        put(tiers, 1, 654, 655, 668);
        put(tiers, 3, 516, 517, 522, 523, 526, 527, 528, 529, 530, 531, 532, 533);
        put(tiers, 2, 580, 581, 587);
        put(tiers, 1, 501, 590, 597);
        for (int value = 413; value <= 423; value++) tiers.put(value, 3);
        for (int value = 426; value <= 433; value++) tiers.put(value, 3);
        put(tiers, 3, 496);
        put(tiers, 2, 480, 481, 487);
        put(tiers, 1, 401, 490, 497);
        put(tiers, 3, 313, 314, 315, 318, 319, 320, 321, 396, 397);
        put(tiers, 2, 301, 390);
        for (int value = 202; value <= 213; value++) tiers.put(value, 3);
        put(tiers, 3, 224, 225, 234, 278, 294, 295, 297, 218, 219, 220, 221);
        put(tiers, 2, 201, 290, 236, 288);
        for (int value = 101; value <= 199; value++) tiers.put(value, 3);
        return Map.copyOf(tiers);
    }

    /// Adds classes that share one comparison tier.
    private static void put(Map<Integer, Integer> tiers, int tier, int... classes) {
        for (int assessmentClass : classes) tiers.put(assessmentClass, tier);
    }

    /// Overall-class result with the complete replacement parcel.
    ///
    /// @param parcel replacement parcel after class selection
    /// @param changed whether the selected class differs from the prior class
    /// @param reportRows number of parcel report rows produced
    /// @param messages recoverable rule observations
    public record Result(
            AssessmentParcel parcel,
            boolean changed,
            int reportRows,
            List<AssessedValueRuleMessage> messages) {
        /// Defensively copies the observation list.
        public Result {
            messages = List.copyOf(messages);
        }
    }

    /// Comparable class candidate for the non-named decision path.
    private record Candidate(
            int assessmentClass, int major, int tier, BigDecimal valuation, int minor)
            implements Comparable<Candidate> {
        /// Builds the comparison tuple, including the accepted neutral class-88 behavior.
        private static Candidate from(AssessmentDetail detail, int tier) {
            int assessmentClass = detail.assessmentClass();
            int minor = Math.floorMod(assessmentClass, 100);
            int major = assessmentClass / 100;
            int effectiveTier = tier;
            if (minor == 88) {
                major = 0;
                effectiveTier = 0;
                minor = 0;
            }
            return new Candidate(assessmentClass, major, effectiveTier, detail.valuation(), minor);
        }

        /// Compares major class, tier, whole-dollar valuation, then minor class.
        @Override
        public int compareTo(Candidate other) {
            int comparison = Integer.compare(major, other.major);
            if (comparison != 0) return comparison;
            comparison = Integer.compare(tier, other.tier);
            if (comparison != 0) return comparison;
            comparison = valuation.compareTo(other.valuation);
            return comparison != 0 ? comparison : Integer.compare(minor, other.minor);
        }
    }
}
