package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.stereotype.Component;

/** Applies the accepted ASREA852/ASREA853 detail-level eligibility policies. */
@Component
public class PropertyTaxExemptionsKernel {

    private static final Set<Integer> ENUMERATED_CLASSES = Set.of(
            202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213,
            218, 219, 220, 221, 234, 236, 278, 294, 295);
    private static final BigDecimal ONE = new BigDecimal("1.000000");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final AssessmentDetailValuator valuator;

    public PropertyTaxExemptionsKernel(AssessmentDetailValuator valuator) {
        this.valuator = valuator;
    }

    public EligibilityDecision evaluate(
            AssessmentParcel parcel,
            List<AssessmentDetail> details,
            HomeownerVariant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("homeowner variant is required");
        }

        List<BigDecimal> qualifyingPercentages = new ArrayList<>();
        Set<Long> keyParcels = new HashSet<>();
        Set<String> splitCodes = new HashSet<>();
        BigDecimal maximumOccupancy = BigDecimal.ZERO;
        boolean eligible = false;
        boolean fullSecondaryOccupancy = false;
        boolean class299 = false;
        int selectedClass = valueOrZero(parcel.getOverallClass());
        long selectedValue = valueOrZero(parcel.getCurrentTotalValue());

        for (AssessmentDetail detail : details) {
            int detailType = parseDetailType(detail.getDetailType());
            if (detailType < 2 || detailType > 5) {
                continue;
            }
            if (detailType == 4 || detailType == 5) {
                valuator.value(detail);
            }

            int assessmentClass = valueOrZero(detail.getAssessmentClass());
            long valuation = valueOrZero(detail.getValuation());
            if (!qualifies(parcel.getParcelNumber(), variant, assessmentClass, valuation)) {
                continue;
            }

            eligible = true;
            selectedClass = assessmentClass;
            selectedValue = valuation;
            class299 |= assessmentClass == 299;
            fullSecondaryOccupancy |= detailType <= 4;
            if (isPositive(detail.getPercentAssessed())) {
                qualifyingPercentages.add(detail.getPercentAssessed());
            }
            if (isPositive(detail.getOccupancyFactor())
                    && detail.getOccupancyFactor().compareTo(maximumOccupancy) > 0) {
                maximumOccupancy = detail.getOccupancyFactor();
            }
            if (detail.getKeyParcelNumber() != null && detail.getKeyParcelNumber() > 0) {
                keyParcels.add(detail.getKeyParcelNumber());
            }
            if (detail.getSplitCode() != null && !detail.getSplitCode().isBlank()) {
                splitCodes.add(detail.getSplitCode());
            }
        }

        List<EligibilityWarning> warnings = new ArrayList<>(2);
        if (keyParcels.size() > 1) {
            warnings.add(new EligibilityWarning(
                    "asrea859-003", "Conflicting key-parcel values were retained as a warning."));
        }
        if (splitCodes.size() > 1) {
            warnings.add(new EligibilityWarning(
                    "asrea859-003", "Conflicting split codes were retained as a warning."));
        }

        return new EligibilityDecision(
                eligible,
                selectedClass,
                selectedValue,
                proration(class299, qualifyingPercentages),
                maximumOccupancy,
                fullSecondaryOccupancy ? ONE_HUNDRED : maximumOccupancy,
                keyParcels.stream().sorted().findFirst().orElse(parcel.getParcelNumber()),
                splitCodes.stream().sorted().findFirst().orElse(null),
                List.copyOf(warnings));
    }

    public BigDecimal proration(boolean class299, List<BigDecimal> percentages) {
        if (class299 || percentages.isEmpty()) {
            return ONE;
        }
        BigDecimal total = percentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averagePercent = total.divide(
                BigDecimal.valueOf(percentages.size()), 8, RoundingMode.HALF_UP);
        return averagePercent.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
    }

    boolean qualifies(
            Long parcelNumber,
            HomeownerVariant variant,
            int assessmentClass,
            long valuation) {
        if (variant == HomeownerVariant.BROAD) {
            return assessmentClass >= 100 && assessmentClass <= 899;
        }
        if (assessmentClass == 297) {
            return valuation > 1_000;
        }
        if (assessmentClass == 299) {
            return valuation > 0 && !isExcludedGarageSuffix(parcelNumber);
        }
        return ENUMERATED_CLASSES.contains(assessmentClass);
    }

    private static boolean isExcludedGarageSuffix(Long parcelNumber) {
        if (parcelNumber == null) {
            return false;
        }
        long suffix = Math.floorMod(parcelNumber, 10_000L);
        return suffix >= 1_000 && suffix <= 2_999;
    }

    private static int parseDetailType(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    public enum HomeownerVariant {
        ENUMERATED,
        BROAD
    }

    public record EligibilityDecision(
            boolean eligible,
            int assessmentClass,
            long assessedValue,
            BigDecimal proration,
            BigDecimal occupancyFactor,
            BigDecimal secondaryOccupancyFactor,
            Long keyParcelNumber,
            String splitCode,
            List<EligibilityWarning> warnings) {}

    public record EligibilityWarning(String ruleId, String message) {}
}
