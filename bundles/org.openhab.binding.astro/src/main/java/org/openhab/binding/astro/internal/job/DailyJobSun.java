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

import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Eclipse;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.model.Sun;

/**
 * Daily scheduled jobs For Sun planet
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public final class DailyJobSun extends AbstractJob {

    private final TimeZone zone;
    private final Locale locale;

    /**
     * Constructor
     *
     * @param handler the {@link AstroThingHandler} instance
     * @throws IllegalArgumentException
     *             if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobSun(AstroThingHandler handler, TimeZone zone, Locale locale) {
        super(handler);
        this.zone = zone;
        this.locale = locale;
    }

    @Override
    public void run() {
        try {
            handler.publishDailyInfo();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Scheduled Astro event-jobs for thing {}", handler.getThing().getUID());
            }

            Planet planet = handler.getPlanet();
            if (planet == null) {
                LOGGER.error("Planet not instantiated");
                return;
            }
            Sun sun = (Sun) planet;
            scheduleRange(handler, sun.getRise(), EVENT_CHANNEL_ID_RISE, zone, locale);
            scheduleRange(handler, sun.getSet(), EVENT_CHANNEL_ID_SET, zone, locale);
            Range range = sun.getNoon();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NOON, zone, locale);
            }
            range = sun.getNight();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NIGHT, zone, locale);
            }
            range = sun.getMorningNight();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_MORNING_NIGHT, zone, locale);
            }
            range = sun.getAstroDawn();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_ASTRO_DAWN, zone, locale);
            }
            range = sun.getNauticDawn();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NAUTIC_DAWN, zone, locale);
            }
            range = sun.getCivilDawn();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_CIVIL_DAWN, zone, locale);
            }
            range = sun.getAstroDusk();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_ASTRO_DUSK, zone, locale);
            }
            range = sun.getNauticDusk();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NAUTIC_DUSK, zone, locale);
            }
            range = sun.getCivilDusk();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_CIVIL_DUSK, zone, locale);
            }
            range = sun.getEveningNight();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_EVENING_NIGHT, zone, locale);
            }
            range = sun.getDaylight();
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_DAYLIGHT, zone, locale);
            }

            Eclipse eclipse = sun.getEclipse();
            eclipse.getKinds().forEach(eclipseKind -> {
                Calendar eclipseDate = eclipse.getDate(eclipseKind);
                if (eclipseDate != null) {
                    scheduleEvent(handler, eclipseDate, eclipseKind.toString(), EVENT_CHANNEL_ID_ECLIPSE, false, zone,
                            locale);
                }
            });

            // schedule republish jobs
            if (sun.getZodiac().getEnd() instanceof Instant when) {
                schedulePublishPlanet(handler, when);
            }

            if (sun.getSeason() instanceof Season season) {
                schedulePublishPlanet(handler, season.getNextSeason());
            }

            // schedule phase jobs
            Calendar cal = sun.getRise().getStart();
            if (cal != null) {
                scheduleSunPhase(handler, SUN_RISE, cal, zone, locale);
            }
            cal = sun.getSet().getStart();
            if (cal != null) {
                scheduleSunPhase(handler, SUN_SET, cal, zone, locale);
            }
            cal = (range = sun.getNight()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NIGHT, cal, zone, locale);
            }
            cal = (range = sun.getDaylight()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, DAYLIGHT, cal, zone, locale);
            }
            cal = (range = sun.getAstroDawn()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, ASTRO_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getNauticDawn()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NAUTIC_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getCivilDawn()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, CIVIL_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getAstroDusk()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, ASTRO_DUSK, cal, zone, locale);
            }
            cal = (range = sun.getNauticDusk()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NAUTIC_DUSK, cal, zone, locale);
            }
            cal = (range = sun.getCivilDusk()) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, CIVIL_DUSK, cal, zone, locale);
            }
        } catch (Exception e) {
            LOGGER.warn("The daily sun job execution for \"{}\" failed: {}", handler.getThing().getUID(),
                    e.getMessage());
            LOGGER.trace("", e);
        }
    }

    @Override
    public String toString() {
        return "Daily job sun " + handler.getThing().getUID();
    }
}
