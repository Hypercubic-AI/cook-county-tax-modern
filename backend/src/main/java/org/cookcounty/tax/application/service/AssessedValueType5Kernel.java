package org.cookcounty.tax.application.service;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/// Converts Type-5 details and revalues the complete parcel detail sequence.
///
/// Conversion preserves source order. A recognized residential improvement owns the immediately
/// following questionnaire occurrence, which conversion and revaluation skip. A nonpositive
/// occupancy produces a recoverable warning and retains the existing reproduction cost.
@Component
public final class AssessedValueType5Kernel {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private final AssessmentDetailValuator valuator;

    /// Creates the kernel with the detail calculator used for whole-parcel revaluation.
    ///
    /// @param valuator side-effect-free calculator for one immutable detail
    public AssessedValueType5Kernel(AssessmentDetailValuator valuator) {
        this.valuator = valuator;
    }

    /// Converts Type-5 details, then revalues all non-questionnaire details when conversion occurs.
    ///
    /// @param parcel parcel whose proposed value slots receive recalculated whole-dollar totals
    /// @param details source-ordered details, including questionnaire companions
    /// @param reportYear four-digit year displayed in recoverable conversion warnings
    /// @return complete replacement parcel and detail list with typed conversion observations
    public Result convert(AssessmentParcel parcel, List<AssessmentDetail> details, int reportYear) {
        List<AssessedValueRuleMessage> messages = new ArrayList<>();
        List<AssessmentDetail> converted = new ArrayList<>(details);
        boolean foundType5 = false;
        boolean detailsChanged = false;
        int conversionErrorRows = 0;
        String recordKey = AssessedValueRuleSupport.recordKey(parcel);

        for (int index = 0; index < converted.size(); index++) {
            AssessmentDetail detail = converted.get(index);
            if ("2".equals(detail.detailType()) && "YR".equals(detail.supplementalDetailCode())) {
                detail = detail.withSupplementalDetailCode("");
                converted.set(index, detail);
                detailsChanged = true;
            }
            if ("5".equals(detail.detailType())) {
                foundType5 = true;
                BigDecimal occupancy = detail.occupancyFactor();
                BigDecimal normalizedCost =
                        AssessedValueRuleSupport.orZero(detail.reproductionCost());
                if (occupancy != null && occupancy.signum() > 0) {
                    normalizedCost =
                            normalizedCost
                                    .multiply(ONE_HUNDRED)
                                    .divide(occupancy, 0, RoundingMode.DOWN);
                } else {
                    conversionErrorRows++;
                    messages.add(
                            new AssessedValueRuleMessage(
                                    "WARNING",
                                    "NONPOSITIVE_TYPE5_OCCUPANCY",
                                    "asrea178-003",
                                    "The Type-5 detail was converted without reproduction-cost"
                                            + " gross-up for report year "
                                            + reportYear
                                            + " because occupancy was nonpositive.",
                                    recordKey));
                }
                detail = detail.convertedFromType5(normalizedCost);
                converted.set(index, detail);
            }
            if (AssessedValueRuleSupport.isType5ResidentialQuestionnaireBearing(detail)) {
                index++;
            }
        }

        if (!foundType5) {
            return new Result(
                    parcel,
                    List.copyOf(converted),
                    false,
                    detailsChanged,
                    conversionErrorRows,
                    List.copyOf(messages));
        }

        BigDecimal proposedLand = BigDecimal.ZERO;
        BigDecimal proposedImprovement = BigDecimal.ZERO;
        for (int index = 0; index < converted.size(); index++) {
            AssessmentDetail detail = converted.get(index);
            if (usesUnsupportedDecimalSelector(detail)) {
                messages.add(
                        new AssessedValueRuleMessage(
                                "WARNING",
                                "UNSUPPORTED_DECIMAL_SELECTOR",
                                "asrea003-003",
                                "Decimal selector "
                                        + detail.decimalScale()
                                        + " followed the observed one-dollar legacy path.",
                                recordKey));
            }
            detail = valuator.value(detail);
            converted.set(index, detail);
            if ("1".equals(detail.detailType())) {
                proposedLand = proposedLand.add(detail.valuation());
            } else {
                proposedImprovement = proposedImprovement.add(detail.valuation());
            }
            if (AssessedValueRuleSupport.isType5ResidentialQuestionnaireBearing(detail)) {
                index++;
            }
        }

        AssessmentParcel convertedParcel =
                parcel.withProposedValuation(proposedLand, proposedImprovement);
        return new Result(
                convertedParcel,
                List.copyOf(converted),
                true,
                true,
                conversionErrorRows,
                List.copyOf(messages));
    }

    /// Converts the two-digit process year to the report year used by conversion messages.
    ///
    /// Values greater than 60 map to 19xx. All other values map to 20xx.
    ///
    /// @param processYear exactly two decimal digits
    /// @return four-digit report year
    /// @throws IllegalArgumentException when the value is absent or not two decimal digits
    public int reportYear(String processYear) {
        if (!processYear.matches("[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "processYear must contain exactly two decimal digits.");
        }
        int year = Integer.parseInt(processYear);
        return year > 60 ? 1900 + year : 2000 + year;
    }

    /// Reports the accepted legacy selector path that produces the one-dollar minimum.
    private static boolean usesUnsupportedDecimalSelector(AssessmentDetail detail) {
        if (!("1".equals(detail.detailType()) || "2".equals(detail.detailType()))) {
            return false;
        }
        Integer selector = detail.decimalScale();
        return selector != null && selector >= 6 && selector <= 9;
    }

    /// Type-5 outcome with complete immutable replacements.
    ///
    /// @param parcel replacement parcel containing recomputed proposed totals
    /// @param details replacement source-ordered detail sequence
    /// @param parcelChanged whether Type-5 conversion recalculated parcel values
    /// @param detailsChanged whether conversion, code clearing, or revaluation changed details
    /// @param conversionErrorRows number of nonpositive-occupancy report rows
    /// @param messages recoverable rule observations
    public record Result(
            AssessmentParcel parcel,
            List<AssessmentDetail> details,
            boolean parcelChanged,
            boolean detailsChanged,
            int conversionErrorRows,
            List<AssessedValueRuleMessage> messages) {
        /// Defensively copies both returned collections.
        public Result {
            details = List.copyOf(details);
            messages = List.copyOf(messages);
        }
    }
}
