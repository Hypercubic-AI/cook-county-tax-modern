package org.cookcounty.tax.application.comparator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class FactorBatchOutcomeRecorder {

    public static final String PROFILE = "factor-comparator";
    public static final String SCHEMA_VERSION = "factor-modern-batch-outcome.v1";
    public static final String PROJECTION_VERSION = "isomorphic-job-golden-observable-v2";

    private static final String SCENARIO_ID_HEADER = "x-factor-scenario-id";
    private static final String CATALOG_ROUTING_ID_HEADER = "x-factor-catalog-routing-id";
    private static final String REFERENCE_JOB_NAME_HEADER = "x-factor-reference-job-name";
    private static final String JCL_MEMBER_HEADER = "x-factor-jcl-member";
    private static final String SCRIPT_SHA256_HEADER = "x-factor-script-sha256";
    private static final String GOLDEN_SHA256_HEADER = "x-factor-golden-sha256";
    private static final String APPROVED_REQUEST_SHA256_HEADER = "x-factor-approved-request-sha256";
    private static final String PROJECTION_VERSION_HEADER = "x-factor-projection-version";

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final ConcurrentMap<RunKey, PendingRun> runs = new ConcurrentHashMap<>();
    @Autowired
    public FactorBatchOutcomeRecorder(ObjectMapper objectMapper, Environment environment) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.enabled = Objects.requireNonNull(environment, "environment")
                .acceptsProfiles(Profiles.of(PROFILE));
    }

    public void accepted(
            String scenarioId,
            long runId,
            Object approvedRequest,
            Map<String, String> headers) {
        if (!enabled) {
            return;
        }
        Optional<Provenance> provenance = validatedProvenance(
                scenarioId, approvedRequest, headers);
        if (provenance.isEmpty()) {
            return;
        }
        RunKey key = new RunKey(scenarioId, runId);
        runs.compute(key, (ignored, current) -> {
            PendingRun pending = current == null ? PendingRun.empty() : current;
            if (pending.provenance() != null
                    && !pending.provenance().equals(provenance.orElseThrow())) {
                return pending;
            }
            return new PendingRun(provenance.orElseThrow(), pending.completion());
        });
    }

    public void completed(
            String scenarioId,
            long runId,
            String status,
            Outcome outcome) {
        if (!enabled) {
            return;
        }
        requireText(scenarioId, "scenarioId");
        requireText(status, "status");
        validateOutcome(Objects.requireNonNull(outcome, "outcome"));

        RunKey key = new RunKey(scenarioId, runId);
        Completion completion = new Completion(status, outcome);
        runs.compute(key, (ignored, current) -> {
            PendingRun pending = current == null ? PendingRun.empty() : current;
            if (pending.completion() != null
                    && !pending.completion().equals(completion)) {
                return pending;
            }
            return new PendingRun(pending.provenance(), completion);
        });
    }

    Optional<RecordedOutcome> get(String scenarioId, long runId) {
        if (!enabled) {
            return Optional.empty();
        }
        PendingRun pending = runs.get(new RunKey(scenarioId, runId));
        if (pending == null || pending.provenance() == null || pending.completion() == null) {
            return Optional.empty();
        }
        return Optional.of(new RecordedOutcome(
                SCHEMA_VERSION,
                PROJECTION_VERSION,
                runId,
                pending.completion().status(),
                pending.provenance(),
                pending.completion().outcome()));
    }

    private Optional<Provenance> validatedProvenance(
            String scenarioId,
            Object approvedRequest,
            Map<String, String> sourceHeaders) {
        if (scenarioId == null || scenarioId.isBlank()
                || approvedRequest == null || sourceHeaders == null) {
            return Optional.empty();
        }
        Optional<Map<String, String>> normalized = normalizeHeaders(sourceHeaders);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        Map<String, String> headers = normalized.orElseThrow();
        String headerScenario = headers.get(SCENARIO_ID_HEADER);
        String catalogRoutingId = headers.get(CATALOG_ROUTING_ID_HEADER);
        String referenceJobName = headers.get(REFERENCE_JOB_NAME_HEADER);
        String jclMember = headers.get(JCL_MEMBER_HEADER);
        String scriptSha256 = headers.get(SCRIPT_SHA256_HEADER);
        String goldenSha256 = headers.get(GOLDEN_SHA256_HEADER);
        String approvedRequestSha256 = headers.get(APPROVED_REQUEST_SHA256_HEADER);
        String projectionVersion = headers.get(PROJECTION_VERSION_HEADER);

        if (!scenarioId.equals(headerScenario)
                || !hasText(catalogRoutingId)
                || !hasText(referenceJobName)
                || !hasText(jclMember)
                || !isSha256(scriptSha256)
                || !isSha256(goldenSha256)
                || !isSha256(approvedRequestSha256)
                || !PROJECTION_VERSION.equals(projectionVersion)
                || !approvedRequestSha256.equals(canonicalSha256(approvedRequest))) {
            return Optional.empty();
        }
        return Optional.of(new Provenance(
                headerScenario,
                catalogRoutingId,
                referenceJobName,
                jclMember,
                scriptSha256,
                goldenSha256,
                approvedRequestSha256));
    }

    private static Optional<Map<String, String>> normalizeHeaders(
            Map<String, String> sourceHeaders) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> header : sourceHeaders.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                continue;
            }
            String name = header.getKey().toLowerCase(Locale.ROOT);
            String previous = normalized.putIfAbsent(name, header.getValue());
            if (previous != null && !previous.equals(header.getValue())) {
                return Optional.empty();
            }
        }
        return Optional.of(normalized);
    }

    private String canonicalSha256(Object approvedRequest) {
        try {
            JsonNode tree = objectMapper.valueToTree(approvedRequest);
            byte[] canonicalJson = objectMapper.writeValueAsBytes(canonicalize(tree));
            return hex(MessageDigest.getInstance("SHA-256").digest(canonicalJson));
        } catch (IllegalArgumentException | JacksonException exception) {
            return "";
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = JsonNodeFactory.instance.objectNode();
            Map<String, JsonNode> fields = new TreeMap<>();
            node.properties().forEach(field -> fields.put(field.getKey(), field.getValue()));
            fields.forEach((name, value) -> sorted.set(name, canonicalize(value)));
            return sorted;
        }
        if (node.isArray()) {
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            node.forEach(value -> array.add(canonicalize(value)));
            return array;
        }
        return node;
    }

    private static void validateOutcome(Outcome outcome) {
        for (Cataloged cataloged : outcome.cataloged()) {
            if (cataloged.records() < 0) {
                throw new IllegalArgumentException("cataloged records must not be negative");
            }
            if (cataloged.recordDataBase64().size() != cataloged.records()) {
                throw new IllegalArgumentException(
                        "cataloged recordDataBase64 count must equal records");
            }
            if (!cataloged.recordData().isEmpty()
                    && cataloged.recordData().size() != cataloged.records()) {
                throw new IllegalArgumentException(
                        "cataloged recordData count must equal records when present");
            }
            for (String encodedRecord : cataloged.recordDataBase64()) {
                try {
                    Base64.getDecoder().decode(encodedRecord);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "cataloged recordDataBase64 contains invalid base64", exception);
                }
            }
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static boolean isSha256(String value) {
        return value != null && value.matches("[0-9a-f]{64}");
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0xf, 16));
            result.append(Character.forDigit(value & 0xf, 16));
        }
        return result.toString();
    }

    private record RunKey(String scenarioId, long runId) {
    }

    private record PendingRun(Provenance provenance, Completion completion) {
        private static PendingRun empty() {
            return new PendingRun(null, null);
        }
    }

    private record Completion(String status, Outcome outcome) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record RecordedOutcome(
            @JsonProperty("schema_version") String schemaVersion,
            @JsonProperty("projection_version") String projectionVersion,
            @JsonProperty("run_id") long runId,
            @JsonProperty("status") String status,
            @JsonProperty("provenance") Provenance provenance,
            @JsonProperty("outcome") Outcome outcome) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Provenance(
            @JsonProperty("scenario_id") String scenarioId,
            @JsonProperty("catalog_routing_id") String catalogRoutingId,
            @JsonProperty("reference_job_name") String referenceJobName,
            @JsonProperty("jcl_member") String jclMember,
            @JsonProperty("script_sha256") String scriptSha256,
            @JsonProperty("golden_sha256") String goldenSha256,
            @JsonProperty("approved_request_sha256") String approvedRequestSha256) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Outcome(
            @JsonProperty("return_code") int returnCode,
            @JsonProperty("steps") List<Step> steps,
            @JsonProperty("outputs") Map<String, List<String>> outputs,
            @JsonProperty("batch_displays") List<String> batchDisplays,
            @JsonProperty("dataset_diffs") Map<String, Object> datasetDiffs,
            @JsonProperty("cataloged") List<Cataloged> cataloged,
            @JsonProperty("abend") Abend abend,
            @JsonProperty("budget_exceeded") boolean budgetExceeded,
            @JsonProperty("rolled_back") boolean rolledBack) {

        public Outcome {
            steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
            outputs = immutableListMap(outputs, "outputs");
            batchDisplays = List.copyOf(
                    Objects.requireNonNull(batchDisplays, "batchDisplays"));
            datasetDiffs = Map.copyOf(
                    Objects.requireNonNull(datasetDiffs, "datasetDiffs"));
            cataloged = List.copyOf(Objects.requireNonNull(cataloged, "cataloged"));
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Step(
            @JsonProperty("name") String name,
            @JsonProperty("program") String program,
            @JsonProperty("return_code") int returnCode,
            @JsonProperty("skipped") boolean skipped,
            @JsonProperty("completion_code") String completionCode,
            @JsonProperty("messages") List<String> messages,
            @JsonProperty("dataset_ops") List<DatasetOp> datasetOps) {

        public Step {
            name = Objects.requireNonNull(name, "name");
            program = Objects.requireNonNull(program, "program");
            messages = List.copyOf(Objects.requireNonNull(messages, "messages"));
            datasetOps = List.copyOf(Objects.requireNonNull(datasetOps, "datasetOps"));
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DatasetOp(
            @JsonProperty("op") String op,
            @JsonProperty("dataset") String dataset,
            @JsonProperty("records") int records) {

        public DatasetOp {
            op = Objects.requireNonNull(op, "op");
            dataset = Objects.requireNonNull(dataset, "dataset");
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Cataloged(
            @JsonProperty("dsn") String dsn,
            @JsonProperty("generation") int generation,
            @JsonProperty("records") int records,
            @JsonProperty("recordData") List<String> recordData,
            @JsonProperty("recordDataBase64") List<String> recordDataBase64) {

        public Cataloged {
            dsn = Objects.requireNonNull(dsn, "dsn");
            recordData = List.copyOf(Objects.requireNonNull(recordData, "recordData"));
            recordDataBase64 = List.copyOf(
                    Objects.requireNonNull(recordDataBase64, "recordDataBase64"));
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Abend(
            @JsonProperty("code") String code,
            @JsonProperty("program") String program,
            @JsonProperty("step") String step) {
    }

    private static Map<String, List<String>> immutableListMap(
            Map<String, List<String>> source,
            String name) {
        Objects.requireNonNull(source, name);
        Map<String, List<String>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(
                Objects.requireNonNull(key, name + " key"),
                List.copyOf(Objects.requireNonNull(value, name + " value"))));
        return Map.copyOf(copy);
    }
}
