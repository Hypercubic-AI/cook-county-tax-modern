package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

class AssessmentDetailValuatorTest {

    private final AssessmentDetailValuator valuator = new AssessmentDetailValuator();

    @Test
    void valuesOnlyTheReplacementDetailsValuation() {
        AssessmentDetail detail = land(100L, 0, "2.00", null, null, null, null, null, null);

        AssessmentDetail valued = valuator.value(detail);

        assertThat(valued.valuation()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(valued.id()).isEqualTo(detail.id());
        assertThat(valued.occurrenceNumber()).isEqualTo(detail.occurrenceNumber());
        assertThat(valued.supplementalDetailCode()).isEqualTo(detail.supplementalDetailCode());
        assertThat(detail.valuation()).isEqualTo(BigDecimal.ZERO);
    }

    @ParameterizedTest(name = "unit {0} has no land value")
    @ValueSource(strings = {"EX", "RR"})
    void exemptAndRailroadLandAreZero(String unit) {
        AssessmentDetail valued =
                valuator.value(land(10_000L, 2, "100.00", unit, null, null, null, null, null));
        assertThat(valued.valuation()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void landUsesScaleAndEveryPositiveAdjustment() {
        AssessmentDetail valued =
                valuator.value(
                        land(
                                12_345L,
                                2,
                                "2.00",
                                null,
                                "1.100",
                                "1.2000",
                                "50.00000",
                                "1.50000",
                                "80.0"));
        assertThat(valued.valuation()).isEqualTo(BigDecimal.valueOf(195));
    }

    @ParameterizedTest(name = "unsupported decimal scale {0} uses one dollar")
    @ValueSource(ints = {6, 7, 8, 9})
    void unsupportedLandAndAreaScalesFollowOneDollarPath(int scale) {
        AssessmentDetail land =
                valuator.value(
                        land(1_000_000L, scale, "99.00", null, null, null, null, null, null));
        AssessmentDetail area =
                valuator.value(
                        improvement(
                                "2",
                                202,
                                0L,
                                1_000_000L,
                                scale,
                                "99.00",
                                "100.0",
                                null,
                                null,
                                null));
        assertThat(land.valuation()).isEqualTo(BigDecimal.ONE);
        assertThat(area.valuation()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void areaImprovementAppliesConditionAndAssessedPercent() {
        AssessmentDetail valued =
                valuator.value(
                        improvement(
                                "2", 202, 0L, 12_345L, 2, "2.00", "80.0", "50.00000", null, null));
        assertThat(valued.valuation()).isEqualTo(BigDecimal.valueOf(98));
    }

    @ParameterizedTest(name = "year {0} deducts only above {1}")
    @CsvSource({
        "78, 15000", "79, 25000", "83, 25000", "84, 30000", "97, 30000",
        "98, 45000", "3, 45000", "4, 75000", "60, 75000", "61, 15000"
    })
    void class288ThresholdBandsPreserveZeroAtBoundaryAndDeductAboveIt(int year, long threshold) {
        AssessmentDetail atThreshold =
                valuator.value(
                        improvement("3", 288, threshold, null, null, null, null, null, year, null));
        AssessmentDetail aboveThreshold =
                valuator.value(
                        improvement(
                                "3",
                                288,
                                threshold + 10L,
                                null,
                                null,
                                null,
                                null,
                                null,
                                year,
                                null));
        assertThat(atThreshold.valuation()).isEqualTo(BigDecimal.ZERO);
        assertThat(aboveThreshold.valuation()).isEqualTo(BigDecimal.TEN);
    }

    @ParameterizedTest(name = "type {0} requires positive condition")
    @ValueSource(strings = {"4", "5"})
    void type4And5RequirePositiveConditionButStillFloorAtOne(String type) {
        AssessmentDetail withoutCondition =
                valuator.value(
                        improvement(
                                type, 400, 20_000L, null, null, null, "0", "50.00000", null, null));
        AssessmentDetail conditioned =
                valuator.value(
                        improvement(
                                type,
                                400,
                                20_000L,
                                null,
                                null,
                                null,
                                "80.0",
                                "50.00000",
                                null,
                                null));
        assertThat(withoutCondition.valuation()).isEqualTo(BigDecimal.ONE);
        assertThat(conditioned.valuation()).isEqualTo(BigDecimal.valueOf(8_000));
    }

    private static AssessmentDetail land(
            long frontage,
            int scale,
            String price,
            @Nullable String unit,
            @Nullable String depthFactor,
            @Nullable String cornerFactor,
            @Nullable String percent,
            @Nullable String extraCornerFactor,
            @Nullable String landConditionFactor) {
        return new AssessmentDetail(
                7L,
                0L,
                null,
                null,
                100,
                "GD",
                null,
                decimal(cornerFactor),
                scale,
                null,
                decimal(depthFactor),
                "0",
                "1",
                decimal(extraCornerFactor),
                frontage,
                null,
                null,
                decimal(landConditionFactor),
                0,
                null,
                3,
                String.format("%015d", 1L),
                String.format("%03d", 1),
                decimal(percent),
                null,
                null,
                unit,
                new BigDecimal(price),
                BigDecimal.valueOf(0));
    }

    private static AssessmentDetail improvement(
            String type,
            int assessmentClass,
            long cost,
            @Nullable Long area,
            @Nullable Integer scale,
            @Nullable String price,
            @Nullable String condition,
            @Nullable String percent,
            @Nullable Integer year,
            @Nullable String occupancy) {
        return new AssessmentDetail(
                7L,
                0L,
                0,
                area,
                assessmentClass,
                "GD",
                decimal(condition),
                null,
                scale,
                null,
                null,
                "0",
                type,
                null,
                null,
                year,
                String.format("%015d", 42L),
                null,
                0,
                decimal(occupancy),
                3,
                String.format("%015d", 1L),
                String.format("%03d", 1),
                decimal(percent),
                BigDecimal.valueOf(cost),
                null,
                null,
                decimal(price),
                BigDecimal.valueOf(0));
    }

    private static @Nullable BigDecimal decimal(@Nullable String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
