/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static java.util.Arrays.asList;
import static java.util.Calendar.SECOND;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.time.DateUtils.truncatedEquals;
import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.util.DateTimeUtils.*;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;

import org.eclipse.smarthome.core.scheduler.SchedulerRunnable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface to be implemented by classes which represent a 'job' to be performed
 *
 * @author Amit Kumar Mondal - New Simplified API, Implementation compliant with ESH Scheduler
 */
public interface Job extends SchedulerRunnable, Runnable {

    /** Logger Instance */
    public final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Schedules the provided {@link Job} instance
     *
     * @param thingUID the UID of the {@link Thing} instance
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param job the {@link Job} instance to schedule
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void schedule(String thingUID, AstroThingHandler astroHandler, Job job, Calendar eventAt) {
        try {
            Calendar today = Calendar.getInstance();
            if (isSameDay(eventAt, today) && isTimeGreaterEquals(eventAt, today)) {
                astroHandler.schedule(job, eventAt);
            }
        } catch (Exception ex) {
            LOGGER.error("{}", ex.getMessage(), ex);
        }
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param thingUID the Thing UID
     * @param astroHandler the {@link ThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param event the event ID
     * @param channelId the channel ID
     */
    public static void scheduleEvent(String thingUID, AstroThingHandler astroHandler, Calendar eventAt, String event,
            String channelId, boolean configAlreadyApplied) {
        scheduleEvent(thingUID, astroHandler, eventAt, singletonList(event), channelId, configAlreadyApplied);
    }

    /**
     * Schedules an {@link EventJob} instance
     *
     * @param thingUID the Thing UID
     * @param astroHandler the {@link ThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     * @param events the event IDs to schedule
     * @param channelId the channel ID
     */
    public static void scheduleEvent(String thingUID, AstroThingHandler astroHandler, Calendar eventAt,
            List<String> events, String channelId, boolean configAlreadyApplied) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");
        boolean eventsNull = checkNull(events, "Events list is null");
        boolean channelIdNull = checkNull(channelId, "Channel ID is null");

        if (thingNull || astroHandlerNull || eventAtNull || eventsNull || channelIdNull || events.isEmpty()) {
            return;
        }
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
        List<Job> jobs = events.stream().map(e -> new EventJob(thingUID, channelId, e)).collect(toList());
        schedule(thingUID, astroHandler, new CompositeJob(thingUID, jobs), instant);
    }

    /**
     * Schedules {@link Channel} events
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param range the {@link Range} instance
     * @param channelId the channel ID
     */
    public static void scheduleRange(String thingUID, AstroThingHandler astroHandler, Range range, String channelId) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean rangeNull = checkNull(range, "Range is null");
        boolean channelIdNull = checkNull(channelId, "Channel ID is null");

        if (thingNull || astroHandlerNull || rangeNull || channelIdNull) {
            return;
        }

        Calendar start = range.getStart();
        Calendar end = range.getEnd();

        // depending on the location you might not have a valid range for day/night, so skip the events:
        if (start == null || end == null) {
            return;
        }

        final Channel channel = astroHandler.getThing().getChannel(channelId);
        if (channel == null) {
            LOGGER.warn("Cannot find channel '{}' for thing '{}'.", channelId, astroHandler.getThing().getUID());
            return;
        }
        AstroChannelConfig config = channel.getConfiguration().as(AstroChannelConfig.class);
        Calendar configStart = applyConfig(start, config);
        Calendar configEnd = applyConfig(end, config);

        if (truncatedEquals(configStart, configEnd, SECOND)) {
            scheduleEvent(thingUID, astroHandler, configStart, asList(EVENT_START, EVENT_END), channelId, true);
        } else {
            scheduleEvent(thingUID, astroHandler, configStart, EVENT_START, channelId, true);
            scheduleEvent(thingUID, astroHandler, configEnd, EVENT_END, channelId, true);
        }
    }

    /**
     * Schedules {@link Planet} events
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void schedulePublishPlanet(String thingUID, AstroThingHandler astroHandler, Calendar eventAt) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");

        if (thingNull || astroHandlerNull || eventAtNull) {
            return;
        }
        Job publishJob = new PublishPlanetJob(thingUID);
        schedule(thingUID, astroHandler, publishJob, eventAt);
    }

    /**
     * Schedules {@link SunPhaseJob}
     *
     * @param thingUID the {@link Thing} UID
     * @param astroHandler the {@link AstroThingHandler} instance
     * @param sunPhaseName {@link SunPhaseName} instance
     * @param eventAt the {@link Calendar} instance denoting scheduled instant
     */
    public static void scheduleSunPhase(String thingUID, AstroThingHandler astroHandler, SunPhaseName sunPhaseName,
            Calendar eventAt) {
        boolean thingNull = checkNull(thingUID, "Thing UID is null");
        boolean astroHandlerNull = checkNull(astroHandler, "AstroThingHandler is null");
        boolean sunPhaseNull = checkNull(sunPhaseName, "Sun Phase Name is null");
        boolean eventAtNull = checkNull(eventAt, "Scheduled Instant is null");

        if (thingNull || astroHandlerNull || sunPhaseNull || eventAtNull) {
            return;
        }
        Job sunPhaseJob = new SunPhaseJob(thingUID, sunPhaseName);
        schedule(thingUID, astroHandler, sunPhaseJob, eventAt);
    }

    /**
     * Checks that the specified object reference is not {@code null} and logs a
     * customized message if it is. This method is designed primarily for doing
     * parameter validation in methods and constructors with multiple
     * parameters, as demonstrated below: <blockquote>
     *
     * <pre>
     * public Foo(Bar bar, Baz baz) {
     *     checkNull(bar, "bar is null");
     *     checkNull(baz, "baz is null");
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param obj the object reference to check for {@code null}
     * @param message detail message to be used in the event
     * @return {@code true} if {@code null} otherwise {@code false}
     */
    public static <T> boolean checkNull(T obj, String message) {
        if (isNull(obj)) {
            LOGGER.trace("{}", message);
            return true;
        }
        return false;
    }

    /**
     * Returns the thing UID that is associated with this {@link Job} (cannot be {@code null})
     */
    public String getThingUID();
}
