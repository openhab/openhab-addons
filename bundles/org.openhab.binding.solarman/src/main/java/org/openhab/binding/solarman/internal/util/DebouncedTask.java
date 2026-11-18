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
package org.openhab.binding.solarman.internal.util;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Runs a task once after a delay. Triggering it again before the task has started restarts the delay, so a burst of
 * triggers results in a single run.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class DebouncedTask {
    private final ScheduledExecutorService scheduler;
    private final Runnable task;
    private final Duration delay;
    private @Nullable ScheduledFuture<?> pending;

    public DebouncedTask(ScheduledExecutorService scheduler, Runnable task, Duration delay) {
        this.scheduler = scheduler;
        this.task = task;
        this.delay = delay;
    }

    public synchronized void trigger() {
        ScheduledFuture<?> previous = pending;
        if (previous != null) {
            previous.cancel(false);
        }
        pending = scheduler.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    public synchronized void cancel() {
        ScheduledFuture<?> previous = pending;
        if (previous != null) {
            previous.cancel(false);
            pending = null;
        }
    }
}
