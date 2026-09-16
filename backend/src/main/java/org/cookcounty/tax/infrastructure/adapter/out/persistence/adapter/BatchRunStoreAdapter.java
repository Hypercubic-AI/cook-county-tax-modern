package org.cookcounty.tax.infrastructure.adapter.out.persistence.adapter;

import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.cookcounty.tax.domain.port.out.DuplicateBatchRunException;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.entity.BatchRunEntity;
import org.cookcounty.tax.infrastructure.adapter.out.persistence.repository.JpaBatchRunRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;

/// Stores complete batch snapshots and arbitrates owner transitions in database transactions.
///
/// Recovery copies a prebuilt failure snapshot. It does not deserialize requests or submit business
/// work. Conditional updates prevent a late worker from changing a recovered terminal snapshot.
@Component
public class BatchRunStoreAdapter implements BatchRunStore {
    private static final String IDEMPOTENCY_CONSTRAINT = "uq_batch_runs_capability_key";
    private static final List<State> NONTERMINAL = List.of(State.QUEUED, State.RUNNING);

    private final JpaBatchRunRepository repository;
    private final ObjectMapper objectMapper;

    /// Creates the adapter for durable run entities and typed JSON snapshots.
    public BatchRunStoreAdapter(JpaBatchRunRepository repository, ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    @Transactional(readOnly = true)
    public <R> Optional<StoredRun<R>> findByKey(
            String capability, String idempotencyKey, Class<R> responseType) {
        return repository
                .findByCapabilityAndIdempotencyKey(capability, idempotencyKey)
                .map(entity -> stored(entity, responseType));
    }

    @Override
    @Transactional(readOnly = true)
    public <R> Optional<R> findById(String capability, long id, Class<R> responseType) {
        return repository
                .findByCapabilityAndId(capability, id)
                .map(entity -> deserialize(entity.getResponseJson(), responseType));
    }

    @Override
    @Transactional
    public <R> StoredRun<R> create(
            String capability,
            String idempotencyKey,
            String fingerprint,
            Class<R> responseType,
            String ownerId,
            Instant leaseExpiresAt,
            LongFunction<R> initialSnapshotFactory,
            LongFunction<R> recoverySnapshotFactory) {
        BatchRunEntity entity;
        try {
            entity =
                    repository.saveAndFlush(
                            new BatchRunEntity(
                                    capability,
                                    idempotencyKey,
                                    fingerprint,
                                    ownerId,
                                    leaseExpiresAt));
        } catch (DataIntegrityViolationException exception) {
            if (isIdempotencyConflict(exception)) {
                throw new DuplicateBatchRunException(exception);
            }
            throw exception;
        }
        long id = Objects.requireNonNull(entity.getId(), "database did not assign a run id");
        R response = Objects.requireNonNull(initialSnapshotFactory.apply(id), "initial snapshot");
        R recovery = Objects.requireNonNull(recoverySnapshotFactory.apply(id), "recovery snapshot");
        entity.setSnapshots(serialize(response), serialize(recovery));
        return new StoredRun<>(id, fingerprint, response);
    }

    @Override
    @Transactional
    public boolean renew(String capability, long id, String ownerId, Instant leaseExpiresAt) {
        return repository.renew(capability, id, ownerId, leaseExpiresAt, NONTERMINAL) == 1;
    }

    @Override
    @Transactional
    public <R> boolean replaceOwned(
            String capability,
            long id,
            String ownerId,
            State targetState,
            R response,
            Instant leaseExpiresAt) {
        boolean terminal = targetState.terminal();
        return repository.replaceOwned(
                        capability,
                        id,
                        ownerId,
                        targetState,
                        serialize(Objects.requireNonNull(response, "response")),
                        terminal ? null : ownerId,
                        terminal ? null : leaseExpiresAt,
                        NONTERMINAL)
                == 1;
    }

    @Override
    @Transactional
    public int recoverExpired(Instant now) {
        return repository.recoverExpired(now, State.FAILED, NONTERMINAL);
    }

    private <R> StoredRun<R> stored(BatchRunEntity entity, Class<R> responseType) {
        return new StoredRun<>(
                Objects.requireNonNull(entity.getId(), "persisted run id is null"),
                entity.getFingerprint(),
                deserialize(entity.getResponseJson(), responseType));
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Batch response snapshot cannot be serialized", exception);
        }
    }

    private <R> R deserialize(String json, Class<R> responseType) {
        try {
            return Objects.requireNonNull(
                    objectMapper.readValue(json, responseType), "Persisted batch response is null");
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Persisted batch response snapshot cannot be read", exception);
        }
    }

    private static boolean isIdempotencyConflict(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && IDEMPOTENCY_CONSTRAINT.equals(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}
