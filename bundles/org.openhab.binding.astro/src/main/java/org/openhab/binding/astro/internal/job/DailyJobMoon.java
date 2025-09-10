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

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.job.Job.scheduleEvent;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.Planet;

/**
 * Daily scheduled jobs for Moon planet
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class DailyJobMoon extends AbstractJob {

    private final AstroThingHandler handler;
    public final TimeZone zone;
    public final Locale locale;

    /**
     * Constructor
     *
     * @param thingUID the Thing UID
     * @param handler the {@link AstroThingHandler} instance
     * @throws IllegalArgumentException if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobMoon(String thingUID, AstroThingHandler handler, TimeZone zone, Locale locale) {
        super(thingUID);
        this.handler = handler;
        this.zone = zone;
        this.locale = locale;
    }

    @Override
    public void run() {
        handler.publishDailyInfo();
        String thingUID = getThingUID();
        LOGGER.debug("Scheduled Astro event-jobs for thing {}", thingUID);

        Planet planet = handler.getPlanet();
        if (planet == null) {
            LOGGER.error("Planet not instantiated");
            return;
        }
        Moon moon = (Moon) planet;
        scheduleEvent(thingUID, handler, moon.getRise().getStart(), EVENT_START, EVENT_CHANNEL_ID_RISE, false, zone,
                locale);
        scheduleEvent(thingUID, handler, moon.getSet().getEnd(), EVENT_END, EVENT_CHANNEL_ID_SET, false, zone, locale);

        MoonPhase moonPhase = moon.getPhase();
        scheduleEvent(thingUID, handler, moonPhase.getFirstQuarter(), EVENT_PHASE_FIRST_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE, false, zone, locale);
        scheduleEvent(thingUID, handler, moonPhase.getThirdQuarter(), EVENT_PHASE_THIRD_QUARTER,
                EVENT_CHANNEL_ID_MOON_PHASE, false, zone, locale);
        scheduleEvent(thingUID, handler, moonPhase.getFull(), EVENT_PHASE_FULL, EVENT_CHANNEL_ID_MOON_PHASE, false,
                zone, locale);
        scheduleEvent(thingUID, handler, moonPhase.getNew(), EVENT_PHASE_NEW, EVENT_CHANNEL_ID_MOON_PHASE, false, zone,
                locale);

        Eclipse eclipse = moon.getEclipse();
        eclipse.getKinds().forEach(eclipseKind -> {
            Calendar eclipseDate = eclipse.getDate(eclipseKind);
            if (eclipseDate != null) {
                scheduleEvent(thingUID, handler, eclipseDate, eclipseKind.toString(), EVENT_CHANNEL_ID_ECLIPSE, false,
                        zone, locale);
            }
        });

        scheduleEvent(thingUID, handler, moon.getPerigee().getDate(), EVENT_PERIGEE, EVENT_CHANNEL_ID_PERIGEE, false,
                zone, locale);
        scheduleEvent(thingUID, handler, moon.getApogee().getDate(), EVENT_APOGEE, EVENT_CHANNEL_ID_APOGEE, false, zone,
                locale);
    }

    @Override
    public String toString() {
        return "Daily job moon " + getThingUID();
    }
}
