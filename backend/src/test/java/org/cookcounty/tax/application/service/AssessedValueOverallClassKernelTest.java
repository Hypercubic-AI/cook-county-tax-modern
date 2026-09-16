package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

class AssessedValueOverallClassKernelTest {

    private final AssessedValueOverallClassKernel kernel = new AssessedValueOverallClassKernel();

    @Test
    void asrea018_002_landOnlyUsesNumericallyHighestLandClass() {
        AssessmentParcel parcel = parcel(200);

        AssessedValueOverallClassKernel.Result result = kernel.prepare(
                parcel, List.of(detail("1", 101, 1L), detail("1", 199, 1L)));

        assertEquals(199, parcel.getOverallClass());
        assertTrue(result.changed());
    }

    @Test
    void asrea018_002_absentLandIsReported() {
        AssessmentParcel parcel = parcel(200);

        AssessedValueOverallClassKernel.Result result = kernel.prepare(
                parcel, List.of(detail("2", 299, 1L)));

        assertTrue(result.messages().stream().anyMatch(message -> "asrea018-002".equals(message.ruleId())));
    }

    @Test
    void asrea018_003_namedClassesUseFixedFirstMatchPrecedence() {
        AssessmentParcel parcel = parcel(200);

        kernel.prepare(parcel, List.of(
                detail("2", 299, 1_000_000L),
                detail("3", 889, 1L),
                detail("4", 959, 1L)));

        assertEquals(959, parcel.getOverallClass());
    }

    @Test
    void asrea018_004_otherClassesCompareMajorThenTierThenValueThenMinor() {
        AssessmentParcel parcel = parcel(200);
        kernel.prepare(parcel, List.of(
                detail("2", 897, 999_999L),
                detail("2", 832, 1L)));
        assertEquals(832, parcel.getOverallClass(), "tier outranks valuation within major class");

        parcel.setOverallClass(200);
        kernel.prepare(parcel, List.of(
                detail("2", 832, 1L),
                detail("2", 733, 999_999L)));
        assertEquals(832, parcel.getOverallClass(), "major class outranks lower-major valuation");

        parcel.setOverallClass(200);
        kernel.prepare(parcel, List.of(
                detail("2", 832, 100L),
                detail("2", 831, 200L)));
        assertEquals(831, parcel.getOverallClass(), "valuation breaks equal major and tier");

        parcel.setOverallClass(200);
        kernel.prepare(parcel, List.of(
                detail("2", 831, 200L),
                detail("2", 832, 200L)));
        assertEquals(832, parcel.getOverallClass(), "minor class breaks a valuation tie");
    }

    @Test
    void asrea018_004_minor88IsNeutralizedForComparison() {
        AssessmentParcel parcel = parcel(200);

        kernel.prepare(parcel, List.of(
                detail("2", 288, 999_999L),
                detail("2", 201, 1L)));

        assertEquals(201, parcel.getOverallClass());
    }

    @Test
    void asrea018_005_unresolvedImprovementPreservesPriorClassAndReportsIt() {
        AssessmentParcel parcel = parcel(745);

        AssessedValueOverallClassKernel.Result result = kernel.prepare(
                parcel, List.of(detail("2", 250, 1_000L)));

        assertEquals(745, parcel.getOverallClass());
        assertTrue(result.messages().stream().anyMatch(message -> "asrea018-005".equals(message.ruleId())));
    }

    @Test
    void asrea018_006_questionnaireOccurrenceDoesNotParticipate() {
        AssessmentParcel parcel = parcel(100);

        kernel.prepare(parcel, List.of(
                detail("2", 202, 1L),
                detail("2", 959, 1_000_000L),
                detail("2", 299, 1L)));

        assertEquals(299, parcel.getOverallClass());
    }

    @Test
    void asrea018_007_major9Tier2ContaminationIsRuleAddressed() {
        AssessmentParcel parcel = parcel(100);

        AssessedValueOverallClassKernel.Result result = kernel.prepare(
                parcel, List.of(detail("2", 901, 10L)));

        assertEquals(901, parcel.getOverallClass());
        assertTrue(result.messages().stream().anyMatch(message -> "asrea018-007".equals(message.ruleId())));
    }

    private static AssessmentParcel parcel(int overallClass) {
        AssessmentParcel parcel = new AssessmentParcel();
        parcel.setOverallClass(overallClass);
        parcel.setTaxCode(10001);
        parcel.setVolumeNumber(1);
        parcel.setParcelNumber(1L);
        parcel.setTaxType("0");
        return parcel;
    }

    private static AssessmentDetail detail(String type, int assessmentClass, long valuation) {
        AssessmentDetail detail = new AssessmentDetail();
        detail.setDetailType(type);
        detail.setAssessmentClass(assessmentClass);
        detail.setValuation(valuation);
        return detail;
    }
}
