/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.metofficedatahub.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MetOfficeDelayedExecutor} wraps up the executor functionality for a delayed execution with the
 * relevant locking.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDelayedExecutor {

    public MetOfficeDelayedExecutor(final ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    private final ScheduledExecutorService scheduler;
    private final Object scheduledFutureRefLock = new Object();
    private @Nullable ScheduledFuture<?> scheduledFutureRef = null;

    public void scheduleExecution(final long initialDelay, final Runnable task) {
        synchronized (scheduledFutureRefLock) {
            cancelScheduledTask(true);
            scheduledFutureRef = scheduler.schedule(task, initialDelay, TimeUnit.MILLISECONDS);
        }
    }

    public void cancelScheduledTask(final boolean allowInterrupt) {
        synchronized (scheduledFutureRefLock) {
            ScheduledFuture<?> job = scheduledFutureRef;
            if (job != null) {
                job.cancel(true);
                scheduledFutureRef = null;
            }
        }
    }
}
