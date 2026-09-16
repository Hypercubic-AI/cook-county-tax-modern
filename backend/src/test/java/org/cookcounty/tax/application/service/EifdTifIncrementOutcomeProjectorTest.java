package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.domain.contract.dto.EifdTifIncrementRunRequest;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AgencyReference;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference;
import org.cookcounty.tax.domain.model.TaxCodeMasterReference.AgencySlot;
import org.cookcounty.tax.domain.model.TownReference;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.EifdTifReferenceDataRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class EifdTifIncrementOutcomeProjectorTest {

    @Test
    void reviewedExecutionMatchesAllNineStepsAndEveryCatalogedRecordByte() throws Exception {
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        FrozenValuationRepository frozen = mock(FrozenValuationRepository.class);
        AgencyEqualizedValuationRepository agencies =
                mock(AgencyEqualizedValuationRepository.class);
        AssessmentDetailValuator valuator = new AssessmentDetailValuator();
        EifdTifReferenceDataRepository references = references();
        when(parcels.findAllInInputOrder()).thenReturn(parcels());
        when(details.findAllInPersistenceOrder()).thenReturn(List.of());
        when(frozen.findAllInPersistenceOrder()).thenReturn(frozenValues());
        when(agencies.findAllInPersistenceOrder()).thenReturn(agencyValues());
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agencies.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EifdTifIncrementProcessResult result =
                new EifdTifIncrementProcessor(
                                parcels,
                                details,
                                frozen,
                                agencies,
                                references,
                                valuator,
                                new org.cookcounty.tax.application.batch.EifdTifIncrementKernel(),
                                new EifdTifIncrementOutcomeProjector(),
                                org.springframework.transaction.support.TransactionOperations
                                        .withoutTransaction())
                        .process(request());
        var outcome = Objects.requireNonNull(result.outcome());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode golden =
                mapper.readTree(
                        ReviewedFixture.string("goldens/eifd-tif-increment.golden.json"));

        assertThat(outcome.returnCode()).isEqualTo(0);
        assertThat(outcome.steps().size()).isEqualTo(9);
        List<String> expectedDisplays = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            JsonNode expected = golden.get("steps").get(index);
            var actual = outcome.steps().get(index);
            assertThat(actual.name()).isEqualTo(expected.get("name").asText());
            assertThat(actual.program()).isEqualTo(expected.get("program").asText());
            assertThat(actual.returnCode()).isEqualTo(expected.get("returnCode").asInt());
            assertThat(actual.skipped()).isEqualTo(expected.path("skipped").asBoolean());
            assertThat(actual.completionCode()).isNull();
            assertThat(actual.messages()).isEqualTo(List.of());
            assertThat(actual.datasetOps()).isEqualTo(List.of());
            expectedDisplays.addAll(
                    mapper.convertValue(
                            expected.get("sysout"),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
        }
        assertThat(outcome.batchDisplays()).isEqualTo(expectedDisplays);
        assertThat(outcome.outputs()).isEqualTo(java.util.Map.of());

        JsonNode actualDiffs = mapper.valueToTree(outcome.datasetDiffs());
        assertThat(outcome.datasetDiffs().keySet())
                .isEqualTo(java.util.Set.of("AGYEQVAL", "FRZVALFL"));
        assertThat(actualDiffs.at("/AGYEQVAL/changed").size()).isEqualTo(9);
        assertThat(actualDiffs.at("/FRZVALFL/changed").size()).isEqualTo(10);
        actualDiffs
                .at("/AGYEQVAL/changed")
                .forEach(record -> assertThat(record.size()).isEqualTo(86));
        actualDiffs
                .at("/FRZVALFL/changed")
                .forEach(record -> assertThat(record.size()).isEqualTo(176));
        String agencyOneKey =
                Character.toString((char) 0).repeat(4) + Character.toString((char) 0x1f);
        JsonNode agencyOne = actualDiffs.at("/AGYEQVAL/changed").get(agencyOneKey);
        assertThat(agencyOne).isNotNull();
        assertThat(agencyOne.get("AEV-AGCYNO").asText()).isEqualTo("1");
        assertThat(agencyOne.get("AEV-ANX-PROP-EQV").asText()).isEqualTo("0");
        assertThat(agencyOne.get("AEV-CC-AIRPOL").asText()).isEqualTo("25000000");
        assertThat(agencyOne.get("AEV-CC-RE").asText()).isEqualTo("1250000000");
        assertThat(agencyOne.get("AEV-CC-RR").asText()).isEqualTo("5500000");
        assertThat(agencyOne.get("AEV-CC-USETAX").asText()).isEqualTo("12000000");
        assertThat(agencyOne.get("AEV_AGCYNO").asText()).isEqualTo("1");
        assertThat(agencyOne.get("AEV_CC_AIRPOL").asText()).isEqualTo("25000000");

        JsonNode expectedFrozenChanged = expectedFrozenChanges(golden);
        assertThat(actualDiffs.at("/FRZVALFL/changed")).isEqualTo(expectedFrozenChanged);

        actualDiffs
                .at("/AGYEQVAL/changed")
                .forEach(
                        record ->
                                record.forEach(
                                        field ->
                                                assertWithMessage(
                                                                "control byte leaked from decoded"
                                                                        + " AGYEQVAL field value")
                                                        .that(
                                                                field.asText()
                                                                        .chars()
                                                                        .anyMatch(
                                                                                Character
                                                                                        ::isISOControl))
                                                        .isFalse()));

        JsonNode expectedCatalog = golden.get("catalogedOutputs");
        assertThat(outcome.cataloged().size()).isEqualTo(expectedCatalog.size());
        for (int index = 0; index < expectedCatalog.size(); index++) {
            JsonNode expected = expectedCatalog.get(index);
            Cataloged actual = outcome.cataloged().get(index);
            assertThat(actual.dsn()).isEqualTo(expected.get("dsn").asText());
            assertThat(actual.generation()).isEqualTo(expected.get("generation").asInt());
            assertThat(actual.records()).isEqualTo(expected.get("recordCount").asInt());
            List<String> expectedRecords =
                    expected.has("records")
                            ? new ObjectMapper()
                                    .convertValue(
                                            expected.get("records"),
                                            new com.fasterxml.jackson.core.type.TypeReference<
                                                    List<String>>() {})
                            : List.of();
            assertWithMessage(actual.dsn())
                    .that(actual.recordData().size())
                    .isEqualTo(expectedRecords.size());
            assertWithMessage(actual.dsn())
                    .that(actual.recordDataBase64().size())
                    .isEqualTo(expectedRecords.size());
            for (int recordIndex = 0; recordIndex < expectedRecords.size(); recordIndex++) {
                String context = actual.dsn() + " record " + recordIndex;
                assertWithMessage(context)
                        .that(actual.recordData().get(recordIndex))
                        .isEqualTo(expectedRecords.get(recordIndex));
                assertWithMessage(context + " base64")
                        .that(
                                new String(
                                        Base64.getDecoder()
                                                .decode(actual.recordDataBase64().get(recordIndex)),
                                        StandardCharsets.US_ASCII))
                        .isEqualTo(expectedRecords.get(recordIndex));
            }
        }

        ArgumentCaptor<FrozenValuation> frozenSaves =
                ArgumentCaptor.forClass(FrozenValuation.class);
        verify(frozen, times(10)).save(frozenSaves.capture());
        List<FrozenValuation> transitionedDivisions =
                frozenSaves.getAllValues().stream()
                        .filter(value -> "00000000000007".equals(value.divisionNumber()))
                        .toList();
        assertThat(transitionedDivisions.size()).isEqualTo(1);
        assertThat(transitionedDivisions.get(0).proposedImprovementValue())
                .isEqualTo(BigDecimal.valueOf(125000));
        List<EifdTifIncrementProcessResult.FrozenValuationReportFact> reportedDivisions =
                result.frozenValuationReportFacts().stream()
                        .filter(value -> "00000000000007".equals(value.divisionNumber()))
                        .toList();
        assertThat(reportedDivisions.size()).isEqualTo(1);
        assertThat(reportedDivisions.get(0).proposedImprovementValue())
                .isEqualTo(BigDecimal.valueOf(125000));

        ArgumentCaptor<AgencyEqualizedValuation> agencySaves =
                ArgumentCaptor.forClass(AgencyEqualizedValuation.class);
        verify(agencies, times(9)).save(agencySaves.capture());
        List<AgencyEqualizedValuation> transitionedAgencies =
                agencySaves.getAllValues().stream()
                        .filter(value -> "000000007".equals(value.agencyNumber()))
                        .toList();
        assertThat(transitionedAgencies.size()).isEqualTo(1);
        assertThat(transitionedAgencies.get(0).newPropertyEqualizedValue())
                .isEqualTo(BigDecimal.valueOf(125000));
    }

    @Test
    void failedProjectionPreservesNonzeroCompletionAndMessages() {
        EifdTifIncrementProcessResult result =
                new EifdTifIncrementProcessResult(
                        false,
                        16,
                        3,
                        1,
                        1,
                        2,
                        List.of(),
                        List.of(
                                new EifdTifIncrementProcessResult.Message(
                                        "ERROR",
                                        "Agency posting input is out of sequence.",
                                        "clrtm756-001")));

        var outcome = new EifdTifIncrementOutcomeProjector().failed(result);

        assertThat(outcome.returnCode()).isEqualTo(16);
        assertThat(outcome.outputs()).isEqualTo(java.util.Map.of());
        assertThat(outcome.datasetDiffs()).isEqualTo(java.util.Map.of());
        assertThat(outcome.cataloged()).isEqualTo(List.of());
        assertThat(outcome.batchDisplays())
                .isEqualTo(List.of("Agency posting input is out of sequence."));
        assertThat(outcome.steps().size()).isEqualTo(1);
        var failed = outcome.steps().get(0);
        assertThat(failed.name()).isEqualTo("S001");
        assertThat(failed.program()).isEqualTo("ASREA740");
        assertThat(failed.returnCode()).isEqualTo(16);
        assertThat(failed.skipped()).isEqualTo(false);
        assertThat(failed.completionCode()).isEqualTo("CC 0016");
        assertThat(failed.messages())
                .isEqualTo(List.of("Agency posting input is out of sequence."));
        assertThat(failed.datasetOps()).isEqualTo(List.of());
    }

    private JsonNode expectedFrozenChanges(JsonNode golden) {
        ObjectNode expected = golden.at("/storeEffects/FRZVALFL/Changed").deepCopy();
        JsonNode report = null;
        for (JsonNode catalog : golden.get("catalogedOutputs")) {
            if (catalog.get("dsn").asText().endsWith("S001-PRINTDTL.dat")) {
                report = catalog.get("records");
                break;
            }
        }
        assertThat(report).isNotNull();
        List<String> scalars = new ArrayList<>();
        for (JsonNode row : Objects.requireNonNull(report)) {
            String line = row.asText().trim();
            if (line.matches("[0-9, -]+") && !line.isBlank()) {
                scalars.addAll(List.of(line.replace(",", "").split("\\s+")));
            }
        }
        // Each division contributes its identifier and 29 reported amounts or counts.
        int scalarsPerDivision = 30;
        assertThat(scalars.size()).isEqualTo(expected.size() * scalarsPerDivision);
        int recordIndex = 0;
        for (JsonNode entry : expected) {
            ObjectNode record = (ObjectNode) entry;
            int offset = recordIndex++ * scalarsPerDivision;
            for (char separator : new char[] {'-', '_'}) {
                String prefix = "FV" + separator;
                record.put(prefix + "DIVNO", Long.toString(Long.parseLong(scalars.get(offset))));
                for (int valueIndex = 1; valueIndex < scalarsPerDivision; valueIndex++) {
                    record.put(
                            prefix + "VAL(" + valueIndex + ")", scalars.get(offset + valueIndex));
                }
                // The capture stores COMP-3 prior-value bytes as text, not decimal scalars.
                for (int valueIndex = 1; valueIndex <= 3; valueIndex++) {
                    String field = prefix + "PRIOR" + separator + "VAL(" + valueIndex + ")";
                    String raw = record.get(field).asText();
                    assertThat(raw).doesNotContain("\uFFFD");
                    String packed =
                            java.util.HexFormat.of()
                                    .formatHex(raw.getBytes(StandardCharsets.ISO_8859_1));
                    long value = Long.parseLong(packed.substring(0, packed.length() - 1));
                    record.put(field, Long.toString(packed.endsWith("d") ? -value : value));
                }
            }
        }
        return expected;
    }

    private EifdTifIncrementRunRequest request() {
        return new EifdTifIncrementRunRequest(
                "10000",
                LocalDate.of(2025, 9, 15),
                "12:00:00",
                "reviewed-eifd-factor-outcome",
                "26",
                "260181202526",
                "2026");
    }

    private List<AssessmentParcel> parcels() {
        long[] numbers = {
            10011000010000L, 12022000020000L, 13066000060000L, 14077000070000L,
            20033000030000L, 22055000050000L, 37044000040000L, 71011100110000L,
            76022200120000L, 77033300130000L
        };
        int[] taxCodes = {10001, 12001, 13001, 14001, 20001, 22001, 37001, 71001, 76001, 77001};
        long[] priorLand = {30000, 35000, 40000, 60000, 75000, 150000, 20000, 20000, 35000, 25000};
        long[] priorImprovement = {
            70000, 90000, 135000, 140000, 175000, 175000, 30000, 60000, 75000, 65000
        };
        long[] currentLand = {
            30000, 35000, 50000, 60000, 70000, 160000, 25000, 20000, 35000, 28000
        };
        long[] currentImprovement = {
            70000, 105000, 125000, 165000, 160000, 205000, 45000, 55000, 75000, 72000
        };
        long[] divisions = {1, 2, 3, 4, 5, 6, 7, 11, 12, 13};
        List<AssessmentParcel> values = new ArrayList<>();
        for (int index = 0; index < numbers.length; index++) {
            values.add(
                    new AssessmentParcel(
                            null,
                            null,
                            BigDecimal.valueOf(0),
                            "",
                            "",
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(currentImprovement[index]),
                            BigDecimal.valueOf(currentLand[index]),
                            BigDecimal.valueOf(currentLand[index] + currentImprovement[index]),
                            0,
                            BigDecimal.valueOf(0),
                            0,
                            String.format("%015d", numbers[index]),
                            "0",
                            BigDecimal.valueOf(priorImprovement[index]),
                            BigDecimal.valueOf(priorLand[index]),
                            BigDecimal.valueOf(priorLand[index] + priorImprovement[index]),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            0,
                            String.format("%05d", taxCodes[index]),
                            "",
                            String.format("%03d", 0),
                            index == 6 ? "1" : "0",
                            String.format("%014d", divisions[index]),
                            BigDecimal.valueOf(priorLand[index]),
                            BigDecimal.valueOf(priorImprovement[index]),
                            BigDecimal.valueOf(priorLand[index] + priorImprovement[index]),
                            BigDecimal.valueOf(currentLand[index]),
                            BigDecimal.valueOf(currentImprovement[index]),
                            BigDecimal.valueOf(currentLand[index] + currentImprovement[index])));
        }
        return values;
    }

    private List<FrozenValuation> frozenValues() {
        String[] divisions = {
            "00000000000001", "00000000000002", "00000000000003", "00000000000004",
            "00000000000005", "00000000000006", "00000000000007", "00000000000011",
            "00000000000012", "00000000000013"
        };
        long[][] v = {
            {
                1200000, 2400000, 3600000, 12, 1250000, 2500000, 3750000, 12, 25000, 0, 0, 25000,
                100000, 0, 0, 0, 0, 0, 0, 0, 0, 1200000, 2400000, 3600000, 12, 1250000, 2500000,
                3750000, 12
            },
            {
                1250000, 2500000, 3750000, 13, 1300000, 2600000, 3900000, 13, 30000, 0, 0, 30000,
                140000, 1250000, 2500000, 3750000, 13, 1300000, 2600000, 3900000, 13, 0, 0, 0, 0, 0,
                0, 0, 0
            },
            {
                1300000, 2600000, 3900000, 14, 1350000, 2700000, 4050000, 14, 35000, 0, 0, 35000,
                175000, 0, 0, 0, 0, 0, 0, 0, 0, 1300000, 2600000, 3900000, 14, 1350000, 2700000,
                4050000, 14
            },
            {
                1350000, 2700000, 4050000, 15, 1400000, 2800000, 4200000, 15, 40000, 12000, 18000,
                70000, 225000, 1350000, 2700000, 4050000, 15, 1400000, 2800000, 4200000, 15, 0, 0,
                0, 0, 0, 0, 0, 0
            },
            {
                1400000, 3000000, 4400000, 16, 1450000, 3100000, 4550000, 16, 45000, 0, 0, 45000,
                230000, 0, 0, 0, 0, 0, 0, 0, 0, 1400000, 3000000, 4400000, 16, 1450000, 3100000,
                4550000, 16
            },
            {
                1450000, 3100000, 4550000, 17, 1500000, 3200000, 4700000, 17, 50000, 0, 0, 50000,
                365000, 1450000, 3100000, 4550000, 17, 1500000, 3200000, 4700000, 17, 0, 0, 0, 0, 0,
                0, 0, 0
            },
            {
                1500000, 3200000, 4700000, 21, 1550000, 3300000, 4850000, 21, 55000, 0, 0, 55000,
                70000, 0, 0, 0, 0, 0, 0, 0, 0, 1500000, 3200000, 4700000, 21, 1550000, 3300000,
                4850000, 21
            },
            {
                1550000, 3300000, 4850000, 22, 1600000, 3400000, 5000000, 22, 60000, 0, 0, 60000,
                75000, 1550000, 3300000, 4850000, 22, 1600000, 3400000, 5000000, 22, 0, 0, 0, 0, 0,
                0, 0, 0
            },
            {
                1600000, 3400000, 5000000, 23, 1650000, 3500000, 5150000, 23, 65000, 0, 0, 65000,
                110000, 0, 0, 0, 0, 0, 0, 0, 0, 1600000, 3400000, 5000000, 23, 1650000, 3500000,
                5150000, 23
            },
            {
                1650000, 3500000, 5150000, 24, 1700000, 3600000, 5300000, 24, 70000, 0, 0, 70000,
                100000, 1650000, 3500000, 5150000, 24, 1700000, 3600000, 5300000, 24, 0, 0, 0, 0, 0,
                0, 0, 0
            }
        };
        List<FrozenValuation> values = new ArrayList<>();
        for (int i = 0; i < divisions.length; i++) {
            values.add(
                    new FrozenValuation(
                            null,
                            null,
                            BigDecimal.valueOf(v[i][18]),
                            BigDecimal.valueOf(v[i][17]),
                            v[i][20],
                            BigDecimal.valueOf(v[i][19]),
                            BigDecimal.valueOf(v[i][14]),
                            BigDecimal.valueOf(v[i][13]),
                            v[i][16],
                            BigDecimal.valueOf(v[i][15]),
                            BigDecimal.valueOf(v[i][5]),
                            BigDecimal.valueOf(v[i][4]),
                            v[i][7],
                            BigDecimal.valueOf(v[i][6]),
                            divisions[i],
                            BigDecimal.valueOf(v[i][26]),
                            BigDecimal.valueOf(v[i][25]),
                            v[i][28],
                            BigDecimal.valueOf(v[i][27]),
                            BigDecimal.valueOf(v[i][22]),
                            BigDecimal.valueOf(v[i][21]),
                            v[i][24],
                            BigDecimal.valueOf(v[i][23]),
                            BigDecimal.valueOf(v[i][1]),
                            BigDecimal.valueOf(v[i][0]),
                            v[i][3],
                            BigDecimal.valueOf(v[i][2]),
                            BigDecimal.valueOf(v[i][12]),
                            BigDecimal.valueOf(v[i][10]),
                            BigDecimal.valueOf(v[i][9]),
                            BigDecimal.valueOf(v[i][8]),
                            BigDecimal.valueOf(v[i][11])));
        }
        return values;
    }

    private EifdTifReferenceDataRepository references() {
        EifdTifReferenceDataRepository references = mock(EifdTifReferenceDataRepository.class);
        long[] agencies = {1, 2, 3, 4, 5, 6, 7, 11, 12, 20};
        int[] towns = {10, 12, 13, 14, 20, 22, 37, 71, 76, 77};
        String[] townNames = {
            "BARRINGTON", "BLOOM", "BREMEN", "CALUMET", "LEYDEN",
            "MAINE", "THORNTON", "JEFFERSON", "SOUTH", "WEST"
        };
        String[] descriptions = {
            "COOK COUNTY GENERAL SERVICES",
            "CHICAGO CENTRAL MUNICIPAL FUND",
            "NORTH PRAIRIE SCHOOL DISTRICT 901",
            "LAKEFRONT COMMUNITY COLLEGE DISTRICT",
            "WEST COOK LIBRARY DISTRICT",
            "SOUTH CANAL PARK DISTRICT",
            "DES PLAINES VALLEY SANITARY DISTRICT",
            "SOUTH SUBURBAN FIRE DISTRICT",
            "CALUMET REGIONAL TRANSIT DISTRICT",
            "NORTH SHORE WATER RECLAMATION DISTRICT"
        };
        String[] taxRates = {
            "6.735", "7.125", "5.625", "7.455", "6.525",
            "7.375", "5.775", "6.275", "7.225", "6.125"
        };
        for (int index = 0; index < agencies.length; index++) {
            String agencyNumber = String.format("%09d", agencies[index]);
            String taxCode = String.format("%02d001", towns[index]);
            when(references.findAgency(agencyNumber))
                    .thenReturn(
                            Optional.of(new AgencyReference(agencyNumber, descriptions[index])));
            when(references.findTown(String.format("%02d", towns[index])))
                    .thenReturn(
                            Optional.of(
                                    new TownReference(
                                            String.format("%02d", towns[index]),
                                            townNames[index])));
            when(references.findTaxCodeMaster(taxCode))
                    .thenReturn(
                            Optional.of(
                                    new TaxCodeMasterReference(
                                            taxCode,
                                            new BigDecimal(taxRates[index]),
                                            List.of(new AgencySlot(1, agencyNumber)))));
        }
        return references;
    }

    private List<AgencyEqualizedValuation> agencyValues() {
        long[] agencies = {1, 2, 3, 4, 5, 6, 7, 11, 12};
        List<AgencyEqualizedValuation> values = new ArrayList<>();
        for (long agency : agencies) {
            values.add(
                    new AgencyEqualizedValuation(
                            null,
                            null,
                            String.format("%09d", agency),
                            BigDecimal.valueOf(0),
                            BigDecimal.ZERO,
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            BigDecimal.valueOf(agency == 1 ? 25_000_000L : 0L),
                            BigDecimal.valueOf(agency == 1 ? 5_500_000L : 0L),
                            BigDecimal.valueOf(agency == 1 ? 1_250_000_000L : 0L),
                            BigDecimal.valueOf(agency == 1 ? 12_000_000L : 0L),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.ZERO,
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            BigDecimal.valueOf(0),
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            "000000000",
                            0,
                            BigDecimal.ZERO,
                            0,
                            BigDecimal.ZERO,
                            0,
                            BigDecimal.ZERO,
                            false,
                            0,
                            BigDecimal.valueOf(0)));
        }
        return values;
    }
}
