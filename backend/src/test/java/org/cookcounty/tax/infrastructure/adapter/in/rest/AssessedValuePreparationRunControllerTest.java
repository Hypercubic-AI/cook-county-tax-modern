package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.domain.contract.dto.AssessedValuePreparationRunResponse;
import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

class AssessedValuePreparationRunControllerTest {

    private MockMvc mockMvc;
    private FakeUseCase useCase;
    private FactorBatchOutcomeRecorder outcomeRecorder;

    @BeforeEach
    void setUp() {
        useCase = new FakeUseCase();
        outcomeRecorder = mock(FactorBatchOutcomeRecorder.class);
        mockMvc =
                MockMvcBuilders.standaloneSetup(
                                new AssessedValuePreparationRunController(useCase, outcomeRecorder))
                        .setControllerAdvice(new BatchRunExceptionHandler())
                        .build();
    }

    @Test
    void postReturnsCreatedSnapshotAndLocation() throws Exception {
        mockMvc.perform(
                        post("/api/assessed-value-preparation-runs")
                                .header("X-Factor-Scenario-Id", "valuation-preparation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "businessDate":"2025-09-15",
                                          "businessTime":"12:00:00",
                                          "idempotencyKey":"controller-test",
                                          "processYear":"26"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        "/api/assessed-value-preparation-runs/2147483648"))
                .andExpect(jsonPath("$.id").value(2147483648L))
                .andExpect(jsonPath("$.status").value("QUEUED"));

        assertThat(useCase.startCalls).isEqualTo(1);
        verify(outcomeRecorder)
                .accepted(
                        eq("valuation-preparation"),
                        eq(2_147_483_648L),
                        any(AssessedValuePreparationRunRequest.class),
                        argThat(
                                headers ->
                                        headers.entrySet().stream()
                                                .anyMatch(
                                                        entry ->
                                                                "X-Factor-Scenario-Id"
                                                                                .equalsIgnoreCase(
                                                                                        entry
                                                                                                .getKey())
                                                                        && "valuation-preparation"
                                                                                .equals(
                                                                                        entry
                                                                                                .getValue()))));
    }

    @Test
    void replayPreservesCreatedStatusAndCanonicalLocation() throws Exception {
        useCase.replayed = true;

        mockMvc.perform(
                        post("/api/assessed-value-preparation-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "businessDate":"2025-09-15",
                                          "businessTime":"12:00:00",
                                          "idempotencyKey":"controller-test",
                                          "processYear":"26"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        "/api/assessed-value-preparation-runs/2147483648"));
    }

    @Test
    void postRejectsInvalidProcessYear() throws Exception {
        mockMvc.perform(
                        post("/api/assessed-value-preparation-runs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "businessDate":"2025-09-15",
                                          "businessTime":"12:00:00",
                                          "idempotencyKey":"controller-test",
                                          "processYear":"2A"
                                        }
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));

        assertThat(useCase.startCalls).isEqualTo(0);
    }

    @Test
    void getReturnsCurrentSnapshot() throws Exception {
        mockMvc.perform(get("/api/assessed-value-preparation-runs/2147483648"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void getUnknownIdReturnsApprovedJsonErrorAndOmitsOptionalRuleId() throws Exception {
        mockMvc.perform(get("/api/assessed-value-preparation-runs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RUN_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No assessed-value preparation run exists for the supplied"
                                                + " id."))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    private static final class FakeUseCase implements AssessedValuePreparationRunUseCase {
        private final AssessedValuePreparationRunResponse response = response();
        private int startCalls;
        private boolean replayed;

        @Override
        public Result<AssessedValuePreparationRunResponse, BatchRunFailure>
                getAssessedValuePreparationRun(Long id) {
            return response.id().equals(id)
                    ? new Result.Ok<>(response)
                    : new Result.Err<>(new BatchRunFailure.RunNotFound(id));
        }

        @Override
        public Result<BatchRunStart<AssessedValuePreparationRunResponse>, BatchRunFailure>
                startAssessedValuePreparationRun(AssessedValuePreparationRunRequest request) {
            startCalls++;
            return new Result.Ok<>(new BatchRunStart<>(response.id(), response, replayed));
        }

        private static AssessedValuePreparationRunResponse response() {
            return new AssessedValuePreparationRunResponse(
                    LocalDate.of(2025, 9, 15),
                    "12:00:00",
                    2_147_483_648L,
                    List.of(),
                    List.of(),
                    null,
                    "26",
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    "QUEUED");
        }
    }
}
