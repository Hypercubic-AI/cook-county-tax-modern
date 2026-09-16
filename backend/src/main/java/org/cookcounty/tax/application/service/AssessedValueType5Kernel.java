package org.cookcounty.tax.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.stereotype.Component;

/** Parcel-local ASREA178 Type-5 conversion and whole-parcel revaluation policy. */
@Component
public class AssessedValueType5Kernel {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final AssessmentDetailValuator valuator;

    public AssessedValueType5Kernel(AssessmentDetailValuator valuator) {
        this.valuator = valuator;
    }

    public Result convert(
            AssessmentParcel parcel, List<AssessmentDetail> details, int reportYear) {
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        boolean foundType5 = false;
        boolean detailChanged = false;
        int conversionErrorRows = 0;
        String recordKey = AssessedValueRuleSupport.recordKey(parcel);

        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            if ("2".equals(detail.getDetailType()) && "YR".equals(detail.getCdu())) {
                detail.setCdu("");
                detailChanged = true;
            }
            if ("5".equals(detail.getDetailType())) {
                foundType5 = true;
                detailChanged = true;
                BigDecimal occupancy = detail.getOccupancyFactor();
                if (occupancy != null && occupancy.signum() > 0) {
                    BigDecimal normalizedCost = BigDecimal.valueOf(AssessedValueRuleSupport.orZero(detail.getReproductionCost()))
                            .multiply(ONE_HUNDRED)
                            .divide(occupancy, 0, RoundingMode.DOWN);
                    detail.setReproductionCost(normalizedCost.longValueExact());
                } else {
                    conversionErrorRows++;
                    messages.add(new AssessedValueRuleMessage(
                            "WARNING", "NONPOSITIVE_TYPE5_OCCUPANCY", "asrea178-003",
                            "The Type-5 detail was converted without reproduction-cost gross-up for report year "
                                    + reportYear + " because occupancy was nonpositive.",
                            recordKey));
                }
                detail.setDetailType("3");
                detail.setDetailCode("2");
                detail.setImprovementYear(0);
                if ("YR".equals(detail.getCdu())) {
                    detail.setCdu("");
                }
            }
            if (AssessedValueRuleSupport.isType5ResidentialQuestionnaireBearing(detail)) {
                index++;
            }
        }

        if (!foundType5) {
            return new Result(false, detailChanged, conversionErrorRows, List.copyOf(messages));
        }

        long proposedLand = 0L;
        long proposedImprovement = 0L;
        for (int index = 0; index < details.size(); index++) {
            AssessmentDetail detail = details.get(index);
            if (usesUnsupportedDecimalSelector(detail)) {
                messages.add(new AssessedValueRuleMessage(
                        "WARNING", "UNSUPPORTED_DECIMAL_SELECTOR", "asrea003-003",
                        "Decimal selector " + detail.getDecimalScale()
                                + " followed the observed one-dollar legacy path.",
                        recordKey));
            }
            valuator.value(detail);
            if ("1".equals(detail.getDetailType())) {
                proposedLand += AssessedValueRuleSupport.value(detail);
            } else {
                proposedImprovement += AssessedValueRuleSupport.value(detail);
            }
            if (AssessedValueRuleSupport.isType5ResidentialQuestionnaireBearing(detail)) {
                index++;
            }
        }

        parcel.setArchivedPreConversionProposedTotal(
                AssessedValueRuleSupport.orZero(parcel.getProposedTotalValue()));
        parcel.setProposedLandValue(proposedLand);
        parcel.setProposedImprovementValue(proposedImprovement);
        parcel.setProposedTotalValue(proposedLand + proposedImprovement);
        return new Result(true, true, conversionErrorRows, List.copyOf(messages));
    }

    public int reportYear(String processYear) {
        if (processYear == null || !processYear.matches("[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "processYear must contain exactly two decimal digits.");
        }
        int year = Integer.parseInt(processYear);
        return year > 60 ? 1900 + year : 2000 + year;
    }

    private static boolean usesUnsupportedDecimalSelector(AssessmentDetail detail) {
        if (!("1".equals(detail.getDetailType()) || "2".equals(detail.getDetailType()))) {
            return false;
        }
        Integer selector = detail.getDecimalScale();
        return selector != null && selector >= 6 && selector <= 9;
    }

    public record Result(
            boolean parcelChanged,
            boolean detailsChanged,
            int conversionErrorRows,
            List<AssessedValueRuleMessage> messages) {}
}
