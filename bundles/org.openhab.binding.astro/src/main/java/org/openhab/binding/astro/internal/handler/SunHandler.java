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
package org.openhab.binding.astro.internal.handler;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;

import java.time.InstantSource;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.calc.CircadianCalc;
import org.openhab.binding.astro.internal.calc.RadiationCalc;
import org.openhab.binding.astro.internal.calc.SunCalc;
import org.openhab.binding.astro.internal.job.DailyJobSun;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.model.Radiation;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Season;
import org.openhab.binding.astro.internal.model.SeasonName;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhase;
import org.openhab.binding.astro.internal.util.DateTimeUtils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SunHandler is responsible for updating calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public class SunHandler extends AstroThingHandler {
    private static final Set<String> POSITIONAL_CHANNELS = Set.of(CHANNEL_ID_POSITION_AZIMUTH,
            CHANNEL_ID_POSITION_ELEVATION, CHANNEL_ID_SUN_RADIATION_DIRECT, CHANNEL_ID_SUN_RADIATION_DIFFUSE,
            CHANNEL_ID_SUN_RADIATION_TOTAL);

    private final SunCalc sunCalc;
    private final Logger logger = LoggerFactory.getLogger(SunHandler.class);
    volatile @Nullable Sun sun;

    /**
     * Constructor
     */
    public SunHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider,
            LocaleProvider localeProvider, InstantSource instantSource) {
        super(thing, scheduler, timeZoneProvider, localeProvider, instantSource, POSITIONAL_CHANNELS);
        sunCalc = new SunCalc(instantSource);
    }

    @Override
    public void publishPositionalInfo() {
        ZoneId zoneId = timeZoneProvider.getTimeZone();
        TimeZone zone = TimeZone.getTimeZone(zoneId);
        Locale locale = localeProvider.getLocale();
        ZonedDateTime now = instantSource.instant().atZone(zoneId);
        Sun sun = getPlanetAt(now, locale);
        double latitude = thingConfig.latitude instanceof Double value ? value : 0;
        double longitude = thingConfig.longitude instanceof Double value ? value : 0;
        double altitude = thingConfig.altitude instanceof Double value ? value : 0;
        Calendar calendar = DateTimeUtils.calFromInstantSource(instantSource, zone, locale);
        sunCalc.setPositionalInfo(calendar, latitude, longitude, sun);

        sun.setCircadian(CircadianCalc.calculate(calendar, sun.getRise(), sun.getSet(), sun.getRange(SunPhase.NOON)));
        sun.setRadiation(RadiationCalc.calculate(now, sun.getPosition().getElevationAsDouble(), altitude));

        this.sun = sun;

        publishPlanet();
    }

    @Override
    public @Nullable Planet getPlanet() {
        return sun;
    }

    @Override
    public void dispose() {
        super.dispose();
        sun = null;
    }

    @Override
    protected State getState(Channel channel) {
        Sun sun = this.sun;
        if (sun == null) {
            return UnDefType.UNDEF;
        }
        Range r;
        Season s;
        switch (channel.getUID().getId()) {
            case CHANNEL_ID_RISE_START:
                return toState(sun.getRise().getStart(), channel);
            case CHANNEL_ID_RISE_END:
                return toState(sun.getRise().getEnd(), channel);
            case CHANNEL_ID_RISE_DURATION:
                return toState(sun.getRise().getDuration(), channel);
            case CHANNEL_ID_SET_START:
                return toState(sun.getSet().getStart(), channel);
            case CHANNEL_ID_SET_END:
                return toState(sun.getSet().getEnd(), channel);
            case CHANNEL_ID_SET_DURATION:
                return toState(sun.getSet().getDuration(), channel);
            case CHANNEL_ID_SUN_NOON_START:
                r = sun.getRange(SunPhase.NOON);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_NOON_END:
                r = sun.getRange(SunPhase.NOON);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_NOON_DURATION:
                r = sun.getRange(SunPhase.NOON);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_MIDNIGHT_START:
                r = sun.getRange(SunPhase.MIDNIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_MIDNIGHT_END:
                r = sun.getRange(SunPhase.MIDNIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_MIDNIGHT_DURATION:
                r = sun.getRange(SunPhase.MIDNIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_NIGHT_START:
                r = sun.getRange(SunPhase.NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_NIGHT_END:
                r = sun.getRange(SunPhase.NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_NIGHT_DURATION:
                r = sun.getRange(SunPhase.NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_MORNING_NIGHT_START:
                r = sun.getRange(SunPhase.MORNING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_MORNING_NIGHT_END:
                r = sun.getRange(SunPhase.MORNING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_MORNING_NIGHT_DURATION:
                r = sun.getRange(SunPhase.MORNING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_ASTRO_DAWN_START:
                r = sun.getRange(SunPhase.ASTRO_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_ASTRO_DAWN_END:
                r = sun.getRange(SunPhase.ASTRO_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_ASTRO_DAWN_DURATION:
                r = sun.getRange(SunPhase.ASTRO_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DAWN_START:
                r = sun.getRange(SunPhase.NAUTIC_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DAWN_END:
                r = sun.getRange(SunPhase.NAUTIC_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DAWN_DURATION:
                r = sun.getRange(SunPhase.NAUTIC_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_CIVIL_DAWN_START:
                r = sun.getRange(SunPhase.CIVIL_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_CIVIL_DAWN_END:
                r = sun.getRange(SunPhase.CIVIL_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_CIVIL_DAWN_DURATION:
                r = sun.getRange(SunPhase.CIVIL_DAWN);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_ASTRO_DUSK_START:
                r = sun.getRange(SunPhase.ASTRO_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_ASTRO_DUSK_END:
                r = sun.getRange(SunPhase.ASTRO_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_ASTRO_DUSK_DURATION:
                r = sun.getRange(SunPhase.ASTRO_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DUSK_START:
                r = sun.getRange(SunPhase.NAUTIC_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DUSK_END:
                r = sun.getRange(SunPhase.NAUTIC_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_NAUTIC_DUSK_DURATION:
                r = sun.getRange(SunPhase.NAUTIC_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_CIVIL_DUSK_START:
                r = sun.getRange(SunPhase.CIVIL_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_CIVIL_DUSK_END:
                r = sun.getRange(SunPhase.CIVIL_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_CIVIL_DUSK_DURATION:
                r = sun.getRange(SunPhase.CIVIL_DUSK);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_EVENING_NIGHT_START:
                r = sun.getRange(SunPhase.EVENING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_EVENING_NIGHT_END:
                r = sun.getRange(SunPhase.EVENING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_EVENING_NIGHT_DURATION:
                r = sun.getRange(SunPhase.EVENING_NIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_SUN_DAYLIGHT_START:
                r = sun.getRange(SunPhase.DAYLIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getStart(), channel);
            case CHANNEL_ID_SUN_DAYLIGHT_END:
                r = sun.getRange(SunPhase.DAYLIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getEnd(), channel);
            case CHANNEL_ID_SUN_DAYLIGHT_DURATION:
                r = sun.getRange(SunPhase.DAYLIGHT);
                return r == null ? UnDefType.UNDEF : toState(r.getDuration(), channel);
            case CHANNEL_ID_POSITION_AZIMUTH:
                return toState(sun.getPosition().getAzimuth(), channel);
            case CHANNEL_ID_POSITION_ELEVATION:
                return toState(sun.getPosition().getElevation(), channel);
            case CHANNEL_ID_SUN_POSITION_SHADE_LENGTH:
                return toState(sun.getPosition().getShadeLength(), channel);
            case CHANNEL_ID_SUN_RADIATION_DIRECT:
                return toState(sun.getRadiation().getDirect(), channel);
            case CHANNEL_ID_SUN_RADIATION_DIFFUSE:
                return toState(sun.getRadiation().getDiffuse(), channel);
            case CHANNEL_ID_SUN_RADIATION_TOTAL:
                return toState(sun.getRadiation().getTotal(), channel);
            case CHANNEL_ID_SUN_ZODIAC_START:
                return toState(sun.getZodiac().getStart(), channel);
            case CHANNEL_ID_SUN_ZODIAC_END:
                return toState(sun.getZodiac().getEnd(), channel);
            case CHANNEL_ID_ZODIAC_SIGN:
                return toState(sun.getZodiac().getSign(), channel);
            case CHANNEL_ID_SUN_SEASON_NAME:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getName(), channel);
            case CHANNEL_ID_SUN_SEASON_SPRING:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getSeasonStart(SeasonName.SPRING), channel);
            case CHANNEL_ID_SUN_SEASON_SUMMER:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getSeasonStart(SeasonName.SUMMER), channel);
            case CHANNEL_ID_SUN_SEASON_AUTUMN:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getSeasonStart(SeasonName.AUTUMN), channel);
            case CHANNEL_ID_SUN_SEASON_WINTER:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getSeasonStart(SeasonName.WINTER), channel);
            case CHANNEL_ID_SUN_SEASON_NEXT_NAME:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getNextName(), channel);
            case CHANNEL_ID_SUN_SEASON_TIME_LEFT:
                s = sun.getSeason();
                return s == null ? UnDefType.UNDEF : toState(s.getTimeLeft(), channel);
            case CHANNEL_ID_ECLIPSE_TOTAL:
                return toState(sun.getEclipseSet().getDate(EclipseKind.TOTAL), channel);
            case CHANNEL_ID_ECLIPSE_TOTAL_ELEVATION:
                return toState(sun.getEclipseSet().getElevation(EclipseKind.TOTAL), channel);
            case CHANNEL_ID_ECLIPSE_PARTIAL:
                return toState(sun.getEclipseSet().getDate(EclipseKind.PARTIAL), channel);
            case CHANNEL_ID_ECLIPSE_PARTIAL_ELEVATION:
                return toState(sun.getEclipseSet().getElevation(EclipseKind.PARTIAL), channel);
            case CHANNEL_ID_SUN_ECLIPSE_RING:
                return toState(sun.getEclipseSet().getDate(EclipseKind.RING), channel);
            case CHANNEL_ID_SUN_ECLIPSE_RING_ELEVATION:
                return toState(sun.getEclipseSet().getElevation(EclipseKind.RING), channel);
            case CHANNEL_ID_PHASE_NAME:
                return toState(sun.getSunPhase(), channel);
            case CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS:
                return toState(sun.getCircadian().getBrightness(), channel);
            case CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE:
                return toState(sun.getCircadian().getTemperature(), channel);
            default:
                logger.warn("Unsupported channel: {}", channel.getUID());
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected Job getDailyJob(TimeZone zone, Locale locale) {
        return new DailyJobSun(this, zone, locale, instantSource);
    }

    @Override
    public Sun getPlanetAt(ZonedDateTime date, Locale locale) {
        double latitude = thingConfig.latitude instanceof Double value ? value : 0;
        double longitude = thingConfig.longitude instanceof Double value ? value : 0;
        double altitude = thingConfig.altitude instanceof Double value ? value : 0;
        return sunCalc.getSunInfo(GregorianCalendar.from(date), latitude, longitude, altitude,
                thingConfig.useMeteorologicalSeason, TimeZone.getTimeZone(timeZoneProvider.getTimeZone()), Locale.ROOT);
    }

    private Sun getPositionedSunAt(ZonedDateTime date) {
        Sun localSun = getPlanetAt(date, Locale.ROOT);
        double latitude = thingConfig.latitude instanceof Double value ? value : 0;
        double longitude = thingConfig.longitude instanceof Double value ? value : 0;
        sunCalc.setPositionalInfo(GregorianCalendar.from(date), latitude, longitude, localSun);
        return localSun;
    }

    public @Nullable ZonedDateTime getEventTime(SunPhase sunPhase, ZonedDateTime date, boolean begin) {
        Range eventRange = getPlanetAt(date, Locale.ROOT).getAllRanges().get(sunPhase);
        if (eventRange != null) {
            Calendar cal = begin ? eventRange.getStart() : eventRange.getEnd();
            return cal == null ? null : ZonedDateTime.ofInstant(cal.toInstant(), date.getZone());
        }
        return null;
    }

    @Override
    public Position getPositionAt(ZonedDateTime date) {
        return getPositionedSunAt(date).getPosition();
    }

    public @Nullable Radiation getRadiationAt(ZonedDateTime date) {
        Sun localSun = getPositionedSunAt(date);
        return RadiationCalc.calculate(date, localSun.getPosition().getElevationAsDouble(), thingConfig.altitude);
    }
}
