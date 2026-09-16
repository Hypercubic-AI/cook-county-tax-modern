package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// Calculates homeowner eligibility and proration from one parcel and its ordered details.
@Component
public class PropertyTaxExemptionsKernel {

    private static final Set<Integer> ENUMERATED_CLASSES =
            Set.of(
                    202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 218, 219, 220, 221,
                    234, 236, 278, 294, 295);
    private static final BigDecimal ONE = new BigDecimal("1.000000");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final AssessmentDetailValuator valuator;

    /// Creates the decision kernel with the canonical assessment-detail valuator.
    public PropertyTaxExemptionsKernel(AssessmentDetailValuator valuator) {
        this.valuator = valuator;
    }

    /// Returns the eligibility decision without persistence side effects.
    public EligibilityDecision evaluate(
            AssessmentParcel parcel, List<AssessmentDetail> details, HomeownerVariant variant) {

        List<BigDecimal> qualifyingPercentages = new ArrayList<>();
        Set<String> keyParcels = new HashSet<>();
        Set<String> splitCodes = new HashSet<>();
        BigDecimal maximumOccupancy = BigDecimal.ZERO;
        boolean eligible = false;
        boolean fullSecondaryOccupancy = false;
        boolean class299 = false;
        int selectedClass = valueOrZero(parcel.overallClass());
        BigDecimal selectedValue = valueOrZero(parcel.currentTotalValue());

        for (AssessmentDetail detail : details) {
            int detailType = parseDetailType(detail.detailType());
            if (detailType < 2 || detailType > 5) {
                continue;
            }
            if (detailType == 4 || detailType == 5) {
                detail = valuator.value(detail);
            }

            int assessmentClass = valueOrZero(detail.assessmentClass());
            BigDecimal valuation = valueOrZero(detail.valuation());
            if (!qualifies(parcel.parcelNumber(), variant, assessmentClass, valuation)) {
                continue;
            }

            eligible = true;
            selectedClass = assessmentClass;
            selectedValue = valuation;
            class299 |= assessmentClass == 299;
            fullSecondaryOccupancy |= detailType <= 4;
            BigDecimal percentAssessed = detail.percentAssessed();
            if (percentAssessed != null && percentAssessed.signum() > 0) {
                qualifyingPercentages.add(percentAssessed);
            }
            BigDecimal occupancyFactor = detail.occupancyFactor();
            if (occupancyFactor != null
                    && occupancyFactor.signum() > 0
                    && occupancyFactor.compareTo(maximumOccupancy) > 0) {
                maximumOccupancy = occupancyFactor;
            }
            if (detail.keyParcelNumber() != null
                    && new BigInteger(detail.keyParcelNumber()).signum() > 0) {
                keyParcels.add(detail.keyParcelNumber());
            }
            if (detail.splitCode() != null && !detail.splitCode().isBlank()) {
                splitCodes.add(detail.splitCode());
            }
        }

        List<EligibilityWarning> warnings = new ArrayList<>(2);
        if (keyParcels.size() > 1) {
            warnings.add(
                    new EligibilityWarning(
                            "asrea859-003",
                            "Conflicting key-parcel values were retained as a warning."));
        }
        if (splitCodes.size() > 1) {
            warnings.add(
                    new EligibilityWarning(
                            "asrea859-003", "Conflicting split codes were retained as a warning."));
        }

        return new EligibilityDecision(
                eligible,
                selectedClass,
                selectedValue,
                proration(class299, qualifyingPercentages),
                maximumOccupancy,
                fullSecondaryOccupancy ? ONE_HUNDRED : maximumOccupancy,
                keyParcels.stream().sorted().findFirst().orElse(parcel.parcelNumber()),
                splitCodes.stream().sorted().findFirst().orElse(null),
                List.copyOf(warnings));
    }

    /// Calculates the rounded mean eligible share as a six-place decimal fraction.
    ///
    /// Class 299 and an empty percentage input both produce a full share.
    public BigDecimal proration(boolean class299, List<BigDecimal> percentages) {
        if (class299 || percentages.isEmpty()) {
            return ONE;
        }
        BigDecimal total = percentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averagePercent =
                total.divide(BigDecimal.valueOf(percentages.size()), 8, RoundingMode.HALF_UP);
        return averagePercent.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
    }

    boolean qualifies(
            String parcelNumber,
            HomeownerVariant variant,
            int assessmentClass,
            BigDecimal valuation) {
        if (variant == HomeownerVariant.BROAD) {
            return assessmentClass >= 100 && assessmentClass <= 899;
        }
        if (assessmentClass == 297) {
            return valuation.compareTo(BigDecimal.valueOf(1_000)) > 0;
        }
        if (assessmentClass == 299) {
            return valuation.signum() > 0 && !isExcludedGarageSuffix(parcelNumber);
        }
        return ENUMERATED_CLASSES.contains(assessmentClass);
    }

    /// Tests the numeric property suffix at the source arithmetic boundary.
    private static boolean isExcludedGarageSuffix(String parcelNumber) {
        int suffix = new BigInteger(parcelNumber).abs().mod(BigInteger.valueOf(10_000)).intValue();
        return suffix >= 1_000 && suffix <= 2_999;
    }

    private static int parseDetailType(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static int valueOrZero(@Nullable Integer value) {
        return value == null ? 0 : value;
    }

    private static BigDecimal valueOrZero(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /// Selects either the source enumeration or the broad numeric classification policy.
    public enum HomeownerVariant {
        ENUMERATED,
        BROAD
    }

    /// Immutable result of evaluating all eligible details for one parcel.
    ///
    /// @param eligible whether at least one detail qualifies
    /// @param assessmentClass selected property classification code
    /// @param assessedValue selected assessed valuation in whole dollars
    /// @param proration rounded eligible share as a six-place decimal fraction
    /// @param occupancyFactor greatest primary occupancy percentage
    /// @param secondaryOccupancyFactor greatest secondary occupancy percentage
    /// @param keyParcelNumber related parcel identity selected from the details
    /// @param splitCode unique split code, or absent when none or multiple values exist
    /// @param warnings nonfatal detail ambiguities
    public record EligibilityDecision(
            boolean eligible,
            int assessmentClass,
            BigDecimal assessedValue,
            BigDecimal proration,
            BigDecimal occupancyFactor,
            BigDecimal secondaryOccupancyFactor,
            String keyParcelNumber,
            @Nullable String splitCode,
            List<EligibilityWarning> warnings) {
        /// Copies ambiguity warnings so the decision does not retain caller-owned mutable state.
        public EligibilityDecision {
            warnings = List.copyOf(warnings);
        }
    }

    /// One nonfatal ambiguity found while selecting parcel detail.
    ///
    /// @param ruleId governing eligibility rule
    /// @param message explanation of the ambiguity
    public record EligibilityWarning(String ruleId, String message) {}
}
