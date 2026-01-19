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
import static org.openhab.binding.astro.internal.job.Job.*;
import static org.openhab.binding.astro.internal.model.SunPhase.*;

import java.time.Instant;
import java.time.InstantSource;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhase;

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
    private final InstantSource instantSource;

    /**
     * Constructor
     *
     * @param handler the {@link AstroThingHandler} instance
     * @param zone the {@link TimeZone} to use.
     * @param locale the {@link Locale} to use.
     * @param instantSource the time source to use.
     * @throws IllegalArgumentException
     *             if {@code thingUID} or {@code handler} is {@code null}
     */
    public DailyJobSun(AstroThingHandler handler, TimeZone zone, Locale locale, InstantSource instantSource) {
        super(handler);
        this.zone = zone;
        this.locale = locale;
        this.instantSource = instantSource;
    }

    @Override
    public void run() {
        try {
            ZonedDateTime now = instantSource.instant().atZone(zone.toZoneId());
            Calendar calNow = GregorianCalendar.from(now);
            ZonedDateTime tomorrow = now.plusDays(1);
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
            Sun sunTomorrow = (Sun) handler.getPlanetAt(tomorrow, locale);

            scheduleRange(handler, sun.getRise(), EVENT_CHANNEL_ID_RISE, zone, locale, instantSource);
            scheduleRange(handler, sun.getSet(), EVENT_CHANNEL_ID_SET, zone, locale, instantSource);

            Calendar cal, cal2;
            Range range = sun.getRange(SunPhase.NIGHT);
            Range range2;
            if (range != null) {
                cal = range.getStart();
                if (cal != null) {
                    scheduleEvent(handler, cal, EVENT_START, EVENT_CHANNEL_ID_NIGHT, false, zone, locale);
                }
                range2 = sun.getRange(SunPhase.ASTRO_DAWN);
                if (range2 == null || (cal = range2.getStart()) == null || cal.before(calNow)) {
                    cal = range.getEnd();
                }
                if (cal != null) {
                    scheduleEvent(handler, cal, EVENT_END, EVENT_CHANNEL_ID_NIGHT, false, zone, locale);
                }
            }
            range = sun.getRange(SunPhase.NOON);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NOON, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.MORNING_NIGHT);
            if (range != null) {
                range2 = sunTomorrow.getRange(SunPhase.MORNING_NIGHT);
                cal = range.getStart();
                if (cal != null) {
                    if (cal.before(calNow) && range2 != null && (cal2 = range2.getStart()) != null) {
                        cal = cal2;
                    }
                    scheduleEvent(handler, cal, EVENT_START, EVENT_CHANNEL_ID_MORNING_NIGHT, false, zone, locale);
                }
                cal = range.getEnd();
                if (cal != null) {
                    if (cal.before(calNow) && range2 != null && (cal2 = range2.getEnd()) != null) {
                        cal = cal2;
                    }
                    scheduleEvent(handler, cal, EVENT_END, EVENT_CHANNEL_ID_MORNING_NIGHT, false, zone, locale);
                }
            }
            range = sun.getRange(SunPhase.ASTRO_DAWN);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_ASTRO_DAWN, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.NAUTIC_DAWN);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NAUTIC_DAWN, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.CIVIL_DAWN);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_CIVIL_DAWN, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.ASTRO_DUSK);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_ASTRO_DUSK, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.NAUTIC_DUSK);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_NAUTIC_DUSK, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.CIVIL_DUSK);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_CIVIL_DUSK, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.EVENING_NIGHT);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_EVENING_NIGHT, zone, locale, instantSource);
            }
            range = sun.getRange(SunPhase.MIDNIGHT);
            if (range != null) {
                range2 = sunTomorrow.getRange(SunPhase.MIDNIGHT);
                cal = range.getStart();
                if (cal != null) {
                    if (cal.before(calNow) && range2 != null && (cal2 = range2.getStart()) != null) {
                        cal = cal2;
                    }
                    scheduleEvent(handler, cal, EVENT_START, EVENT_CHANNEL_ID_MIDNIGHT, false, zone, locale);
                }
                cal = range.getEnd();
                if (cal != null) {
                    if (cal.before(calNow) && range2 != null && (cal2 = range2.getEnd()) != null) {
                        cal = cal2;
                    }
                    scheduleEvent(handler, cal, EVENT_END, EVENT_CHANNEL_ID_MIDNIGHT, false, zone, locale);
                }
            }
            range = sun.getRange(SunPhase.DAYLIGHT);
            if (range != null) {
                scheduleRange(handler, range, EVENT_CHANNEL_ID_DAYLIGHT, zone, locale, instantSource);
            }

            sun.getEclipseSet().getEclipses().forEach(eclipse -> {
                scheduleEvent(handler, eclipse.when(), eclipse.kind().toString(), EVENT_CHANNEL_ID_ECLIPSE, false,
                        zone.toZoneId());
            });

            // schedule republish jobs
            if (sun.getZodiac().getEnd() instanceof Instant when) {
                schedulePublishPlanet(handler, PUBLISH_ZODIAC_JOB, when, zone.toZoneId());
            }

            if (sun.getSeason() instanceof Season season) {
                schedulePublishPlanet(handler, PUBLISH_SEASON_JOB, season.getNextSeason(), zone.toZoneId());
            }

            // schedule phase jobs
            cal = sun.getRise().getStart();
            if (cal != null) {
                scheduleSunPhase(handler, SUN_RISE.name(), SUN_RISE, cal, zone, locale);
            }
            cal = sun.getSet().getStart();
            if (cal != null) {
                scheduleSunPhase(handler, SUN_SET.name(), SUN_SET, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.NIGHT)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NIGHT.name(), NIGHT, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.DAYLIGHT)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, DAYLIGHT.name(), DAYLIGHT, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.ASTRO_DAWN)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, ASTRO_DAWN.name(), ASTRO_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.NAUTIC_DAWN)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NAUTIC_DAWN.name(), NAUTIC_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.CIVIL_DAWN)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, CIVIL_DAWN.name(), CIVIL_DAWN, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.ASTRO_DUSK)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, ASTRO_DUSK.name(), ASTRO_DUSK, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.NAUTIC_DUSK)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, NAUTIC_DUSK.name(), NAUTIC_DUSK, cal, zone, locale);
            }
            cal = (range = sun.getRange(SunPhase.CIVIL_DUSK)) == null ? null : range.getStart();
            if (cal != null) {
                scheduleSunPhase(handler, CIVIL_DUSK.name(), CIVIL_DUSK, cal, zone, locale);
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
