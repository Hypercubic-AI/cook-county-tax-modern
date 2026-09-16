package org.cookcounty.tax.application.batch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class BatchRunCoordinatorTest {

    @Test
    void reservesBeforeDispatchAndReplaysWithoutDispatchingAgain() {
        CapturingExecutor executor = new CapturingExecutor();
        AtomicInteger workerCalls = new AtomicInteger();
        BatchRunCoordinator<Snapshot> coordinator = new BatchRunCoordinator<>(executor);

        BatchRunStartResult<Snapshot> first = coordinator.start(
                "request-1",
                "fingerprint-1",
                id -> new Snapshot(id, "QUEUED", 0, 0),
                id -> {
                    workerCalls.incrementAndGet();
                    coordinator.replace(id, new Snapshot(id, "COMPLETED", 1, 1));
                });

        assertTrue(first.id() > Integer.MAX_VALUE);
        assertFalse(first.replayed());
        assertSame(first.response(), coordinator.find(first.id()).orElseThrow());
        assertEquals(0, workerCalls.get());
        assertEquals(1, executor.submissions());

        BatchRunStartResult<Snapshot> replay = coordinator.start(
                "request-1",
                "fingerprint-1",
                id -> fail("replay must not create another snapshot"),
                id -> fail("replay must not create another worker"));

        assertTrue(replay.replayed());
        assertEquals(first.id(), replay.id());
        assertSame(first.response(), replay.response());
        assertEquals(1, executor.submissions());

        executor.task().run();
        assertEquals(1, workerCalls.get());

        BatchRunStartResult<Snapshot> completedReplay = coordinator.start(
                "request-1",
                "fingerprint-1",
                id -> fail("completed replay must not create another snapshot"),
                id -> fail("completed replay must not create another worker"));
        assertTrue(completedReplay.replayed());
        assertEquals("COMPLETED", completedReplay.response().status());
        assertSame(coordinator.find(first.id()).orElseThrow(), completedReplay.response());
        assertEquals(1, executor.submissions());
    }

    @Test
    void concurrentIdenticalStartsReserveOneRunAndDispatchOnce() throws InterruptedException {
        int callerCount = 8;
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = new BatchRunCoordinator<>(executor);
        CountDownLatch ready = new CountDownLatch(callerCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicInteger snapshotCreations = new AtomicInteger();
        List<BatchRunStartResult<Snapshot>> results = new CopyOnWriteArrayList<>();
        List<Thread> callers = new ArrayList<>();

        for (int index = 0; index < callerCount; index++) {
            Thread caller = new Thread(() -> {
                ready.countDown();
                await(start, failure);
                try {
                    results.add(coordinator.start(
                            "shared-key",
                            "shared-fingerprint",
                            id -> {
                                snapshotCreations.incrementAndGet();
                                return new Snapshot(id, "QUEUED", 0, 0);
                            },
                            id -> {}));
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
        assertEquals(callerCount, results.size());
        assertEquals(1, snapshotCreations.get());
        assertEquals(1, executor.submissions());
        assertEquals(1L, results.stream().filter(result -> !result.replayed()).count());
        long id = results.get(0).id();
        assertTrue(results.stream().allMatch(result -> result.id() == id));
    }

    @Test
    void rejectsAConflictingFingerprintWithoutCreatingOrDispatchingAnotherRun() {
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = new BatchRunCoordinator<>(executor);
        BatchRunStartResult<Snapshot> original = coordinator.start(
                "request-1",
                "fingerprint-1",
                id -> new Snapshot(id, "QUEUED", 0, 0),
                id -> {});

        BatchRunIdempotencyConflictException exception = assertThrows(
                BatchRunIdempotencyConflictException.class,
                () -> coordinator.start(
                        "request-1",
                        "different-fingerprint",
                        id -> fail("conflict must not create another snapshot"),
                        id -> fail("conflict must not create another worker")));

        assertEquals(
                "The idempotency key is already associated with different request parameters.",
                exception.getMessage());
        assertSame(original.response(), coordinator.find(original.id()).orElseThrow());
        assertEquals(1, executor.submissions());
    }

    @Test
    void replacementPublishesOnlyWholeSnapshots() throws InterruptedException {
        BatchRunCoordinator<Snapshot> coordinator = new BatchRunCoordinator<>(command -> {});
        long id = coordinator.start(
                        "request-1",
                        "fingerprint-1",
                        runId -> new Snapshot(runId, "QUEUED", 0, 0),
                        runId -> {})
                .id();
        Snapshot zero = new Snapshot(id, "RUNNING", 0, 0);
        Snapshot one = new Snapshot(id, "RUNNING", 1, 1);
        coordinator.replace(id, zero);

        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread writer = new Thread(() -> {
            await(start, failure);
            for (int index = 0; index < 50_000; index++) {
                coordinator.replace(id, (index & 1) == 0 ? one : zero);
            }
        });
        Thread reader = new Thread(() -> {
            await(start, failure);
            for (int index = 0; index < 50_000; index++) {
                Snapshot observed = coordinator.find(id).orElseThrow();
                if (observed.left() != observed.right()) {
                    failure.compareAndSet(null, new AssertionError("observed a torn snapshot"));
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
    void workerFailureIsContainedByTheDispatchedTask() {
        CapturingExecutor executor = new CapturingExecutor();
        BatchRunCoordinator<Snapshot> coordinator = new BatchRunCoordinator<>(executor);
        coordinator.start(
                "request-1",
                "fingerprint-1",
                id -> new Snapshot(id, "QUEUED", 0, 0),
                id -> {
                    throw new IllegalStateException("worker failed");
                });

        assertDoesNotThrow(executor.task()::run);
    }

    private static void await(CountDownLatch latch, AtomicReference<Throwable> failure) {
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
        private final AtomicReference<Runnable> task = new AtomicReference<>();

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
            return task.get();
        }
    }
}
