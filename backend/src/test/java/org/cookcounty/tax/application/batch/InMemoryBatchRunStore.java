package org.cookcounty.tax.application.batch;

import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.cookcounty.tax.domain.port.out.DuplicateBatchRunException;
import org.jspecify.annotations.Nullable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongFunction;

/// Thread-safe test store that models durable ownership and terminal arbitration.
public final class InMemoryBatchRunStore implements BatchRunStore {
    private final AtomicLong ids = new AtomicLong(Integer.MAX_VALUE);
    private final Map<String, Entry> runsByKey = new HashMap<>();
    private final Map<String, Entry> runsById = new HashMap<>();

    /// Creates a daemon-backed lease manager for a test store.
    public static BatchRunLifecycle lifecycle(BatchRunStore store) {
        return new BatchRunLifecycle(
                store,
                "test-owner",
                Duration.ofMinutes(5),
                Clock.systemUTC(),
                Executors.newSingleThreadScheduledExecutor(
                        command -> {
                            Thread thread = new Thread(command, "test-batch-lease");
                            thread.setDaemon(true);
                            return thread;
                        }));
    }

    @Override
    public synchronized <R> Optional<StoredRun<R>> findByKey(
            String capability, String idempotencyKey, Class<R> responseType) {
        return cast(runsByKey.get(key(capability, idempotencyKey)), responseType);
    }

    @Override
    public synchronized <R> Optional<R> findById(
            String capability, long id, Class<R> responseType) {
        return cast(runsById.get(idKey(capability, id)), responseType).map(StoredRun::response);
    }

    @Override
    public synchronized <R> StoredRun<R> create(
            String capability,
            String idempotencyKey,
            String fingerprint,
            Class<R> responseType,
            String ownerId,
            Instant leaseExpiresAt,
            LongFunction<R> initialSnapshotFactory,
            LongFunction<R> recoverySnapshotFactory) {
        String key = key(capability, idempotencyKey);
        if (runsByKey.containsKey(key)) {
            throw new DuplicateBatchRunException(null);
        }
        long id = ids.incrementAndGet();
        R response = initialSnapshotFactory.apply(id);
        Entry entry =
                new Entry(
                        id,
                        fingerprint,
                        response,
                        recoverySnapshotFactory.apply(id),
                        State.QUEUED,
                        ownerId,
                        leaseExpiresAt);
        runsByKey.put(key, entry);
        runsById.put(idKey(capability, id), entry);
        return new StoredRun<>(id, fingerprint, response);
    }

    @Override
    public synchronized boolean renew(
            String capability, long id, String ownerId, Instant leaseExpiresAt) {
        Entry entry = runsById.get(idKey(capability, id));
        if (entry == null || !ownedNonterminal(entry, ownerId)) {
            return false;
        }
        entry.leaseExpiresAt = leaseExpiresAt;
        return true;
    }

    @Override
    public synchronized <R> boolean replaceOwned(
            String capability,
            long id,
            String ownerId,
            State targetState,
            R response,
            Instant leaseExpiresAt) {
        Entry entry = runsById.get(idKey(capability, id));
        if (entry == null || !ownedNonterminal(entry, ownerId)) {
            return false;
        }
        entry.response = response;
        entry.state = targetState;
        entry.ownerId = targetState.terminal() ? null : ownerId;
        entry.leaseExpiresAt = targetState.terminal() ? null : leaseExpiresAt;
        return true;
    }

    @Override
    public synchronized int recoverExpired(Instant now) {
        int count = 0;
        for (Entry entry : runsById.values()) {
            if (!entry.state.terminal()
                    && entry.leaseExpiresAt != null
                    && !entry.leaseExpiresAt.isAfter(now)) {
                entry.response = entry.recoveryResponse;
                entry.state = State.FAILED;
                entry.ownerId = null;
                entry.leaseExpiresAt = null;
                count++;
            }
        }
        return count;
    }

    private static boolean ownedNonterminal(Entry entry, String ownerId) {
        return !entry.state.terminal() && ownerId.equals(entry.ownerId);
    }

    private static String key(String capability, String idempotencyKey) {
        return capability + '\u0000' + idempotencyKey;
    }

    private static String idKey(String capability, long id) {
        return capability + '\u0000' + id;
    }

    private static <R> Optional<StoredRun<R>> cast(@Nullable Entry entry, Class<R> responseType) {
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(
                new StoredRun<>(entry.id, entry.fingerprint, responseType.cast(entry.response)));
    }

    private static final class Entry {
        private final long id;
        private final String fingerprint;
        private final Object recoveryResponse;
        private Object response;
        private State state;
        private @Nullable String ownerId;
        private @Nullable Instant leaseExpiresAt;

        private Entry(
                long id,
                String fingerprint,
                Object response,
                Object recoveryResponse,
                State state,
                String ownerId,
                Instant leaseExpiresAt) {
            this.id = id;
            this.fingerprint = fingerprint;
            this.response = response;
            this.recoveryResponse = recoveryResponse;
            this.state = state;
            this.ownerId = ownerId;
            this.leaseExpiresAt = leaseExpiresAt;
        }
    }
}
