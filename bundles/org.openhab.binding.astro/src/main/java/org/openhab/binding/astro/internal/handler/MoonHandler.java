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
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.job.DailyJobMoon;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.model.DistanceType;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
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
 * The MoonHandler is responsible for updating calculated moon data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public class MoonHandler extends AstroThingHandler {

    private final String[] positionalChannelIds = new String[] { "phase#name", "phase#age", "phase#agePercent",
            "phase#ageDegree", "phase#illumination", "position#azimuth", "position#elevation", "zodiac#sign" };
    private final MoonCalc moonCalc;
    private final Logger logger = LoggerFactory.getLogger(MoonHandler.class);
    private volatile @Nullable Moon moon;

    /**
     * Constructor
     */
    public MoonHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider,
            LocaleProvider localeProvider, InstantSource instantSource) {
        super(thing, scheduler, timeZoneProvider, localeProvider, instantSource);
        moonCalc = new MoonCalc(instantSource);
    }

    @Override
    public void publishPositionalInfo() {
        ZoneId zoneId = timeZoneProvider.getTimeZone();
        TimeZone zone = TimeZone.getTimeZone(zoneId);
        Locale locale = localeProvider.getLocale();
        ZonedDateTime now = instantSource.instant().atZone(zone.toZoneId());
        Moon moon = getMoonAt(now, locale);
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
<<<<<<< Upstream, based on main
        moonCalc.setPositionalInfo(DateTimeUtils.calFromInstantSource(instantSource, zone, locale),
                latitude != null ? latitude : 0, longitude != null ? longitude : 0, moon, zone, locale);
=======
        moonCalc.setPositionalInfo(Calendar.getInstance(zone, locale), latitude != null ? latitude : 0,
<<<<<<< Upstream, based on main
                longitude != null ? longitude : 0, moon, zone, locale);
>>>>>>> 48a7069 Reworked sun and moon position Reworked eclipse calculations Transitioned these to Instant Added unit tests for eclipses
=======
                longitude != null ? longitude : 0, moon, zone);
>>>>>>> bb4de3d Starting to work on transition to Instant for MoonPhase
        this.moon = moon;

        publishPlanet();
    }

    @Override
    public @Nullable Planet getPlanet() {
        return moon;
    }

    @Override
    public void dispose() {
        super.dispose();
        moon = null;
    }

    @Override
    protected State getState(Channel channel) {
        Moon moon = this.moon;
        if (moon == null) {
            return UnDefType.UNDEF;
        }
        switch (channel.getUID().getId()) {
            case CHANNEL_ID_MOON_RISE_START:
                return toState(moon.getRise().getStart(), channel);
            case CHANNEL_ID_MOON_RISE_END:
                return toState(moon.getRise().getEnd(), channel);
            case CHANNEL_ID_MOON_RISE_DURATION:
                return toState(moon.getRise().getDuration(), channel);
            case CHANNEL_ID_MOON_SET_START:
                return toState(moon.getSet().getStart(), channel);
            case CHANNEL_ID_MOON_SET_END:
                return toState(moon.getSet().getEnd(), channel);
            case CHANNEL_ID_MOON_SET_DURATION:
                return toState(moon.getSet().getDuration(), channel);
            case CHANNEL_ID_MOON_PHASE_FIRST_QUARTER:
                return toState(moon.getPhase().getFirstQuarter(), channel);
            case CHANNEL_ID_MOON_PHASE_THIRD_QUARTER:
                return toState(moon.getPhase().getThirdQuarter(), channel);
            case CHANNEL_ID_MOON_PHASE_FULL:
                return toState(moon.getPhase().getFull(), channel);
            case CHANNEL_ID_MOON_PHASE_NEW:
                return toState(moon.getPhase().getNew(), channel);
            case CHANNEL_ID_MOON_PHASE_AGE:
                return toState(moon.getPhase().getAge(), channel);
            case CHANNEL_ID_MOON_PHASE_AGE_DEGREE:
                return toState(moon.getPhase().getAgeDegree(), channel);
            case CHANNEL_ID_MOON_PHASE_AGE_PERCENT:
                return toState(moon.getPhase().getAgePercent(), channel);
            case CHANNEL_ID_MOON_PHASE_ILLUMINATION:
                return toState(moon.getPhase().getIllumination(), channel);
            case CHANNEL_ID_MOON_PHASE_NAME:
                return toState(moon.getPhase().getName(), channel);
            case CHANNEL_ID_MOON_ECLIPSE_TOTAL:
                return toState(moon.getEclipseSet().getDate(EclipseKind.TOTAL), channel);
            case CHANNEL_ID_MOON_ECLIPSE_TOTAL_ELEVATION:
                return toState(moon.getEclipseSet().getElevation(EclipseKind.TOTAL), channel);
            case CHANNEL_ID_MOON_ECLIPSE_PARTIAL:
                return toState(moon.getEclipseSet().getDate(EclipseKind.PARTIAL), channel);
            case CHANNEL_ID_MOON_ECLIPSE_PARTIAL_ELEVATION:
                return toState(moon.getEclipseSet().getElevation(EclipseKind.PARTIAL), channel);
            case CHANNEL_ID_MOON_DISTANCE_DATE:
                return toState(moon.getDistanceType(DistanceType.CURRENT).getDate(), channel);
            case CHANNEL_ID_MOON_DISTANCE_DISTANCE:
                return toState(moon.getDistanceType(DistanceType.CURRENT).getDistance(), channel);
            case CHANNEL_ID_MOON_PERIGEE_DATE:
                return toState(moon.getDistanceType(DistanceType.PERIGEE).getDate(), channel);
            case CHANNEL_ID_MOON_PERIGEE_DISTANCE:
                return toState(moon.getDistanceType(DistanceType.PERIGEE).getDistance(), channel);
            case CHANNEL_ID_MOON_APOGEE_DATE:
                return toState(moon.getDistanceType(DistanceType.APOGEE).getDate(), channel);
            case CHANNEL_ID_MOON_APOGEE_DISTANCE:
                return toState(moon.getDistanceType(DistanceType.APOGEE).getDistance(), channel);
            case CHANNEL_ID_MOON_POSITION_AZIMUTH:
                return toState(moon.getPosition().getAzimuth(), channel);
            case CHANNEL_ID_MOON_POSITION_ELEVATION:
                return toState(moon.getPosition().getElevation(), channel);
            case CHANNEL_ID_MOON_POSITION_SHADE_LENGTH:
                return toState(moon.getPosition().getShadeLength(), channel);
            case CHANNEL_ID_MOON_ZODIAC_SIGN:
                return toState(moon.getZodiac().getSign(), channel);
            default:
                logger.warn("Unsupported channel: {}", channel.getUID());
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

    @Override
    protected Job getDailyJob(TimeZone zone, Locale locale) {
        return new DailyJobMoon(this, zone, locale);
    }

    private Moon getMoonAt(ZonedDateTime date, Locale locale) {
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        return moonCalc.getMoonInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, TimeZone.getTimeZone(date.getZone()), locale);
    }

    @Override
    public Position getPositionAt(ZonedDateTime date) {
        Moon localMoon = getMoonAt(date, Locale.ROOT);
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        moonCalc.setPositionalInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, localMoon, TimeZone.getTimeZone(date.getZone()));
        return localMoon.getPosition();
    }
}
