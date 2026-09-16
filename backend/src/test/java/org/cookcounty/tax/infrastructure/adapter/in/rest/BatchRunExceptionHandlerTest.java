package org.cookcounty.tax.infrastructure.adapter.in.rest;

import static com.google.common.truth.Truth.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.stream.Stream;

class BatchRunExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new FailingController())
                        .setControllerAdvice(new BatchRunExceptionHandler())
                        .build();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBodies")
    void reportsEveryInvalidFieldWithoutReportingValidFields(
            String description, String body, List<String> invalidFields) throws Exception {
        String response =
                mockMvc.perform(
                                post("/test/body")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String message = JsonMapper.builder().build().readTree(response).path("message").asString();
        for (String field : List.of("businessDate", "businessTime")) {
            if (invalidFields.contains(field)) {
                assertThat(message).contains(field);
            } else {
                assertThat(message).doesNotContain(field);
            }
        }
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("both controls absent", "{}", List.of("businessDate", "businessTime")),
                Arguments.of(
                        "only time absent",
                        "{\"businessDate\":\"2026-09-16\"}",
                        List.of("businessTime")));
    }

    @Test
    void methodValidationReportsTheParameterName() throws Exception {
        String response =
                mockMvc.perform(get("/test/parameter").param("page", "0"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(JsonMapper.builder().build().readTree(response).path("message").asString())
                .contains("page");
    }

    @Test
    void malformedJsonUsesTheSharedErrorShape() throws Exception {
        mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void unexpectedFailuresDoNotExposeExceptionDetails() throws Exception {
        String response =
                mockMvc.perform(get("/test/unexpected"))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                        .andExpect(jsonPath("$.message").isString())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(response).doesNotContain("private-database-detail");
        assertThat(response).doesNotContain("IllegalStateException");
    }

    @RestController
    private static final class FailingController {
        @PostMapping("/test/body")
        void body(@Valid @RequestBody ValidationBody body) {}

        @GetMapping("/test/parameter")
        void parameter(@RequestParam @Positive long page) {}

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("private-database-detail");
        }
    }

    private record ValidationBody(@NotBlank String businessDate, @NotBlank String businessTime) {}
}
