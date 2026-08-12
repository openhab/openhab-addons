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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

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
}
