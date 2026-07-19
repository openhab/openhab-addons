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
package org.openhab.binding.atv.internal.client.core;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically sends heartbeat messages to a device.
 *
 * <p>
 * A beat is sent every interval; when a beat fails it is retried immediately (no initial delay) up to the
 * configured number of retries, after which the failure action runs and the heartbeater stops itself.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class Heartbeater {

    /**
     * Default time between heartbeats.
     */
    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(30);

    /** Default number of immediate re-attempts after a failed beat. */
    public static final int DEFAULT_RETRIES = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(Heartbeater.class);

    private final String name;
    private final Supplier<CompletableFuture<?>> beat;
    private final Runnable failureAction;
    private final ScheduledExecutorService scheduler;
    private final Duration interval;
    private final int retries;

    private final Object lock = new Object();
    private @Nullable ScheduledFuture<?> pending;
    private boolean running;
    private int attempts;
    private long count;

    /**
     * Creates a heartbeater with the default 30 second interval and one retry.
     *
     * @param name name used in log output
     * @param beat supplier starting one heartbeat exchange; the returned future's outcome
     *            decides success or failure
     * @param failureAction action run after a beat has failed all attempts, or {@code null}
     * @param scheduler scheduler used for timing
     */
    public Heartbeater(String name, Supplier<CompletableFuture<?>> beat, Runnable failureAction,
            ScheduledExecutorService scheduler) {
        this(name, beat, failureAction, scheduler, DEFAULT_INTERVAL, DEFAULT_RETRIES);
    }

    /**
     * Creates a heartbeater with an explicit interval and retry count.
     *
     * @param name name used in log output
     * @param beat supplier starting one heartbeat exchange
     * @param failureAction action run after a beat has failed all attempts, or {@code null}
     * @param scheduler scheduler used for timing
     * @param interval time between successful beats
     * @param retries number of immediate re-attempts after a failed beat
     */
    public Heartbeater(String name, Supplier<CompletableFuture<?>> beat, Runnable failureAction,
            ScheduledExecutorService scheduler, Duration interval, int retries) {
        this.name = Objects.requireNonNull(name, "name");
        this.beat = Objects.requireNonNull(beat, "beat");
        this.failureAction = failureAction != null ? failureAction : () -> {
        };
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.interval = Objects.requireNonNull(interval, "interval");
        if (retries < 0) {
            throw new IllegalArgumentException("retries must be >= 0");
        }
        this.retries = retries;
    }

    /**
     * Starts the heartbeat loop; the first beat is sent after one interval. Does nothing
     * if already running.
     */
    public void start() {
        synchronized (lock) {
            if (running) {
                return;
            }
            LOGGER.debug("Starting heartbeat loop ({})", name);
            running = true;
            attempts = 0;
            schedule(interval.toMillis());
        }
    }

    /**
     * Stops the heartbeat loop. An in-flight beat may still complete but triggers no
     * further action.
     */
    public void stop() {
        synchronized (lock) {
            if (!running) {
                return;
            }
            LOGGER.debug("Stopping heartbeat loop at {} ({})", count, name);
            running = false;
            if (pending != null) {
                pending.cancel(false);
                pending = null;
            }
        }
    }

    /**
     * Returns whether the heartbeat loop is currently running.
     */
    public boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    private void schedule(long delayMillis) {
        try {
            pending = scheduler.schedule(this::sendBeat, delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            LOGGER.debug("Heartbeat scheduling rejected ({})", name, e);
            running = false;
        }
    }

    private void sendBeat() {
        synchronized (lock) {
            if (!running) {
                return;
            }
            LOGGER.trace("Sending periodic heartbeat {} ({})", count, name);
        }
        CompletableFuture<?> future;
        try {
            future = beat.get();
        } catch (RuntimeException e) {
            onBeatDone(e);
            return;
        }
        if (future == null) {
            onBeatDone(new NullPointerException("heartbeat supplier returned null"));
            return;
        }
        future.whenComplete((value, error) -> onBeatDone(error));
    }

    private void onBeatDone(Throwable error) {
        boolean failed = false;
        synchronized (lock) {
            long current = count;
            count++;
            if (!running) {
                return;
            }
            if (error == null) {
                LOGGER.trace("Got heartbeat {} ({})", current, name);
                attempts = 0;
                schedule(interval.toMillis());
                return;
            }
            attempts++;
            if (attempts > retries) {
                LOGGER.debug("Heartbeat {} failed after {} tries ({})", current, attempts, name);
                running = false;
                pending = null;
                failed = true;
            } else {
                LOGGER.debug("Heartbeat {} failed ({})", current, name);
                // Re-attempts are made with no initial delay to recover quickly
                schedule(0);
            }
        }
        if (failed) {
            try {
                failureAction.run();
            } catch (RuntimeException e) {
                LOGGER.warn("Heartbeat failure action threw ({})", name, e);
            }
        }
    }
}
