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
package org.openhab.binding.metofficedatahub.internal;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.DAY_IN_MILLIS;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.RANDOM_GENERATOR;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBridgeHandler.getMillisSinceDayStart;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

/**
 * The {@link MetOfficeDataHubSiteApiHandler} wraps up the executor functionality for a delayed execution with the
 * relevant locking.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDelayedExecutor {

    private final Logger logger;

    public MetOfficeDelayedExecutor(final ScheduledExecutorService scheduler, final Logger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
    }

    private final ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> scheduledFutureRef = null;
    private final Object scheduledFutureRefLock = new Object();

    public void scheduleNextPoll(final long dayCycleMillis, final Runnable task) {
        long millisSinceDayStart = DAY_IN_MILLIS - (DAY_IN_MILLIS - getMillisSinceDayStart());
        // long pollRateMillis = config.siteSpecificDailyForecastPollRate * 3600000;
        long initialDelayTimeToFirstCycle = ((millisSinceDayStart - (millisSinceDayStart % dayCycleMillis))
                + dayCycleMillis) - millisSinceDayStart;
        if (initialDelayTimeToFirstCycle + millisSinceDayStart > DAY_IN_MILLIS) {
            logger.debug("Not scheduling poll after next daily cycle reset");
        } else {
            logger.debug("Can scheduling next Daily forecast data poll to be in {} milliseconds",
                    initialDelayTimeToFirstCycle);

            initialDelayTimeToFirstCycle += RANDOM_GENERATOR.nextInt(60000);
            // Schedule the first poll to occur after the given delay
            this.scheduleDailyForecastPoll(initialDelayTimeToFirstCycle, () -> {
                task.run();
            });
        }
    }

    public void scheduleDailyForecastPoll(final long initialDelay, final Runnable task) {
        synchronized (scheduledFutureRefLock) {
            cancelScheduledTask(true);
            scheduledFutureRef = scheduler.schedule(() -> {
                task.run();
            }, initialDelay, TimeUnit.MILLISECONDS);
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
