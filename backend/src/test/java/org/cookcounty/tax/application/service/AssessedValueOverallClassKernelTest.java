package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class AssessedValueOverallClassKernelTest {

    private final AssessedValueOverallClassKernel kernel = new AssessedValueOverallClassKernel();

    @Test
    void landOnlyUsesNumericallyHighestLandClass() {
        AssessmentParcel original = parcel(200);
        var result = kernel.prepare(original, List.of(detail("1", 101, 1L), detail("1", 199, 1L)));
        assertThat(result.parcel().overallClass()).isEqualTo(199);
        assertThat(result.changed()).isTrue();
        assertThat(original.overallClass()).isEqualTo(200);
    }

    @Test
    void namedClassesUseFixedFirstMatchPrecedence() {
        var result =
                kernel.prepare(
                        parcel(200),
                        List.of(
                                detail("2", 299, 1_000_000L),
                                detail("3", 889, 1L),
                                detail("4", 959, 1L)));
        assertThat(result.parcel().overallClass()).isEqualTo(959);
    }

    @Test
    void otherClassesCompareMajorThenTierThenValueThenMinor() {
        assertThat(
                        kernel.prepare(
                                        parcel(200),
                                        List.of(detail("2", 897, 999_999L), detail("2", 832, 1L)))
                                .parcel()
                                .overallClass())
                .isEqualTo(832);
        assertThat(
                        kernel.prepare(
                                        parcel(200),
                                        List.of(detail("2", 832, 100L), detail("2", 831, 200L)))
                                .parcel()
                                .overallClass())
                .isEqualTo(831);
        assertThat(
                        kernel.prepare(
                                        parcel(200),
                                        List.of(detail("2", 831, 200L), detail("2", 832, 200L)))
                                .parcel()
                                .overallClass())
                .isEqualTo(832);
    }

    @Test
    void unresolvedImprovementPreservesPriorClassAndReportsIt() {
        var result = kernel.prepare(parcel(745), List.of(detail("2", 250, 1_000L)));
        assertThat(result.parcel().overallClass()).isEqualTo(745);
        assertThat(result.messages().stream().map(AssessedValueRuleMessage::ruleId))
                .contains("asrea018-005");
    }

    @Test
    void questionnaireOccurrenceDoesNotParticipate() {
        var result =
                kernel.prepare(
                        parcel(100),
                        List.of(
                                detail("2", 202, 1L),
                                detail("2", 959, 1_000_000L),
                                detail("2", 299, 1L)));
        assertThat(result.parcel().overallClass()).isEqualTo(299);
    }

    private static AssessmentParcel parcel(int overallClass) {
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
                overallClass,
                String.format("%015d", 1L),
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
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

    private static AssessmentDetail detail(String type, int assessmentClass, long valuation) {
        return new AssessmentDetail(
                1L,
                0L,
                null,
                null,
                assessmentClass,
                null,
                null,
                null,
                null,
                null,
                null,
                "0",
                type,
                null,
                null,
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
                null,
                BigDecimal.valueOf(valuation));
    }
}
