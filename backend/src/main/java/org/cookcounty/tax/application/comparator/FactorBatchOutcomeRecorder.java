package org.cookcounty.tax.application.comparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

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

/// Joins approved request provenance with completed outcomes for comparator-only inspection.
///
/// Either event can arrive first. Conflicting repeated events do not replace accepted evidence.
/// This process-local cache is not the durable store for production run state or idempotency.
@Component
public class FactorBatchOutcomeRecorder {

    /// Profile that enables evidence recording and the internal outcome endpoint.
    public static final String PROFILE = "factor-comparator";

    /// Version of the serialized outcome envelope consumed by the comparator.
    public static final String SCHEMA_VERSION = "factor-modern-batch-outcome.v1";

    /// Version of the observable projection used to compare modern and reference outcomes.
    public static final String PROJECTION_VERSION = "isomorphic-job-golden-observable-v2";

    private static final String SCENARIO_ID_HEADER = "x-factor-scenario-id";
    private static final String CATALOG_ROUTING_ID_HEADER = "x-factor-catalog-routing-id";
    private static final String REFERENCE_JOB_NAME_HEADER = "x-factor-reference-job-name";
    private static final String JCL_MEMBER_HEADER = "x-factor-jcl-member";
    private static final String SCRIPT_SHA256_HEADER = "x-factor-script-sha256";
    private static final String GOLDEN_SHA256_HEADER = "x-factor-golden-sha256";
    private static final String APPROVED_REQUEST_SHA256_HEADER = "x-factor-approved-request-sha256";
    private static final String PROJECTION_VERSION_HEADER = "x-factor-projection-version";

    /// Serializes the approved request for canonical checksum verification.
    private final ObjectMapper objectMapper;

    /// Keeps evidence collection inactive outside the comparator profile.
    private final boolean enabled;

    /// Atomically joins the two evidence events for each scenario and application run.
    private final ConcurrentMap<RunKey, PendingRun> runs = new ConcurrentHashMap<>();

    /// Connects request serialization and profile selection to the evidence cache.
    ///
    /// @param objectMapper mapper configured for the application's request contracts
    /// @param environment active profiles that determine whether recording is enabled
    @Autowired
    public FactorBatchOutcomeRecorder(ObjectMapper objectMapper, Environment environment) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.enabled =
                Objects.requireNonNull(environment, "environment")
                        .acceptsProfiles(Profiles.of(PROFILE));
    }

    /// Records request provenance only when all headers match the approved request.
    ///
    /// Missing or conflicting metadata is ignored. Repeated acceptance cannot replace different
    /// provenance already associated with the same scenario and run.
    ///
    /// @param scenarioId comparator scenario, or null when no scenario was supplied
    /// @param runId database-assigned application run identifier
    /// @param approvedRequest request whose canonical JSON must match the supplied checksum
    /// @param headers comparator provenance headers, or null when absent
    public void accepted(
            @Nullable String scenarioId,
            long runId,
            @Nullable Object approvedRequest,
            @Nullable Map<String, String> headers) {
        if (!enabled) {
            return;
        }
        Optional<Provenance> provenance = validatedProvenance(scenarioId, approvedRequest, headers);
        if (provenance.isEmpty()) {
            return;
        }
        RunKey key = new RunKey(provenance.orElseThrow().scenarioId(), runId);
        runs.compute(
                key,
                (ignored, current) -> {
                    PendingRun pending = current == null ? PendingRun.empty() : current;
                    if (pending.provenance() != null
                            && !pending.provenance().equals(provenance.orElseThrow())) {
                        return pending;
                    }
                    return new PendingRun(provenance.orElseThrow(), pending.completion());
                });
    }

    /// Records a completed projection without replacing a conflicting prior completion.
    ///
    /// @param scenarioId nonblank comparator scenario identifier
    /// @param runId database-assigned application run identifier
    /// @param status nonblank terminal status supplied by the capability
    /// @param outcome validated observable output with complete encoded records
    /// @throws IllegalArgumentException when record counts or encoded record data are invalid
    public void completed(String scenarioId, long runId, String status, Outcome outcome) {
        if (!enabled) {
            return;
        }
        requireText(scenarioId, "scenarioId");
        requireText(status, "status");
        validateOutcome(Objects.requireNonNull(outcome, "outcome"));

        RunKey key = new RunKey(scenarioId, runId);
        Completion completion = new Completion(status, outcome);
        runs.compute(
                key,
                (ignored, current) -> {
                    PendingRun pending = current == null ? PendingRun.empty() : current;
                    if (pending.completion() != null && !pending.completion().equals(completion)) {
                        return pending;
                    }
                    return new PendingRun(pending.provenance(), completion);
                });
    }

    /// Returns evidence only after both approval and completion arrive for the same run.
    Optional<RecordedOutcome> get(String scenarioId, long runId) {
        if (!enabled) {
            return Optional.empty();
        }
        PendingRun pending = runs.get(new RunKey(scenarioId, runId));
        if (pending == null || pending.provenance() == null || pending.completion() == null) {
            return Optional.empty();
        }
        return Optional.of(
                new RecordedOutcome(
                        SCHEMA_VERSION,
                        PROJECTION_VERSION,
                        runId,
                        pending.completion().status(),
                        pending.provenance(),
                        pending.completion().outcome()));
    }

    /// Validates provenance fields and the checksum of the canonical request representation.
    private Optional<Provenance> validatedProvenance(
            @Nullable String scenarioId,
            @Nullable Object approvedRequest,
            @Nullable Map<String, String> sourceHeaders) {
        if (scenarioId == null
                || scenarioId.isBlank()
                || approvedRequest == null
                || sourceHeaders == null) {
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

        if (headerScenario == null
                || !scenarioId.equals(headerScenario)
                || catalogRoutingId == null
                || catalogRoutingId.isBlank()
                || referenceJobName == null
                || referenceJobName.isBlank()
                || jclMember == null
                || jclMember.isBlank()
                || scriptSha256 == null
                || !isSha256(scriptSha256)
                || goldenSha256 == null
                || !isSha256(goldenSha256)
                || approvedRequestSha256 == null
                || !isSha256(approvedRequestSha256)
                || !PROJECTION_VERSION.equals(projectionVersion)
                || !approvedRequestSha256.equals(canonicalSha256(approvedRequest))) {
            return Optional.empty();
        }
        return Optional.of(
                new Provenance(
                        headerScenario,
                        catalogRoutingId,
                        referenceJobName,
                        jclMember,
                        scriptSha256,
                        goldenSha256,
                        approvedRequestSha256));
    }

    /// Normalizes case-insensitive header names and rejects conflicting repeated values.
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

    /// Hashes canonical JSON, or returns an empty checksum when the request cannot be serialized.
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

    /// Sorts object keys recursively while preserving array order and scalar representations.
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

    /// Checks that every catalog entry has the declared number of valid encoded records.
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

    /// Requires a present, nonblank identifier at the internal evidence boundary.
    private static String requireText(String value, String name) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /// Recognizes a lowercase SHA-256 digest in its 64-character hexadecimal representation.
    private static boolean isSha256(String value) {
        return value.matches("[0-9a-f]{64}");
    }

    /// Encodes digest bytes as lowercase hexadecimal without changing their order.
    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0xf, 16));
            result.append(Character.forDigit(value & 0xf, 16));
        }
        return result.toString();
    }

    /// Identifies one comparison run independently of every other scenario.
    private record RunKey(String scenarioId, long runId) {}

    /// Holds whichever evidence event arrived first until the second event becomes available.
    private record PendingRun(@Nullable Provenance provenance, @Nullable Completion completion) {
        private static PendingRun empty() {
            return new PendingRun(null, null);
        }
    }

    /// Retains the terminal status and its corresponding observable projection as one value.
    private record Completion(String status, Outcome outcome) {}

    /// Complete comparison evidence exposed by the internal endpoint.
    ///
    /// @param schemaVersion serialized envelope version
    /// @param projectionVersion observable projection version
    /// @param runId database-assigned application run identifier
    /// @param status terminal application status
    /// @param provenance verified approval metadata
    /// @param outcome completed observable results
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record RecordedOutcome(
            @JsonProperty("schema_version") String schemaVersion,
            @JsonProperty("projection_version") String projectionVersion,
            @JsonProperty("run_id") long runId,
            @JsonProperty("status") String status,
            @JsonProperty("provenance") Provenance provenance,
            @JsonProperty("outcome") Outcome outcome) {}

    /// Verified links between an approved request and its reference evidence.
    ///
    /// Reference names belong to the comparison protocol, not the application's business model.
    ///
    /// @param scenarioId approved scenario identifier
    /// @param catalogRoutingId catalog entry that selected the reference evidence
    /// @param referenceJobName name carried by the reference job's evidence
    /// @param jclMember reference job member carried by the comparison protocol
    /// @param scriptSha256 lowercase SHA-256 digest of the reference script
    /// @param goldenSha256 lowercase SHA-256 digest of the approved observable output
    /// @param approvedRequestSha256 lowercase SHA-256 digest of canonical request JSON
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Provenance(
            @JsonProperty("scenario_id") String scenarioId,
            @JsonProperty("catalog_routing_id") String catalogRoutingId,
            @JsonProperty("reference_job_name") String referenceJobName,
            @JsonProperty("jcl_member") String jclMember,
            @JsonProperty("script_sha256") String scriptSha256,
            @JsonProperty("golden_sha256") String goldenSha256,
            @JsonProperty("approved_request_sha256") String approvedRequestSha256) {}

    /// Immutable observable effects from one completed batch run.
    ///
    /// @param returnCode final numeric completion code
    /// @param steps ordered step outcomes
    /// @param outputs ordered output lines grouped by output channel
    /// @param batchDisplays ordered batch display messages
    /// @param datasetDiffs projected persistent-record differences
    /// @param cataloged complete cataloged record output
    /// @param abend abnormal termination details, or null after normal completion
    /// @param budgetExceeded whether the reference execution exceeded its execution allowance
    /// @param rolledBack whether the run reports rolled-back effects
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Outcome(
            @JsonProperty("return_code") int returnCode,
            @JsonProperty("steps") List<Step> steps,
            @JsonProperty("outputs") Map<String, List<String>> outputs,
            @JsonProperty("batch_displays") List<String> batchDisplays,
            @JsonProperty("dataset_diffs") Map<String, Object> datasetDiffs,
            @JsonProperty("cataloged") List<Cataloged> cataloged,
            @JsonProperty("abend") @Nullable Abend abend,
            @JsonProperty("budget_exceeded") boolean budgetExceeded,
            @JsonProperty("rolled_back") boolean rolledBack) {

        /// Copies collection inputs so later caller mutation cannot alter published evidence.
        public Outcome {
            steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
            outputs = immutableListMap(outputs, "outputs");
            batchDisplays = List.copyOf(Objects.requireNonNull(batchDisplays, "batchDisplays"));
            datasetDiffs = Map.copyOf(Objects.requireNonNull(datasetDiffs, "datasetDiffs"));
            cataloged = List.copyOf(Objects.requireNonNull(cataloged, "cataloged"));
        }
    }

    /// Observable completion of one ordered processing step.
    ///
    /// @param name step name from the comparison protocol
    /// @param program reference program identity supplied by external provenance
    /// @param returnCode numeric step completion code
    /// @param skipped whether execution omitted this step
    /// @param completionCode textual completion code, or null when no code was emitted
    /// @param messages ordered step messages
    /// @param datasetOps ordered persistent-dataset operations
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Step(
            @JsonProperty("name") String name,
            @JsonProperty("program") String program,
            @JsonProperty("return_code") int returnCode,
            @JsonProperty("skipped") boolean skipped,
            @JsonProperty("completion_code") @Nullable String completionCode,
            @JsonProperty("messages") List<String> messages,
            @JsonProperty("dataset_ops") List<DatasetOp> datasetOps) {

        /// Requires step identity and copies the observable message and operation sequences.
        public Step {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(program, "program");
            messages = List.copyOf(Objects.requireNonNull(messages, "messages"));
            datasetOps = List.copyOf(Objects.requireNonNull(datasetOps, "datasetOps"));
        }
    }

    /// Counted operation on one dataset in the observable comparison projection.
    ///
    /// @param op operation name defined by the projection protocol
    /// @param dataset dataset identity from external evidence
    /// @param records number of records affected by the operation
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DatasetOp(
            @JsonProperty("op") String op,
            @JsonProperty("dataset") String dataset,
            @JsonProperty("records") int records) {

        /// Requires the operation and dataset identities before the value can be published.
        public DatasetOp {
            Objects.requireNonNull(op, "op");
            Objects.requireNonNull(dataset, "dataset");
        }
    }

    /// Complete record output for one cataloged dataset generation.
    ///
    /// @param dsn dataset identity from the comparison protocol
    /// @param generation catalog generation number
    /// @param records number of encoded records
    /// @param recordData optional decoded record text, empty when only bytes are available
    /// @param recordDataBase64 exact record bytes encoded in Base64, in output order
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Cataloged(
            @JsonProperty("dsn") String dsn,
            @JsonProperty("generation") int generation,
            @JsonProperty("records") int records,
            @JsonProperty("recordData") List<String> recordData,
            @JsonProperty("recordDataBase64") List<String> recordDataBase64) {

        /// Preserves record order and prevents later mutation of the supplied record lists.
        public Cataloged {
            Objects.requireNonNull(dsn, "dsn");
            recordData = List.copyOf(Objects.requireNonNull(recordData, "recordData"));
            recordDataBase64 =
                    List.copyOf(Objects.requireNonNull(recordDataBase64, "recordDataBase64"));
        }
    }

    /// Abnormal termination metadata supplied by a completed comparison projection.
    ///
    /// @param code termination code from the reference evidence
    /// @param program program identity, or null when the failure has no reference-program context
    /// @param step step identity, or null when the failure occurred outside a projected step
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Abend(
            @JsonProperty("code") String code,
            @JsonProperty("program") @Nullable String program,
            @JsonProperty("step") @Nullable String step) {}

    /// Copies both a channel map and each ordered line list before publication.
    private static Map<String, List<String>> immutableListMap(
            Map<String, List<String>> source, String name) {
        Objects.requireNonNull(source, name);
        Map<String, List<String>> copy = new LinkedHashMap<>();
        source.forEach(
                (key, value) ->
                        copy.put(
                                Objects.requireNonNull(key, name + " key"),
                                List.copyOf(Objects.requireNonNull(value, name + " value"))));
        return Map.copyOf(copy);
    }
}
