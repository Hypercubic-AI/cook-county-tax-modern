package org.cookcounty.tax.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
import org.cookcounty.tax.domain.port.out.AssessmentDetailRepository;
import org.cookcounty.tax.domain.port.out.AssessmentParcelRepository;
import org.cookcounty.tax.domain.port.out.HomeownerExemptionRepository;
import org.cookcounty.tax.domain.port.out.HomeownerMasterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class PropertyTaxExemptionsFactorOutcomeProjectorTest {

    private static final List<ReviewedRoot> REVIEWED_ROOTS = List.of(
            new ReviewedRoot(1, 10011000010000L, 10001, 202, 0, "1.000000", 1, 100000L),
            new ReviewedRoot(9, 12022000020000L, 12001, 203, 1, "1.050000", 1, 125000L),
            new ReviewedRoot(25, 13066000060000L, 13001, 201, 3, "0.950000", 1, 175000L),
            new ReviewedRoot(36, 14077000070000L, 14001, 212, 4, "1.150000", 1, 325000L),
            new ReviewedRoot(63, 20033000030000L, 20001, 295, 2, "1.100000", 1, 250000L),
            new ReviewedRoot(86, 22055000050000L, 22001, 241, 6, "1.200000", 1, 750000L),
            new ReviewedRoot(193, 37044000040000L, 37001, 299, 5, "0.500000", 12, 50000L),
            new ReviewedRoot(350, 71011100110000L, 71001, 278, 7, "0.800000", 1, 80000L),
            new ReviewedRoot(508, 76022200120000L, 76001, 297, 8, "1.250000", 1, 110000L),
            new ReviewedRoot(528, 77033300130000L, 77001, 234, 9, "0.750000", 1, 90000L));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PropertyTaxExemptionsFactorOutcomeProjector projector =
            new PropertyTaxExemptionsFactorOutcomeProjector();

    @Test
    void enumeratedReviewedPathProjectsByteForByteHome852Golden() throws Exception {
        assertReviewedPath(
                HomeownerVariant.ENUMERATED,
                "HOME852",
                "ASREA852",
                "homeowner-enumerated-variant.golden.json",
                "asrea852-",
                "asrea853-");
    }

    @Test
    void broadReviewedPathProjectsByteForByteHome853Golden() throws Exception {
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
        assertThat(outcome.abend().code()).isEqualTo("MODERN_PROCESSING_FAILURE");
        assertThat(outcome.rolledBack()).isTrue();
    }

    private void assertReviewedPath(
            HomeownerVariant variant,
            String job,
            String variantProgram,
            String goldenFile,
            String selectedRulePrefix,
            String excludedRulePrefix) throws Exception {
        ProcessResult result = reviewedProcessor().process(
                LocalDate.of(2025, 9, 15), "12:00:00", variant);
        Outcome outcome = projector.project(variant, result);

        JsonNode expected = objectMapper.readTree(repositoryFile("goldens", goldenFile).toFile());
        assertThat(goldenObservable(job, outcome)).isEqualTo(expected);

        Cataloged errors = cataloged(outcome, "output/ASREA841-ERRPRINT.dat");
        Cataloged eligibility = cataloged(
                outcome, "output/" + variantProgram + "-PRNTOUT.dat");
        Cataloged homeout = cataloged(
                outcome, "output/ASREA859-HOMEOUT--ASHMOWFD01.dat");
        assertThat(errors.records()).isEqualTo(9);
        assertThat(errors.recordDataBase64()).hasSize(9);
        assertThat(eligibility.recordData().subList(0, 10))
                .allSatisfy(record -> assertThat(record)
                        .hasSize(133)
                        .contains("PARCEL IS NON-RESIDENTIAL"));
        assertThat(homeout.records()).isZero();
        outcome.cataloged().forEach(output -> assertThat(output.recordDataBase64())
                .containsExactlyElementsOf(output.recordData().stream()
                        .map(record -> Base64.getEncoder().encodeToString(
                                record.getBytes(StandardCharsets.UTF_8)))
                        .toList()));
        assertThat(homeout.recordData()).isEmpty();
        assertThat(outcome.outputs()).isEmpty();
        assertThat(outcome.datasetDiffs()).isEmpty();
        assertThat(outcome.batchDisplays())
                .containsExactly("PROGRAM ASREA841 DATE AND TIME OF RUN =  25/09/20   01200");
        assertThat(outcome.steps()).allSatisfy(step -> {
            assertThat(step.returnCode()).isZero();
            assertThat(step.skipped()).isFalse();
            assertThat(step.completionCode()).isNull();
            assertThat(step.messages()).isEmpty();
            assertThat(step.datasetOps()).isEmpty();
        });
        assertThat(homeout.recordDataBase64()).isEmpty();
        assertThat(outcome.steps().get(2).program()).isEqualTo(variantProgram);
        assertThat(result.ruleOutcomes())
                .filteredOn(rule -> rule.ruleId().startsWith(selectedRulePrefix))
                .hasSize(6)
                .extracting(RuleDisposition::outcome)
                .containsOnly("APPLIED");
        assertThat(result.ruleOutcomes())
                .filteredOn(rule -> rule.ruleId().startsWith(excludedRulePrefix))
                .extracting(RuleDisposition::outcome)
                .hasSize(6)
                .containsOnly("NOT_APPLICABLE");
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
        outcome.cataloged().forEach(output -> {
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

    private static Path repositoryFile(String directory, String file) {
        Path cursor = Path.of("").toAbsolutePath();
        while (cursor != null) {
            Path candidate = cursor.resolve(directory).resolve(file);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            cursor = cursor.getParent();
        }
        throw new IllegalStateException("Repository fixture not found: " + directory + "/" + file);
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
        when(exemptions.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(staleExemptions()));
        when(exemptions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new PropertyTaxExemptionsProcessor(
                homeowners,
                exemptions,
                parcels,
                details,
                new PropertyTaxExemptionsKernel(new AssessmentDetailValuator()));
    }

    private static List<HomeownerMaster> homeowners() {
        List<HomeownerMaster> result = new ArrayList<>();
        for (int index = 0; index < REVIEWED_ROOTS.size(); index++) {
            ReviewedRoot root = REVIEWED_ROOTS.get(index);
            HomeownerMaster homeowner = new HomeownerMaster();
            homeowner.setId((long) index + 1);
            homeowner.setVolumeNumber(root.volume());
            homeowner.setPropertyNumber(root.property());
            homeowner.setTaxCode(root.taxCode());
            homeowner.setAssessmentClass(root.assessmentClass());
            homeowner.setApplicationYear(root.applicationYear());
            homeowner.setProration(new BigDecimal(root.proration()));
            homeowner.setCooperativeQuantity(root.cooperativeQuantity());
            homeowner.setAssessedValue(root.assessedValue());
            homeowner.setResponseStatus(0);
            result.add(homeowner);
        }
        return result;
    }

    private static List<AssessmentParcel> parcels() {
        List<AssessmentParcel> result = new ArrayList<>();
        for (int index = 0; index < REVIEWED_ROOTS.size(); index++) {
            ReviewedRoot root = REVIEWED_ROOTS.get(index);
            AssessmentParcel parcel = new AssessmentParcel();
            parcel.setId((long) index + 1);
            parcel.setVolumeNumber(root.volume());
            parcel.setParcelNumber(root.property());
            parcel.setTaxCode(root.taxCode());
            parcel.setOverallClass(root.assessmentClass());
            parcel.setCurrentTotalValue(root.assessedValue());
            result.add(parcel);
        }
        return result;
    }

    private static List<HomeownerExemption> staleExemptions() {
        List<HomeownerExemption> result = new ArrayList<>();
        for (long id = 1; id <= REVIEWED_ROOTS.size(); id++) {
            HomeownerExemption exemption = new HomeownerExemption();
            exemption.setId(id);
            result.add(exemption);
        }
        return result;
    }

    private record ReviewedRoot(
            int volume,
            long property,
            int taxCode,
            int assessmentClass,
            int applicationYear,
            String proration,
            int cooperativeQuantity,
            long assessedValue) {}
}
