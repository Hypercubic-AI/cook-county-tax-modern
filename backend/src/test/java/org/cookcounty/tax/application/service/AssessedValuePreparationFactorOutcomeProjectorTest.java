package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.OutputRecord;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.ProcessResult;
import org.cookcounty.tax.application.service.AssessedValuePreparationProcessor.StageOutcome;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.AssessmentParcelSourceRecord;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelSourceRecordRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

class AssessedValuePreparationFactorOutcomeProjectorTest {

    @Test
    void reviewedPathMatchesExactSharedLiveWireOutcome() throws Exception {
        List<AssessmentParcel> parcels = reviewedParcels();
        List<AssessmentParcel> persistedParcels = new ArrayList<>(parcels);
        AssessmentParcelRepository parcelRepository = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository detailRepository = mock(AssessmentDetailRepository.class);
        AssessmentParcelSourceRecordRepository sourceRepository =
                mock(AssessmentParcelSourceRecordRepository.class);
        when(parcelRepository.findAllInInputOrder())
                .thenAnswer(ignored -> List.copyOf(persistedParcels));
        when(parcelRepository.save(any()))
                .thenAnswer(
                        invocation -> {
                            AssessmentParcel saved = invocation.getArgument(0);
                            for (int index = 0; index < persistedParcels.size(); index++) {
                                if (persistedParcels
                                        .get(index)
                                        .parcelNumber()
                                        .equals(saved.parcelNumber())) {
                                    persistedParcels.set(index, saved);
                                    return saved;
                                }
                            }
                            persistedParcels.add(saved);
                            return saved;
                        });
        when(detailRepository.findAllInInputOrder()).thenReturn(List.of());

        List<AssessmentParcelSourceRecord> sourceRecords = sourceRecords(parcels);
        when(sourceRepository.findAllInSourceOrder()).thenReturn(sourceRecords);
        AssessedValuePreparationProcessor processor =
                new AssessedValuePreparationProcessor(
                        parcelRepository,
                        detailRepository,
                        new AssessedValueOverallClassKernel(),
                        new AssessedValueBucketingKernel(),
                        new AssessedValueType5Kernel(new AssessmentDetailValuator()));
        var result = processor.process(LocalDate.of(2025, 9, 15), "12:00:00", "26");
        Outcome outcome =
                new AssessedValuePreparationFactorOutcomeProjector(
                                parcelRepository, detailRepository, sourceRepository)
                        .project(result);
        JsonNode golden =
                new ObjectMapper()
                        .readTree(
                                ReviewedFixture.string(
                                        "goldens/valuation-preparation.golden.json"));

        assertThat(outcome.returnCode()).isEqualTo(golden.path("maxRC").intValue());
        assertThat(outcome.budgetExceeded()).isFalse();
        assertThat(outcome.rolledBack()).isFalse();
        assertThat(outcome.abend()).isNull();
        assertThat(outcome.datasetDiffs().isEmpty()).isTrue();
        assertThat(outcome.outputs()).isEqualTo(Map.of());

        JsonNode goldenSteps = golden.path("steps");
        List<String> expectedDisplays = new ArrayList<>();
        assertThat(outcome.steps().size()).isEqualTo(goldenSteps.size());
        for (int index = 0; index < goldenSteps.size(); index++) {
            JsonNode expected = goldenSteps.get(index);
            var actual = outcome.steps().get(index);
            assertThat(actual.name()).isEqualTo(expected.path("name").textValue());
            assertThat(actual.program()).isEqualTo(expected.path("program").textValue());
            assertThat(actual.returnCode()).isEqualTo(expected.path("returnCode").intValue());
            assertThat(actual.skipped()).isFalse();
            assertThat(actual.completionCode()).isNull();
            assertThat(actual.messages()).isEqualTo(List.of());
            assertThat(actual.datasetOps()).isEqualTo(List.of());
            expectedDisplays.addAll(strings(expected.path("sysout")));
        }
        assertThat(outcome.batchDisplays()).isEqualTo(expectedDisplays);

        JsonNode goldenCataloged = golden.path("catalogedOutputs");
        assertThat(outcome.cataloged().size()).isEqualTo(goldenCataloged.size());
        assertTextCatalogMatches(goldenCataloged.get(0), outcome.cataloged().get(0));
        assertTextCatalogMatches(goldenCataloged.get(1), outcome.cataloged().get(1));
        assertThat(outcome.cataloged().get(0).recordData().get(0).substring(3, 11))
                .isEqualTo("25/09/20");
        assertThat(outcome.cataloged().get(1).recordData().get(1).substring(3, 11))
                .isEqualTo("20250920");

        Cataloged master = outcome.cataloged().get(2);
        assertThat(master.dsn()).isEqualTo(goldenCataloged.get(2).path("dsn").textValue());
        assertThat(master.generation())
                .isEqualTo(goldenCataloged.get(2).path("generation").intValue());
        assertThat(master.records())
                .isEqualTo(goldenCataloged.get(2).path("recordCount").intValue());
        assertThat(master.recordDataBase64().size()).isEqualTo(master.records());
        assertThat(master.recordData().size()).isEqualTo(master.records());
        assertThat(master.recordData()).isEqualTo(strings(goldenCataloged.get(2).path("records")));
        for (int index = 0; index < master.records(); index++) {
            byte[] bytes = Base64.getDecoder().decode(master.recordDataBase64().get(index));
            assertThat(bytes.length).isEqualTo(18_706);
            assertThat(new String(bytes, StandardCharsets.UTF_8))
                    .isEqualTo(master.recordData().get(index));
            assertMasterFields(bytes, persistedParcels.get(index));
            assertSourceEnvelopePreserved(
                    bytes,
                    Base64.getDecoder().decode(sourceRecords.get(index).sourceRecordBase64()));
        }

        Cataloged emptyType5Report = outcome.cataloged().get(3);
        assertThat(emptyType5Report.dsn())
                .isEqualTo(goldenCataloged.get(3).path("dsn").textValue());
        assertThat(emptyType5Report.records()).isEqualTo(0);
        assertThat(emptyType5Report.recordData()).isEqualTo(List.of());
        assertThat(emptyType5Report.recordDataBase64()).isEqualTo(List.of());
    }

    @Test
    void failedAndSkippedStagesRetainWireEvidence() {
        StageOutcome completed =
                new StageOutcome(
                        "OVERALL_CLASS",
                        "COMPLETED",
                        0,
                        2,
                        2,
                        2,
                        0,
                        true,
                        false,
                        2,
                        Map.of("reported", 2L),
                        List.of("overall report"));
        StageOutcome failed =
                new StageOutcome(
                        "VALUATION_BUCKETING",
                        "FAILED",
                        0,
                        2,
                        1,
                        1,
                        1,
                        true,
                        true,
                        1,
                        Map.of(
                                "farm",
                                BigDecimal.ZERO,
                                "homeowner",
                                BigDecimal.ZERO,
                                "nonHomeowner",
                                BigDecimal.ZERO),
                        List.of("breakdown report"));
        ProcessResult result =
                new ProcessResult(
                        true,
                        0,
                        4,
                        3,
                        3,
                        1,
                        true,
                        List.of(
                                OutputRecord.text(
                                        "OVERALL_CLASS_REPORT",
                                        "text/plain",
                                        completed.reportRecords()),
                                OutputRecord.text(
                                        "VALUATION_BREAKDOWN_REPORT",
                                        "text/plain",
                                        failed.reportRecords()),
                                new OutputRecord(
                                        "ASSESSMENT_MASTER", "application/octet-stream", 0)),
                        List.of(),
                        List.of(completed, failed, StageOutcome.notStarted("TYPE5_CONVERSION")));
        AssessedValuePreparationFactorOutcomeProjector projector =
                new AssessedValuePreparationFactorOutcomeProjector(
                        mock(AssessmentParcelRepository.class),
                        mock(AssessmentDetailRepository.class),
                        mock(AssessmentParcelSourceRecordRepository.class));

        Outcome outcome = projector.project(result);

        assertThat(outcome.steps().get(0).completionCode()).isNull();
        assertThat(outcome.steps().get(0).messages()).isEqualTo(List.of());
        assertThat(outcome.steps().get(0).datasetOps()).isEqualTo(List.of());
        assertThat(outcome.steps().get(1).completionCode()).isEqualTo("CC 0000");
        assertThat(outcome.steps().get(1).messages().isEmpty()).isFalse();
        assertThat(outcome.steps().get(1).datasetOps().isEmpty()).isFalse();
        assertThat(outcome.steps().get(2).skipped()).isTrue();
        assertThat(outcome.steps().get(2).messages().isEmpty()).isFalse();
        assertThat(outcome.steps().get(2).datasetOps().isEmpty()).isFalse();
        assertThat(outcome.outputs().isEmpty()).isFalse();
        assertThat(outcome.batchDisplays().isEmpty()).isFalse();

        Outcome workerFailure =
                projector.workerFailure(new IllegalStateException("database unavailable"));
        assertThat(workerFailure.returnCode()).isEqualTo(16);
        assertThat(workerFailure.steps().get(0).messages())
                .isEqualTo(List.of("database unavailable"));
        assertThat(workerFailure.batchDisplays()).isEqualTo(List.of("database unavailable"));
        var abend = workerFailure.abend();
        assertThat(abend).isNotNull();
        if (abend == null) {
            throw new AssertionError("Worker failure did not include abnormal-end metadata");
        }
        assertThat(abend.code()).isEqualTo("WORKER_FAILURE");
    }

    @Test
    void workerFailureWithoutAnExceptionMessagePublishesSafeEvidence() {
        AssessedValuePreparationFactorOutcomeProjector projector =
                new AssessedValuePreparationFactorOutcomeProjector(
                        mock(AssessmentParcelRepository.class),
                        mock(AssessmentDetailRepository.class),
                        mock(AssessmentParcelSourceRecordRepository.class));

        Outcome outcome = projector.workerFailure(new IllegalStateException());

        assertThat(outcome.steps().getFirst().messages())
                .containsExactly("The assessed-value preparation worker failed.");
        assertThat(outcome.batchDisplays())
                .containsExactly("The assessed-value preparation worker failed.");
    }

    private static void assertTextCatalogMatches(JsonNode expected, Cataloged actual) {
        List<String> expectedRecords = strings(expected.path("records"));
        assertThat(actual.dsn()).isEqualTo(expected.path("dsn").textValue());
        assertThat(actual.generation()).isEqualTo(expected.path("generation").intValue());
        assertThat(actual.records()).isEqualTo(expected.path("recordCount").intValue());
        assertThat(actual.recordData()).isEqualTo(expectedRecords);
        for (int index = 0; index < expectedRecords.size(); index++) {
            assertThat(
                            new String(
                                    Base64.getDecoder()
                                            .decode(actual.recordDataBase64().get(index)),
                                    StandardCharsets.UTF_8))
                    .isEqualTo(expectedRecords.get(index));
        }
    }

    private static void assertMasterFields(byte[] bytes, AssessmentParcel parcel) {
        assertThat(character(bytes[0])).isEqualTo(character(parcel.assessmentStatus()));
        assertThat(packed(bytes, 1, 2)).isEqualTo(Long.parseLong(parcel.volumeNumber()));
        assertThat(packed(bytes, 3, 8)).isEqualTo(Long.parseLong(parcel.parcelNumber()));
        assertThat(character(bytes[11])).isEqualTo(character(parcel.taxType()));
        assertThat(packed(bytes, 13, 3)).isEqualTo(Long.parseLong(parcel.taxCode()));
        assertThat(character(bytes[16])).isEqualTo(character(parcel.parcelStatus()));
        assertThat(packed(bytes, 17, 2)).isEqualTo(parcel.overallClass().longValue());
        assertThat(character(bytes[37])).isEqualTo(character(parcel.clerkMajorClass()));
        assertThat(packed(bytes, 56, 5)).isEqualTo(value(parcel.priorLandValue()));
        assertThat(packed(bytes, 61, 5)).isEqualTo(value(parcel.priorImprovementValue()));
        assertThat(packed(bytes, 66, 5)).isEqualTo(value(parcel.priorTotalValue()));
        assertThat(packed(bytes, 71, 5)).isEqualTo(value(parcel.currentLandValue()));
        assertThat(packed(bytes, 76, 5)).isEqualTo(value(parcel.currentImprovementValue()));
        assertThat(packed(bytes, 81, 5)).isEqualTo(value(parcel.currentTotalValue()));
        assertThat(packed(bytes, 86, 5)).isEqualTo(value(parcel.proposedLandValue()));
        assertThat(packed(bytes, 91, 5)).isEqualTo(value(parcel.proposedImprovementValue()));
        assertThat(packed(bytes, 96, 5)).isEqualTo(value(parcel.proposedTotalValue()));
        assertThat(packed(bytes, 101, 5)).isEqualTo(value(parcel.farmValue()));
        assertThat(packed(bytes, 106, 5))
                .isEqualTo(value(parcel.combinedHomeownerNonHomeownerValue()));
        assertThat(packed(bytes, 111, 5))
                .isEqualTo(value(parcel.archivedPreConversionProposedTotal()));
    }

    private static void assertSourceEnvelopePreserved(byte[] actual, byte[] source) {
        for (int offset = 19; offset <= 36; offset++) {
            assertWithMessage("source byte " + offset)
                    .that(actual[offset])
                    .isEqualTo(source[offset]);
        }
        for (int offset = 38; offset <= 55; offset++) {
            assertWithMessage("source byte " + offset)
                    .that(actual[offset])
                    .isEqualTo(source[offset]);
        }
        assertWithMessage("source byte 116").that(actual[116]).isEqualTo(source[116]);
        assertWithMessage("source byte 117").that(actual[117]).isEqualTo(source[117]);
        for (int offset = 122; offset < actual.length; offset++) {
            assertWithMessage("zero-detail padding byte " + offset)
                    .that(actual[offset])
                    .isEqualTo((byte) ' ');
        }
    }

    private static long packed(byte[] bytes, int offset, int length) {
        long value = 0;
        for (int index = 0; index < length; index++) {
            int raw = Byte.toUnsignedInt(bytes[offset + index]);
            value = (value * 10) + (raw >>> 4);
            if (index < length - 1) {
                value = (value * 10) + (raw & 0x0f);
            } else if ((raw & 0x0f) == 0x0d) {
                value = -value;
            }
        }
        return value;
    }

    private static long value(BigDecimal value) {
        return value == null ? 0L : value.longValueExact();
    }

    private static String character(String value) {
        return value == null || value.isEmpty() ? " " : value.substring(0, 1);
    }

    private static String character(byte value) {
        return Character.toString((char) Byte.toUnsignedInt(value));
    }

    private static List<AssessmentParcel> reviewedParcels() {
        int[] volumes = {1, 9, 25, 36, 63, 86, 193, 350, 508, 528};
        long[] properties = {
            10_011_000_010_000L,
            12_022_000_020_000L,
            13_066_000_060_000L,
            14_077_000_070_000L,
            20_033_000_030_000L,
            22_055_000_050_000L,
            37_044_000_040_000L,
            71_011_100_110_000L,
            76_022_200_120_000L,
            77_033_300_130_000L
        };
        String[] taxTypes = {"0", "0", "1", "2", "0", "3", "0", "5", "0", "0"};
        int[] taxCodes = {10001, 12001, 13001, 14001, 20001, 22001, 37001, 71001, 76001, 77001};
        String[] parcelStatuses = {"0", "0", "0", "1", "0", "0", "0", "2", "0", "0"};
        String[] clerkClasses = {"N", "Y", "N", "Y", "A", "A", "E", "E", "F", "F"};
        int[] overallClasses = {202, 203, 201, 212, 295, 241, 299, 278, 297, 234};
        long[] values = {
            100000, 125000, 175000, 325000, 250000, 750000, 50000, 80000, 110000, 90000
        };
        List<AssessmentParcel> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            long value = values[index];
            rows.add(
                    new AssessmentParcel(
                            (long) index + 1,
                            0L,
                            BigDecimal.valueOf(value),
                            "1",
                            clerkClasses[index],
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            0,
                            BigDecimal.valueOf(value),
                            overallClasses[index],
                            String.format("%015d", properties[index]),
                            parcelStatuses[index],
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            BigDecimal.valueOf(value),
                            0,
                            String.format("%05d", taxCodes[index]),
                            taxTypes[index],
                            String.format("%03d", volumes[index]),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null));
        }
        return List.copyOf(rows);
    }

    private static List<AssessmentParcelSourceRecord> sourceRecords(List<AssessmentParcel> parcels)
            throws Exception {
        byte[] source =
                ReviewedFixture.bytes("data/cobol-fixtures/mastin--asreasfd01.bin");
        List<AssessmentParcelSourceRecord> records = new ArrayList<>();
        int offset = 0;
        int index = 0;
        while (offset < source.length) {
            int length =
                    (Byte.toUnsignedInt(source[offset]) << 8)
                            | Byte.toUnsignedInt(source[offset + 1]);
            byte[] record = java.util.Arrays.copyOfRange(source, offset + 4, offset + length);
            records.add(
                    new AssessmentParcelSourceRecord(
                            parcels.get(index).parcelNumber(),
                            index + 1,
                            Base64.getEncoder().encodeToString(record)));
            offset += length;
            index++;
        }
        return List.copyOf(records);
    }


    private static List<String> strings(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.textValue()));
        return List.copyOf(values);
    }

}
