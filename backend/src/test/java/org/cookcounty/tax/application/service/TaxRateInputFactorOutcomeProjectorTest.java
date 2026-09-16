package org.cookcounty.tax.application.service;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.cookcounty.tax.domain.port.out.FrozenAgencyAdjustmentRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

class TaxRateInputFactorOutcomeProjectorTest {
    @Test
    void successfulProjectionMatchesExactIsomorphicWireOutcome() throws Exception {
        TaxRateInputSeedFixture seed = TaxRateInputSeedFixture.fromV9();
        TaxRateInputProcessor processor =
                new TaxRateInputProcessor(
                        seed,
                        mock(FrozenAgencyAdjustmentRepository.class),
                        new TaxRateInputKernel());
        TaxRateInputFactorOutcomeProjector projector = new TaxRateInputFactorOutcomeProjector();
        TaxRateInputKernel.Result result = processor.process();
        LocalDate businessDate = LocalDate.of(2025, 9, 15);
        String businessTime = "12:00:00";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode golden =
                mapper.readTree(
                        ReviewedFixture.string(
                                "goldens/clerk-agency-attachment.golden.json"));
        JsonNode wireOutcome =
                mapper.valueToTree(projector.project(businessDate, businessTime, result));

        assertThat(wireOutcome).isEqualTo(expectedWireOutcome(mapper, golden));
    }

    private static JsonNode expectedWireOutcome(ObjectMapper mapper, JsonNode golden) {
        ObjectNode expected = mapper.createObjectNode();
        expected.put("return_code", golden.path("maxRC").intValue());

        ArrayNode expectedSteps = expected.putArray("steps");
        ArrayNode expectedDisplays = mapper.createArrayNode();
        for (JsonNode goldenStep : golden.path("steps")) {
            ObjectNode expectedStep = expectedSteps.addObject();
            expectedStep.put("name", goldenStep.path("name").textValue());
            expectedStep.put("program", goldenStep.path("program").textValue());
            expectedStep.put("return_code", goldenStep.path("returnCode").intValue());
            expectedStep.put("skipped", false);
            expectedStep.put("completion_code", "");
            expectedStep.putArray("messages");
            expectedStep.putArray("dataset_ops");
            goldenStep.path("sysout").forEach(message -> expectedDisplays.add(message.textValue()));
        }

        expected.set("outputs", mapper.createObjectNode());
        expected.set("batch_displays", expectedDisplays);
        expected.set("dataset_diffs", mapper.createObjectNode());

        ArrayNode expectedCataloged = expected.putArray("cataloged");
        for (JsonNode goldenOutput : golden.path("catalogedOutputs")) {
            ObjectNode expectedOutput = expectedCataloged.addObject();
            expectedOutput.put("dsn", goldenOutput.path("dsn").textValue());
            expectedOutput.put("generation", goldenOutput.path("generation").intValue());
            expectedOutput.put("records", goldenOutput.path("recordCount").intValue());
            ArrayNode expectedRecordData = expectedOutput.putArray("recordData");
            ArrayNode expectedRecordDataBase64 = expectedOutput.putArray("recordDataBase64");
            goldenOutput
                    .path("records")
                    .forEach(
                            record -> {
                                String value = record.textValue();
                                expectedRecordData.add(value);
                                expectedRecordDataBase64.add(
                                        Base64.getEncoder()
                                                .encodeToString(
                                                        value.getBytes(StandardCharsets.US_ASCII)));
                            });
        }

        expected.putNull("abend");
        expected.put("budget_exceeded", false);
        expected.put("rolled_back", false);
        return expected;
    }

}
