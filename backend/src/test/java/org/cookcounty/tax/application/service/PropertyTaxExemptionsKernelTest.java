package org.cookcounty.tax.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

class PropertyTaxExemptionsKernelTest {

    private final PropertyTaxExemptionsKernel kernel =
            new PropertyTaxExemptionsKernel(new AssessmentDetailValuator());

    @Test
    void reviewedNoDetailParcelIsNonResidentialForBothVariants() {
        AssessmentParcel parcel = parcel(10011000010000L);

        assertThat(kernel.evaluate(parcel, List.of(), HomeownerVariant.ENUMERATED).eligible())
                .isFalse();
        assertThat(kernel.evaluate(parcel, List.of(), HomeownerVariant.BROAD).eligible())
                .isFalse();
    }

    @Test
    void enumeratedClass297UsesExclusiveValueFloor() {
        AssessmentParcel parcel = parcel(10011000010000L);
        AssessmentDetail atFloor = detail(2, 297, 1_000L, "50", "10");
        AssessmentDetail aboveFloor = detail(2, 297, 1_001L, "50", "10");

        assertThat(kernel.evaluate(parcel, List.of(atFloor), HomeownerVariant.ENUMERATED).eligible())
                .isFalse();
        assertThat(kernel.evaluate(parcel, List.of(aboveFloor), HomeownerVariant.ENUMERATED).eligible())
                .isTrue();
    }

    @Test
    void enumeratedClass299ExcludesOnlyReviewedGarageSuffixRange() {
        AssessmentDetail garage = detail(2, 299, 1L, "50", "10");

        assertThat(kernel.evaluate(parcel(10011000011500L), List.of(garage),
                HomeownerVariant.ENUMERATED).eligible()).isFalse();
        assertThat(kernel.evaluate(parcel(10011000013000L), List.of(garage),
                HomeownerVariant.ENUMERATED).eligible()).isTrue();
    }

    @Test
    void broadVariantAcceptsClassInsideInclusiveRangeWithoutEnumeratedCarveOuts() {
        AssessmentDetail detail = detail(2, 150, 0L, "50", "10");

        assertThat(kernel.evaluate(parcel(10011000011500L), List.of(detail),
                HomeownerVariant.BROAD).eligible()).isTrue();
    }

    @Test
    void type4UsesSharedValuationAndRetainsOccupancyAndProration() {
        AssessmentDetail detail = detail(4, 202, null, "80", "42");
        detail.setReproductionCost(100_000L);
        detail.setConditionFactor(new BigDecimal("50"));

        var decision = kernel.evaluate(
                parcel(10011000010000L), List.of(detail), HomeownerVariant.ENUMERATED);

        assertThat(detail.getValuation()).isEqualTo(40_000L);
        assertThat(decision.assessedValue()).isEqualTo(40_000L);
        assertThat(decision.occupancyFactor()).isEqualByComparingTo("42");
        assertThat(decision.secondaryOccupancyFactor()).isEqualByComparingTo("100");
        assertThat(decision.proration()).isEqualByComparingTo("0.800000");
    }

    @Test
    void multiplePercentagesUseRoundedArithmeticMean() {
        AssessmentDetail first = detail(2, 202, 1L, "50", "10");
        AssessmentDetail second = detail(2, 203, 1L, "75", "20");

        var decision = kernel.evaluate(
                parcel(10011000010000L), List.of(first, second), HomeownerVariant.ENUMERATED);

        assertThat(decision.proration()).isEqualByComparingTo("0.625000");
        assertThat(decision.occupancyFactor()).isEqualByComparingTo("20");
    }

    private static AssessmentParcel parcel(long parcelNumber) {
        AssessmentParcel parcel = new AssessmentParcel();
        parcel.setParcelNumber(parcelNumber);
        parcel.setVolumeNumber(1);
        parcel.setOverallClass(202);
        parcel.setCurrentTotalValue(100_000L);
        return parcel;
    }

    private static AssessmentDetail detail(
            int type,
            int assessmentClass,
            Long valuation,
            String percent,
            String occupancy) {
        AssessmentDetail detail = new AssessmentDetail();
        detail.setDetailType(Integer.toString(type));
        detail.setAssessmentClass(assessmentClass);
        detail.setValuation(valuation);
        detail.setPercentAssessed(new BigDecimal(percent));
        detail.setOccupancyFactor(new BigDecimal(occupancy));
        return detail;
    }
}
