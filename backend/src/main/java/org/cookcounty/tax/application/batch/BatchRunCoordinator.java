package org.cookcounty.tax.application.batch;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.cookcounty.tax.domain.port.out.BatchRunStore.State;
import org.cookcounty.tax.domain.port.out.BatchRunStore.StoredRun;
import org.cookcounty.tax.domain.port.out.DuplicateBatchRunException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/// Coordinates durable reservation, replay, ownership, and execution for one capability.
///
/// A replay never submits work. Lease recovery publishes the stored failure snapshot and never
/// repeats business writes. Completion and recovery use one conditional database update, so a late
/// worker cannot replace a recovered terminal response.
public final class BatchRunCoordinator<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRunCoordinator.class);
    private static final String CONFLICT_MESSAGE =
            "The idempotency key is already associated with different request parameters.";
    private static final String ADMISSION_REJECTION_MESSAGE =
            "The service cannot accept another batch run at this time.";

    /// Namespace that prevents a replay key from selecting another capability's run.
    private final String capability;

    /// Concrete immutable response type used when restoring persisted snapshots.
    private final Class<R> responseType;

    /// Database boundary that arbitrates reservation and terminal-state races.
    private final BatchRunStore store;

    /// Qualified, admission-bounded executor shared with the other capabilities.
    private final Executor executor;

    /// Process ownership and lease renewal, independent of business transactions.
    private final BatchRunLifecycle lifecycle;

    /// Connects one capability to its durable store, lease manager, and bounded executor.
    public BatchRunCoordinator(
            String capability,
            Class<R> responseType,
            BatchRunStore store,
            Executor executor,
            BatchRunLifecycle lifecycle) {
        this.capability = requireText(capability, "capability");
        this.responseType = Objects.requireNonNull(responseType, "responseType");
        this.store = Objects.requireNonNull(store, "store");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    }

    /// Accepts a new run or returns the snapshot for an exact replay.
    public Result<BatchRunStart<R>, BatchRunFailure> start(
            String key,
            String fingerprint,
            LongFunction<R> queuedSnapshotFactory,
            LongFunction<R> admissionRejectedSnapshotFactory,
            LongFunction<R> workerFailureSnapshotFactory,
            LongConsumer worker) {
        requireText(key, "key");
        requireText(fingerprint, "fingerprint");
        Objects.requireNonNull(queuedSnapshotFactory, "queuedSnapshotFactory");
        Objects.requireNonNull(
                admissionRejectedSnapshotFactory, "admissionRejectedSnapshotFactory");
        Objects.requireNonNull(workerFailureSnapshotFactory, "workerFailureSnapshotFactory");
        Objects.requireNonNull(worker, "worker");

        Optional<StoredRun<R>> existing = store.findByKey(capability, key, responseType);
        if (existing.isPresent()) {
            return replay(existing.orElseThrow(), fingerprint);
        }

        StoredRun<R> created;
        try {
            created =
                    store.create(
                            capability,
                            key,
                            fingerprint,
                            responseType,
                            lifecycle.ownerId(),
                            lifecycle.nextDeadline(),
                            queuedSnapshotFactory,
                            workerFailureSnapshotFactory);
        } catch (DuplicateBatchRunException race) {
            StoredRun<R> winner =
                    store.findByKey(capability, key, responseType)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "A concurrent batch reservation was not"
                                                            + " visible",
                                                    race));
            return replay(winner, fingerprint);
        }

        try {
            executor.execute(
                    () ->
                            lifecycle.runWhileOwned(
                                    capability,
                                    created.id(),
                                    () ->
                                            runWorker(
                                                    worker,
                                                    created.id(),
                                                    workerFailureSnapshotFactory)));
        } catch (RejectedExecutionException exception) {
            fail(created.id(), admissionRejectedSnapshotFactory.apply(created.id()));
            return new Result.Err<>(
                    new BatchRunFailure.AdmissionRejected(ADMISSION_REJECTION_MESSAGE));
        }
        return new Result.Ok<>(new BatchRunStart<>(created.id(), created.response(), false));
    }

    /// Reads the latest persisted snapshot for this capability.
    public Result<R, BatchRunFailure> find(long id) {
        Optional<R> stored = store.findById(capability, id, responseType);
        if (stored.isEmpty()) {
            return new Result.Err<>(new BatchRunFailure.RunNotFound(id));
        }
        return new Result.Ok<>(stored.orElseThrow());
    }

    /// Publishes the running snapshot only while this process still owns the reservation.
    public boolean running(long id, R response) {
        return replaceOwned(id, State.RUNNING, response);
    }

    /// Publishes a completed snapshot only while this process still owns a nonterminal run.
    public boolean complete(long id, R response) {
        return replaceOwned(id, State.COMPLETED, response);
    }

    /// Publishes a failed snapshot only while this process still owns a nonterminal run.
    public boolean fail(long id, R response) {
        return replaceOwned(id, State.FAILED, response);
    }

    /// Uses a conditional write so an expired owner's late response cannot revive a run.
    private boolean replaceOwned(long id, State state, R response) {
        return store.replaceOwned(
                capability,
                id,
                lifecycle.ownerId(),
                state,
                Objects.requireNonNull(response, "response"),
                lifecycle.nextDeadline());
    }

    /// Preserves the original identity on exact replay and rejects changed request controls.
    private Result<BatchRunStart<R>, BatchRunFailure> replay(StoredRun<R> run, String fingerprint) {
        if (!run.fingerprint().equals(fingerprint)) {
            return new Result.Err<>(new BatchRunFailure.IdempotencyConflict(CONFLICT_MESSAGE));
        }
        return new Result.Ok<>(new BatchRunStart<>(run.id(), run.response(), true));
    }

    /// Logs unexpected failures and attempts one owner-guarded terminal publication.
    private void runWorker(
            LongConsumer worker, long id, LongFunction<R> workerFailureSnapshotFactory) {
        try {
            worker.accept(id);
        } catch (RuntimeException exception) {
            LOGGER.error("Batch run {} for capability {} failed", id, capability, exception);
            fail(id, workerFailureSnapshotFactory.apply(id));
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
