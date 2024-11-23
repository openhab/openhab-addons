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

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.DAY_IN_MILLIS;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.EXPECTED_TS_FORMAT;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.RANDOM_GENERATOR;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.metofficedatahub.internal.api.ResponseDataProcessor;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PollManager} manages basic poll management functionality.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class PollManager {

    private volatile long lastForecastPoll = -1;

    private final Logger logger = LoggerFactory.getLogger(PollManager.class);

    private final String pollName;

    ResponseDataProcessor runCachedDataPoll;
    Runnable runLiveDataPoll;

    Duration durationBetweenPolls;

    String lastRepsonse = "";

    private @Nullable ScheduledFuture<?> pollScheduled = null;

    private final Object pollScheduledLock = new Object();
    private final TimeZoneProvider timeZoneProvider;

    /**
     * This handles the scheduling of an hourly forecast poll, to be applied with the given delay.
     * When run, if requests the run-time of the next one is calculated and scheduled.
     */
    private final MetOfficeDelayedExecutor forecastJob;

    private volatile boolean dataRequired;

    public PollManager(final String pollName, @Reference TimeZoneProvider timeZoneProvider,
            @Reference ScheduledExecutorService scheduler, final Duration durationBetweenPolls,
            final ResponseDataProcessor cachedDataPoll, final Runnable liveDataPoll) {
        this.pollName = pollName;
        this.durationBetweenPolls = durationBetweenPolls;
        this.runCachedDataPoll = cachedDataPoll;
        this.runLiveDataPoll = liveDataPoll;
        this.timeZoneProvider = timeZoneProvider;
        forecastJob = new MetOfficeDelayedExecutor(scheduler);
        dataRequired = false;
    }

    public void dispose() {
        cancelScheduledPoll(true);
        forecastJob.cancelScheduledTask(true);
    }

    public void cachedPollOrLiveStart(final boolean attemptLivePollIfRequired) {
        if (dataRequired) {
            if (!lastRepsonse.isEmpty()) {
                logger.trace("Using cached {} forecast response data", pollName);
                runCachedDataPoll.processResponse(lastRepsonse);
            } else {
                logger.trace("Starting poll sequence for {} forecast data", pollName);
                if (attemptLivePollIfRequired) {
                    reconfigurePolling();
                }
            }
        } else {
            logger.trace("Skipping refresh on non-required data for {} forecast", pollName);
        }
    }

    public void setPollDuration(final Duration durationBetweenPolls) {
        this.durationBetweenPolls = durationBetweenPolls;
    }

    public void setDataRequired(final boolean dataRequired, final boolean reconfigureIfRequired) {
        final boolean previousValue = this.dataRequired;
        this.dataRequired = dataRequired;

        if (dataRequired) {
            logger.trace("{} data poll required", pollName);
        } else {
            logger.trace("{} data poll not required", pollName);
        }

        if (dataRequired) {
            final boolean reconfigureRequired = reconfigureIfRequired && !previousValue;
            if (reconfigureRequired) {
                reconfigurePolling();
            }
            cachedPollOrLiveStart(!reconfigureRequired);
        }
    }

    public boolean getIsDataRequired() {
        return dataRequired;
    }

    public void reconfigurePolling() {
        final long millisSinceDayStart = getMillisSinceDayStart();
        final long pollRateMillis = durationBetweenPolls.toMillis();
        final long initialDelayTimeToFirstCycle = pollRateMillis - (millisSinceDayStart % pollRateMillis);
        final long lastPollExpectedTime = System.currentTimeMillis() - (pollRateMillis - initialDelayTimeToFirstCycle);

        logger.trace("Last {} poll expected time should have been : {}", pollName,
                millisToLocalDateTime(lastPollExpectedTime));

        logger.trace("Last {} poll time should have been : {}", pollName, millisToLocalDateTime(lastForecastPoll));

        // Poll if a poll hasn't been done before, or if the previous poll was before what would be now the new
        // poll intervals last poll time then a poll should be run now.
        if (lastForecastPoll == -1 || lastPollExpectedTime > lastForecastPoll) {
            liveDataPoll();
        } else {
            if (dataRequired && !lastRepsonse.isEmpty()) {
                runCachedDataPoll.processResponse(lastRepsonse);
            }
        }
        scheduleNextPoll();
    }

    private void cancelScheduledPoll(final boolean allowInterrupt) {
        synchronized (pollScheduledLock) {
            ScheduledFuture<?> job = pollScheduled;
            if (job != null) {
                job.cancel(allowInterrupt);
                pollScheduled = null;
            }
        }
    }

    public void setDataContentReceived(final String responseContent) {
        this.lastRepsonse = responseContent;
    }

    protected static long getMillisSinceDayStart() {
        return Duration.between(LocalDate.now().atStartOfDay(), LocalDateTime.now()).toMillis();
    }

    private String millisToLocalDateTime(final long milliseconds) {
        ZonedDateTime cvDate = Instant.ofEpochMilli(milliseconds).atZone(timeZoneProvider.getTimeZone());
        return cvDate.format(DateTimeFormatter.ofPattern(EXPECTED_TS_FORMAT));
    }

    private void scheduleNextPoll() {
        final long millisSinceDayStart = getMillisSinceDayStart();
        long pollRateMillis = durationBetweenPolls.toMillis();
        long initialDelayTimeToFirstCycle = pollRateMillis - (millisSinceDayStart % pollRateMillis);
        if (initialDelayTimeToFirstCycle + millisSinceDayStart > DAY_IN_MILLIS) {
            logger.debug("Not scheduling {} poll after next daily cycle reset", pollName);
        } else {
            logger.debug("Scheduling next {} forecast data poll to be in {} milliseconds at {}", pollName,
                    initialDelayTimeToFirstCycle,
                    millisToLocalDateTime(System.currentTimeMillis() + initialDelayTimeToFirstCycle));

            initialDelayTimeToFirstCycle += RANDOM_GENERATOR.nextInt(60000);
            // Schedule the first poll to occur after the given delay
            forecastJob.scheduleExecution(initialDelayTimeToFirstCycle, () -> {
                liveDataPoll();
                scheduleNextPoll();
            });
        }
    }

    public void liveDataPoll() {
        if (getIsDataRequired()) {
            logger.debug("Doing a POLL for the {} forecast", pollName);
            runLiveDataPoll.run();
        } else {
            logger.debug("Skipping a POLL for the {} forecast", pollName);
        }
    };
}
