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
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.job.DailyJobMoon;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Position;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;

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
    private final MoonCalc moonCalc = new MoonCalc();
    private @NonNullByDefault({}) Moon moon;

    /**
     * Constructor
     */
    public MoonHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider) {
        super(thing, scheduler, timeZoneProvider);
    }

    @Override
    public void publishPositionalInfo() {
        moon = getMoonAt(ZonedDateTime.now());
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        moonCalc.setPositionalInfo(Calendar.getInstance(), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, moon);

        moon.getEclipse().setElevations(this, timeZoneProvider);

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
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

    @Override
    protected Job getDailyJob() {
        return new DailyJobMoon(thing.getUID().getAsString(), this);
    }

    private Moon getMoonAt(ZonedDateTime date) {
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        return moonCalc.getMoonInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0);
    }

    @Override
    public @Nullable Position getPositionAt(ZonedDateTime date) {
        Moon localMoon = getMoonAt(date);
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        moonCalc.setPositionalInfo(GregorianCalendar.from(date), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, localMoon);
        return localMoon.getPosition();
    }
}
