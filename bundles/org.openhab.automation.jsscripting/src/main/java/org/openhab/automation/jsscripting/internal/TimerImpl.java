/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.automation.jsscripting.internal;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.model.script.actions.Timer;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;
import org.openhab.core.scheduler.SchedulerRunnable;

/**
 * This is an implementation of the {@link Timer} interface.
 * Copy of {@link org.openhab.core.model.script.internal.actions.TimerImpl} as this is not accessible from outside the
 * package.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TimerImpl implements Timer {

    private final Scheduler scheduler;
    private final ZonedDateTime startTime;
    private final SchedulerRunnable runnable;
    private final @Nullable String identifier;
    private ScheduledCompletableFuture<?> future;

    public TimerImpl(Scheduler scheduler, ZonedDateTime startTime, SchedulerRunnable runnable) {
        this(scheduler, startTime, runnable, null);
    }

    public TimerImpl(Scheduler scheduler, ZonedDateTime startTime, SchedulerRunnable runnable,
            @Nullable String identifier) {
        this.scheduler = scheduler;
        this.startTime = startTime;
        this.runnable = runnable;
        this.identifier = identifier;

        future = scheduler.schedule(runnable, identifier, startTime.toInstant());
    }

    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    @Override
    public synchronized boolean reschedule(ZonedDateTime newTime) {
        future.cancel(false);
        future = scheduler.schedule(runnable, identifier, newTime.toInstant());
        return true;
    }

    @Override
    public @Nullable ZonedDateTime getExecutionTime() {
        return future.isCancelled() ? null : ZonedDateTime.now().plusNanos(future.getDelay(TimeUnit.NANOSECONDS));
    }

    @Override
    public boolean isActive() {
        return !future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isRunning() {
        return isActive() && ZonedDateTime.now().isAfter(startTime);
    }

    @Override
    public boolean hasTerminated() {
        return future.isDone();
    }
}
