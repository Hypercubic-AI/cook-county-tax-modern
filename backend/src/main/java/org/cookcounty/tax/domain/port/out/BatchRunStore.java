package org.cookcounty.tax.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.function.LongFunction;

/// Persists request-driven batch identity, ownership, liveness, and response snapshots.
///
/// The database owns run identifiers and capability-scoped replay keys. Each nonterminal run has
/// one owner and a bounded lease. Recovery changes an expired run to its persisted failure
/// snapshot. Recovery never submits work. A terminal state cannot change again.
public interface BatchRunStore {

    /// Finds a run by its capability-scoped idempotency key.
    <R> Optional<StoredRun<R>> findByKey(
            String capability, String idempotencyKey, Class<R> responseType);

    /// Finds one persisted response snapshot by run identifier.
    <R> Optional<R> findById(String capability, long id, Class<R> responseType);

    /// Creates one run, its queued snapshot, and its recovery snapshot in one transaction.
    ///
    /// @param ownerId unique identity of the application instance that accepted the work
    /// @param leaseExpiresAt time after which another instance can prove that the owner is stale
    /// @param recoverySnapshotFactory terminal response used after interruption
    /// @throws DuplicateBatchRunException when a competing transaction reserved the same key
    <R> StoredRun<R> create(
            String capability,
            String idempotencyKey,
            String fingerprint,
            Class<R> responseType,
            String ownerId,
            Instant leaseExpiresAt,
            LongFunction<R> initialSnapshotFactory,
            LongFunction<R> recoverySnapshotFactory);

    /// Renews a nonterminal run only while the same application instance owns it.
    boolean renew(String capability, long id, String ownerId, Instant leaseExpiresAt);

    /// Atomically publishes a whole snapshot while the run is nonterminal and owned by the caller.
    ///
    /// A terminal target clears ownership. The method returns false after recovery or another
    /// terminal publication wins the race.
    <R> boolean replaceOwned(
            String capability,
            long id,
            String ownerId,
            State targetState,
            R response,
            Instant leaseExpiresAt);

    /// Atomically fails all expired nonterminal runs without submitting their work.
    ///
    /// @return number of runs changed to their persisted recovery snapshots
    int recoverExpired(Instant now);

    /// Persisted lifecycle state used for atomic completion and recovery arbitration.
    enum State {
        /// The reservation is durable but its worker has not started.
        QUEUED,
        /// The owner started the whole business step.
        RUNNING,
        /// The owner published a successful terminal snapshot.
        COMPLETED,
        /// The owner, admission path, or lease recovery published a terminal failure.
        FAILED;

        /// Reports whether no later state transition is permitted.
        public boolean terminal() {
            return this == COMPLETED || this == FAILED;
        }
    }

    /// An immutable persisted run view.
    ///
    /// @param id database-assigned run identifier
    /// @param fingerprint canonical identity of the original request parameters
    /// @param response complete response snapshot
    /// @param <R> immutable response type
    record StoredRun<R>(long id, String fingerprint, R response) {}
}
