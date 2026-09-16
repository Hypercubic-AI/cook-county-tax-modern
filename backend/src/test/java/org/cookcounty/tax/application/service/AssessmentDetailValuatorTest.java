package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.junit.jupiter.api.Test;

class AssessmentDetailValuatorTest {

    private final AssessmentDetailValuator valuator = new AssessmentDetailValuator();

    @Test
    void asrea003_001_valuesOnlyTheSuppliedDetailsValuationField() {
        AssessmentDetail detail = land(100L, 0, "2.00");
        detail.setId(7L);
        detail.setOccurrenceNumber(3);
        detail.setCdu("GD");

        valuator.value(detail);

        assertEquals(200L, detail.getValuation());
        assertEquals(7L, detail.getId());
        assertEquals(3, detail.getOccurrenceNumber());
        assertEquals("GD", detail.getCdu());
    }

    @Test
    void asrea003_002_exemptAndRailroadLandAreZero() {
        for (String unit : new String[] {"EX", "RR"}) {
            AssessmentDetail detail = land(10_000L, 2, "100.00");
            detail.setUnitMeasure(unit);
            detail.setValuation(99L);

            valuator.value(detail);

            assertEquals(0L, detail.getValuation());
        }
    }

    @Test
    void asrea003_002_landUsesScaleAndEveryPositiveAdjustment() {
        AssessmentDetail detail = land(12_345L, 2, "2.00");
        detail.setDepthFactor(new BigDecimal("1.100"));
        detail.setCornerFactor(new BigDecimal("1.2000"));
        detail.setPercentAssessed(new BigDecimal("50.00000"));
        detail.setExtraCornerFactor(new BigDecimal("1.50000"));
        detail.setLandConditionFactor(new BigDecimal("80.0"));

        valuator.value(detail);

        assertEquals(195L, detail.getValuation());
        assertEquals(12_345L, detail.getFrontFootage());
        assertEquals(new BigDecimal("2.00"), detail.getUnitPrice());
    }

    @Test
    void asrea003_002_nonpositiveOptionalLandAdjustmentsAreIgnored() {
        AssessmentDetail detail = land(100L, 0, "2.00");
        detail.setDepthFactor(BigDecimal.ZERO);
        detail.setCornerFactor(new BigDecimal("-1.0"));
        detail.setPercentAssessed(BigDecimal.ZERO);
        detail.setExtraCornerFactor(new BigDecimal("-1.0"));
        detail.setLandConditionFactor(BigDecimal.ZERO);

        valuator.value(detail);

        assertEquals(200L, detail.getValuation());
    }

    @Test
    void asrea003_002_allSupportedLandScalesDecodeTheSameQuantity() {
        IntStream.rangeClosed(0, 5).forEach(scale -> {
            AssessmentDetail detail = land(12L * powerOfTen(scale), scale, "2.00");
            valuator.value(detail);
            assertEquals(24L, detail.getValuation(), "decimal scale " + scale);
        });
    }

    @Test
    void asrea003_003_unsupportedLandAndAreaScalesFollowOneDollarPath() {
        IntStream.rangeClosed(6, 9).forEach(scale -> {
            AssessmentDetail land = land(1_000_000L, scale, "99.00");
            valuator.value(land);
            assertEquals(1L, land.getValuation(), "land decimal scale " + scale);

            AssessmentDetail area = improvement("2", 202, 0L);
            area.setArea(1_000_000L);
            area.setDecimalScale(scale);
            area.setUnitPrice(new BigDecimal("99.00"));
            area.setConditionFactor(new BigDecimal("100.0"));
            valuator.value(area);
            assertEquals(1L, area.getValuation(), "area decimal scale " + scale);
        });
    }

    @Test
    void asrea003_004_type2AppliesConditionAndOptionalAssessedPercent() {
        AssessmentDetail detail = improvement("2", 202, 0L);
        detail.setArea(12_345L);
        detail.setDecimalScale(2);
        detail.setUnitPrice(new BigDecimal("2.00"));
        detail.setConditionFactor(new BigDecimal("80.0"));
        detail.setPercentAssessed(new BigDecimal("50.00000"));

        valuator.value(detail);

        assertEquals(98L, detail.getValuation());
    }

    @Test
    void asrea003_004_type2BelowOneIsRaisedToOne() {
        AssessmentDetail detail = improvement("2", 202, 0L);
        detail.setArea(1L);
        detail.setDecimalScale(5);
        detail.setUnitPrice(new BigDecimal("0.01"));
        detail.setConditionFactor(BigDecimal.ZERO);

        valuator.value(detail);

        assertEquals(1L, detail.getValuation());
    }

    @Test
    void asrea003_005_class288ThresholdBandsPreserveZeroAtBoundaryAndDeductAboveIt() {
        assertThreshold(78, 15_000L);
        assertThreshold(79, 25_000L);
        assertThreshold(83, 25_000L);
        assertThreshold(84, 30_000L);
        assertThreshold(97, 30_000L);
        assertThreshold(98, 45_000L);
        assertThreshold(3, 45_000L);
        assertThreshold(4, 75_000L);
        assertThreshold(60, 75_000L);
        assertThreshold(61, 15_000L);
    }

    @Test
    void asrea003_006_type3StartsFromCostAndAppliesOnlyPositiveFactors() {
        AssessmentDetail detail = improvement("3", 300, 20_000L);
        detail.setConditionFactor(new BigDecimal("80.0"));
        detail.setPercentAssessed(new BigDecimal("50.00000"));

        valuator.value(detail);

        assertEquals(8_000L, detail.getValuation());
    }

    @Test
    void asrea003_006_type4And5RequirePositiveConditionButStillFloorAtOne() {
        for (String type : new String[] {"4", "5"}) {
            AssessmentDetail noCondition = improvement(type, 400, 20_000L);
            noCondition.setConditionFactor(BigDecimal.ZERO);
            noCondition.setPercentAssessed(new BigDecimal("50.00000"));
            valuator.value(noCondition);
            assertEquals(1L, noCondition.getValuation());

            AssessmentDetail conditioned = improvement(type, 400, 20_000L);
            conditioned.setConditionFactor(new BigDecimal("80.0"));
            conditioned.setPercentAssessed(new BigDecimal("50.00000"));
            valuator.value(conditioned);
            assertEquals(8_000L, conditioned.getValuation());
        }
    }

    private void assertThreshold(int year, long threshold) {
        AssessmentDetail atThreshold = improvement("3", 288, threshold);
        atThreshold.setImprovementYear(year);
        valuator.value(atThreshold);
        assertEquals(0L, atThreshold.getValuation(), "at threshold for year " + year);

        AssessmentDetail aboveThreshold = improvement("3", 288, threshold + 10L);
        aboveThreshold.setImprovementYear(year);
        valuator.value(aboveThreshold);
        assertEquals(10L, aboveThreshold.getValuation(), "above threshold for year " + year);
    }

    private static AssessmentDetail land(long frontage, int scale, String price) {
        AssessmentDetail detail = new AssessmentDetail();
        detail.setDetailType("1");
        detail.setAssessmentClass(100);
        detail.setFrontFootage(frontage);
        detail.setDecimalScale(scale);
        detail.setUnitPrice(new BigDecimal(price));
        return detail;
    }

    private static AssessmentDetail improvement(String type, int assessmentClass, long cost) {
        AssessmentDetail detail = new AssessmentDetail();
        detail.setDetailType(type);
        detail.setAssessmentClass(assessmentClass);
        detail.setReproductionCost(cost);
        return detail;
    }

    private static long powerOfTen(int exponent) {
        long result = 1L;
        for (int index = 0; index < exponent; index++) {
            result *= 10L;
        }
        return result;
    }
}
