package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

class AssessedValueType5KernelTest {

    private final AssessedValueType5Kernel kernel =
            new AssessedValueType5Kernel(new AssessmentDetailValuator());

    @Test
    void asrea178_001_reportYearUsesStrictGreaterThan60Pivot() {
        assertEquals(1961, kernel.reportYear("61"));
        assertEquals(2060, kernel.reportYear("60"));
        assertEquals(2000, kernel.reportYear("00"));
    }

    @Test
    void asrea178_002_clearsType2YrWithoutInventingAConversion() {
        AssessmentParcel parcel = parcel();
        AssessmentDetail detail = detail("2", 500, 1_000L);
        detail.setCdu("YR");

        AssessedValueType5Kernel.Result result =
                kernel.convert(parcel, List.of(detail), 2026);

        assertEquals("", detail.getCdu());
        assertTrue(result.detailsChanged());
        assertFalse(result.parcelChanged());
    }

    @Test
    void asrea178_003_nonpositiveOccupancyReportsAndConvertsWithoutGrossUp() {
        for (BigDecimal occupancy : List.of(BigDecimal.ZERO, new BigDecimal("-1.0"))) {
            AssessmentParcel parcel = parcel();
            AssessmentDetail detail = detail("5", 500, 10_000L);
            detail.setOccupancyFactor(occupancy);

            AssessedValueType5Kernel.Result result = kernel.convert(parcel, List.of(detail), 2026);

            assertEquals(10_000L, detail.getReproductionCost());
            assertEquals("3", detail.getDetailType());
            assertEquals(1, result.conversionErrorRows());
            assertTrue(result.messages().stream().anyMatch(
                    message -> "asrea178-003".equals(message.ruleId())));
        }
    }

    @Test
    void asrea178_004_positiveOccupancyNormalizesCostAndMapsType3Fields() {
        AssessmentParcel parcel = parcel();
        AssessmentDetail detail = detail("5", 500, 10_000L);
        detail.setOccupancyFactor(new BigDecimal("50.0"));
        detail.setDetailCode("5");
        detail.setImprovementYear(26);
        detail.setCdu("GD");
        detail.setAge(7);
        detail.setKeyParcelNumber(42L);

        kernel.convert(parcel, List.of(detail), 2026);

        assertEquals(20_000L, detail.getReproductionCost());
        assertEquals("3", detail.getDetailType());
        assertEquals("2", detail.getDetailCode());
        assertEquals(0, detail.getImprovementYear());
        assertEquals("GD", detail.getCdu());
        assertEquals(7, detail.getAge());
        assertEquals(42L, detail.getKeyParcelNumber());
    }

    @Test
    void asrea178_005_006_007_revaluesWholeParcelSeparatesTotalsAndArchivesPriorTotal() {
        AssessmentParcel parcel = parcel();
        parcel.setProposedTotalValue(999L);
        AssessmentDetail land = land(1_000L, 2, "2.00");
        AssessmentDetail converted = detail("5", 500, 10_000L);
        converted.setOccupancyFactor(new BigDecimal("50.0"));
        AssessmentDetail otherImprovement = detail("3", 500, 5_000L);

        kernel.convert(parcel, List.of(land, converted, otherImprovement), 2026);

        assertEquals(20L, land.getValuation());
        assertEquals(20_000L, converted.getValuation());
        assertEquals(5_000L, otherImprovement.getValuation());
        assertEquals(20L, parcel.getProposedLandValue());
        assertEquals(25_000L, parcel.getProposedImprovementValue());
        assertEquals(25_020L, parcel.getProposedTotalValue());
        assertEquals(999L, parcel.getArchivedPreConversionProposedTotal());
    }

    @Test
    void asrea178_002_005_006_attachedQuestionnaireIsExcludedFromBothPasses() {
        AssessmentParcel parcel = parcel();
        AssessmentDetail type5Parent = detail("5", 502, 10_000L);
        type5Parent.setOccupancyFactor(new BigDecimal("50.0"));
        AssessmentDetail questionnaire = detail("3", 500, 99_999L);
        questionnaire.setReproductionCost(99_999L);
        AssessmentDetail later = detail("3", 500, 100L);

        kernel.convert(parcel, List.of(type5Parent, questionnaire, later), 2026);

        assertEquals(99_999L, questionnaire.getValuation());
        assertEquals(20_100L, parcel.getProposedImprovementValue());
    }

    @Test
    void asrea003_003_revaluationReportsUnsupportedDecimalSelectorAndUsesOneDollar() {
        AssessmentParcel parcel = parcel();
        AssessmentDetail type5 = detail("5", 500, 100L);
        type5.setOccupancyFactor(new BigDecimal("100.0"));
        AssessmentDetail land = land(1_000L, 6, "10.00");

        AssessedValueType5Kernel.Result result =
                kernel.convert(parcel, List.of(type5, land), 2026);

        assertEquals(1L, land.getValuation());
        assertTrue(result.messages().stream().anyMatch(
                message -> "asrea003-003".equals(message.ruleId())));
    }

    @Test
    void asrea178_008_conversionDoesNotGuardOnTownshipCode() {
        AssessmentParcel parcel = parcel();
        parcel.setTaxCode(99_999);
        AssessmentDetail type5 = detail("5", 500, 100L);
        type5.setOccupancyFactor(new BigDecimal("100.0"));

        AssessedValueType5Kernel.Result result =
                kernel.convert(parcel, List.of(type5), 2026);

        assertTrue(result.parcelChanged());
        assertEquals("3", type5.getDetailType());
    }

    private static AssessmentParcel parcel() {
        AssessmentParcel parcel = new AssessmentParcel();
        parcel.setTaxCode(10001);
        parcel.setVolumeNumber(1);
        parcel.setParcelNumber(1L);
        parcel.setTaxType("0");
        parcel.setProposedTotalValue(0L);
        return parcel;
    }

    private static AssessmentDetail detail(String type, int assessmentClass, long cost) {
        AssessmentDetail detail = new AssessmentDetail();
        detail.setDetailType(type);
        detail.setAssessmentClass(assessmentClass);
        detail.setReproductionCost(cost);
        detail.setValuation(cost);
        detail.setConditionFactor(new BigDecimal("100.0"));
        detail.setPercentAssessed(new BigDecimal("100.00000"));
        return detail;
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
}
