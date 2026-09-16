package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.model.AssessmentDetail;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class AssessedValuePreparationProcessorTest {

    @Test
    void reviewedValprepTenRootScenarioHasZeroDetailsAndZeroType5Conversions() {
        List<AssessmentParcel> roots = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            roots.add(parcel((long) index + 1, 10 + index, index + 1, 10_000L + index, "0"));
        }
        InMemoryParcelRepository parcels = new InMemoryParcelRepository(roots);
        InMemoryDetailRepository details = new InMemoryDetailRepository(List.of());

        ProcessResult result =
                processor(parcels, details).process(LocalDate.of(2025, 9, 15), "12:00:00", "26");

        assertThat(result.failed()).isFalse();
        assertThat(result.returnCode()).isEqualTo(0);
        assertThat(result.recordsRead()).isEqualTo(30);
        assertThat(result.recordsWritten()).isEqualTo(30);
        assertThat(result.recordsUpdated()).isEqualTo(10);
        assertThat(result.recordsRejected()).isEqualTo(0);
        assertThat(
                        result.messages().stream()
                                .filter(message -> "asrea018-002".equals(message.ruleId()))
                                .count())
                .isEqualTo(10);
        StageOutcome overall = result.stages().get(0);
        assertThat(overall.metrics().get("reported")).isEqualTo(10L);
        assertThat(result.outputs().get(0).recordData().get(0).substring(3, 11))
                .isEqualTo("25/09/20");
        StageOutcome bucketing = result.stages().get(1);
        assertThat(bucketing.metrics().get("farm")).isEqualTo(BigDecimal.ZERO);
        assertThat(bucketing.metrics().get("homeowner")).isEqualTo(BigDecimal.ZERO);
        assertThat(bucketing.metrics().get("nonHomeowner")).isEqualTo(BigDecimal.ZERO);
        assertThat(result.outputs().get(1).recordData().get(1).substring(3, 11))
                .isEqualTo("20250920");
        assertThat(details.saveCalls).isEqualTo(0);
        StageOutcome conversion = result.stages().get(2);
        assertThat(conversion.status()).isEqualTo("COMPLETED");
        assertThat(conversion.recordsUpdated()).isEqualTo(0);
        assertThat(conversion.reportRows()).isEqualTo(0);
        assertThat(conversion.metrics().get("type5Hits")).isEqualTo(0L);
        assertThat(conversion.metrics().get("zeroType5")).isEqualTo(0L);
        assertThat(conversion.metrics().get("convertedType5")).isEqualTo(0L);
    }

    @Test
    void overallClassSequenceFailureContinuesPublishingLaterRootsButMarksPartialFailure() {
        AssessmentParcel first = parcel(1L, 20, 1, 1L, "0");
        AssessmentParcel descending = parcel(2L, 10, 2, 2L, "0");
        AssessmentParcel later = parcel(3L, 30, 3, 3L, "0");
        InMemoryParcelRepository parcels =
                new InMemoryParcelRepository(List.of(first, descending, later));

        ProcessResult result =
                processor(parcels, new InMemoryDetailRepository(List.of()))
                        .process(LocalDate.of(2025, 9, 15), "12:00:00", "26");

        assertThat(result.failed()).isTrue();
        assertThat(result.partialOutput()).isTrue();
        assertThat(result.returnCode()).isEqualTo(16);
        assertThat(
                        result.messages().stream()
                                .anyMatch(message -> "asrea018-001".equals(message.ruleId())))
                .isTrue();
        assertWithMessage("later overall-class root was published")
                .that(parcels.savedIds.contains(later.id()))
                .isTrue();
        assertThat(result.stages().get(0).status()).isEqualTo("FAILED");
    }

    @Test
    void valuationBucketingSequenceFailureStopsAtDescendingRootDespiteZeroReturnCode() {
        AssessmentParcel first = parcel(1L, 20, 1, 1L, "0");
        AssessmentParcel descending = parcel(2L, 10, 2, 2L, "0");
        AssessmentParcel later = parcel(3L, 30, 3, 3L, "0");

        ProcessResult result =
                processor(
                                new InMemoryParcelRepository(List.of(first, descending, later)),
                                new InMemoryDetailRepository(List.of()))
                        .process(LocalDate.of(2025, 9, 15), "12:00:00", "26");

        StageOutcome bucketing = result.stages().get(1);
        assertThat(bucketing.status()).isEqualTo("FAILED");
        assertThat(bucketing.returnCode()).isEqualTo(0);
        assertThat(bucketing.recordsRead()).isEqualTo(2);
        assertThat(bucketing.recordsWritten()).isEqualTo(1);
        assertThat(bucketing.partialOutput()).isTrue();
        assertThat(result.failed()).isTrue();
        assertThat(
                        result.messages().stream()
                                .anyMatch(message -> "asrea151-001".equals(message.ruleId())))
                .isTrue();
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
            long id, int township, int volume, long parcelNumber, String taxType) {
        return new AssessmentParcel(
                id,
                0L,
                BigDecimal.valueOf(1),
                "1",
                "0",
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(0),
                0,
                BigDecimal.valueOf(1),
                202,
                String.format("%015d", parcelNumber),
                "0",
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1),
                0,
                String.format("%05d", township * 1_000 + 1),
                taxType,
                String.format("%03d", volume),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
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
        public List<AssessmentParcel> findAllInPersistenceOrder() {
            return List.copyOf(records);
        }

        @Override
        public Optional<AssessmentParcel> findById(Long id) {
            return records.stream().filter(record -> id.equals(record.id())).findFirst();
        }

        @Override
        public AssessmentParcel save(AssessmentParcel assessmentParcel) {
            Long id = assessmentParcel.id();
            if (id == null) {
                throw new IllegalStateException("A saved assessment parcel must have an identity");
            }
            savedIds.add(id);
            return assessmentParcel;
        }

        @Override
        public void deleteById(Long id) {
            records.removeIf(record -> id.equals(record.id()));
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
        public List<AssessmentDetail> findAllInPersistenceOrder() {
            return List.copyOf(records);
        }

        @Override
        public Optional<AssessmentDetail> findById(Long id) {
            return records.stream().filter(record -> id.equals(record.id())).findFirst();
        }

        @Override
        public AssessmentDetail save(AssessmentDetail assessmentDetail) {
            saveCalls++;
            return assessmentDetail;
        }

        @Override
        public void deleteById(Long id) {
            records.removeIf(record -> id.equals(record.id()));
        }
    }
}
