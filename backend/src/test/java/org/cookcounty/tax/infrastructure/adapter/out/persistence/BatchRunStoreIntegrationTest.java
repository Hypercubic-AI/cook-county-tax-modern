package org.cookcounty.tax.infrastructure.adapter.out.persistence;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.cookcounty.tax.application.batch.BatchRunCoordinator;
import org.cookcounty.tax.application.batch.BatchRunLifecycle;
import org.cookcounty.tax.domain.contract.BatchRunFailure;
import org.cookcounty.tax.domain.contract.BatchRunStart;
import org.cookcounty.tax.domain.contract.Result;
import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.cookcounty.tax.domain.port.out.BatchRunStore.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class BatchRunStoreIntegrationTest extends PostgreSqlIntegrationTestSupport {

    @Autowired private BatchRunStore store;
    @Autowired private BatchRunLifecycle lifecycle;
    private String capability;

    @BeforeEach
    void isolateCapability() {
        capability = "test-" + UUID.randomUUID();
    }

    @ParameterizedTest(name = "fresh deployment has no scenario rows in {0}")
    @ValueSource(
            strings = {
                "AssessmentParcelEntity",
                "HomeownerMasterEntity",
                "MaintainedHomesteadExemptionEntity",
                "FrozenValuationEntity",
                "AgencyEqualizedValuationEntity",
                "FrozenAgencyAdjustmentEntity"
            })
    void freshDeploymentDoesNotLoadScenarioRecords(String entityName) {
        Long count =
                entityManager
                        .createQuery(
                                "select count(entry) from " + entityName + " entry", Long.class)
                        .getSingleResult();
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void committedSnapshotsRemainReadableAndStayWithinTheirCapability() {
        Instant lease = Instant.now().plusSeconds(300);
        BatchRunStore.StoredRun<Snapshot> created =
                store.create(
                        capability,
                        "request",
                        "controls",
                        Snapshot.class,
                        "owner",
                        lease,
                        id -> new Snapshot(id, "QUEUED", List.of()),
                        id -> new Snapshot(id, "FAILED", List.of("interrupted")));
        assertThat(
                        store.replaceOwned(
                                capability,
                                created.id(),
                                "owner",
                                State.COMPLETED,
                                new Snapshot(created.id(), "COMPLETED", List.of("report")),
                                lease))
                .isTrue();

        assertThat(store.findById(capability, created.id(), Snapshot.class).orElseThrow())
                .isEqualTo(new Snapshot(created.id(), "COMPLETED", List.of("report")));
        assertThat(store.findByKey(capability, "request", Snapshot.class).orElseThrow().id())
                .isEqualTo(created.id());
        assertThat(store.findById("different-capability", created.id(), Snapshot.class)).isEmpty();
    }

    @Test
    void failedSnapshotCreationRollsBackTheKeyReservation() {
        assertThrows(
                IllegalStateException.class,
                () ->
                        store.create(
                                capability,
                                "request",
                                "controls",
                                Snapshot.class,
                                "owner",
                                Instant.now().plusSeconds(300),
                                id -> {
                                    throw new IllegalStateException("snapshot construction failed");
                                },
                                id -> new Snapshot(id, "FAILED", List.of())));

        assertThat(store.findByKey(capability, "request", Snapshot.class)).isEmpty();
        BatchRunStore.StoredRun<Snapshot> replacement =
                createOwned("request", "owner", Instant.now().plusSeconds(300));
        assertThat(
                        store.findById(capability, replacement.id(), Snapshot.class)
                                .orElseThrow()
                                .status())
                .isEqualTo("QUEUED");
    }

    @Test
    void concurrentCoordinatorsReserveOneIdentityAndDispatchOneWorker() throws Exception {
        int callerCount = 4;
        CyclicBarrier start = new CyclicBarrier(callerCount);
        AtomicInteger dispatched = new AtomicInteger();
        List<Future<BatchRunStart<Snapshot>>> futures = new ArrayList<>();
        List<BatchRunStart<Snapshot>> outcomes = new ArrayList<>();
        try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int index = 0; index < callerCount; index++) {
                futures.add(
                        callers.submit(
                                () -> {
                                    BatchRunCoordinator<Snapshot> coordinator =
                                            new BatchRunCoordinator<>(
                                                    capability,
                                                    Snapshot.class,
                                                    store,
                                                    command -> dispatched.incrementAndGet(),
                                                    lifecycle);
                                    start.await(10, TimeUnit.SECONDS);
                                    return success(
                                            coordinator.start(
                                                    "shared-key",
                                                    "shared-controls",
                                                    id -> new Snapshot(id, "QUEUED", List.of()),
                                                    id ->
                                                            new Snapshot(
                                                                    id,
                                                                    "FAILED",
                                                                    List.of("capacity")),
                                                    id ->
                                                            new Snapshot(
                                                                    id,
                                                                    "FAILED",
                                                                    List.of("worker failure")),
                                                    id -> {}));
                                }));
            }
            for (Future<BatchRunStart<Snapshot>> future : futures) {
                outcomes.add(future.get(20, TimeUnit.SECONDS));
            }
        }

        long id = outcomes.getFirst().id();
        assertThat(outcomes.stream().map(BatchRunStart::id).distinct().toList())
                .containsExactly(id);
        assertThat(outcomes.stream().filter(outcome -> !outcome.replayed()).count()).isEqualTo(1L);
        assertThat(dispatched.get()).isEqualTo(1);
        assertThat(store.findById(capability, id, Snapshot.class).orElseThrow().status())
                .isEqualTo("QUEUED");
    }

    @Test
    void recoveryFailsAbandonedQueuedAndRunningRunsWithoutRepeatingWork() {
        Instant expired = Instant.parse("2026-09-16T10:00:00Z");
        Instant recoveryTime = expired.plusSeconds(1);
        BatchRunStore.StoredRun<Snapshot> queued = createOwned("queued", "owner-a", expired);
        BatchRunStore.StoredRun<Snapshot> running = createOwned("running", "owner-a", expired);
        assertThat(
                        store.replaceOwned(
                                capability,
                                running.id(),
                                "owner-a",
                                State.RUNNING,
                                new Snapshot(running.id(), "RUNNING", List.of()),
                                expired))
                .isTrue();

        assertThat(store.recoverExpired(recoveryTime)).isEqualTo(2);

        assertThat(store.findById(capability, queued.id(), Snapshot.class).orElseThrow().status())
                .isEqualTo("FAILED");
        assertThat(store.findById(capability, running.id(), Snapshot.class).orElseThrow().status())
                .isEqualTo("FAILED");
    }

    @Test
    void recoveryPreservesAHealthySiblingLease() {
        Instant now = Instant.parse("2026-09-16T10:00:00Z");
        BatchRunStore.StoredRun<Snapshot> active =
                createOwned("active", "healthy-sibling", now.plusSeconds(60));

        assertThat(store.recoverExpired(now)).isEqualTo(0);
        assertThat(store.findById(capability, active.id(), Snapshot.class).orElseThrow().status())
                .isEqualTo("QUEUED");
    }

    @Test
    void recoveryAndCompletionAllowOnlyOneTerminalSnapshot() {
        Instant expired = Instant.parse("2026-09-16T10:00:00Z");
        BatchRunStore.StoredRun<Snapshot> run = createOwned("race", "owner-a", expired);

        assertThat(store.recoverExpired(expired.plusSeconds(1))).isEqualTo(1);
        assertThat(
                        store.replaceOwned(
                                capability,
                                run.id(),
                                "owner-a",
                                State.COMPLETED,
                                new Snapshot(run.id(), "COMPLETED", List.of("late")),
                                expired.plusSeconds(30)))
                .isFalse();
        assertThat(store.findById(capability, run.id(), Snapshot.class).orElseThrow())
                .isEqualTo(new Snapshot(run.id(), "FAILED", List.of("interrupted")));
    }

    @Test
    void replayAfterRecoveryKeepsTheOriginalIdentityAndFailure() {
        Instant expired = Instant.parse("2026-09-16T10:00:00Z");
        BatchRunStore.StoredRun<Snapshot> run = createOwned("replay", "owner-a", expired);
        store.recoverExpired(expired.plusSeconds(1));

        BatchRunStore.StoredRun<Snapshot> replay =
                store.findByKey(capability, "replay", Snapshot.class).orElseThrow();

        assertThat(replay.id()).isEqualTo(run.id());
        assertThat(replay.fingerprint()).isEqualTo("controls");
        assertThat(replay.response())
                .isEqualTo(new Snapshot(run.id(), "FAILED", List.of("interrupted")));
    }

    private BatchRunStore.StoredRun<Snapshot> createOwned(
            String key, String owner, Instant leaseExpiresAt) {
        return store.create(
                capability,
                key,
                "controls",
                Snapshot.class,
                owner,
                leaseExpiresAt,
                id -> new Snapshot(id, "QUEUED", List.of()),
                id -> new Snapshot(id, "FAILED", List.of("interrupted")));
    }

    private static BatchRunStart<Snapshot> success(
            Result<BatchRunStart<Snapshot>, BatchRunFailure> result) {
        return switch (result) {
            case Result.Ok(var value) -> value;
            case Result.Err(var failure) -> throw new AssertionError(failure);
        };
    }

    public record Snapshot(long id, String status, List<String> outputs) {
        public Snapshot {
            outputs = List.copyOf(outputs);
        }
    }
}
