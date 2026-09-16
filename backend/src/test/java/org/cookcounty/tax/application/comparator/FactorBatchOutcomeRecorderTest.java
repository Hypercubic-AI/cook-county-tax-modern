package org.cookcounty.tax.application.comparator;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Cataloged;
import org.cookcounty.tax.application.comparator.FactorBatchOutcomeRecorder.Outcome;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class FactorBatchOutcomeRecorderTest {

    private static final String SCENARIO = "valuation-preparation";
    private static final long LARGE_RUN_ID = 4_294_967_296L;
    private static final ObjectMapper OBJECT_MAPPER =
            JsonMapper.builder().findAndAddModules().build();

    @Test
    void mergesCompletionBeforeAcceptanceAndPreservesSignedLongRunId() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        Map<String, Object> request = requestWithUnsortedKeys();

        recorder.completed(SCENARIO, LARGE_RUN_ID, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, LARGE_RUN_ID).isEmpty()).isTrue();

        recorder.accepted(SCENARIO, LARGE_RUN_ID, request, mixedCaseHeaders(request));

        FactorBatchOutcomeRecorder.RecordedOutcome recorded =
                recorder.get(SCENARIO, LARGE_RUN_ID).orElseThrow();
        assertThat(recorded.runId()).isEqualTo(LARGE_RUN_ID);
        assertThat(recorded.status()).isEqualTo("SUCCEEDED");
        assertThat(recorded.provenance().scenarioId()).isEqualTo(SCENARIO);
    }

    @Test
    void canonicalRequestHashSortsObjectKeysRecursively() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        Map<String, Object> request = requestWithUnsortedKeys();
        Map<String, String> headers = headers(request);

        assertThat(headers.get("X-Factor-Approved-Request-Sha256"))
                .isEqualTo(sha256("{\"a\":{\"a\":1,\"b\":2},\"z\":3}"));

        recorder.accepted(SCENARIO, 1L, request, headers);
        recorder.completed(SCENARIO, 1L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 1L).isPresent()).isTrue();
    }

    @Test
    void publicMapperHashesLocalDatesAsApprovedJsonStrings() throws Exception {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(FactorBatchOutcomeRecorder.PROFILE);
        FactorBatchOutcomeRecorder recorder =
                new FactorBatchOutcomeRecorder(OBJECT_MAPPER, environment);
        Map<String, Object> request = Map.of("businessDate", LocalDate.of(2025, 9, 15));
        Map<String, String> headers = new LinkedHashMap<>(headers(request));
        headers.put(
                "X-Factor-Approved-Request-Sha256", sha256("{\"businessDate\":\"2025-09-15\"}"));

        recorder.accepted(SCENARIO, 10L, request, headers);
        recorder.completed(SCENARIO, 10L, "SUCCEEDED", emptyOutcome());

        assertThat(recorder.get(SCENARIO, 10L).isPresent()).isTrue();
    }

    @Test
    void refusesMissingAndInvalidProvenanceWithoutPublishing() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        Map<String, Object> request = requestWithUnsortedKeys();

        Map<String, String> missingHeader = new LinkedHashMap<>(headers(request));
        missingHeader.remove("X-Factor-Golden-Sha256");
        recorder.accepted(SCENARIO, 2L, request, missingHeader);
        recorder.completed(SCENARIO, 2L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 2L).isEmpty()).isTrue();

        Map<String, String> wrongRequestHash = new LinkedHashMap<>(headers(request));
        wrongRequestHash.put("X-Factor-Approved-Request-Sha256", "0".repeat(64));
        recorder.accepted(SCENARIO, 3L, request, wrongRequestHash);
        recorder.completed(SCENARIO, 3L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 3L).isEmpty()).isTrue();

        Map<String, String> wrongProjection = new LinkedHashMap<>(headers(request));
        wrongProjection.put("X-Factor-Projection-Version", "other-projection");
        recorder.accepted(SCENARIO, 4L, request, wrongProjection);
        recorder.completed(SCENARIO, 4L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 4L).isEmpty()).isTrue();

        Map<String, String> malformedScriptHash = new LinkedHashMap<>(headers(request));
        malformedScriptHash.put("X-Factor-Script-Sha256", "not-a-sha");
        recorder.accepted(SCENARIO, 5L, request, malformedScriptHash);
        recorder.completed(SCENARIO, 5L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 5L).isEmpty()).isTrue();
    }

    @Test
    void acceptsKnownEmptyCatalogWithExplicitEmptyBase64() throws Exception {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();
        Map<String, Object> request = requestWithUnsortedKeys();
        Outcome outcome =
                outcomeWithCatalog(new Cataloged("output/EMPTY.dat", 1, 0, List.of(), List.of()));

        recorder.accepted(SCENARIO, 6L, request, headers(request));
        recorder.completed(SCENARIO, 6L, "SUCCEEDED", outcome);
        assertThat(recorder.get(SCENARIO, 6L).isPresent()).isTrue();
    }

    @Test
    void rejectsCatalogWhoseCountOrBase64ContentIsIncomplete() {
        FactorBatchOutcomeRecorder recorder = enabledRecorder();

        Outcome countMismatch =
                outcomeWithCatalog(new Cataloged("output/BAD.dat", 1, 1, List.of(), List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> recorder.completed(SCENARIO, 7L, "SUCCEEDED", countMismatch));
        assertThat(recorder.get(SCENARIO, 7L).isEmpty()).isTrue();

        Outcome invalidBase64 =
                outcomeWithCatalog(
                        new Cataloged("output/BAD.dat", 1, 1, List.of("record"), List.of("%%%")));
        assertThrows(
                IllegalArgumentException.class,
                () -> recorder.completed(SCENARIO, 8L, "SUCCEEDED", invalidBase64));
        assertThat(recorder.get(SCENARIO, 8L).isEmpty()).isTrue();
    }

    @Test
    void disabledProfileMakesEveryRecordingOperationANoOp() throws Exception {
        FactorBatchOutcomeRecorder recorder =
                new FactorBatchOutcomeRecorder(OBJECT_MAPPER, new MockEnvironment());
        Map<String, Object> request = requestWithUnsortedKeys();

        recorder.accepted(SCENARIO, 9L, request, headers(request));
        recorder.completed(SCENARIO, 9L, "SUCCEEDED", emptyOutcome());
        assertThat(recorder.get(SCENARIO, 9L).isPresent()).isFalse();
    }

    private static FactorBatchOutcomeRecorder enabledRecorder() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(FactorBatchOutcomeRecorder.PROFILE);
        return new FactorBatchOutcomeRecorder(OBJECT_MAPPER, environment);
    }

    private static Outcome emptyOutcome() {
        return new Outcome(
                0, List.of(), Map.of(), List.of(), Map.of(), List.of(), null, false, false);
    }

    private static Outcome outcomeWithCatalog(Cataloged cataloged) {
        return new Outcome(
                0,
                List.of(),
                Map.of(),
                List.of(),
                Map.of(),
                List.of(cataloged),
                null,
                false,
                false);
    }

    private static Map<String, Object> requestWithUnsortedKeys() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("b", 2);
        nested.put("a", 1);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("z", 3);
        request.put("a", nested);
        return request;
    }

    private static Map<String, String> headers(Object request) throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Factor-Scenario-Id", SCENARIO);
        headers.put("X-Factor-Catalog-Routing-Id", "valuation-preparation");
        headers.put("X-Factor-Reference-Job-Name", "VALPREP");
        headers.put("X-Factor-Jcl-Member", "VALPREP");
        headers.put("X-Factor-Script-Sha256", "1".repeat(64));
        headers.put("X-Factor-Golden-Sha256", "2".repeat(64));
        headers.put(
                "X-Factor-Approved-Request-Sha256", sha256("{\"a\":{\"a\":1,\"b\":2},\"z\":3}"));
        headers.put("X-Factor-Projection-Version", FactorBatchOutcomeRecorder.PROJECTION_VERSION);
        return headers;
    }

    private static Map<String, String> mixedCaseHeaders(Object request) throws Exception {
        Map<String, String> mixedCase = new LinkedHashMap<>();
        headers(request).forEach((name, value) -> mixedCase.put(alternatingCase(name), value));
        return mixedCase;
    }

    private static String alternatingCase(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            result.append(
                    index % 2 == 0
                            ? Character.toUpperCase(character)
                            : Character.toLowerCase(character));
        }
        return result.toString();
    }

    private static String sha256(String value) throws Exception {
        byte[] digest =
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(digest);
    }
}
