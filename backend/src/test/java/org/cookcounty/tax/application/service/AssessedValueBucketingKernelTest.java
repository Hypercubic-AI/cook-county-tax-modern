package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

class AssessedValueBucketingKernelTest {

    private final AssessedValueBucketingKernel kernel = new AssessedValueBucketingKernel();

    @Test
    void mapsCompiledClassAndReportsNonzeroFallback() {
        var mapped = kernel.bucket(parcel(202), List.of());
        var missed = kernel.bucket(parcel(250), List.of());
        assertThat(mapped.parcel().clerkMajorClass()).isEqualTo("2");
        assertThat(missed.parcel().clerkMajorClass()).isEqualTo("2");
        assertThat(missed.messages().stream().map(AssessedValueRuleMessage::ruleId))
                .contains("asrea151-002");
    }

    @Test
    void residentialThresholdIsStrictlyAboveOneThousandDollars() {
        var atBoundary =
                kernel.bucket(
                        parcel(202), List.of(detail("1", 200, 100L), detail("2", 213, 1_000L)));
        var aboveBoundary =
                kernel.bucket(
                        parcel(202), List.of(detail("1", 200, 100L), detail("2", 213, 1_001L)));
        assertThat(atBoundary.homeownerValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(atBoundary.nonHomeownerValue()).isEqualTo(new BigDecimal("1100"));
        assertThat(aboveBoundary.homeownerValue()).isEqualTo(new BigDecimal("1101"));
    }

    @Test
    void farmAndCombinedBucketsReplaceOnlyDocumentedSlots() {
        AssessmentParcel original = parcel(239);
        var result =
                kernel.bucket(original, List.of(detail("1", 239, 100L), detail("2", 500, 300L)));
        AssessmentParcel bucketed = result.parcel();
        assertThat(bucketed.priorTotalValue()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(bucketed.currentLandValue()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(bucketed.farmValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(bucketed.combinedHomeownerNonHomeownerValue())
                .isEqualTo(result.homeownerValue().add(result.nonHomeownerValue()));
        assertThat(bucketed.archivedPreConversionProposedTotal()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(original.farmValue()).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    void questionnaireIsExcludedFromPresenceAndAccumulationPasses() {
        var result =
                kernel.bucket(
                        parcel(202),
                        List.of(
                                detail("1", 200, 100L),
                                detail("2", 202, 1_001L),
                                detail("2", 299, 999_999L),
                                detail("2", 500, 50L)));
        assertThat(result.homeownerValue()).isEqualTo(new BigDecimal("1151"));
        assertThat(result.nonHomeownerValue()).isEqualTo(BigDecimal.ZERO);
    }

    private static AssessmentParcel parcel(int overallClass) {
        return new AssessmentParcel(
                1L,
                0L,
                BigDecimal.valueOf(3),
                "1",
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(0),
                0,
                BigDecimal.valueOf(1),
                overallClass,
                String.format("%015d", 1L),
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1),
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
