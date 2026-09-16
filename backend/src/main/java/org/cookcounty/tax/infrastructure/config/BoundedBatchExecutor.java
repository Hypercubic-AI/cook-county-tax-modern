package org.cookcounty.tax.infrastructure.config;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/// Admits a configured number of request-driven batch runs to virtual threads.
///
/// The permit spans the complete worker call. A request that arrives when all permits are in use
/// fails before its worker starts. The REST exception advice maps that rejection to a safe
/// service-unavailable response.
final class BoundedBatchExecutor implements Executor {

    private final Executor delegate;
    private final Semaphore permits;

    BoundedBatchExecutor(Executor delegate, int maximumConcurrentRuns) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        if (maximumConcurrentRuns < 1) {
            throw new IllegalArgumentException("maximumConcurrentRuns must be positive");
        }
        this.permits = new Semaphore(maximumConcurrentRuns);
    }

    /// Starts one worker when admission capacity is available.
    ///
    /// @throws RejectedExecutionException if all configured permits are in use
    @Override
    public void execute(Runnable command) {
        Objects.requireNonNull(command, "command");
        if (!permits.tryAcquire()) {
            throw new RejectedExecutionException("The batch run admission limit is full");
        }
        try {
            delegate.execute(
                    () -> {
                        try {
                            command.run();
                        } finally {
                            permits.release();
                        }
                    });
        } catch (RuntimeException exception) {
            permits.release();
            throw exception;
        }
    }
}
