package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.junit.jupiter.api.Test;

class AssessedValueBucketingKernelTest {

    private final AssessedValueBucketingKernel kernel = new AssessedValueBucketingKernel();

    @Test
    void asrea151_002_mapsCompiledClassAndFallsBackOnNonzeroMiss() {
        AssessmentParcel mapped = parcel(202);
        kernel.bucket(mapped, List.of());
        assertEquals("2", mapped.getClerkMajorClass());

        AssessmentParcel missed = parcel(250);
        AssessedValueBucketingKernel.Result result = kernel.bucket(missed, List.of());
        assertEquals("2", missed.getClerkMajorClass());
        assertTrue(result.messages().stream().anyMatch(message -> "asrea151-002".equals(message.ruleId())));
    }

    @Test
    void asrea151_003_residentialEligibilityIsStrictlyAbove1000() {
        AssessedValueBucketingKernel.Result atBoundary = kernel.bucket(
                parcel(202), List.of(detail("1", 200, 100L), detail("2", 213, 1_000L)));
        assertEquals(0L, atBoundary.homeownerValue());
        assertEquals(1_100L, atBoundary.nonHomeownerValue());

        AssessedValueBucketingKernel.Result aboveBoundary = kernel.bucket(
                parcel(202), List.of(detail("1", 200, 100L), detail("2", 213, 1_001L)));
        assertEquals(1_101L, aboveBoundary.homeownerValue());
        assertEquals(0L, aboveBoundary.nonHomeownerValue());
    }

    @Test
    void asrea151_003_class299EligibilityIsStrictlyAbove1499() {
        AssessedValueBucketingKernel.Result atBoundary = kernel.bucket(
                parcel(299), List.of(detail("1", 200, 100L), detail("2", 299, 1_499L)));
        assertEquals(0L, atBoundary.homeownerValue());

        AssessedValueBucketingKernel.Result aboveBoundary = kernel.bucket(
                parcel(299), List.of(detail("1", 200, 100L), detail("2", 299, 1_500L)));
        assertEquals(1_600L, aboveBoundary.homeownerValue());
    }

    @Test
    void asrea151_004_onlyClass239LandAndClass224ImprovementsEnterFarmBucket() {
        AssessedValueBucketingKernel.Result result = kernel.bucket(
                parcel(239), List.of(
                        detail("1", 239, 100L),
                        detail("2", 224, 200L),
                        detail("2", 225, 300L)));

        assertEquals(300L, result.farmValue());
        assertEquals(300L, result.nonHomeownerValue());
    }

    @Test
    void asrea151_005_nonfarmDetailsUseParcelWideHomeownerDecision() {
        AssessedValueBucketingKernel.Result result = kernel.bucket(
                parcel(202), List.of(
                        detail("1", 200, 100L),
                        detail("2", 213, 1_001L),
                        detail("3", 500, 300L)));

        assertEquals(1_401L, result.homeownerValue());
        assertEquals(0L, result.nonHomeownerValue());
    }

    @Test
    void asrea151_006_persistsFarmAndCombinedBucketsWhileClearingDocumentedSlots() {
        AssessmentParcel parcel = parcel(239);
        parcel.setPriorTotalValue(1L);
        parcel.setCurrentLandValue(2L);
        parcel.setArchivedPreConversionProposedTotal(3L);

        AssessedValueBucketingKernel.Result result = kernel.bucket(
                parcel, List.of(detail("1", 239, 100L), detail("2", 500, 300L)));

        assertEquals(0L, parcel.getPriorTotalValue());
        assertEquals(0L, parcel.getCurrentLandValue());
        assertEquals(100L, parcel.getFarmValue());
        assertEquals(result.homeownerValue() + result.nonHomeownerValue(),
                parcel.getCombinedHomeownerNonHomeownerValue());
        assertEquals(0L, parcel.getArchivedPreConversionProposedTotal());
    }

    @Test
    void asrea151_007_questionnaireIsExcludedFromPresenceAndAccumulationPasses() {
        AssessmentParcel parcel = parcel(202);
        AssessedValueBucketingKernel.Result result = kernel.bucket(
                parcel, List.of(
                        detail("1", 200, 100L),
                        detail("2", 202, 1_001L),
                        detail("2", 299, 999_999L),
                        detail("2", 500, 50L)));

        assertEquals(1_151L, result.homeownerValue());
        assertEquals(0L, result.nonHomeownerValue());
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
