package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

class AssessedValueType5KernelTest {

    private final AssessedValueType5Kernel kernel =
            new AssessedValueType5Kernel(new AssessmentDetailValuator());

    @Test
    void reportYearUsesStrictGreaterThanSixtyPivot() {
        assertThat(kernel.reportYear("61")).isEqualTo(1961);
        assertThat(kernel.reportYear("60")).isEqualTo(2060);
        assertThat(kernel.reportYear("00")).isEqualTo(2000);
    }

    @Test
    void clearsType2YrWithoutInventingAConversion() {
        AssessmentDetail original = improvement("2", 500, 1_000L, null, "YR");
        var result = kernel.convert(parcel(0L), List.of(original), 2026);
        assertThat(result.details().getFirst().supplementalDetailCode()).isEmpty();
        assertThat(result.detailsChanged()).isTrue();
        assertThat(result.parcelChanged()).isFalse();
        assertThat(original.supplementalDetailCode()).isEqualTo("YR");
    }

    @ParameterizedTest(name = "occupancy {0} reports and converts without gross-up")
    @ValueSource(strings = {"0", "-1.0"})
    void nonpositiveOccupancyReportsAndConvertsWithoutGrossUp(String occupancy) {
        var result =
                kernel.convert(
                        parcel(0L), List.of(improvement("5", 500, 10_000L, occupancy, "GD")), 2026);
        AssessmentDetail converted = result.details().getFirst();
        assertThat(converted.reproductionCost()).isEqualTo(BigDecimal.valueOf(10_000));
        assertThat(converted.detailType()).isEqualTo("3");
        assertThat(result.conversionErrorRows()).isEqualTo(1);
    }

    @Test
    void positiveOccupancyNormalizesCostAndMapsTypeThreeFields() {
        var result =
                kernel.convert(
                        parcel(0L), List.of(improvement("5", 500, 10_000L, "50.0", "GD")), 2026);
        AssessmentDetail converted = result.details().getFirst();
        assertThat(converted.reproductionCost()).isEqualTo(BigDecimal.valueOf(20_000));
        assertThat(converted.detailType()).isEqualTo("3");
        assertThat(converted.detailCode()).isEqualTo("2");
        assertThat(converted.improvementYear()).isEqualTo(0);
        assertThat(converted.supplementalDetailCode()).isEqualTo("GD");
        assertThat(converted.keyParcelNumber()).isEqualTo("000000000000042");
    }

    @Test
    void revaluesWholeParcelSeparatesTotalsAndArchivesPriorTotal() {
        AssessmentDetail land = land(1_000L, 2, "2.00");
        AssessmentDetail converted = improvement("5", 500, 10_000L, "50.0", "GD");
        AssessmentDetail other = improvement("3", 500, 5_000L, null, "GD");

        var result = kernel.convert(parcel(999L), List.of(land, converted, other), 2026);

        assertThat(result.details().stream().map(AssessmentDetail::valuation).toList())
                .containsExactly(
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(20_000),
                        BigDecimal.valueOf(5_000))
                .inOrder();
        assertThat(result.parcel().proposedLandValue()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(result.parcel().proposedImprovementValue()).isEqualTo(BigDecimal.valueOf(25000));
        assertThat(result.parcel().proposedTotalValue()).isEqualTo(BigDecimal.valueOf(25020));
        assertThat(result.parcel().archivedPreConversionProposedTotal())
                .isEqualTo(BigDecimal.valueOf(999));
    }

    @Test
    void attachedQuestionnaireIsExcludedFromBothPasses() {
        AssessmentDetail parent = improvement("5", 502, 10_000L, "50.0", "GD");
        AssessmentDetail questionnaire = improvement("3", 500, 99_999L, null, "GD");
        AssessmentDetail later = improvement("3", 500, 100L, null, "GD");
        var result = kernel.convert(parcel(0L), List.of(parent, questionnaire, later), 2026);
        assertThat(result.details().get(1)).isEqualTo(questionnaire);
        assertThat(result.parcel().proposedImprovementValue()).isEqualTo(BigDecimal.valueOf(20100));
    }

    private static AssessmentParcel parcel(long proposedTotal) {
        return new AssessmentParcel(
                1L,
                0L,
                BigDecimal.valueOf(0),
                "1",
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                0,
                BigDecimal.valueOf(0),
                202,
                String.format("%015d", 1L),
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(proposedTotal),
                0,
                String.format("%05d", 10_001),
                "0",
                String.format("%03d", 1),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private static AssessmentDetail improvement(
            String type,
            int assessmentClass,
            long cost,
            @Nullable String occupancy,
            String supplementalDetailCode) {
        return new AssessmentDetail(
                1L,
                0L,
                7,
                null,
                assessmentClass,
                supplementalDetailCode,
                new BigDecimal("100.0"),
                null,
                null,
                null,
                null,
                "5",
                type,
                null,
                null,
                26,
                String.format("%015d", 42L),
                null,
                0,
                decimal(occupancy),
                1,
                String.format("%015d", 1L),
                String.format("%03d", 1),
                new BigDecimal("100.00000"),
                BigDecimal.valueOf(cost),
                null,
                null,
                null,
                BigDecimal.valueOf(cost));
    }

    private static AssessmentDetail land(long frontage, int scale, String price) {
        return new AssessmentDetail(
                1L,
                0L,
                null,
                null,
                100,
                null,
                null,
                null,
                scale,
                null,
                null,
                "0",
                "1",
                null,
                frontage,
                null,
                null,
                null,
                0,
                null,
                1,
                String.format("%015d", 1L),
                String.format("%03d", 1),
                null,
                null,
                null,
                null,
                new BigDecimal(price),
                BigDecimal.valueOf(0));
    }

    private static @Nullable BigDecimal decimal(@Nullable String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
