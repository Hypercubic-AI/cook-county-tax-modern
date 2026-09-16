package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/// Calculates the whole-dollar assessed value for one land or improvement detail.
///
/// The calculator reads no repository and publishes no batch output. It decodes frontage or area
/// with the detail's decimal selector. It applies only positive optional factors and truncates the
/// final amount toward zero. Unsupported detail types preserve their existing valuation.
@Component
public final class AssessmentDetailValuator {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    /// Class-288 whole-dollar relief caps selected by the final year of each source-defined period.
    private static final BigDecimal RELIEF_THROUGH_1978 = new BigDecimal("15000");

    private static final BigDecimal RELIEF_THROUGH_1983 = new BigDecimal("25000");
    private static final BigDecimal RELIEF_THROUGH_1997 = new BigDecimal("30000");
    private static final BigDecimal RELIEF_THROUGH_2003 = new BigDecimal("45000");
    private static final BigDecimal RELIEF_AFTER_2003 = new BigDecimal("75000");

    /// Returns a complete detail with its type-specific valuation replaced.
    ///
    /// @param detail source detail whose factors and encoded quantities drive the calculation
    /// @return a replacement detail, or the supplied detail when its type is not valued here
    /// @throws ArithmeticException when the calculated amount exceeds the detail's nine-digit
    ///   precision
    public AssessmentDetail value(AssessmentDetail detail) {
        return switch (detail.detailType()) {
            case "1" -> valueLand(detail);
            case "2" -> valueAreaImprovement(detail);
            case "3" -> valueReproductionCostImprovement(detail);
            case "4", "5" -> valueConditionedReproductionCostImprovement(detail);
            default -> detail;
        };
    }

    /// Values land from decoded frontage, unit price, and positive adjustment factors.
    private static AssessmentDetail valueLand(AssessmentDetail detail) {
        if ("EX".equals(detail.unitMeasure()) || "RR".equals(detail.unitMeasure())) {
            return detail.withValuation(BigDecimal.ZERO);
        }

        BigDecimal amount =
                scaled(detail.frontFootage(), detail.decimalScale())
                        .multiply(orZero(detail.unitPrice()));
        amount = multiplyWhenPositive(amount, detail.depthFactor());
        amount = multiplyWhenPositive(amount, detail.cornerFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.percentAssessed());
        amount = multiplyWhenPositive(amount, detail.extraCornerFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.landConditionFactor());
        return detail.withValuation(atLeastOne(amount));
    }

    /// Values an area improvement from decoded area, unit price, and percentage factors.
    private static AssessmentDetail valueAreaImprovement(AssessmentDetail detail) {
        BigDecimal amount =
                scaled(detail.area(), detail.decimalScale())
                        .multiply(orZero(detail.unitPrice()))
                        .multiply(percentage(orZero(detail.conditionFactor())));
        amount = multiplyPercentageWhenPositive(amount, detail.percentAssessed());
        return detail.withValuation(atLeastOne(amount));
    }

    /// Values a reproduction-cost improvement and applies class-288 relief when applicable.
    private static AssessmentDetail valueReproductionCostImprovement(AssessmentDetail detail) {
        BigDecimal reproductionCost = orZero(detail.reproductionCost());
        if (Integer.valueOf(288).equals(detail.assessmentClass())) {
            BigDecimal threshold = class288Threshold(detail.improvementYear());
            if (reproductionCost.compareTo(threshold) <= 0) {
                return detail.withValuation(BigDecimal.ZERO);
            }
            reproductionCost = reproductionCost.subtract(threshold);
        }

        BigDecimal amount = reproductionCost;
        amount = multiplyPercentageWhenPositive(amount, detail.conditionFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.percentAssessed());
        return detail.withValuation(atLeastOne(amount));
    }

    /// Values Type-4 or Type-5 cost only when condition is positive.
    private static AssessmentDetail valueConditionedReproductionCostImprovement(
            AssessmentDetail detail) {
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal condition = detail.conditionFactor();
        if (condition != null && condition.signum() > 0) {
            amount = orZero(detail.reproductionCost()).multiply(percentage(condition));
        }
        amount = multiplyPercentageWhenPositive(amount, detail.percentAssessed());
        return detail.withValuation(atLeastOne(amount));
    }

    /// Decodes a source integer with selectors zero through five.
    private static BigDecimal scaled(@Nullable Long encodedValue, @Nullable Integer decimalScale) {
        if (encodedValue == null || decimalScale == null || decimalScale < 0 || decimalScale > 5) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(encodedValue).movePointLeft(decimalScale);
    }

    /// Applies a positive multiplicative factor and ignores absent or nonpositive factors.
    private static BigDecimal multiplyWhenPositive(BigDecimal amount, @Nullable BigDecimal factor) {
        return factor != null && factor.signum() > 0 ? amount.multiply(factor) : amount;
    }

    /// Applies a positive percentage and ignores absent or nonpositive percentages.
    private static BigDecimal multiplyPercentageWhenPositive(
            BigDecimal amount, @Nullable BigDecimal percent) {
        return percent != null && percent.signum() > 0
                ? amount.multiply(percentage(percent))
                : amount;
    }

    /// Converts a stored percentage number to its multiplier.
    private static BigDecimal percentage(BigDecimal value) {
        return value.divide(ONE_HUNDRED, MathContext.UNLIMITED);
    }

    /// Converts an absent optional decimal to the source calculation's zero value.
    private static BigDecimal orZero(@Nullable BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /// Applies the observed one-dollar minimum and truncates higher amounts toward zero.
    private static BigDecimal atLeastOne(BigDecimal amount) {
        return amount.max(BigDecimal.ONE).setScale(0, RoundingMode.DOWN);
    }

    /// Selects class-288 relief from the two-digit improvement year.
    private static BigDecimal class288Threshold(@Nullable Integer encodedYear) {
        int year = encodedYear == null ? 0 : encodedYear % 100;
        int fullYear = year > 60 ? 1900 + year : 2000 + year;
        if (fullYear <= 1978) {
            return RELIEF_THROUGH_1978;
        }
        if (fullYear <= 1983) {
            return RELIEF_THROUGH_1983;
        }
        if (fullYear <= 1997) {
            return RELIEF_THROUGH_1997;
        }
        if (fullYear <= 2003) {
            return RELIEF_THROUGH_2003;
        }
        return RELIEF_AFTER_2003;
    }
}
