package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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

class AssessedValuePreparationFactorOutcomeProjectorTest {

    @Test
    void reviewedPathMatchesExactSharedLiveWireOutcome() throws Exception {
        List<AssessmentParcel> parcels = reviewedParcels();
        AssessmentParcelRepository parcelRepository = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository detailRepository = mock(AssessmentDetailRepository.class);
        AssessmentParcelSourceRecordRepository sourceRepository =
                mock(AssessmentParcelSourceRecordRepository.class);
        when(parcelRepository.findAllInInputOrder()).thenReturn(parcels);
        when(parcelRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(detailRepository.findAllInInputOrder()).thenReturn(List.of());

        List<AssessmentParcelSourceRecord> sourceRecords = sourceRecords(parcels);
        when(sourceRepository.findAllInSourceOrder()).thenReturn(sourceRecords);
        AssessedValuePreparationProcessor processor = new AssessedValuePreparationProcessor(
                parcelRepository,
                detailRepository,
                new AssessedValueOverallClassKernel(),
                new AssessedValueBucketingKernel(),
                new AssessedValueType5Kernel(new AssessmentDetailValuator()));
        var result = processor.process(
                LocalDate.of(2025, 9, 15), "12:00:00", "26");
        Outcome outcome = new AssessedValuePreparationFactorOutcomeProjector(
                parcelRepository, detailRepository, sourceRepository).project(result);
        JsonNode golden = new ObjectMapper().readTree(Files.readString(goldenPath()));

        assertEquals(golden.path("maxRC").intValue(), outcome.returnCode());
        assertFalse(outcome.budgetExceeded());
        assertFalse(outcome.rolledBack());
        assertNull(outcome.abend());
        assertTrue(outcome.datasetDiffs().isEmpty());
        assertEquals(Map.of(), outcome.outputs());

        JsonNode goldenSteps = golden.path("steps");
        List<String> expectedDisplays = new ArrayList<>();
        assertEquals(goldenSteps.size(), outcome.steps().size());
        for (int index = 0; index < goldenSteps.size(); index++) {
            JsonNode expected = goldenSteps.get(index);
            var actual = outcome.steps().get(index);
            assertEquals(expected.path("name").textValue(), actual.name());
            assertEquals(expected.path("program").textValue(), actual.program());
            assertEquals(expected.path("returnCode").intValue(), actual.returnCode());
            assertFalse(actual.skipped());
            assertNull(actual.completionCode());
            assertEquals(List.of(), actual.messages());
            assertEquals(List.of(), actual.datasetOps());
            expectedDisplays.addAll(strings(expected.path("sysout")));
        }
        assertEquals(expectedDisplays, outcome.batchDisplays());

        JsonNode goldenCataloged = golden.path("catalogedOutputs");
        assertEquals(goldenCataloged.size(), outcome.cataloged().size());
        assertTextCatalogMatches(goldenCataloged.get(0), outcome.cataloged().get(0));
        assertTextCatalogMatches(goldenCataloged.get(1), outcome.cataloged().get(1));
        assertEquals("25/09/20",
                outcome.cataloged().get(0).recordData().get(0).substring(3, 11));
        assertEquals("20250920",
                outcome.cataloged().get(1).recordData().get(1).substring(3, 11));

        Cataloged master = outcome.cataloged().get(2);
        assertEquals(goldenCataloged.get(2).path("dsn").textValue(), master.dsn());
        assertEquals(goldenCataloged.get(2).path("generation").intValue(), master.generation());
        assertEquals(goldenCataloged.get(2).path("recordCount").intValue(), master.records());
        assertEquals(master.records(), master.recordDataBase64().size());
        assertEquals(master.records(), master.recordData().size());
        assertEquals(strings(goldenCataloged.get(2).path("records")), master.recordData());
        for (int index = 0; index < master.records(); index++) {
            byte[] bytes = Base64.getDecoder().decode(master.recordDataBase64().get(index));
            assertEquals(18_706, bytes.length);
            assertEquals(master.recordData().get(index), new String(bytes, StandardCharsets.UTF_8));
            assertMasterFields(bytes, parcels.get(index));
            assertSourceEnvelopePreserved(
                    bytes,
                    Base64.getDecoder().decode(sourceRecords.get(index).sourceRecordBase64()));
        }

        Cataloged emptyType5Report = outcome.cataloged().get(3);
        assertEquals(goldenCataloged.get(3).path("dsn").textValue(), emptyType5Report.dsn());
        assertEquals(0, emptyType5Report.records());
        assertEquals(List.of(), emptyType5Report.recordData());
        assertEquals(List.of(), emptyType5Report.recordDataBase64());
    }

    @Test
    void failedAndSkippedStagesRetainWireEvidence() {
        StageOutcome completed = new StageOutcome(
                "OVERALL_CLASS", "COMPLETED", 0,
                2, 2, 2, 0, true, false, 2,
                Map.of("reported", 2L), List.of("overall report"));
        StageOutcome failed = new StageOutcome(
                "VALUATION_BUCKETING", "FAILED", 0,
                2, 1, 1, 1, true, true, 1,
                Map.of("farm", 0L, "homeowner", 0L, "nonHomeowner", 0L),
                List.of("breakdown report"));
        ProcessResult result = new ProcessResult(
                true, 0, 4, 3, 3, 1, true,
                List.of(
                        OutputRecord.text(
                                "OVERALL_CLASS_REPORT", "text/plain", completed.reportRecords()),
                        OutputRecord.text(
                                "VALUATION_BREAKDOWN_REPORT", "text/plain",
                                failed.reportRecords()),
                        new OutputRecord("ASSESSMENT_MASTER", "application/octet-stream", 0)),
                List.of(),
                List.of(completed, failed, StageOutcome.notStarted("TYPE5_CONVERSION")));
        AssessedValuePreparationFactorOutcomeProjector projector =
                new AssessedValuePreparationFactorOutcomeProjector(
                        mock(AssessmentParcelRepository.class),
                        mock(AssessmentDetailRepository.class),
                        mock(AssessmentParcelSourceRecordRepository.class));

        Outcome outcome = projector.project(result);

        assertNull(outcome.steps().get(0).completionCode());
        assertEquals(List.of(), outcome.steps().get(0).messages());
        assertEquals(List.of(), outcome.steps().get(0).datasetOps());
        assertEquals("CC 0000", outcome.steps().get(1).completionCode());
        assertFalse(outcome.steps().get(1).messages().isEmpty());
        assertFalse(outcome.steps().get(1).datasetOps().isEmpty());
        assertTrue(outcome.steps().get(2).skipped());
        assertFalse(outcome.steps().get(2).messages().isEmpty());
        assertFalse(outcome.steps().get(2).datasetOps().isEmpty());
        assertFalse(outcome.outputs().isEmpty());
        assertFalse(outcome.batchDisplays().isEmpty());

        Outcome workerFailure = projector.workerFailure(
                new IllegalStateException("database unavailable"));
        assertEquals(16, workerFailure.returnCode());
        assertEquals(
                List.of("database unavailable"), workerFailure.steps().get(0).messages());
        assertEquals(
                List.of("database unavailable"), workerFailure.batchDisplays());
        assertEquals("WORKER_FAILURE", workerFailure.abend().code());
    }

    private static void assertTextCatalogMatches(JsonNode expected, Cataloged actual) {
        List<String> expectedRecords = strings(expected.path("records"));
        assertEquals(expected.path("dsn").textValue(), actual.dsn());
        assertEquals(expected.path("generation").intValue(), actual.generation());
        assertEquals(expected.path("recordCount").intValue(), actual.records());
        assertEquals(expectedRecords, actual.recordData());
        for (int index = 0; index < expectedRecords.size(); index++) {
            assertEquals(
                    expectedRecords.get(index),
                    new String(
                            Base64.getDecoder().decode(actual.recordDataBase64().get(index)),
                            StandardCharsets.UTF_8));
        }
    }

    private static void assertMasterFields(byte[] bytes, AssessmentParcel parcel) {
        assertEquals(character(parcel.getAssessmentStatus()), character(bytes[0]));
        assertEquals(parcel.getVolumeNumber().longValue(), packed(bytes, 1, 2));
        assertEquals(parcel.getParcelNumber().longValue(), packed(bytes, 3, 8));
        assertEquals(character(parcel.getTaxType()), character(bytes[11]));
        assertEquals(parcel.getTaxCode().longValue(), packed(bytes, 13, 3));
        assertEquals(character(parcel.getParcelStatus()), character(bytes[16]));
        assertEquals(parcel.getOverallClass().longValue(), packed(bytes, 17, 2));
        assertEquals(character(parcel.getClerkMajorClass()), character(bytes[37]));
        assertEquals(value(parcel.getPriorLandValue()), packed(bytes, 56, 5));
        assertEquals(value(parcel.getPriorImprovementValue()), packed(bytes, 61, 5));
        assertEquals(value(parcel.getPriorTotalValue()), packed(bytes, 66, 5));
        assertEquals(value(parcel.getCurrentLandValue()), packed(bytes, 71, 5));
        assertEquals(value(parcel.getCurrentImprovementValue()), packed(bytes, 76, 5));
        assertEquals(value(parcel.getCurrentTotalValue()), packed(bytes, 81, 5));
        assertEquals(value(parcel.getProposedLandValue()), packed(bytes, 86, 5));
        assertEquals(value(parcel.getProposedImprovementValue()), packed(bytes, 91, 5));
        assertEquals(value(parcel.getProposedTotalValue()), packed(bytes, 96, 5));
        assertEquals(value(parcel.getFarmValue()), packed(bytes, 101, 5));
        assertEquals(value(parcel.getCombinedHomeownerNonHomeownerValue()), packed(bytes, 106, 5));
        assertEquals(value(parcel.getArchivedPreConversionProposedTotal()), packed(bytes, 111, 5));
    }

    private static void assertSourceEnvelopePreserved(byte[] actual, byte[] source) {
        for (int offset = 19; offset <= 36; offset++) {
            assertEquals(source[offset], actual[offset], "source byte " + offset);
        }
        for (int offset = 38; offset <= 55; offset++) {
            assertEquals(source[offset], actual[offset], "source byte " + offset);
        }
        assertEquals(source[116], actual[116], "source byte 116");
        assertEquals(source[117], actual[117], "source byte 117");
        for (int offset = 122; offset < actual.length; offset++) {
            assertEquals((byte) ' ', actual[offset], "zero-detail padding byte " + offset);
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

    private static long value(Long value) {
        return value == null ? 0L : value;
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
        long[] values = {100000, 125000, 175000, 325000, 250000, 750000, 50000, 80000, 110000, 90000};
        List<AssessmentParcel> rows = new ArrayList<>();
        for (int index = 0; index < properties.length; index++) {
            AssessmentParcel parcel = new AssessmentParcel();
            parcel.setId((long) index + 1);
            parcel.setAssessmentStatus("1");
            parcel.setVolumeNumber(volumes[index]);
            parcel.setParcelNumber(properties[index]);
            parcel.setTaxType(taxTypes[index]);
            parcel.setTaxCode(taxCodes[index]);
            parcel.setParcelStatus(parcelStatuses[index]);
            parcel.setClerkMajorClass(clerkClasses[index]);
            parcel.setOverallClass(overallClasses[index]);
            parcel.setSalesSegmentCount(0);
            parcel.setDetailQuestionnaireCount(0);
            parcel.setPriorLandValue(values[index]);
            parcel.setPriorImprovementValue(values[index]);
            parcel.setPriorTotalValue(values[index]);
            parcel.setCurrentLandValue(values[index]);
            parcel.setCurrentImprovementValue(values[index]);
            parcel.setCurrentTotalValue(values[index]);
            parcel.setProposedLandValue(values[index]);
            parcel.setProposedImprovementValue(values[index]);
            parcel.setProposedTotalValue(values[index]);
            parcel.setFarmValue(values[index]);
            parcel.setCombinedHomeownerNonHomeownerValue(values[index]);
            parcel.setArchivedPreConversionProposedTotal(values[index]);
            rows.add(parcel);
        }
        return List.copyOf(rows);
    }

    private static List<AssessmentParcelSourceRecord> sourceRecords(
            List<AssessmentParcel> parcels) throws Exception {
        byte[] source = Files.readAllBytes(fixturePath());
        List<AssessmentParcelSourceRecord> records = new ArrayList<>();
        int offset = 0;
        int index = 0;
        while (offset < source.length) {
            int length = (Byte.toUnsignedInt(source[offset]) << 8)
                    | Byte.toUnsignedInt(source[offset + 1]);
            byte[] record = java.util.Arrays.copyOfRange(source, offset + 4, offset + length);
            records.add(new AssessmentParcelSourceRecord(
                    parcels.get(index).getParcelNumber(),
                    index + 1,
                    Base64.getEncoder().encodeToString(record)));
            offset += length;
            index++;
        }
        return List.copyOf(records);
    }

    private static Path fixturePath() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve("data/cobol-fixtures/mastin--asreasfd01.bin");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("Reviewed assessed master fixture was not found");
    }

    private static List<String> strings(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.textValue()));
        return List.copyOf(values);
    }

    private static Path goldenPath() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve("goldens/valuation-preparation.golden.json");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("Reviewed valuation-preparation golden was not found");
    }
}
