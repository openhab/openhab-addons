/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.melcloud.internal.home.api;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;

/**
 * Enforces a minimum interval between the start of consecutive MELCloud Home API calls issued for one bridge, since
 * the platform's rate limit is account-wide, not per-unit.
 *
 * <p>
 * {@link #schedule(Runnable)} never blocks the calling thread: it runs the task immediately when no wait is needed
 * and otherwise hands it to the provided {@link ScheduledExecutorService}. One instance is owned per bridge and is
 * shared by every unit Thing under it.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeRequestPacer {

    /**
     * Minimum spacing between calls, in milliseconds; a starting point to be revisited once the platform's rate-limit
     * behavior is observed.
     */
    static final long DEFAULT_MIN_REQUEST_INTERVAL_MILLIS = 500;

    /** Upper bound for {@link #scheduleBlocking(PacedCall)}, so a lost task cannot block its caller forever. */
    private static final long BLOCKING_TIMEOUT_MILLIS = 60_000;

    private final ScheduledExecutorService scheduler;
    private final long minRequestIntervalMillis;
    private final long blockingTimeoutMillis;
    private final Object lock = new Object();

    private Instant nextAllowedInstant = Instant.EPOCH;

    public MelCloudHomeRequestPacer(ScheduledExecutorService scheduler) {
        this(scheduler, DEFAULT_MIN_REQUEST_INTERVAL_MILLIS, BLOCKING_TIMEOUT_MILLIS);
    }

    /**
     * @param scheduler the bridge's own scheduler; never a new/dedicated thread
     * @param minRequestIntervalMillis minimum spacing between calls, in milliseconds
     */
    MelCloudHomeRequestPacer(ScheduledExecutorService scheduler, long minRequestIntervalMillis) {
        this(scheduler, minRequestIntervalMillis, BLOCKING_TIMEOUT_MILLIS);
    }

    /**
     * @param scheduler the bridge's own scheduler; never a new/dedicated thread
     * @param minRequestIntervalMillis minimum spacing between calls, in milliseconds
     * @param blockingTimeoutMillis how long {@link #scheduleBlocking(PacedCall)} waits for its call, in milliseconds
     */
    MelCloudHomeRequestPacer(ScheduledExecutorService scheduler, long minRequestIntervalMillis,
            long blockingTimeoutMillis) {
        this.scheduler = scheduler;
        this.minRequestIntervalMillis = minRequestIntervalMillis;
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    /**
     * Runs {@code apiCall} immediately if enough time has passed since the last scheduled call, or hands it to the
     * scheduler with just enough delay to respect the minimum interval otherwise. Every call to this method reserves
     * the next slot, so concurrent callers are serialized in call order.
     *
     * @param apiCall the call to pace; never dropped, only delayed
     */
    public void schedule(Runnable apiCall) {
        long delayMillis;
        synchronized (lock) {
            Instant now = Instant.now();
            Instant allowedFrom = nextAllowedInstant.isAfter(now) ? nextAllowedInstant : now;
            delayMillis = Duration.between(now, allowedFrom).toMillis();
            nextAllowedInstant = allowedFrom.plusMillis(minRequestIntervalMillis);
        }
        if (delayMillis <= 0) {
            apiCall.run();
        } else {
            scheduler.schedule(apiCall, delayMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Like {@link #schedule(Runnable)}, but blocks the calling thread until {@code apiCall} has run and returns its
     * result (or propagates its exception), for synchronous {@code ThingActions} callers.
     *
     * <p>
     * If the caller stops waiting before its slot arrives (timeout or interruption), {@code apiCall} is skipped
     * instead of being executed later, so a call that was already reported as failed cannot still reach the
     * platform afterwards.
     *
     * @param <T> the call's result type
     * @param apiCall the call to pace and run
     * @return {@code apiCall}'s result
     * @throws MelCloudCommException if {@code apiCall} throws it, or if waiting for it is interrupted or times out
     */
    public <T> T scheduleBlocking(PacedCall<T> apiCall) throws MelCloudCommException {
        CompletableFuture<T> future = new CompletableFuture<>();
        schedule(() -> {
            if (future.isDone()) {
                return;
            }
            try {
                future.complete(apiCall.call());
            } catch (MelCloudCommException | RuntimeException e) {
                // An unchecked failure must complete the future too, or the caller would wait for it forever.
                future.completeExceptionally(e);
            }
        });
        try {
            return future.get(blockingTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new MelCloudCommException("Timed out waiting for a paced call to complete", e);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new MelCloudCommException("Interrupted while waiting for a paced call to complete", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MelCloudCommException melCloudCommException) {
                throw melCloudCommException;
            }
            throw new MelCloudCommException("Paced call failed", cause != null ? cause : e);
        }
    }

    /**
     * A call routed through {@link #scheduleBlocking(PacedCall)}; unlike {@link Runnable}, it may return a value and
     * throw the binding's own checked communication exception.
     *
     * @param <T> the call's result type
     */
    @FunctionalInterface
    public interface PacedCall<T> {
        T call() throws MelCloudCommException;
    }
}
