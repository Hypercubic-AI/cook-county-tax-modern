package org.cookcounty.tax.application.batch;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.fail;

import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class BatchRunCoordinatorTest {

    @Test
    void reservesBeforeDispatchAndReplaysWithoutDispatchingAgain() {
        CapturingExecutor executor = new CapturingExecutor();
        AtomicInteger workerCalls = new AtomicInteger();
        BatchRunCoordinator<Snapshot> coordinator = coordinator(executor);

        BatchRunStart<Snapshot> first =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> new Snapshot(id, "QUEUED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> {
                                    workerCalls.incrementAndGet();
                                    coordinator.complete(id, new Snapshot(id, "COMPLETED", 1, 1));
                                }));

        assertThat(first.id()).isGreaterThan((long) Integer.MAX_VALUE);
        assertThat(first.replayed()).isFalse();
        assertThat(success(coordinator.find(first.id()))).isEqualTo(first.response());
        assertThat(workerCalls.get()).isEqualTo(0);
        assertThat(executor.submissions()).isEqualTo(1);

        BatchRunStart<Snapshot> replay =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> fail("replay must not create another snapshot"),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> fail("replay must not create another worker")));

        assertThat(replay.replayed()).isTrue();
        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(replay.response()).isEqualTo(first.response());
        assertThat(executor.submissions()).isEqualTo(1);

        executor.task().run();
        assertThat(workerCalls.get()).isEqualTo(1);

        BatchRunStart<Snapshot> completedReplay =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> fail("completed replay must not create another snapshot"),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> fail("completed replay must not create another worker")));
        assertThat(completedReplay.replayed()).isTrue();
        assertThat(completedReplay.response().status()).isEqualTo("COMPLETED");
        assertThat(completedReplay.response()).isEqualTo(success(coordinator.find(first.id())));
        assertThat(executor.submissions()).isEqualTo(1);
    }

    @Test
    void concurrentIdenticalStartsReserveOneRunAndDispatchOnce() throws InterruptedException {
        int callerCount = 8;
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = coordinator(executor);
        CountDownLatch ready = new CountDownLatch(callerCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        AtomicInteger snapshotCreations = new AtomicInteger();
        List<BatchRunStart<Snapshot>> results = new CopyOnWriteArrayList<>();
        List<Thread> callers = new ArrayList<>();

        for (int index = 0; index < callerCount; index++) {
            Thread caller =
                    new Thread(
                            () -> {
                                ready.countDown();
                                await(start, failure);
                                try {
                                    results.add(
                                            success(
                                                    coordinator.start(
                                                            "shared-key",
                                                            "shared-fingerprint",
                                                            id -> {
                                                                snapshotCreations.incrementAndGet();
                                                                return new Snapshot(
                                                                        id, "QUEUED", 0, 0);
                                                            },
                                                            rejectedRunId ->
                                                                    new Snapshot(
                                                                            rejectedRunId,
                                                                            "FAILED",
                                                                            0,
                                                                            0),
                                                            rejectedRunId ->
                                                                    new Snapshot(
                                                                            rejectedRunId,
                                                                            "FAILED",
                                                                            0,
                                                                            0),
                                                            id -> {})));
                                } catch (Throwable throwable) {
                                    failure.compareAndSet(null, throwable);
                                }
                            });
            callers.add(caller);
            caller.start();
        }

        ready.await();
        start.countDown();
        for (Thread caller : callers) {
            caller.join();
        }

        if (failure.get() != null) {
            fail(failure.get());
        }
        assertThat(results).hasSize(callerCount);
        assertThat(snapshotCreations.get()).isEqualTo(1);
        assertThat(executor.submissions()).isEqualTo(1);
        assertThat(results.stream().filter(result -> !result.replayed()).count()).isEqualTo(1L);
        long id = results.getFirst().id();
        assertThat(results.stream().allMatch(result -> result.id() == id)).isTrue();
    }

    @Test
    void conflictingFingerprintReturnsTypedFailureWithoutCreatingOrDispatching() {
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = coordinator(executor);
        BatchRunStart<Snapshot> original =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> new Snapshot(id, "QUEUED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> {}));

        BatchRunFailure failure =
                failure(
                        coordinator.start(
                                "request-1",
                                "different-fingerprint",
                                id -> fail("conflict must not create another snapshot"),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> fail("conflict must not create another worker")));

        assertThat(failure).isInstanceOf(BatchRunFailure.IdempotencyConflict.class);
        assertThat(success(coordinator.find(original.id()))).isEqualTo(original.response());
        assertThat(executor.submissions()).isEqualTo(1);
    }

    @Test
    void unknownLongIdentifierReturnsTypedMissingRun() {
        BatchRunCoordinator<Snapshot> coordinator = coordinator(command -> {});

        assertThat(failure(coordinator.find(Long.MAX_VALUE)))
                .isEqualTo(new BatchRunFailure.RunNotFound(Long.MAX_VALUE));
    }

    @Test
    void admissionRejectionPreservesTheRunAlreadyObservedByAConcurrentReplay() {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        BatchRunCoordinator<Snapshot> replayReader =
                new BatchRunCoordinator<>(
                        "test-capability",
                        Snapshot.class,
                        store,
                        command -> fail("replay must not dispatch work"),
                        InMemoryBatchRunStore.lifecycle(store));
        List<BatchRunStart<Snapshot>> observedReplays = new ArrayList<>();
        Executor rejectingExecutor =
                command -> {
                    observedReplays.add(
                            success(
                                    replayReader.start(
                                            "request-1",
                                            "fingerprint-1",
                                            id -> fail("replay must not reserve a second run"),
                                            id -> fail("replay must not reject an existing run"),
                                            id -> fail("replay must not reject an existing run"),
                                            id -> fail("replay must not start a worker"))));
                    throw new RejectedExecutionException("full");
                };
        BatchRunCoordinator<Snapshot> coordinator =
                new BatchRunCoordinator<>(
                        "test-capability",
                        Snapshot.class,
                        store,
                        rejectingExecutor,
                        InMemoryBatchRunStore.lifecycle(store));

        BatchRunFailure rejection =
                failure(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> new Snapshot(id, "QUEUED", 0, 0),
                                id -> new Snapshot(id, "FAILED", 0, 0),
                                id -> new Snapshot(id, "FAILED", 0, 0),
                                id -> fail("rejected work must not run")));

        assertThat(rejection).isInstanceOf(BatchRunFailure.AdmissionRejected.class);
        assertThat(observedReplays).hasSize(1);
        BatchRunStart<Snapshot> replay = observedReplays.getFirst();
        assertThat(replay.replayed()).isTrue();
        assertThat(replay.response().status()).isEqualTo("QUEUED");
        assertThat(success(replayReader.find(replay.id())))
                .isEqualTo(new Snapshot(replay.id(), "FAILED", 0, 0));
        BatchRunStart<Snapshot> terminalReplay =
                success(
                        replayReader.start(
                                "request-1",
                                "fingerprint-1",
                                id -> fail("terminal replay must not reserve a second run"),
                                id -> fail("terminal replay must not reject an existing run"),
                                id -> fail("terminal replay must not reject an existing run"),
                                id -> fail("terminal replay must not start a worker")));
        assertThat(terminalReplay.id()).isEqualTo(replay.id());
        assertThat(terminalReplay.response().status()).isEqualTo("FAILED");
    }

    @Test
    void replacementPublishesOnlyWholeSnapshots() throws InterruptedException {
        BatchRunCoordinator<Snapshot> coordinator = coordinator(command -> {});
        long id =
                success(
                                coordinator.start(
                                        "request-1",
                                        "fingerprint-1",
                                        runId -> new Snapshot(runId, "QUEUED", 0, 0),
                                        rejectedRunId ->
                                                new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                        rejectedRunId ->
                                                new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                        runId -> {}))
                        .id();
        Snapshot zero = new Snapshot(id, "RUNNING", 0, 0);
        Snapshot one = new Snapshot(id, "RUNNING", 1, 1);
        assertThat(coordinator.running(id, zero)).isTrue();

        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<@Nullable Throwable> failure = new AtomicReference<>();
        Thread writer =
                new Thread(
                        () -> {
                            await(start, failure);
                            for (int index = 0; index < 50_000; index++) {
                                coordinator.running(id, (index & 1) == 0 ? one : zero);
                            }
                        });
        Thread reader =
                new Thread(
                        () -> {
                            await(start, failure);
                            for (int index = 0; index < 50_000; index++) {
                                Snapshot observed = success(coordinator.find(id));
                                if (observed.left() != observed.right()) {
                                    failure.compareAndSet(
                                            null, new AssertionError("observed a torn snapshot"));
                                    return;
                                }
                            }
                        });

        writer.start();
        reader.start();
        start.countDown();
        writer.join();
        reader.join();

        if (failure.get() != null) {
            fail(failure.get());
        }
    }

    @Test
    void unhandledWorkerFailurePublishesATerminalSnapshot() {
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = coordinator(executor);
        BatchRunStart<Snapshot> run =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> new Snapshot(id, "QUEUED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                rejectedRunId -> new Snapshot(rejectedRunId, "FAILED", 0, 0),
                                id -> {
                                    throw new IllegalStateException("worker failed");
                                }));

        executor.task().run();
        assertThat(success(coordinator.find(run.id())))
                .isEqualTo(new Snapshot(run.id(), "FAILED", 0, 0));
    }

    @Test
    void recoveredQueuedRunDoesNotStartItsBusinessWorker() {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator =
                new BatchRunCoordinator<>(
                        "test-capability",
                        Snapshot.class,
                        store,
                        executor,
                        InMemoryBatchRunStore.lifecycle(store));
        AtomicInteger workerCalls = new AtomicInteger();
        BatchRunStart<Snapshot> run =
                success(
                        coordinator.start(
                                "request-1",
                                "fingerprint-1",
                                id -> new Snapshot(id, "QUEUED", 0, 0),
                                id -> new Snapshot(id, "FAILED", 0, 0),
                                id -> new Snapshot(id, "FAILED", 0, 0),
                                id -> workerCalls.incrementAndGet()));

        assertThat(store.recoverExpired(Instant.MAX)).isEqualTo(1);
        executor.task().run();

        assertThat(workerCalls.get()).isEqualTo(0);
        assertThat(success(coordinator.find(run.id())).status()).isEqualTo("FAILED");
    }

    private static BatchRunCoordinator<Snapshot> coordinator(Executor executor) {
        InMemoryBatchRunStore store = new InMemoryBatchRunStore();
        return new BatchRunCoordinator<>(
                "test-capability",
                Snapshot.class,
                store,
                executor,
                InMemoryBatchRunStore.lifecycle(store));
    }

    private static <T> T success(Result<T, BatchRunFailure> result) {
        return switch (result) {
            case Result.Ok<T, BatchRunFailure>(var value) -> value;
            case Result.Err<T, BatchRunFailure>(var error) ->
                    throw new AssertionError("Expected success but got " + error);
        };
    }

    private static <T> BatchRunFailure failure(Result<T, BatchRunFailure> result) {
        return switch (result) {
            case Result.Ok<T, BatchRunFailure>(var value) ->
                    throw new AssertionError("Expected failure but got " + value);
            case Result.Err<T, BatchRunFailure>(var error) -> error;
        };
    }

    private static void await(CountDownLatch latch, AtomicReference<@Nullable Throwable> failure) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            failure.compareAndSet(null, exception);
        }
    }

    private record Snapshot(long id, String status, int left, int right) {}

    private static final class CapturingExecutor implements Executor {
        private final AtomicInteger submissions = new AtomicInteger();
        private final AtomicReference<@Nullable Runnable> task = new AtomicReference<>();

        @Override
        public void execute(Runnable command) {
            submissions.incrementAndGet();
            if (!task.compareAndSet(null, command)) {
                throw new AssertionError("more than one task was dispatched");
            }
        }

        int submissions() {
            return submissions.get();
        }

        Runnable task() {
            return Objects.requireNonNull(task.get(), "No worker was submitted");
        }
    }
}
