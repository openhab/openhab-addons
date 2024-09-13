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
package org.openhab.binding.astro.internal.handler;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.calc.SunCalc;
import org.openhab.binding.astro.internal.job.DailyJobSun;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.binding.astro.internal.model.Radiation;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;

/**
 * The SunHandler is responsible for updating calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public class SunHandler extends AstroThingHandler {

    private final String[] positionalChannelIds = new String[] { "position#azimuth", "position#elevation",
            "radiation#direct", "radiation#diffuse", "radiation#total" };
    private final SunCalc sunCalc = new SunCalc();
    private @NonNullByDefault({}) Sun sun;

    /**
     * Constructor
     */
    public SunHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider) {
        super(thing, scheduler, timeZoneProvider);
    }

    @Override
    public void publishPositionalInfo() {
        sun = getSunAt(ZonedDateTime.now());
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        Double altitude = thingConfig.altitude;
        sunCalc.setPositionalInfo(Calendar.getInstance(), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, altitude != null ? altitude : 0, sun);

        sun.getEclipse().setElevations(this, timeZoneProvider);

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
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

    @Override
    protected Job getDailyJob() {
        return new DailyJobSun(thing.getUID().getAsString(), this);
    }

    private Sun getSunAt(ZonedDateTime date) {
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        Double altitude = thingConfig.altitude;
        return sunCalc.getSunInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, altitude != null ? altitude : 0,
                thingConfig.useMeteorologicalSeason);
    }

    private Sun getPositionedSunAt(ZonedDateTime date) {
        Sun localSun = getSunAt(date);
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        Double altitude = thingConfig.altitude;
        sunCalc.setPositionalInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, altitude != null ? altitude : 0, localSun);
        return localSun;
    }

    public @Nullable ZonedDateTime getEventTime(SunPhaseName sunPhase, ZonedDateTime date, boolean begin) {
        Range eventRange = getSunAt(date).getAllRanges().get(sunPhase);
        if (eventRange != null) {
            Calendar cal = begin ? eventRange.getStart() : eventRange.getEnd();
            return ZonedDateTime.ofInstant(cal.toInstant(), date.getZone());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Position getPositionAt(ZonedDateTime date) {
        Sun localSun = getPositionedSunAt(date);
        return localSun.getPosition();
    }

    public @Nullable Radiation getRadiationAt(ZonedDateTime date) {
        Sun localSun = getPositionedSunAt(date);
        return localSun.getRadiation();
    }
}
