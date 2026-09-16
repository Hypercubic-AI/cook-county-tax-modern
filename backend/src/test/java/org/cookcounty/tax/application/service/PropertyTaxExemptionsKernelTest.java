package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class PropertyTaxExemptionsKernelTest {

    private final PropertyTaxExemptionsKernel kernel =
            new PropertyTaxExemptionsKernel(new AssessmentDetailValuator());

    @Test
    void reviewedNoDetailParcelIsNonResidentialForBothVariants() {
        AssessmentParcel parcel = parcel("010011000010000");

        assertThat(kernel.evaluate(parcel, List.of(), HomeownerVariant.ENUMERATED).eligible())
                .isFalse();
        assertThat(kernel.evaluate(parcel, List.of(), HomeownerVariant.BROAD).eligible()).isFalse();
    }

    @Test
    void enumeratedClass297UsesExclusiveValueFloor() {
        AssessmentParcel parcel = parcel("010011000010000");
        AssessmentDetail atFloor = detail(2, 297, new BigDecimal("1000"), "50", "10");
        AssessmentDetail aboveFloor = detail(2, 297, new BigDecimal("1001"), "50", "10");

        assertThat(
                        kernel.evaluate(parcel, List.of(atFloor), HomeownerVariant.ENUMERATED)
                                .eligible())
                .isFalse();
        assertThat(
                        kernel.evaluate(parcel, List.of(aboveFloor), HomeownerVariant.ENUMERATED)
                                .eligible())
                .isTrue();
    }

    @Test
    void enumeratedClass299ExcludesOnlyReviewedGarageSuffixRange() {
        AssessmentDetail garage = detail(2, 299, new BigDecimal("1"), "50", "10");

        assertThat(
                        kernel.evaluate(
                                        parcel("010011000011500"),
                                        List.of(garage),
                                        HomeownerVariant.ENUMERATED)
                                .eligible())
                .isFalse();
        assertThat(
                        kernel.evaluate(
                                        parcel("010011000013000"),
                                        List.of(garage),
                                        HomeownerVariant.ENUMERATED)
                                .eligible())
                .isTrue();
    }

    @Test
    void broadVariantAcceptsClassInsideInclusiveRangeWithoutEnumeratedCarveOuts() {
        AssessmentDetail detail = detail(2, 150, new BigDecimal("0"), "50", "10");

        assertThat(
                        kernel.evaluate(
                                        parcel("010011000011500"),
                                        List.of(detail),
                                        HomeownerVariant.BROAD)
                                .eligible())
                .isTrue();
    }

    @Test
    void type4UsesSharedValuationAndRetainsOccupancyAndProration() {
        AssessmentDetail detail =
                detail(
                        4,
                        202,
                        BigDecimal.ZERO,
                        "80",
                        "42",
                        new BigDecimal("100000"),
                        new BigDecimal("50"));

        var decision =
                kernel.evaluate(
                        parcel("010011000010000"), List.of(detail), HomeownerVariant.ENUMERATED);

        assertThat(detail.valuation()).isEqualTo(BigDecimal.ZERO);
        assertThat(decision.assessedValue()).isEqualTo(new BigDecimal("40000"));
        assertThat(decision.occupancyFactor().compareTo(new BigDecimal("42"))).isEqualTo(0);
        assertThat(decision.secondaryOccupancyFactor().compareTo(new BigDecimal("100")))
                .isEqualTo(0);
        assertThat(decision.proration().compareTo(new BigDecimal("0.800000"))).isEqualTo(0);
    }

    @Test
    void multiplePercentagesUseRoundedArithmeticMean() {
        AssessmentDetail first = detail(2, 202, new BigDecimal("1"), "50", "10");
        AssessmentDetail second = detail(2, 203, new BigDecimal("1"), "75", "20");

        var decision =
                kernel.evaluate(
                        parcel("010011000010000"),
                        List.of(first, second),
                        HomeownerVariant.ENUMERATED);

        assertThat(decision.proration().compareTo(new BigDecimal("0.625000"))).isEqualTo(0);
        assertThat(decision.occupancyFactor().compareTo(new BigDecimal("20"))).isEqualTo(0);
    }

    private static AssessmentParcel parcel(String parcelNumber) {
        return new AssessmentParcel(
                null,
                null,
                BigDecimal.ZERO,
                "",
                "",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100000"),
                0,
                BigDecimal.ZERO,
                202,
                parcelNumber,
                "",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                "00000",
                "0",
                "001",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static AssessmentDetail detail(
            int type, int assessmentClass, BigDecimal valuation, String percent, String occupancy) {
        return detail(type, assessmentClass, valuation, percent, occupancy, null, null);
    }

    private static AssessmentDetail detail(
            int type,
            int assessmentClass,
            BigDecimal valuation,
            String percent,
            String occupancy,
            @Nullable BigDecimal reproductionCost,
            @Nullable BigDecimal conditionFactor) {
        return new AssessmentDetail(
                null,
                null,
                null,
                null,
                assessmentClass,
                "",
                conditionFactor,
                null,
                null,
                null,
                null,
                "",
                Integer.toString(type),
                null,
                null,
                null,
                null,
                null,
                0,
                new BigDecimal(occupancy),
                1,
                "000000000000001",
                "001",
                new BigDecimal(percent),
                reproductionCost,
                null,
                null,
                null,
                valuation);
    }
}
