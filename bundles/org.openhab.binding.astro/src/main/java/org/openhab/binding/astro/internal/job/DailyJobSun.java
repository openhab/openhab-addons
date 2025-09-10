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
import static org.openhab.binding.astro.internal.job.Job.*;
import static org.openhab.binding.astro.internal.model.SunPhaseName.*;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Sun;

/**
 * Daily scheduled jobs For Sun planet
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class DailyJobSun extends AbstractJob {

    private final AstroThingHandler handler;
    private final TimeZone zone;
    private final Locale locale;

    /**
     * Constructor
     *
     * @param thingUID the Thing UID
     * @param handler the {@link AstroThingHandler} instance
     * @throws IllegalArgumentException
     *             if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobSun(String thingUID, AstroThingHandler handler, TimeZone zone, Locale locale) {
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
        Sun sun = (Sun) planet;
        scheduleRange(thingUID, handler, sun.getRise(), EVENT_CHANNEL_ID_RISE, zone, locale);
        scheduleRange(thingUID, handler, sun.getSet(), EVENT_CHANNEL_ID_SET, zone, locale);
        scheduleRange(thingUID, handler, sun.getNoon(), EVENT_CHANNEL_ID_NOON, zone, locale);
        scheduleRange(thingUID, handler, sun.getNight(), EVENT_CHANNEL_ID_NIGHT, zone, locale);
        scheduleRange(thingUID, handler, sun.getMorningNight(), EVENT_CHANNEL_ID_MORNING_NIGHT, zone, locale);
        scheduleRange(thingUID, handler, sun.getAstroDawn(), EVENT_CHANNEL_ID_ASTRO_DAWN, zone, locale);
        scheduleRange(thingUID, handler, sun.getNauticDawn(), EVENT_CHANNEL_ID_NAUTIC_DAWN, zone, locale);
        scheduleRange(thingUID, handler, sun.getCivilDawn(), EVENT_CHANNEL_ID_CIVIL_DAWN, zone, locale);
        scheduleRange(thingUID, handler, sun.getAstroDusk(), EVENT_CHANNEL_ID_ASTRO_DUSK, zone, locale);
        scheduleRange(thingUID, handler, sun.getNauticDusk(), EVENT_CHANNEL_ID_NAUTIC_DUSK, zone, locale);
        scheduleRange(thingUID, handler, sun.getCivilDusk(), EVENT_CHANNEL_ID_CIVIL_DUSK, zone, locale);
        scheduleRange(thingUID, handler, sun.getEveningNight(), EVENT_CHANNEL_ID_EVENING_NIGHT, zone, locale);
        scheduleRange(thingUID, handler, sun.getDaylight(), EVENT_CHANNEL_ID_DAYLIGHT, zone, locale);

        Eclipse eclipse = sun.getEclipse();
        eclipse.getKinds().forEach(eclipseKind -> {
            Calendar eclipseDate = eclipse.getDate(eclipseKind);
            if (eclipseDate != null) {
                scheduleEvent(thingUID, handler, eclipseDate, eclipseKind.toString(), EVENT_CHANNEL_ID_ECLIPSE, false,
                        zone, locale);
            }
        });

        // schedule republish jobs
        schedulePublishPlanet(thingUID, handler, sun.getZodiac().getEnd(), zone, locale);
        schedulePublishPlanet(thingUID, handler, sun.getSeason().getNextSeason(zone, locale), zone, locale);

        // schedule phase jobs
        scheduleSunPhase(thingUID, handler, SUN_RISE, sun.getRise().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, SUN_SET, sun.getSet().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, NIGHT, sun.getNight().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, DAYLIGHT, sun.getDaylight().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, ASTRO_DAWN, sun.getAstroDawn().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, NAUTIC_DAWN, sun.getNauticDawn().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, CIVIL_DAWN, sun.getCivilDawn().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, ASTRO_DUSK, sun.getAstroDusk().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, NAUTIC_DUSK, sun.getNauticDusk().getStart(), zone, locale);
        scheduleSunPhase(thingUID, handler, CIVIL_DUSK, sun.getCivilDusk().getStart(), zone, locale);
    }

    @Override
    public String toString() {
        return "Daily job sun " + getThingUID();
    }
}
