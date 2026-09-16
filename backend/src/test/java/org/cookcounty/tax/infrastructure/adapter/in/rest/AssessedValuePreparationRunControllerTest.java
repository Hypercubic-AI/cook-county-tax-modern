package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder;
import org.cookcounty.tax.domain.port.in.AssessedValuePreparationRunUseCase;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunRequest;
import org.cookcounty.tax.infrastructure.adapter.in.rest.dto.AssessedValuePreparationRunResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssessedValuePreparationRunControllerTest {

    private MockMvc mockMvc;
    private FakeUseCase useCase;
    private FactorBatchOutcomeRecorder outcomeRecorder;

    @BeforeEach
    void setUp() {
        useCase = new FakeUseCase();
        outcomeRecorder = mock(FactorBatchOutcomeRecorder.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AssessedValuePreparationRunController(useCase, outcomeRecorder))
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }

    @Test
    void postReturnsCreatedSnapshotAndLocation() throws Exception {
        mockMvc.perform(post("/api/assessed-value-preparation-runs")
                        .header("X-Factor-Scenario-Id", "valuation-preparation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessDate":"2025-09-15",
                                  "businessTime":"12:00:00",
                                  "idempotencyKey":"controller-test",
                                  "processYear":"26"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/assessed-value-preparation-runs/2147483648"))
                .andExpect(jsonPath("$.id").value(2147483648L))
                .andExpect(jsonPath("$.status").value("QUEUED"));

        assertEquals(1, useCase.startCalls);
        verify(outcomeRecorder).accepted(
                eq("valuation-preparation"),
                eq(2_147_483_648L),
                any(AssessedValuePreparationRunRequest.class),
                argThat(headers -> headers.entrySet().stream().anyMatch(entry ->
                        "X-Factor-Scenario-Id".equalsIgnoreCase(entry.getKey())
                                && "valuation-preparation".equals(entry.getValue()))));
    }

    @Test
    void postRejectsInvalidProcessYear() throws Exception {
        mockMvc.perform(post("/api/assessed-value-preparation-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessDate":"2025-09-15",
                                  "businessTime":"12:00:00",
                                  "idempotencyKey":"controller-test",
                                  "processYear":"2A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "processYear must contain exactly two decimal digits."));

        assertEquals(0, useCase.startCalls);
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
                .andExpect(jsonPath("$.message").value(
                        "No assessed-value preparation run exists for the supplied id."))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    private static final class FakeUseCase implements AssessedValuePreparationRunUseCase {
        private final AssessedValuePreparationRunResponse response = response();
        private int startCalls;

        @Override
        public Optional<AssessedValuePreparationRunResponse> getAssessedValuePreparationRun(Long id) {
            return response.getId().equals(id) ? Optional.of(response) : Optional.empty();
        }

        @Override
        public AssessedValuePreparationRunResponse startAssessedValuePreparationRun(
                AssessedValuePreparationRunRequest request) {
            startCalls++;
            return response;
        }

        private static AssessedValuePreparationRunResponse response() {
            AssessedValuePreparationRunResponse response = new AssessedValuePreparationRunResponse();
            response.setId(2_147_483_648L);
            response.setStatus("QUEUED");
            response.setBusinessDate(LocalDate.of(2025, 9, 15));
            response.setBusinessTime("12:00:00");
            response.setProcessYear("26");
            response.setOutputs(List.of());
            response.setMessages(List.of());
            return response;
        }
    }
}
