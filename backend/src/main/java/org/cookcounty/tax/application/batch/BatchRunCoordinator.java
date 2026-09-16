package org.cookcounty.tax.application.batch;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-memory acceptance and snapshot registry for one type of request-driven batch run.
 * Response objects are immutable by convention; callers publish changes by replacing a
 * complete response snapshot.
 */
public final class BatchRunCoordinator<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRunCoordinator.class);
    private static final long FIRST_RUN_ID = (long) Integer.MAX_VALUE + 1L;
    private static final String CONFLICT_MESSAGE =
            "The idempotency key is already associated with different request parameters.";

    private final Executor executor;
    private final AtomicLong nextRunId = new AtomicLong(FIRST_RUN_ID - 1L);
    private final ConcurrentMap<Long, R> snapshots = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Reservation> reservations = new ConcurrentHashMap<>();

    public BatchRunCoordinator(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public BatchRunStartResult<R> start(
            String key,
            String fingerprint,
            LongFunction<R> queuedSnapshotFactory,
            LongConsumer worker) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(fingerprint, "fingerprint");
        Objects.requireNonNull(queuedSnapshotFactory, "queuedSnapshotFactory");
        Objects.requireNonNull(worker, "worker");

        AtomicBoolean created = new AtomicBoolean();
        Reservation reservation = reservations.compute(key, (unused, existing) -> {
            if (existing != null) {
                if (!existing.fingerprint().equals(fingerprint)) {
                    throw new BatchRunIdempotencyConflictException(CONFLICT_MESSAGE);
                }
                return existing;
            }

            long id = allocateId();
            R queuedSnapshot = Objects.requireNonNull(
                    queuedSnapshotFactory.apply(id), "queuedSnapshotFactory returned null");
            snapshots.put(id, queuedSnapshot);
            created.set(true);
            return new Reservation(fingerprint, id);
        });

        R response = snapshots.get(reservation.id());
        BatchRunStartResult<R> result =
                new BatchRunStartResult<>(reservation.id(), response, !created.get());
        if (created.get()) {
            executor.execute(() -> runWorker(worker, reservation.id()));
        }
        return result;
    }

    public Optional<R> find(long id) {
        return Optional.ofNullable(snapshots.get(id));
    }

    public void replace(long id, R response) {
        Objects.requireNonNull(response, "response");
        if (snapshots.replace(id, response) == null) {
            throw new IllegalArgumentException("Unknown batch run id: " + id);
        }
    }

    private long allocateId() {
        long id = nextRunId.incrementAndGet();
        if (id <= 0) {
            throw new IllegalStateException("Batch run id space is exhausted");
        }
        return id;
    }

    private void runWorker(LongConsumer worker, long id) {
        try {
            worker.accept(id);
        } catch (RuntimeException exception) {
            LOGGER.error("Batch run {} worker failed before publishing a terminal snapshot", id, exception);
        }
    }

    private record Reservation(String fingerprint, long id) {}
}
