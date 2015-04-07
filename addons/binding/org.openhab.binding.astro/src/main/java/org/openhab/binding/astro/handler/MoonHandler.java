/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.handler;

import static org.openhab.binding.astro.AstroBindingConstants.THING_TYPE_MOON;

import java.util.Calendar;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.model.Moon;
import org.openhab.binding.astro.internal.model.Planet;

import com.google.common.collect.Sets;

/**
 * The MoonHandler is responsible for updating calculated moon data.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class MoonHandler extends AstroThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_MOON);

    private String[] positionalChannelIds = new String[] { "phase#name", "phase#age", "phase#illumination", "position#azimuth", "position#elevation",
            "zodiac#sign" };
    private MoonCalc moonCalc = new MoonCalc();
    private Moon moon;

    public MoonHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDailyInfo() {
        moon = moonCalc.getMoonInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude());
        publishPositionalInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPositionalInfo() {
        moonCalc.setPositionalInfo(Calendar.getInstance(), thingConfig.getLatitude(), thingConfig.getLongitude(), moon);
        publishPlanet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Planet getPlanet() {
        return moon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
        moon = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getPositionalChannelIds() {
        return positionalChannelIds;
    }

}
