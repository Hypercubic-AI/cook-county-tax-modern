package org.cookcounty.tax.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.cookcounty.tax.application.batch.EifdTifIncrementProcessResult;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.domain.model.AgencyEqualizedValuation;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.FrozenValuation;
import org.cookcounty.tax.domain.port.out.AgencyEqualizedValuationRepository;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.FrozenValuationRepository;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.EifdTifIncrementRunRequest;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class EifdTifIncrementOutcomeProjectorTest {
    private static final String[][] LIVE_FROZEN_SCALARS = {
            {"1", "1230000", "2470000", "3700000", "13", "1280000", "2570000", "3850000", "13", "25000", "0", "0", "25000", "100000", "0", "0", "0", "0", "0", "0", "0", "0", "1200000", "2400000", "3600000", "12", "1250000", "2500000", "3750000", "12"},
            {"2", "1285000", "2590000", "3875000", "14", "1335000", "2705000", "4040000", "14", "30000", "0", "0", "30000", "140000", "1250000", "2500000", "3750000", "13", "1300000", "2600000", "3900000", "13", "0", "0", "0", "0", "0", "0", "0", "0"},
            {"3", "1340000", "2735000", "4075000", "15", "1400000", "2825000", "4225000", "15", "35000", "0", "0", "35000", "175000", "0", "0", "0", "0", "0", "0", "0", "0", "1300000", "2600000", "3900000", "14", "1350000", "2700000", "4050000", "14"},
            {"4", "1410000", "2840000", "4250000", "16", "1460000", "2965000", "4425000", "16", "40000", "12000", "18000", "70000", "225000", "1350000", "2700000", "4050000", "15", "1400000", "2800000", "4200000", "15", "0", "0", "0", "0", "0", "0", "0", "0"},
            {"5", "1475000", "3175000", "4650000", "17", "1520000", "3260000", "4780000", "17", "45000", "0", "0", "45000", "230000", "0", "0", "0", "0", "0", "0", "0", "0", "1400000", "3000000", "4400000", "16", "1450000", "3100000", "4550000", "16"},
            {"6", "1600000", "3275000", "4875000", "18", "1660000", "3405000", "5065000", "18", "50000", "0", "0", "50000", "365000", "1450000", "3100000", "4550000", "17", "1500000", "3200000", "4700000", "17", "0", "0", "0", "0", "0", "0", "0", "0"},
            {"7", "1520000", "3230000", "4750000", "22", "1575000", "3345000", "4920000", "22", "125000", "0", "0", "125000", "70000", "0", "0", "0", "0", "0", "0", "0", "0", "1500000", "3200000", "4700000", "21", "1550000", "3300000", "4850000", "21"},
            {"11", "1570000", "3360000", "4930000", "23", "1620000", "3455000", "5075000", "23", "60000", "0", "0", "60000", "75000", "1550000", "3300000", "4850000", "22", "1600000", "3400000", "5000000", "22", "0", "0", "0", "0", "0", "0", "0", "0"},
            {"12", "1635000", "3475000", "5110000", "24", "1685000", "3575000", "5260000", "24", "65000", "0", "0", "65000", "110000", "0", "0", "0", "0", "0", "0", "0", "0", "1600000", "3400000", "5000000", "23", "1650000", "3500000", "5150000", "23"},
            {"13", "1675000", "3565000", "5240000", "25", "1728000", "3672000", "5400000", "25", "70000", "0", "0", "70000", "100000", "1650000", "3500000", "5150000", "24", "1700000", "3600000", "5300000", "24", "0", "0", "0", "0", "0", "0", "0", "0"}
    };


    @Test
    void reviewedExecutionMatchesAllNineStepsAndEveryCatalogedRecordByte() throws Exception {
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);
        FrozenValuationRepository frozen = mock(FrozenValuationRepository.class);
        AgencyEqualizedValuationRepository agencies = mock(AgencyEqualizedValuationRepository.class);
        AssessmentDetailValuator valuator = mock(AssessmentDetailValuator.class);
        when(parcels.findAllInInputOrder()).thenReturn(parcels());
        when(details.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(frozen.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(frozenValues()));
        when(agencies.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(agencyValues()));
        when(frozen.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agencies.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EifdTifIncrementProcessResult result = new EifdTifIncrementProcessor(
                parcels, details, frozen, agencies, valuator).process(request());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode golden = mapper.readTree(Files.readString(
                Path.of("..", "..", "goldens", "eifd-tif-increment.golden.json")));

        assertEquals(0, result.outcome().returnCode());
        assertEquals(9, result.outcome().steps().size());
        List<String> expectedDisplays = new ArrayList<>();
        for (int index = 0; index < 9; index++) {
            JsonNode expected = golden.get("steps").get(index);
            var actual = result.outcome().steps().get(index);
            assertEquals(expected.get("name").asText(), actual.name());
            assertEquals(expected.get("program").asText(), actual.program());
            assertEquals(expected.get("returnCode").asInt(), actual.returnCode());
            assertEquals(expected.path("skipped").asBoolean(), actual.skipped());
            assertNull(actual.completionCode());
            assertEquals(List.of(), actual.messages());
            assertEquals(List.of(), actual.datasetOps());
            expectedDisplays.addAll(mapper.convertValue(
                    expected.get("sysout"),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
        }
        assertEquals(expectedDisplays, result.outcome().batchDisplays());
        assertEquals(java.util.Map.of(), result.outcome().outputs());

        JsonNode actualDiffs = mapper.valueToTree(result.outcome().datasetDiffs());
        assertEquals(java.util.Set.of("AGYEQVAL", "FRZVALFL"),
                result.outcome().datasetDiffs().keySet());
        assertEquals(9, actualDiffs.at("/AGYEQVAL/changed").size());
        assertEquals(10, actualDiffs.at("/FRZVALFL/changed").size());
        actualDiffs.at("/AGYEQVAL/changed").forEach(record -> assertEquals(86, record.size()));
        actualDiffs.at("/FRZVALFL/changed").forEach(record -> assertEquals(176, record.size()));
        String agencyOneKey = Character.toString((char) 0).repeat(4)
                + Character.toString((char) 0x1f);
        JsonNode agencyOne = actualDiffs.at("/AGYEQVAL/changed").get(agencyOneKey);
        assertNotNull(agencyOne);
        assertEquals("1", agencyOne.get("AEV-AGCYNO").asText());
        assertEquals("0", agencyOne.get("AEV-ANX-PROP-EQV").asText());
        assertEquals("25000000", agencyOne.get("AEV-CC-AIRPOL").asText());
        assertEquals("1250000000", agencyOne.get("AEV-CC-RE").asText());
        assertEquals("5500000", agencyOne.get("AEV-CC-RR").asText());
        assertEquals("12000000", agencyOne.get("AEV-CC-USETAX").asText());
        assertEquals("1", agencyOne.get("AEV_AGCYNO").asText());
        assertEquals("25000000", agencyOne.get("AEV_CC_AIRPOL").asText());

        JsonNode expectedFrozenChanged = authoritativeFrozenChanged(golden);
        assertEquals(expectedFrozenChanged, actualDiffs.at("/FRZVALFL/changed"));

        actualDiffs.at("/AGYEQVAL/changed").forEach(record ->
                record.forEach(field -> assertFalse(
                        field.asText().chars().anyMatch(Character::isISOControl),
                        "control byte leaked from decoded AGYEQVAL field value")));

        JsonNode expectedCatalog = golden.get("catalogedOutputs");
        assertEquals(expectedCatalog.size(), result.outcome().cataloged().size());
        for (int index = 0; index < expectedCatalog.size(); index++) {
            JsonNode expected = expectedCatalog.get(index);
            Cataloged actual = result.outcome().cataloged().get(index);
            assertEquals(expected.get("dsn").asText(), actual.dsn());
            assertEquals(expected.get("generation").asInt(), actual.generation());
            assertEquals(expected.get("recordCount").asInt(), actual.records());
            List<String> expectedRecords = expected.has("records")
                    ? new ObjectMapper().convertValue(expected.get("records"),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {})
                    : List.of();
            assertEquals(expectedRecords.size(), actual.recordData().size(), actual.dsn());
            assertEquals(expectedRecords.size(), actual.recordDataBase64().size(), actual.dsn());
            for (int recordIndex = 0; recordIndex < expectedRecords.size(); recordIndex++) {
                String context = actual.dsn() + " record " + recordIndex;
                assertEquals(expectedRecords.get(recordIndex), actual.recordData().get(recordIndex), context);
                assertEquals(
                        expectedRecords.get(recordIndex),
                        new String(
                                Base64.getDecoder().decode(actual.recordDataBase64().get(recordIndex)),
                                StandardCharsets.US_ASCII),
                        context + " base64");
            }
        }

        assertEquals(List.of(14, 75, 117), result.outputs().stream().skip(1)
                .map(EifdTifIncrementProcessResult.Output::recordCount).toList());
        ArgumentCaptor<FrozenValuation> frozenSaves = ArgumentCaptor.forClass(FrozenValuation.class);
        verify(frozen, times(10)).save(frozenSaves.capture());
        List<FrozenValuation> transitionedDivisions = frozenSaves.getAllValues().stream()
                .filter(value -> "00000000000007".equals(value.getDivisionNumber()))
                .toList();
        assertEquals(1, transitionedDivisions.size());
        assertEquals(125_000L, transitionedDivisions.get(0).getProposedImprovementValue());
        List<EifdTifIncrementProcessResult.Asrea740ReportFact> reportedDivisions =
                result.asrea740ReportFacts().stream()
                        .filter(value -> "00000000000007".equals(value.divisionNumber()))
                        .toList();
        assertEquals(1, reportedDivisions.size());
        assertEquals(125_000L, reportedDivisions.get(0).proposedImprovementValue());

        ArgumentCaptor<AgencyEqualizedValuation> agencySaves =
                ArgumentCaptor.forClass(AgencyEqualizedValuation.class);
        verify(agencies, times(9)).save(agencySaves.capture());
        List<AgencyEqualizedValuation> transitionedAgencies = agencySaves.getAllValues().stream()
                .filter(value -> "000000007".equals(value.getAgencyNumber()))
                .toList();
        assertEquals(1, transitionedAgencies.size());
        assertEquals(125_000L, transitionedAgencies.get(0).getNewPropertyEqualizedValue());
    }

    @Test
    void failedProjectionPreservesNonzeroCompletionAndMessages() {
        EifdTifIncrementProcessResult result = new EifdTifIncrementProcessResult(
                false, 16, 3, 1, 1, 2, List.of(),
                List.of(new EifdTifIncrementProcessResult.Message(
                        "ERROR", "Agency posting input is out of sequence.", "clrtm756-001")));

        var outcome = new EifdTifIncrementOutcomeProjector().failed(result);

        assertEquals(16, outcome.returnCode());
        assertEquals(java.util.Map.of(), outcome.outputs());
        assertEquals(java.util.Map.of(), outcome.datasetDiffs());
        assertEquals(List.of(), outcome.cataloged());
        assertEquals(List.of("Agency posting input is out of sequence."), outcome.batchDisplays());
        assertEquals(1, outcome.steps().size());
        var failed = outcome.steps().get(0);
        assertEquals("S001", failed.name());
        assertEquals("ASREA740", failed.program());
        assertEquals(16, failed.returnCode());
        assertEquals(false, failed.skipped());
        assertEquals("CC 0016", failed.completionCode());
        assertEquals(List.of("Agency posting input is out of sequence."), failed.messages());
        assertEquals(List.of(), failed.datasetOps());
    }

    private JsonNode authoritativeFrozenChanged(JsonNode golden) {
        ObjectNode expected = golden.at("/storeEffects/FRZVALFL/Changed").deepCopy();
        List<JsonNode> records = new ArrayList<>();
        expected.forEach(records::add);
        assertEquals(LIVE_FROZEN_SCALARS.length, records.size());
        for (int recordIndex = 0; recordIndex < LIVE_FROZEN_SCALARS.length; recordIndex++) {
            String[] scalars = LIVE_FROZEN_SCALARS[recordIndex];
            ObjectNode record = (ObjectNode) records.get(recordIndex);
            for (char separator : new char[] {'-', '_'}) {
                String prefix = "FV" + separator;
                record.put(prefix + "DIVNO", scalars[0]);
                for (int valueIndex = 1; valueIndex < scalars.length; valueIndex++) {
                    record.put(prefix + "VAL(" + valueIndex + ")", scalars[valueIndex]);
                }
                String prior = "PRIOR" + separator + "VAL";
                record.put(prefix + prior + "(1)", "2020202020202");
                record.put(prefix + prior + "(2)", "2020202020202");
                record.put(prefix + prior + "(3)", "202020202");
            }
        }
        return expected;
    }

    private EifdTifIncrementRunRequest request() {
        EifdTifIncrementRunRequest request = new EifdTifIncrementRunRequest();
        request.setBusinessDate(LocalDate.of(2025, 9, 15));
        request.setBusinessTime("12:00:00");
        request.setIdempotencyKey("reviewed-eifd-factor-outcome");
        request.setReassessmentControl("260181202526");
        request.setProcessingYear("26");
        request.setReportingYear("2026");
        request.setAnnualEqualizationFactor("10000");
        return request;
    }

    private List<AssessmentParcel> parcels() {
        long[] numbers = {
                10011000010000L, 12022000020000L, 13066000060000L, 14077000070000L,
                20033000030000L, 22055000050000L, 37044000040000L, 71011100110000L,
                76022200120000L, 77033300130000L};
        int[] taxCodes = {10001, 12001, 13001, 14001, 20001, 22001, 37001, 71001, 76001, 77001};
        long[] priorLand = {30000, 35000, 40000, 60000, 75000, 150000, 20000, 20000, 35000, 25000};
        long[] priorImprovement = {70000, 90000, 135000, 140000, 175000, 175000, 30000, 60000, 75000, 65000};
        long[] currentLand = {30000, 35000, 50000, 60000, 70000, 160000, 25000, 20000, 35000, 28000};
        long[] currentImprovement = {70000, 105000, 125000, 165000, 160000, 205000, 45000, 55000, 75000, 72000};
        List<AssessmentParcel> values = new ArrayList<>();
        for (int index = 0; index < numbers.length; index++) {
            AssessmentParcel parcel = new AssessmentParcel();
            parcel.setParcelNumber(numbers[index]);
            parcel.setTaxCode(taxCodes[index]);
            parcel.setPriorLandValue(priorLand[index]);
            parcel.setPriorImprovementValue(priorImprovement[index]);
            parcel.setPriorTotalValue(priorLand[index] + priorImprovement[index]);
            parcel.setCurrentLandValue(currentLand[index]);
            parcel.setCurrentImprovementValue(currentImprovement[index]);
            parcel.setCurrentTotalValue(currentLand[index] + currentImprovement[index]);
            parcel.setEifdPriorLandValue(priorLand[index]);
            parcel.setEifdPriorImprovementValue(priorImprovement[index]);
            parcel.setEifdPriorTotalValue(priorLand[index] + priorImprovement[index]);
            parcel.setEifdCurrentLandValue(currentLand[index]);
            parcel.setEifdCurrentImprovementValue(currentImprovement[index]);
            parcel.setEifdCurrentTotalValue(currentLand[index] + currentImprovement[index]);
            parcel.setPriorParcelStatus(index == 6 ? "1" : "0");
            parcel.setParcelStatus("0");
            values.add(parcel);
        }
        return values;
    }

    private List<FrozenValuation> frozenValues() {
        String[] divisions = {
                "00000000000001", "00000000000002", "00000000000003", "00000000000004",
                "00000000000005", "00000000000006", "00000000000007", "00000000000011",
                "00000000000012", "00000000000013"};
        long[][] v = {
                {1200000,2400000,3600000,12,1250000,2500000,3750000,12,25000,0,0,25000,100000,0,0,0,0,0,0,0,0,1200000,2400000,3600000,12,1250000,2500000,3750000,12},
                {1250000,2500000,3750000,13,1300000,2600000,3900000,13,30000,0,0,30000,140000,1250000,2500000,3750000,13,1300000,2600000,3900000,13,0,0,0,0,0,0,0,0},
                {1300000,2600000,3900000,14,1350000,2700000,4050000,14,35000,0,0,35000,175000,0,0,0,0,0,0,0,0,1300000,2600000,3900000,14,1350000,2700000,4050000,14},
                {1350000,2700000,4050000,15,1400000,2800000,4200000,15,40000,12000,18000,70000,225000,1350000,2700000,4050000,15,1400000,2800000,4200000,15,0,0,0,0,0,0,0,0},
                {1400000,3000000,4400000,16,1450000,3100000,4550000,16,45000,0,0,45000,230000,0,0,0,0,0,0,0,0,1400000,3000000,4400000,16,1450000,3100000,4550000,16},
                {1450000,3100000,4550000,17,1500000,3200000,4700000,17,50000,0,0,50000,365000,1450000,3100000,4550000,17,1500000,3200000,4700000,17,0,0,0,0,0,0,0,0},
                {1500000,3200000,4700000,21,1550000,3300000,4850000,21,55000,0,0,55000,70000,0,0,0,0,0,0,0,0,1500000,3200000,4700000,21,1550000,3300000,4850000,21},
                {1550000,3300000,4850000,22,1600000,3400000,5000000,22,60000,0,0,60000,75000,1550000,3300000,4850000,22,1600000,3400000,5000000,22,0,0,0,0,0,0,0,0},
                {1600000,3400000,5000000,23,1650000,3500000,5150000,23,65000,0,0,65000,110000,0,0,0,0,0,0,0,0,1600000,3400000,5000000,23,1650000,3500000,5150000,23},
                {1650000,3500000,5150000,24,1700000,3600000,5300000,24,70000,0,0,70000,100000,1650000,3500000,5150000,24,1700000,3600000,5300000,24,0,0,0,0,0,0,0,0}};
        List<FrozenValuation> values = new ArrayList<>();
        for (int i = 0; i < divisions.length; i++) {
            FrozenValuation f = new FrozenValuation();
            f.setDivisionNumber(divisions[i]);
            f.setPriorLandValue(v[i][0]); f.setPriorImprovementValue(v[i][1]); f.setPriorTotalValue(v[i][2]); f.setPriorParcelCount(v[i][3]);
            f.setCurrentLandValue(v[i][4]); f.setCurrentImprovementValue(v[i][5]); f.setCurrentTotalValue(v[i][6]); f.setCurrentParcelCount(v[i][7]);
            f.setProposedImprovementValue(v[i][8]); f.setProposedExpired288Value(v[i][9]); f.setProposedCurrent288Value(v[i][10]); f.setProposedTotalValue(v[i][11]); f.setProposedActualValue(v[i][12]);
            f.setChangeActionPriorLandValue(v[i][13]); f.setChangeActionPriorImprovementValue(v[i][14]); f.setChangeActionPriorTotalValue(v[i][15]); f.setChangeActionPriorParcelCount(v[i][16]);
            f.setChangeActionCurrentLandValue(v[i][17]); f.setChangeActionCurrentImprovementValue(v[i][18]); f.setChangeActionCurrentTotalValue(v[i][19]); f.setChangeActionCurrentParcelCount(v[i][20]);
            f.setNoChangeActionPriorLandValue(v[i][21]); f.setNoChangeActionPriorImprovementValue(v[i][22]); f.setNoChangeActionPriorTotalValue(v[i][23]); f.setNoChangeActionPriorParcelCount(v[i][24]);
            f.setNoChangeActionCurrentLandValue(v[i][25]); f.setNoChangeActionCurrentImprovementValue(v[i][26]); f.setNoChangeActionCurrentTotalValue(v[i][27]); f.setNoChangeActionCurrentParcelCount(v[i][28]);
            values.add(f);
        }
        return values;
    }

    private List<AgencyEqualizedValuation> agencyValues() {
        long[] agencies = {1, 2, 3, 4, 5, 6, 7, 11, 12};
        List<AgencyEqualizedValuation> values = new ArrayList<>();
        for (long agency : agencies) {
            AgencyEqualizedValuation value = new AgencyEqualizedValuation();
            value.setAgencyNumber(String.format("%09d", agency));
            if (agency == 1) {
                value.setAnnexedPropertyEqualizedValue(0L);
                value.setCookCountyAirPollutionValue(25_000_000L);
                value.setCookCountyRealEstateValue(1_250_000_000L);
                value.setCookCountyRailroadValue(5_500_000L);
                value.setCookCountyUseTaxValue(12_000_000L);
            }
            values.add(value);
        }
        return values;
    }
}
