package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.cookcounty.tax.application.batch.BatchRunIdempotencyConflictException;
import org.cookcounty.tax.application.batch.BatchRunInvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class BatchRunExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new BatchRunExceptionHandler())
                .build();
    }

    @Test
    void invalidRequestReturnsTheOperationalErrorEnvelope() throws Exception {
        mockMvc.perform(get("/test/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("businessDate is required"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @Test
    void beanValidationReturnsTheFirstDeterministicFieldMessage() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("businessDate is required"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @Test
    void computedConstraintKeepsItsExplicitRequestFieldMessage() throws Exception {
        mockMvc.perform(post("/test/computed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"processYear":"2A"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "processYear must contain exactly two decimal digits."));
    }

    @Test
    void methodParameterValidationReturnsAUsefulMessage() throws Exception {
        mockMvc.perform(get("/test/parameter").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("page must be positive"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @Test
    void constraintViolationReturnsAUsefulMessage() throws Exception {
        mockMvc.perform(get("/test/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be positive"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @Test
    void malformedJsonReturnsADeterministicMessage() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("Request body is malformed or contains an invalid value"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @Test
    void idempotencyConflictReturnsTheOperationalErrorEnvelope() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY_CONFLICT"))
                .andExpect(jsonPath("$.message").value("idempotencyKey already identifies another request"))
                .andExpect(jsonPath("$.ruleId").doesNotExist());
    }

    @RestController
    private static final class FailingController {

        @GetMapping("/test/invalid")
        void invalid() {
            throw new BatchRunInvalidRequestException("businessDate is required");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new BatchRunIdempotencyConflictException(
                    "idempotencyKey already identifies another request");
        }

        @PostMapping("/test/body")
        void body(@Valid @RequestBody ValidationBody body) {}

        @PostMapping("/test/computed")
        void computed(@Valid @RequestBody ComputedValidationBody body) {}

        @GetMapping("/test/parameter")
        void parameter(
                @RequestParam
                @Positive(message = "page must be positive")
                long page) {}

        @GetMapping("/test/constraint")
        void constraint() {
            throw new ConstraintViolationException(
                    VALIDATOR.validate(new ConstraintProbe()));
        }
    }

    private record ValidationBody(
            @NotBlank(message = "businessDate is required") String businessDate) {}

    private static final class ComputedValidationBody {
        private String processYear;

        public void setProcessYear(String processYear) {
            this.processYear = processYear;
        }

        @AssertTrue(message = "processYear must contain exactly two decimal digits.")
        @JsonIgnore
        public boolean isProcessYearFormatted() {
            return processYear != null && processYear.matches("^[0-9]{2}$");
        }
    }

    private static final class ConstraintProbe {
        @Positive(message = "id must be positive")
        private final long id = 0;
    }

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();
}
