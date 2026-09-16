package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class AssessedValuePreparationProcessorTest {

    @Test
    void reviewedValprepTenRootScenarioHasZeroDetailsAndZeroType5Conversions() {
        List<AssessmentParcel> roots = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            roots.add(parcel((long) index + 1, 10 + index, index + 1, 10_000L + index, "0"));
        }
        InMemoryParcelRepository parcels = new InMemoryParcelRepository(roots);
        InMemoryDetailRepository details = new InMemoryDetailRepository(List.of());

        ProcessResult result = processor(parcels, details).process(
                LocalDate.of(2025, 9, 15), "12:00:00", "26");

        assertFalse(result.failed());
        assertEquals(0, result.returnCode());
        assertEquals(30, result.recordsRead());
        assertEquals(30, result.recordsWritten());
        assertEquals(10, result.recordsUpdated());
        assertEquals(0, result.recordsRejected());
        assertEquals(10, result.messages().stream()
                .filter(message -> "asrea018-002".equals(message.ruleId()))
                .count());
        StageOutcome overall = result.stages().get(0);
        assertEquals(10L, overall.metrics().get("reported"));
        assertEquals("25/09/20",
                result.outputs().get(0).recordData().get(0).substring(3, 11));
        StageOutcome bucketing = result.stages().get(1);
        assertEquals(0L, bucketing.metrics().get("farm"));
        assertEquals(0L, bucketing.metrics().get("homeowner"));
        assertEquals(0L, bucketing.metrics().get("nonHomeowner"));
        assertEquals("20250920",
                result.outputs().get(1).recordData().get(1).substring(3, 11));
        assertEquals(0, details.saveCalls);
        StageOutcome conversion = result.stages().get(2);
        assertEquals("COMPLETED", conversion.status());
        assertEquals(0, conversion.recordsUpdated());
        assertEquals(0, conversion.reportRows());
        assertEquals(0L, conversion.metrics().get("type5Hits"));
        assertEquals(0L, conversion.metrics().get("zeroType5"));
        assertEquals(0L, conversion.metrics().get("convertedType5"));
    }

    @Test
    void asrea018_001_continuesPublishingLaterRootsButMarksPartialFailure() {
        AssessmentParcel first = parcel(1L, 20, 1, 1L, "0");
        AssessmentParcel descending = parcel(2L, 10, 2, 2L, "0");
        AssessmentParcel later = parcel(3L, 30, 3, 3L, "0");
        InMemoryParcelRepository parcels = new InMemoryParcelRepository(
                List.of(first, descending, later));

        ProcessResult result = processor(parcels, new InMemoryDetailRepository(List.of())).process(
                LocalDate.of(2025, 9, 15), "12:00:00", "26");

        assertTrue(result.failed());
        assertTrue(result.partialOutput());
        assertEquals(16, result.returnCode());
        assertTrue(result.messages().stream().anyMatch(message -> "asrea018-001".equals(message.ruleId())));
        assertTrue(parcels.savedIds.contains(later.getId()), "later overall-class root was published");
        assertEquals("FAILED", result.stages().get(0).status());
    }

    @Test
    void asrea151_001_stopsAtDescendingRootAndIsFailedDespiteLegacyZeroReturnCode() {
        AssessmentParcel first = parcel(1L, 20, 1, 1L, "0");
        AssessmentParcel descending = parcel(2L, 10, 2, 2L, "0");
        AssessmentParcel later = parcel(3L, 30, 3, 3L, "0");

        ProcessResult result = processor(
                new InMemoryParcelRepository(List.of(first, descending, later)),
                new InMemoryDetailRepository(List.of())).process(
                        LocalDate.of(2025, 9, 15), "12:00:00", "26");

        StageOutcome bucketing = result.stages().get(1);
        assertEquals("FAILED", bucketing.status());
        assertEquals(0, bucketing.returnCode());
        assertEquals(2, bucketing.recordsRead());
        assertEquals(1, bucketing.recordsWritten());
        assertTrue(bucketing.partialOutput());
        assertTrue(result.failed());
        assertTrue(result.messages().stream().anyMatch(message -> "asrea151-001".equals(message.ruleId())));
    }

    private static AssessedValuePreparationProcessor processor(
            AssessmentParcelRepository parcels, AssessmentDetailRepository details) {
        AssessmentDetailValuator valuator = new AssessmentDetailValuator();
        return new AssessedValuePreparationProcessor(
                parcels,
                details,
                new AssessedValueOverallClassKernel(),
                new AssessedValueBucketingKernel(),
                new AssessedValueType5Kernel(valuator));
    }

    private static AssessmentParcel parcel(
            Long id, int township, int volume, long parcelNumber, String taxType) {
        AssessmentParcel parcel = new AssessmentParcel();
        parcel.setId(id);
        parcel.setTaxCode(township * 1_000 + 1);
        parcel.setVolumeNumber(volume);
        parcel.setParcelNumber(parcelNumber);
        parcel.setTaxType(taxType);
        parcel.setOverallClass(202);
        parcel.setPriorTotalValue(1L);
        parcel.setCurrentLandValue(1L);
        parcel.setFarmValue(1L);
        parcel.setCombinedHomeownerNonHomeownerValue(1L);
        parcel.setArchivedPreConversionProposedTotal(1L);
        parcel.setProposedTotalValue(1L);
        return parcel;
    }

    private static final class InMemoryParcelRepository implements AssessmentParcelRepository {
        private final List<AssessmentParcel> records;
        private final List<Long> savedIds = new ArrayList<>();

        private InMemoryParcelRepository(List<AssessmentParcel> records) {
            this.records = new ArrayList<>(records);
        }

        @Override
        public List<AssessmentParcel> findAllInInputOrder() {
            return records;
        }

        @Override
        public Page<AssessmentParcel> findAll(Pageable pageable) {
            return new PageImpl<>(records);
        }

        @Override
        public Optional<AssessmentParcel> findById(Long id) {
            return records.stream().filter(record -> id.equals(record.getId())).findFirst();
        }

        @Override
        public AssessmentParcel save(AssessmentParcel assessmentParcel) {
            savedIds.add(assessmentParcel.getId());
            return assessmentParcel;
        }

        @Override
        public void deleteById(Long id) {
            records.removeIf(record -> id.equals(record.getId()));
        }
    }

    private static final class InMemoryDetailRepository implements AssessmentDetailRepository {
        private final List<AssessmentDetail> records;
        private int saveCalls;

        private InMemoryDetailRepository(List<AssessmentDetail> records) {
            this.records = new ArrayList<>(records);
        }

        @Override
        public List<AssessmentDetail> findAllInInputOrder() {
            return records;
        }

        @Override
        public Page<AssessmentDetail> findAll(Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Optional<AssessmentDetail> findById(Long id) {
            return records.stream().filter(record -> id.equals(record.getId())).findFirst();
        }

        @Override
        public AssessmentDetail save(AssessmentDetail assessmentDetail) {
            saveCalls++;
            return assessmentDetail;
        }

        @Override
        public void deleteById(Long id) {
            records.removeIf(record -> id.equals(record.getId()));
        }
    }
}
