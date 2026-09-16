package org.cookcounty.tax.application.batch;

import org.cookcounty.tax.domain.port.out.BatchRunStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/// Maintains durable liveness for the runs that one application instance owns.
///
/// The manager renews leases and recovers expired reservations. Recovery only publishes the failure
/// snapshot that the reservation stored before dispatch. It never invokes a worker. If a lease
/// expires during an in-flight transaction, recovery wins the terminal snapshot race. The
/// transaction can still commit business effects. Operators must inspect those effects. The
/// coordinator never retries them.
public final class BatchRunLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchRunLifecycle.class);

    /// Durable lease and recovery boundary shared by all application instances.
    private final BatchRunStore store;

    /// Unique process identity, never reused to resume an earlier process's writes.
    private final String ownerId;

    /// Maximum tolerated liveness gap before a reservation becomes recoverable.
    private final Duration leaseDuration;

    /// UTC time source used for deadlines. Hosts must keep their clocks synchronized.
    private final Clock clock;

    /// Dedicated scheduler so a blocked business worker cannot prevent its own heartbeat.
    private final ScheduledExecutorService scheduler;

    /// Renewal interval in milliseconds, bounded below by one scheduler tick.
    private final long heartbeatMilliseconds;

    /// Starts periodic recovery for one unique application-instance owner.
    public BatchRunLifecycle(
            BatchRunStore store,
            String ownerId,
            Duration leaseDuration,
            Clock clock,
            ScheduledExecutorService scheduler) {
        this.store = Objects.requireNonNull(store, "store");
        this.ownerId = requireText(ownerId, "ownerId");
        this.leaseDuration = Objects.requireNonNull(leaseDuration, "leaseDuration");
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
        this.clock = Objects.requireNonNull(clock, "clock");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.heartbeatMilliseconds = Math.max(1L, leaseDuration.toMillis() / 3L);
        scheduler.scheduleWithFixedDelay(
                this::recoverSafely,
                heartbeatMilliseconds,
                heartbeatMilliseconds,
                TimeUnit.MILLISECONDS);
    }

    /// Returns the owner persisted with each new reservation.
    public String ownerId() {
        return ownerId;
    }

    /// Returns a new deadline based on the configured application clock.
    public Instant nextDeadline() {
        return clock.instant().plus(leaseDuration);
    }

    /// Runs work while a dedicated scheduler renews its durable lease.
    ///
    /// @return false when recovery made the reservation terminal before work started
    public boolean runWhileOwned(String capability, long id, Runnable work) {
        if (!store.renew(capability, id, ownerId, nextDeadline())) {
            return false;
        }
        ScheduledFuture<?> heartbeat =
                scheduler.scheduleAtFixedRate(
                        () -> renewSafely(capability, id),
                        heartbeatMilliseconds,
                        heartbeatMilliseconds,
                        TimeUnit.MILLISECONDS);
        try {
            work.run();
            return true;
        } finally {
            heartbeat.cancel(false);
        }
    }

    /// Retains diagnostics on renewal failure and lets the persisted lease determine recovery.
    private void renewSafely(String capability, long id) {
        try {
            store.renew(capability, id, ownerId, nextDeadline());
        } catch (RuntimeException exception) {
            LOGGER.error("Batch run {} lease renewal failed", id, exception);
        }
    }

    /// Terminalizes stale reservations without dispatching or reversing business work.
    private void recoverSafely() {
        try {
            int recovered = store.recoverExpired(clock.instant());
            if (recovered > 0) {
                LOGGER.warn("Recovered {} expired batch run reservations", recovered);
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Expired batch run recovery failed", exception);
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
