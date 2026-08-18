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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;

/**
 * Enforces a minimum interval between the start of consecutive MELCloud Home API calls issued for one bridge, since
 * the platform's rate limit is account-wide, not per-unit (see ADR-008).
 *
 * <p>
 * {@link #schedule(Runnable)} never blocks the calling thread. When no wait is needed (the common case: no other
 * call happened recently), the task runs synchronously on the caller's thread, exactly as an unpaced call would.
 * Only when spacing is actually required does the task get handed to the provided {@link ScheduledExecutorService}
 * — per the project's coding rules, this class never creates a thread of its own and never calls
 * {@code Thread.sleep(...)}.
 *
 * <p>
 * One instance is owned per bridge ({@code MelCloudHomeAccountHandler}) and shared by every unit Thing under it, so
 * the spacing applies across the bridge's {@code /context} poll and every unit's control calls together.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeRequestPacer {

    /**
     * Minimum spacing between calls, in milliseconds. Matches the value the {@code andrew-blake/melcloudhome} Home
     * Assistant project settled on (its {@code RequestPacer}, {@code DEFAULT_MIN_REQUEST_INTERVAL = 0.5}) as a
     * starting point; revisit once this binding's own rate-limit behavior is observed (see
     * {@code docs/changes/add-melcloud-home-request-resilience/proposal.md}).
     */
    static final long DEFAULT_MIN_REQUEST_INTERVAL_MILLIS = 500;

    private final ScheduledExecutorService scheduler;
    private final long minRequestIntervalMillis;
    private final Object lock = new Object();

    private Instant nextAllowedInstant = Instant.EPOCH;

    public MelCloudHomeRequestPacer(ScheduledExecutorService scheduler) {
        this(scheduler, DEFAULT_MIN_REQUEST_INTERVAL_MILLIS);
    }

    /**
     * @param scheduler the bridge's own scheduler; never a new/dedicated thread
     * @param minRequestIntervalMillis minimum spacing between calls, in milliseconds
     */
    MelCloudHomeRequestPacer(ScheduledExecutorService scheduler, long minRequestIntervalMillis) {
        this.scheduler = scheduler;
        this.minRequestIntervalMillis = minRequestIntervalMillis;
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
     * Like {@link #schedule(Runnable)}, but blocks the calling thread until {@code apiCall} has actually run and
     * returns its result (or propagates its exception).
     *
     * <p>
     * {@code ThingActions} methods (see {@code MelCloudHomeAtwScheduleActions}) are invoked synchronously by
     * openHAB's rule engine/scripting layer and are expected to return a real result or throw, unlike
     * {@link #handleCommand}'s existing fire-and-forget use of {@link #schedule(Runnable)}. This method still goes
     * through the same pacing/serialization as every other call.
     *
     * @param <T> the call's result type
     * @param apiCall the call to pace and run
     * @return {@code apiCall}'s result
     * @throws MelCloudCommException if {@code apiCall} throws it, or if waiting for it is interrupted
     */
    public <T> T scheduleBlocking(PacedCall<T> apiCall) throws MelCloudCommException {
        CompletableFuture<T> future = new CompletableFuture<>();
        schedule(() -> {
            try {
                future.complete(apiCall.call());
            } catch (MelCloudCommException e) {
                future.completeExceptionally(e);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException e) {
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
