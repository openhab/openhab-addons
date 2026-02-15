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
package org.openhab.binding.astro.internal.job;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.util.DateTimeUtils.*;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.SunPhase;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.scheduler.SchedulerRunnable;
import org.openhab.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface to be implemented by classes which represent a 'job' to be performed
 *
 * @author Amit Kumar Mondal - Initial contribution
 */
@NonNullByDefault
public interface Job extends SchedulerRunnable, Runnable {

    final int DAILY_SCHEDULE_TIME_WINDOW_LENGTH = 26;
    final TimeUnit DAILY_SCHEDULE_TIME_WINDOW_UNIT = TimeUnit.HOURS;
    final ChronoUnit DAILY_SCHEDULE_TIME_WINDOW_CHRONOUNIT = ChronoUnit.HOURS;

    /** The {@link Logger} Instance */
    final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Schedules the provided {@link Job} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param job the {@link Job} instance to schedule
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param zone the configured time zone
     * @param locale the configured locale
     */
    static void schedule(AstroThingHandler astroHandler, String identifier, Job job, Calendar eventAt, TimeZone zone,
            Locale locale) {
        try {
            // Don't use InstantSource here, because we always want to schedule relative to the system clock
            Calendar today = Calendar.getInstance(zone, locale);
            if (isWithinTimeWindow(eventAt, today, DAILY_SCHEDULE_TIME_WINDOW_LENGTH,
                    DAILY_SCHEDULE_TIME_WINDOW_UNIT)) {
                astroHandler.schedule(identifier, job, eventAt);
            } else if (LOGGER.isDebugEnabled()) {
                if (eventAt.before(today)) {
                    LOGGER.debug("Not scheduling {} because it's in the past ({})", job, eventAt.getTime());
                } else {
                    LOGGER.debug("Not scheduling {} because it's outside the schedulable time window ({})", job,
                            eventAt.getTime());
                }
            }
        } catch (Exception ex) {
            LOGGER.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Schedules the provided {@link Job} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param job the {@link Job} instance to schedule
     * @param eventAt the {@link Instant} instance denoting scheduled instant
     * @param zone the configured time zone
     */
    static void schedule(AstroThingHandler astroHandler, String identifier, Job job, Instant eventAt, ZoneId zone) {
        // Don't use InstantSource here, because we always want to schedule relative to the system clock
        Instant now = Instant.now();
        if (isWithinTimeWindow(eventAt, now, DAILY_SCHEDULE_TIME_WINDOW_LENGTH,
                DAILY_SCHEDULE_TIME_WINDOW_CHRONOUNIT)) {
            astroHandler.schedule(identifier, job, eventAt);
        } else if (LOGGER.isDebugEnabled()) {
            if (eventAt.isBefore(now)) {
                LOGGER.debug("Not scheduling {} because it's in the past ({})", job, eventAt.atZone(zone));
            } else {
                LOGGER.debug("Not scheduling {} because it's outside the schedulable time window ({})", job,
                        eventAt.atZone(zone));
            }
        }
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param event the event ID
     * @param channelId the channel ID
     * @param configAlreadyApplied whether the configuration has already been "applied"
     * @param zone the configured time zone
     * @param locale the configured locale
     */
    static void scheduleEvent(AstroThingHandler astroHandler, Calendar eventAt, String event, String channelId,
            boolean configAlreadyApplied, TimeZone zone, Locale locale) {
        final Calendar instant;
        if (!configAlreadyApplied) {
            final Channel channel = astroHandler.getThing().getChannel(channelId);
            if (channel == null) {
                LOGGER.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
                return;
            }
            AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
            instant = applyConfig(eventAt, config);
        } else {
            instant = eventAt;
        }
        Job eventJob = new EventJob(astroHandler, channelId, event);
        schedule(astroHandler, channelId + '@' + event, eventJob, instant, zone, locale);
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Instant} instance denoting scheduled instant
     * @param events the event IDs to schedule
     * @param channelId the channel ID
     * @param configAlreadyApplied whether the configuration has already been "applied"
     * @param zone the configured time zone
     */
    static void scheduleEvent(AstroThingHandler astroHandler, Instant eventAt, String event, String channelId,
            boolean configAlreadyApplied, ZoneId zone) {
        final Instant instant;
        if (!configAlreadyApplied) {
            final Channel channel = astroHandler.getThing().getChannel(channelId);
            if (channel == null) {
                LOGGER.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
                return;
            }
            AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
            instant = applyConfig(eventAt, config);
        } else {
            instant = eventAt;
        }
        Job eventJob = new EventJob(astroHandler, channelId, event);
        schedule(astroHandler, channelId + '@' + event, eventJob, instant, zone);
    }

    /**
     * Schedules {@link Channel} events
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param range the {@link Range} instance
     * @param channelId the channel ID
     */
    static void scheduleRange(AstroThingHandler astroHandler, Range range, String channelId, TimeZone zone,
            Locale locale, InstantSource instantSource) {
        final Channel channel = astroHandler.getThing().getChannel(channelId);
        if (channel == null) {
            LOGGER.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
            return;
        }
        AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
        Range adjustedRange = adjustRangeToConfig(range, config, zone, locale, instantSource);

        Calendar start = adjustedRange.getStart();
        Calendar end = adjustedRange.getEnd();

        if (start == null || end == null) {
            LOGGER.debug("event was not scheduled as either start or end was null");
            return;
        }

        scheduleEvent(astroHandler, start, EVENT_START, channelId, true, zone, locale);
        scheduleEvent(astroHandler, end, EVENT_END, channelId, true, zone, locale);
    }

    static Range adjustRangeToConfig(Range range, AstroChannelConfig config, TimeZone zone, Locale locale,
            InstantSource instantSource) {
        Calendar start = range.getStart();
        Calendar end = range.getEnd();

        if (config.forceEvent) {
            Calendar reference = start != null ? start : end;
            if (reference == null) {
                reference = DateTimeUtils.calFromInstantSource(instantSource, zone, locale);
            }
            if (start == null) {
                start = getAdjustedEarliest(truncateToMidnight(reference), config);
            }
            if (end == null) {
                end = getAdjustedLatest(endOfDayDate(reference), config);
            }
        }

        // depending on the location and configuration you might not have a valid range for day/night, so skip the
        // events:
        if (start == null || end == null) {
            return range;
        }

        return new Range(applyConfig(start, config), applyConfig(end, config));
    }

    /**
     * Schedules Planet events
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param zone the configured time zone
     * @param locale the configured locale
     */
    static void schedulePublishPlanet(AstroThingHandler astroHandler, String identifier, Calendar eventAt,
            TimeZone zone, Locale locale) {
        Job publishJob = new PublishPlanetJob(astroHandler);
        schedule(astroHandler, identifier, publishJob, eventAt, zone, locale);
    }

    /**
     * Schedules Planet events
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param when the {@link Instant} instance denoting scheduled instant
     * @param zone the configured time zone
     */
    static void schedulePublishPlanet(AstroThingHandler astroHandler, String identifier, Instant when, ZoneId zone) {
        Job publishJob = new PublishPlanetJob(astroHandler);
        schedule(astroHandler, identifier, publishJob, when, zone);
    }

    /**
     * Schedules {@link SunPhaseJob}
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param sunPhase {@link SunPhase} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    static void scheduleSunPhase(AstroThingHandler astroHandler, String identifier, SunPhase sunPhase, Calendar eventAt,
            TimeZone zone, Locale locale) {
        Job sunPhaseJob = new SunPhaseJob(astroHandler, sunPhase);
        schedule(astroHandler, identifier, sunPhaseJob, eventAt, zone, locale);
    }

    /**
     * @return The {@link AstroThingHandler} associated with this {@link Job}.
     */
    AstroThingHandler getHandler();
}
