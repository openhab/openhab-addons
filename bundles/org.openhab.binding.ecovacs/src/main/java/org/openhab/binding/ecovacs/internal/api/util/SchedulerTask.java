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
package org.openhab.binding.ecovacs.internal.api.util;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class SchedulerTask implements Runnable {
    private final Logger logger;
    private final String name;
    private final Runnable runnable;
    private final ScheduledExecutorService scheduler;
    private @Nullable Future<?> future;

    public SchedulerTask(ScheduledExecutorService scheduler, Logger logger, String name, Runnable runnable) {
        this.logger = logger;
        this.name = name;
        this.runnable = runnable;
        this.scheduler = scheduler;
    }

    public void submit() {
        schedule(0);
    }

    public synchronized void schedule(long delaySeconds) {
        if (future != null) {
            logger.trace("{}: Already scheduled to run", name);
            return;
        }
        logger.trace("{}: Scheduling to run in {} seconds", name, delaySeconds);
        if (delaySeconds == 0) {
            future = scheduler.submit(this);
        } else {
            future = scheduler.schedule(this, delaySeconds, TimeUnit.SECONDS);
        }
    }

    public synchronized void scheduleRecurring(long intervalSeconds) {
        if (future != null) {
            logger.trace("{}: Already scheduled to run", name);
            return;
        }
        logger.trace("{}: Scheduling to run in {} second intervals", name, intervalSeconds);
        future = scheduler.scheduleWithFixedDelay(runnable, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public synchronized void cancel() {
        Future<?> future = this.future;
        this.future = null;
        if (future != null) {
            future.cancel(true);
            logger.trace("{}: Cancelled", name);
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            future = null;
        }
        logger.trace("{}: Running one-shot", name);
        runnable.run();
    }
}
