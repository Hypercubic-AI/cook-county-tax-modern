package org.cookcounty.tax.application.comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.DatasetOp;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Step;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

class FactorBatchOutcomeControllerTest {

    private static final String SCENARIO = "valuation-preparation";
    private static final long RUN_ID = 4_294_967_296L;
    private static final ObjectMapper OBJECT_MAPPER =
            JsonMapper.builder().findAndAddModules().build();

    @Test
    void returnsExactCompletedSchemaIncludingRequiredEmptyAndNullFields() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        Map<String, Object> request = Map.of("z", 3);
        recorder.accepted(SCENARIO, RUN_ID, request, headers());
        recorder.completed(SCENARIO, RUN_ID, "SUCCEEDED", outcome());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new FactorBatchOutcomeController(recorder)).build();

        mockMvc.perform(get(
                        "/internal/factor-comparator/valuation-preparation-runs/4294967296/outcome"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "schema_version":"factor-modern-batch-outcome.v1",
                          "projection_version":"isomorphic-job-golden-observable-v2",
                          "run_id":4294967296,
                          "status":"SUCCEEDED",
                          "provenance":{
                            "scenario_id":"valuation-preparation",
                            "catalog_routing_id":"valuation-preparation",
                            "reference_job_name":"VALPREP",
                            "jcl_member":"VALPREP",
                            "script_sha256":"1111111111111111111111111111111111111111111111111111111111111111",
                            "golden_sha256":"2222222222222222222222222222222222222222222222222222222222222222",
                            "approved_request_sha256":"59068e3769f66a9341b13678cc54dd69d9bbc848608d0228a1e506d729e5f7a0"
                          },
                          "outcome":{
                            "return_code":0,
                            "steps":[{
                              "name":"S001",
                              "program":"ASREA018",
                              "return_code":0,
                              "skipped":false,
                              "completion_code":null,
                              "messages":[],
                              "dataset_ops":[{
                                "op":"WRITE",
                                "dataset":"output/EMPTY.dat",
                                "records":0
                              }]
                            }],
                            "outputs":{},
                            "batch_displays":[],
                            "dataset_diffs":{},
                            "cataloged":[{
                              "dsn":"output/EMPTY.dat",
                              "generation":1,
                              "records":0,
                              "recordData":[],
                              "recordDataBase64":[]
                            }],
                            "abend":null,
                            "budget_exceeded":false,
                            "rolled_back":false
                          }
                        }
                        """, true));
    }

    @Test
    void returnsDeterministicNotFoundUntilBothHalvesExist() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new FactorBatchOutcomeController(recorder)).build();

        recorder.completed(SCENARIO, RUN_ID, "SUCCEEDED", outcome());
        mockMvc.perform(get(
                        "/internal/factor-comparator/valuation-preparation-runs/4294967296/outcome"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void routeIsAbsentWhenComparatorProfileIsDisabled() throws Exception {
        try (AnnotationConfigWebApplicationContext context =
                     new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(DisabledProfileWebConfiguration.class);
            context.refresh();
            assertFalse(context.containsBean("factorBatchOutcomeController"));

            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
            mockMvc.perform(get(
                            "/internal/factor-comparator/valuation-preparation-runs/1/outcome"))
                    .andExpect(status().isNotFound());
        }
    }

    private static FactorBatchOutcomeRecorder enabledRecorder() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(FactorBatchOutcomeRecorder.PROFILE);
        return new FactorBatchOutcomeRecorder(OBJECT_MAPPER, environment);
    }

    private static Outcome outcome() {
        return new Outcome(
                0,
                List.of(new Step(
                        "S001",
                        "ASREA018",
                        0,
                        false,
                        null,
                        List.of(),
                        List.of(new DatasetOp(
                                "WRITE", "output/EMPTY.dat", 0)))),
                Map.of(),
                List.of(),
                Map.of(),
                List.of(new Cataloged(
                        "output/EMPTY.dat", 1, 0, List.of(), List.of())),
                null,
                false,
                false);
    }

    private static Map<String, String> headers() throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Factor-Scenario-Id", SCENARIO);
        headers.put("X-Factor-Catalog-Routing-Id", SCENARIO);
        headers.put("X-Factor-Reference-Job-Name", "VALPREP");
        headers.put("X-Factor-Jcl-Member", "VALPREP");
        headers.put("X-Factor-Script-Sha256", "1".repeat(64));
        headers.put("X-Factor-Golden-Sha256", "2".repeat(64));
        headers.put("X-Factor-Approved-Request-Sha256", sha256("{\"z\":3}"));
        headers.put(
                "X-Factor-Projection-Version",
                FactorBatchOutcomeRecorder.PROJECTION_VERSION);
        return headers;
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(digest);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({FactorBatchOutcomeRecorder.class, FactorBatchOutcomeController.class})
    static class DisabledProfileWebConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
