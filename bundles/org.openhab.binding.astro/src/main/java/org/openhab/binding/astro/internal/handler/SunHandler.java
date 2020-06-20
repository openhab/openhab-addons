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
package org.openhab.binding.astro.internal.handler;

import static org.openhab.binding.astro.internal.AstroBindingConstants.THING_TYPE_SUN;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.astro.internal.calc.SunCalc;
import org.openhab.binding.astro.internal.job.DailyJobSun;
import org.openhab.binding.astro.internal.job.Job;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.model.Sun;

/**
 * The SunHandler is responsible for updating calculated sun data.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Implementation to be compliant with ESH Scheduler
 */
@NonNullByDefault
public class SunHandler extends AstroThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_SUN));

    private final String[] positionalChannelIds = new String[] { "position#azimuth", "position#elevation",
            "radiation#direct", "radiation#diffuse", "radiation#total" };
    private final SunCalc sunCalc = new SunCalc();
    private @Nullable Sun sun;

    /**
     * Constructor
     */
    public SunHandler(Thing thing, final CronScheduler scheduler, final TimeZoneProvider timeZoneProvider) {
        super(thing, scheduler, timeZoneProvider);
    }

    @Override
    public void publishDailyInfo() {
        initializeSun();
        publishPositionalInfo();
    }

    @Override
    public void publishPositionalInfo() {
        initializeSun();
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        sunCalc.setPositionalInfo(Calendar.getInstance(), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, thingConfig.altitude, sun);
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

    private void initializeSun() {
        Double latitude = thingConfig.latitude;
        Double longitude = thingConfig.longitude;
        sun = sunCalc.getSunInfo(Calendar.getInstance(), latitude != null ? latitude : 0,
                longitude != null ? longitude : 0, thingConfig.altitude, thingConfig.useMeteorologicalSeason);
    }
}
