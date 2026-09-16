package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsKernel.HomeownerVariant;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.ProcessResult;
import org.cookcounty.tax.application.service.PropertyTaxExemptionsProcessor.RuleDisposition;
import org.cookcounty.tax.domain.model.AssessmentParcel;
import org.cookcounty.tax.domain.model.HomeownerExemption;
import org.cookcounty.tax.domain.model.HomeownerMaster;
import org.cookcounty.tax.domain.model.PropertyTaxRenewal;
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

class PropertyTaxExemptionsFactorOutcomeProjectorTest {

    private static final List<ReviewedRoot> REVIEWED_ROOTS =
            List.of(
                    new ReviewedRoot(
                            "001",
                            "010011000010000",
                            "10001",
                            202,
                            0,
                            "1.000000",
                            1,
                            new BigDecimal("100000")),
                    new ReviewedRoot(
                            "009",
                            "012022000020000",
                            "12001",
                            203,
                            1,
                            "1.050000",
                            1,
                            new BigDecimal("125000")),
                    new ReviewedRoot(
                            "025",
                            "013066000060000",
                            "13001",
                            201,
                            3,
                            "0.950000",
                            1,
                            new BigDecimal("175000")),
                    new ReviewedRoot(
                            "036",
                            "014077000070000",
                            "14001",
                            212,
                            4,
                            "1.150000",
                            1,
                            new BigDecimal("325000")),
                    new ReviewedRoot(
                            "063",
                            "020033000030000",
                            "20001",
                            295,
                            2,
                            "1.100000",
                            1,
                            new BigDecimal("250000")),
                    new ReviewedRoot(
                            "086",
                            "022055000050000",
                            "22001",
                            241,
                            6,
                            "1.200000",
                            1,
                            new BigDecimal("750000")),
                    new ReviewedRoot(
                            "193",
                            "037044000040000",
                            "37001",
                            299,
                            5,
                            "0.500000",
                            12,
                            new BigDecimal("50000")),
                    new ReviewedRoot(
                            "350",
                            "071011100110000",
                            "71001",
                            278,
                            7,
                            "0.800000",
                            1,
                            new BigDecimal("80000")),
                    new ReviewedRoot(
                            "508",
                            "076022200120000",
                            "76001",
                            297,
                            8,
                            "1.250000",
                            1,
                            new BigDecimal("110000")),
                    new ReviewedRoot(
                            "528",
                            "077033300130000",
                            "77001",
                            234,
                            9,
                            "0.750000",
                            1,
                            new BigDecimal("90000")));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PropertyTaxExemptionsFactorOutcomeProjector projector =
            new PropertyTaxExemptionsFactorOutcomeProjector();

    @Test
    void enumeratedReviewedPathProjectsByteForByteGolden() throws Exception {
        assertReviewedPath(
                HomeownerVariant.ENUMERATED,
                "HOME852",
                "ASREA852",
                "homeowner-enumerated-variant.golden.json",
                "asrea852-",
                "asrea853-");
    }

    @Test
    void broadReviewedPathProjectsByteForByteGolden() throws Exception {
        assertReviewedPath(
                HomeownerVariant.BROAD,
                "HOME853",
                "ASREA853",
                "homeowner-broad-variant.golden.json",
                "asrea853-",
                "asrea852-");
    }

    @Test
    void failedProjectionRetainsFailureEvidence() {
        Outcome outcome = projector.failed(new IllegalStateException("database unavailable"));

        assertThat(outcome.returnCode()).isEqualTo(16);
        assertThat(outcome.steps()).isEmpty();
        assertThat(outcome.outputs()).isEmpty();
        assertThat(outcome.batchDisplays()).containsExactly("database unavailable");
        assertThat(outcome.datasetDiffs()).isEmpty();
        assertThat(outcome.cataloged()).isEmpty();
        assertThat(requireNonNull(outcome.abend(), "failed outcome abend").code())
                .isEqualTo("MODERN_PROCESSING_FAILURE");
        assertThat(outcome.rolledBack()).isTrue();
    }

    private void assertReviewedPath(
            HomeownerVariant variant,
            String job,
            String eligibilityGenerationProgram,
            String goldenFile,
            String selectedRulePrefix,
            String excludedRulePrefix)
            throws Exception {
        ProcessResult result =
                reviewedProcessor().process(LocalDate.of(2025, 9, 15), "12:00:00", variant);
        Outcome outcome = projector.project(variant, result);

        JsonNode expected =
                objectMapper.readTree(ReviewedFixture.string("goldens/" + goldenFile));
        assertThat(goldenObservable(job, outcome)).isEqualTo(expected);

        Cataloged errors = cataloged(outcome, "output/ASREA841-ERRPRINT.dat");
        Cataloged eligibility =
                cataloged(outcome, "output/" + eligibilityGenerationProgram + "-PRNTOUT.dat");
        Cataloged annualExemptionOutput =
                cataloged(outcome, "output/ASREA859-HOMEOUT--ASHMOWFD01.dat");
        assertThat(errors.records()).isEqualTo(9);
        assertThat(errors.recordDataBase64()).hasSize(9);
        for (String record : eligibility.recordData().subList(0, 10)) {
            assertThat(record).hasLength(133);
            assertThat(record).contains("PARCEL IS NON-RESIDENTIAL");
        }
        assertThat(annualExemptionOutput.records()).isEqualTo(0);
        outcome.cataloged()
                .forEach(
                        output ->
                                assertThat(output.recordDataBase64())
                                        .containsExactlyElementsIn(
                                                output.recordData().stream()
                                                        .map(
                                                                record ->
                                                                        Base64.getEncoder()
                                                                                .encodeToString(
                                                                                        record
                                                                                                .getBytes(
                                                                                                        StandardCharsets
                                                                                                                .UTF_8)))
                                                        .toList())
                                        .inOrder());
        assertThat(annualExemptionOutput.recordData()).isEmpty();
        assertThat(outcome.outputs()).isEmpty();
        assertThat(outcome.datasetDiffs()).isEmpty();
        assertThat(outcome.batchDisplays())
                .containsExactly("PROGRAM ASREA841 DATE AND TIME OF RUN =  25/09/20   01200");
        for (var step : outcome.steps()) {
            assertThat(step.returnCode()).isEqualTo(0);
            assertThat(step.skipped()).isFalse();
            assertThat(step.completionCode()).isNull();
            assertThat(step.messages()).isEmpty();
            assertThat(step.datasetOps()).isEmpty();
        }
        assertThat(annualExemptionOutput.recordDataBase64()).isEmpty();
        assertThat(outcome.steps().get(2).program()).isEqualTo(eligibilityGenerationProgram);
        List<String> selectedOutcomes =
                result.ruleOutcomes().stream()
                        .filter(rule -> rule.ruleId().startsWith(selectedRulePrefix))
                        .map(RuleDisposition::outcome)
                        .toList();
        assertThat(selectedOutcomes).hasSize(6);
        assertThat(selectedOutcomes)
                .containsExactlyElementsIn(
                        List.of("APPLIED", "APPLIED", "APPLIED", "APPLIED", "APPLIED", "APPLIED"));
        List<String> excludedOutcomes =
                result.ruleOutcomes().stream()
                        .filter(rule -> rule.ruleId().startsWith(excludedRulePrefix))
                        .map(RuleDisposition::outcome)
                        .toList();
        assertThat(excludedOutcomes).hasSize(6);
        assertThat(excludedOutcomes)
                .containsExactlyElementsIn(
                        List.of(
                                "NOT_APPLICABLE",
                                "NOT_APPLICABLE",
                                "NOT_APPLICABLE",
                                "NOT_APPLICABLE",
                                "NOT_APPLICABLE",
                                "NOT_APPLICABLE"));
    }

    private JsonNode goldenObservable(String job, Outcome outcome) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("job", job);
        root.put("jcl", job);
        root.put("date", "20250915");
        root.put("time", "120000");
        root.put("maxRC", outcome.returnCode());

        ArrayNode steps = root.putArray("steps");
        for (int index = 0; index < outcome.steps().size(); index++) {
            var step = outcome.steps().get(index);
            ObjectNode projected = steps.addObject();
            projected.put("name", step.name());
            projected.put("program", step.program());
            projected.put("returnCode", step.returnCode());
            if (index == 0 && !outcome.batchDisplays().isEmpty()) {
                ArrayNode sysout = projected.putArray("sysout");
                outcome.batchDisplays().forEach(sysout::add);
            }
        }

        ArrayNode cataloged = root.putArray("catalogedOutputs");
        outcome.cataloged()
                .forEach(
                        output -> {
                            ObjectNode projected = cataloged.addObject();
                            projected.put("dsn", output.dsn());
                            projected.put("generation", output.generation());
                            projected.put("recordCount", output.records());
                            if (!output.recordData().isEmpty()) {
                                ArrayNode records = projected.putArray("records");
                                output.recordData().forEach(records::add);
                            }
                        });
        return root;
    }


    private static Cataloged cataloged(Outcome outcome, String dsn) {
        return outcome.cataloged().stream()
                .filter(output -> dsn.equals(output.dsn()))
                .findFirst()
                .orElseThrow();
    }

    private static PropertyTaxExemptionsProcessor reviewedProcessor() {
        HomeownerMasterRepository homeowners = mock(HomeownerMasterRepository.class);
        HomeownerExemptionRepository exemptions = mock(HomeownerExemptionRepository.class);
        AssessmentParcelRepository parcels = mock(AssessmentParcelRepository.class);
        AssessmentDetailRepository details = mock(AssessmentDetailRepository.class);

        when(homeowners.findAllInPropertyOrder()).thenReturn(homeowners());
        when(homeowners.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(parcels.findAllInInputOrder()).thenReturn(parcels());
        when(details.findAllInInputOrder()).thenReturn(List.of());
        when(exemptions.findAllInPersistenceOrder()).thenReturn(staleExemptions());
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new PropertyTaxExemptionsProcessor(
                homeowners,
                exemptions,
                parcels,
                details,
                PropertyTaxExemptionsFactorOutcomeProjectorTest::renewals,
                new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));
    }

    private static List<PropertyTaxRenewal> renewals() {
        return List.of(
                new PropertyTaxRenewal("10011000010000", "B0001"),
                new PropertyTaxRenewal("12022000020000", "B0002"),
                new PropertyTaxRenewal("13066000060000", "B0003"),
                new PropertyTaxRenewal("14077000070000", "B0004"),
                new PropertyTaxRenewal("20033000030000", "B0005"),
                new PropertyTaxRenewal("37044000040000", "B0007"),
                new PropertyTaxRenewal("76022200120000", "B0009"),
                new PropertyTaxRenewal("12022000020000", "LOW01"),
                new PropertyTaxRenewal("12500000000000", "MISS1"),
                new PropertyTaxRenewal("60000000000000", "MISS2"));
    }

    private static List<HomeownerMaster> homeowners() {
        List<HomeownerMaster> result = new ArrayList<>();
        for (int index = 0; index < REVIEWED_ROOTS.size(); index++) {
            ReviewedRoot root = REVIEWED_ROOTS.get(index);
            result.add(
                    new HomeownerMaster(
                            (long) index + 1,
                            0L,
                            root.applicationYear(),
                            root.assessedValue(),
                            root.assessmentClass(),
                            null,
                            null,
                            root.cooperativeQuantity(),
                            BigDecimal.ONE,
                            root.assessedValue(),
                            null,
                            null,
                            null,
                            null,
                            "AB",
                            BigDecimal.ZERO,
                            null,
                            root.property(),
                            new BigDecimal(root.proration()),
                            0,
                            0,
                            null,
                            root.taxCode(),
                            0,
                            null,
                            0,
                            root.volume(),
                            null));
        }
        return result;
    }

    private static List<AssessmentParcel> parcels() {
        List<AssessmentParcel> result = new ArrayList<>();
        for (int index = 0; index < REVIEWED_ROOTS.size(); index++) {
            ReviewedRoot root = REVIEWED_ROOTS.get(index);
            result.add(
                    new AssessmentParcel(
                            (long) index + 1,
                            0L,
                            BigDecimal.ZERO,
                            "",
                            "",
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            root.assessedValue(),
                            0,
                            BigDecimal.ZERO,
                            root.assessmentClass(),
                            root.property(),
                            "",
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            0,
                            root.taxCode(),
                            "0",
                            root.volume(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null));
        }
        return result;
    }

    private static List<HomeownerExemption> staleExemptions() {
        List<HomeownerExemption> result = new ArrayList<>();
        for (long id = 1; id <= REVIEWED_ROOTS.size(); id++) {
            result.add(
                    new HomeownerExemption(
                            id,
                            0L,
                            25,
                            BigDecimal.ZERO,
                            0,
                            null,
                            null,
                            0,
                            null,
                            0,
                            BigDecimal.ONE,
                            BigDecimal.ZERO,
                            null,
                            String.format("%015d", id),
                            null,
                            BigDecimal.ZERO,
                            null,
                            String.format("%015d", id),
                            BigDecimal.ONE,
                            0,
                            0,
                            0,
                            null,
                            null,
                            "00000",
                            null,
                            0,
                            "001",
                            null));
        }
        return result;
    }

    private record ReviewedRoot(
            String volume,
            String property,
            String taxCode,
            int assessmentClass,
            int applicationYear,
            String proration,
            int cooperativeQuantity,
            BigDecimal assessedValue) {}
}
