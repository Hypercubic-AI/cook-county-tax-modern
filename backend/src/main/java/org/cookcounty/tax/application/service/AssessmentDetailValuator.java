package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.springframework.stereotype.Component;

/** Applies the accepted ASREA003 policy to one supplied detail occurrence. */
@Component
public class AssessmentDetailValuator {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public void value(AssessmentDetail detail) {
        String type = detail.getDetailType();
        if ("1".equals(type)) {
            valueLand(detail);
        } else if ("2".equals(type)) {
            valueAreaImprovement(detail);
        } else if ("3".equals(type)) {
            valueReproductionCostImprovement(detail);
        } else if ("4".equals(type) || "5".equals(type)) {
            valueConditionedReproductionCostImprovement(detail);
        }
    }

    private void valueLand(AssessmentDetail detail) {
        if ("EX".equals(detail.getUnitMeasure()) || "RR".equals(detail.getUnitMeasure())) {
            detail.setValuation(0L);
            return;
        }

        BigDecimal amount = scaled(detail.getFrontFootage(), detail.getDecimalScale())
                .multiply(orZero(detail.getUnitPrice()));
        amount = multiplyWhenPositive(amount, detail.getDepthFactor());
        amount = multiplyWhenPositive(amount, detail.getCornerFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.getPercentAssessed());
        amount = multiplyWhenPositive(amount, detail.getExtraCornerFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.getLandConditionFactor());
        detail.setValuation(atLeastOne(amount));
    }

    private void valueAreaImprovement(AssessmentDetail detail) {
        BigDecimal amount = scaled(detail.getArea(), detail.getDecimalScale())
                .multiply(orZero(detail.getUnitPrice()))
                .multiply(percentage(orZero(detail.getConditionFactor())));
        amount = multiplyPercentageWhenPositive(amount, detail.getPercentAssessed());
        detail.setValuation(atLeastOne(amount));
    }

    private void valueReproductionCostImprovement(AssessmentDetail detail) {
        long reproductionCost = orZero(detail.getReproductionCost());
        if (Integer.valueOf(288).equals(detail.getAssessmentClass())) {
            long threshold = class288Threshold(detail.getImprovementYear());
            if (reproductionCost <= threshold) {
                detail.setValuation(0L);
                return;
            }
            reproductionCost -= threshold;
        }

        BigDecimal amount = BigDecimal.valueOf(reproductionCost);
        amount = multiplyPercentageWhenPositive(amount, detail.getConditionFactor());
        amount = multiplyPercentageWhenPositive(amount, detail.getPercentAssessed());
        detail.setValuation(atLeastOne(amount));
    }

    private void valueConditionedReproductionCostImprovement(AssessmentDetail detail) {
        BigDecimal amount = BigDecimal.ZERO;
        if (isPositive(detail.getConditionFactor())) {
            amount = BigDecimal.valueOf(orZero(detail.getReproductionCost()))
                    .multiply(percentage(detail.getConditionFactor()));
        }
        amount = multiplyPercentageWhenPositive(amount, detail.getPercentAssessed());
        detail.setValuation(atLeastOne(amount));
    }

    private static BigDecimal scaled(Long encodedValue, Integer decimalScale) {
        if (encodedValue == null || decimalScale == null || decimalScale < 0 || decimalScale > 5) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(encodedValue).movePointLeft(decimalScale);
    }

    private static BigDecimal multiplyWhenPositive(BigDecimal amount, BigDecimal factor) {
        return isPositive(factor) ? amount.multiply(factor) : amount;
    }

    private static BigDecimal multiplyPercentageWhenPositive(BigDecimal amount, BigDecimal percent) {
        return isPositive(percent) ? amount.multiply(percentage(percent)) : amount;
    }

    private static BigDecimal percentage(BigDecimal value) {
        return value.divide(ONE_HUNDRED);
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static long orZero(Long value) {
        return value == null ? 0L : value;
    }

    private static long atLeastOne(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ONE) < 0) {
            return 1L;
        }
        return amount.setScale(0, RoundingMode.DOWN).longValueExact();
    }

    private static long class288Threshold(Integer encodedYear) {
        int year = encodedYear == null ? 0 : encodedYear % 100;
        int fullYear = year > 60 ? 1900 + year : 2000 + year;
        if (fullYear <= 1978) {
            return 15_000L;
        }
        if (fullYear <= 1983) {
            return 25_000L;
        }
        if (fullYear <= 1997) {
            return 30_000L;
        }
        if (fullYear <= 2003) {
            return 45_000L;
        }
        return 75_000L;
    }
}
