/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.util.DateTimeUtils.*;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.SunPhaseName;
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

    /** The {@link Logger} Instance */
    final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Schedules the provided {@link Job} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param job the {@link Job} instance to schedule
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    static void schedule(AstroThingHandler astroHandler, Job job, Calendar eventAt, TimeZone zone, Locale locale) {
        try {
            Calendar today = Calendar.getInstance(zone, locale);
            if (isSameDay(eventAt, today) && isTimeGreaterEquals(eventAt, today)) {
                astroHandler.schedule(job, eventAt);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param event the event ID
     * @param channelId the channel ID
     */
    static void scheduleEvent(AstroThingHandler astroHandler, Calendar eventAt, String event, String channelId,
            boolean configAlreadyApplied, TimeZone zone, Locale locale) {
        scheduleEvent(astroHandler, eventAt, List.of(event), channelId, configAlreadyApplied, zone, locale);
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param events the event IDs to schedule
     * @param channelId the channel ID
     */
    static void scheduleEvent(AstroThingHandler astroHandler, Calendar eventAt, List<String> events, String channelId,
            boolean configAlreadyApplied, TimeZone zone, Locale locale) {
        if (events.isEmpty()) {
            return;
        }
        final Calendar instant;
        if (!configAlreadyApplied) {
            final Channel channel = astroHandler.getThing().getChannel(channelId);
            if (channel == null) {
                logger.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
                return;
            }
            AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
            instant = applyConfig(eventAt, config);
        } else {
            instant = eventAt;
        }
        List<Job> jobs = events.stream().map(e -> new EventJob(astroHandler, channelId, e)).collect(toList());
        schedule(astroHandler, new CompositeJob(astroHandler, jobs), instant, zone, locale);
    }

    /**
     * Schedules {@link Channel} events
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param range the {@link Range} instance
     * @param channelId the channel ID
     */
    static void scheduleRange(AstroThingHandler astroHandler, Range range, String channelId, TimeZone zone,
            Locale locale) {
        final Channel channel = astroHandler.getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
            return;
        }
        AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
        Range adjustedRange = adjustRangeToConfig(range, config, zone, locale);

        Calendar start = adjustedRange.getStart();
        Calendar end = adjustedRange.getEnd();

        if (start == null || end == null) {
            logger.debug("event was not scheduled as either start or end was null");
            return;
        }

        scheduleEvent(astroHandler, start, EVENT_START, channelId, true, zone, locale);
        scheduleEvent(astroHandler, end, EVENT_END, channelId, true, zone, locale);
    }

    static Range adjustRangeToConfig(Range range, AstroChannelConfig config, TimeZone zone, Locale locale) {
        Calendar start = range.getStart();
        Calendar end = range.getEnd();

        if (config.forceEvent) {
            Calendar reference = start != null ? start : end;
            if (reference == null) {
                reference = Calendar.getInstance(zone, locale);
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

        return new Range(truncateToSecond(applyConfig(start, config)), truncateToSecond(applyConfig(end, config)));
    }

    /**
     * Schedules Planet events
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    static void schedulePublishPlanet(AstroThingHandler astroHandler, Calendar eventAt, TimeZone zone, Locale locale) {
        Job publishJob = new PublishPlanetJob(astroHandler);
        schedule(astroHandler, publishJob, eventAt, zone, locale);
    }

    /**
     * Schedules {@link SunPhaseJob}
     *
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param sunPhaseName {@link SunPhaseName} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    static void scheduleSunPhase(AstroThingHandler astroHandler, SunPhaseName sunPhaseName, Calendar eventAt,
            TimeZone zone, Locale locale) {
        Job sunPhaseJob = new SunPhaseJob(astroHandler, sunPhaseName);
        schedule(astroHandler, sunPhaseJob, eventAt, zone, locale);
    }

    /**
     * @return The {@link AstroThingHandler} associated with this {@link Job}.
     */
    AstroThingHandler getHandler();
}
