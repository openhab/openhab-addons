/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.homekit.internal;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Highly performant generic debouncer
 *
 * @author Tim Harper - Initial contribution
 *
 */
class Debouncer {

    private final Clock clock;
    private final ScheduledExecutorService scheduler;
    private final Long delayMs;
    private final Runnable action;
    private final AtomicBoolean pending = new AtomicBoolean(false);
    private final AtomicInteger calls = new AtomicInteger(0);
    private final String name;

    private final Logger logger = LoggerFactory.getLogger(Debouncer.class);

    private volatile Long lastCallAttempt;
    private ScheduledFuture<?> future;

    /**
     * Highly performant generic debouncer
     *
     * Note: Debounced calls are filtered synchronously, in the caller thread, without the need for locks, context
     * switches, or heap allocations. We use AtomicBoolean to resolve concurrent races; the probability of
     * contending on an AtomicBoolean transition is very low.
     *
     * @param name The name of this debouncer
     * @param scheduler The scheduler implementation to use
     * @param delay The time after which to invoke action; each time [[Debouncer.call]] is invoked, this delay is
     *            reset
     * @param clock The source from which we get the current time. This input should use the same source. Specified
     *            for testing purposes
     * @param action The action to invoke
     */
    Debouncer(String name, ScheduledExecutorService scheduler, Duration delay, Clock clock, Runnable action) {
        this.name = name;
        this.scheduler = scheduler;
        this.action = action;

        this.delayMs = delay.toMillis();
        this.clock = clock;
        this.lastCallAttempt = clock.millis();
    }

    /**
     * Register that the provided action should be called according to the debounce logic
     */
    void call() {
        lastCallAttempt = clock.millis();
        calls.incrementAndGet();
        if (pending.compareAndSet(false, true)) {
            future = scheduler.schedule(this::tryActionOrPostpone, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        logger.trace("stop debouncer");
        if (future != null) {
            future.cancel(true);
            calls.set(0);
            pending.set(false);
        }
    }

    private void tryActionOrPostpone() {
        long now = clock.millis();

        boolean delaySurpassed = ((now - lastCallAttempt) >= delayMs);

        if (delaySurpassed) {
            if (pending.compareAndSet(true, false)) {
                int foldedCalls = calls.getAndSet(0);
                logger.trace("Debouncer action {} invoked after delay {}  ({} calls)", name, delayMs, foldedCalls);
                try {
                    action.run();
                } catch (Exception e) {
                    logger.warn("Debouncer {} action resulted in error", name, e);
                }
            } else {
                logger.warn("Invalid state in debouncer. Should not have reached here!");
            }
        } else {
            // reschedule at origLastInvocation + delayMs
            // Note: we use Math.max as there's a _very_ small chance lastCallAttempt could advance in another thread,
            // and result in a negative calculation
            long delay = Math.max(1, lastCallAttempt - now + delayMs);
            future = scheduler.schedule(this::tryActionOrPostpone, delay, TimeUnit.MILLISECONDS);
        }
    }
}
