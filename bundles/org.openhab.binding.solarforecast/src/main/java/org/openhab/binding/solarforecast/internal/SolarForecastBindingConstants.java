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
package org.openhab.binding.solarforecast.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarForecastBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolarForecastBindingConstants {

    private static final String BINDING_ID = "solarforecast";

    public static final ThingTypeUID FORECAST_SOLAR_MULTI_STRING = new ThingTypeUID(BINDING_ID, "fs-site");
    public static final ThingTypeUID FORECAST_SOLAR_PART_STRING = new ThingTypeUID(BINDING_ID, "fs-plane");
    public static final ThingTypeUID SOLCAST_BRIDGE_STRING = new ThingTypeUID(BINDING_ID, "sc-site");
    public static final ThingTypeUID SOLCAST_PART_STRING = new ThingTypeUID(BINDING_ID, "sc-plane");
    public static final Set<ThingTypeUID> SUPPORTED_THING_SET = Set.of(FORECAST_SOLAR_MULTI_STRING,
            FORECAST_SOLAR_PART_STRING, SOLCAST_BRIDGE_STRING, SOLCAST_PART_STRING);

    public static final String CHANNEL_POWER_ACTUAL = "power-actual";
    public static final String CHANNEL_POWER_ESTIMATE = "power-estimate";
    public static final String CHANNEL_POWER_ESTIMATE10 = "power-estimate10";
    public static final String CHANNEL_POWER_ESTIMATE90 = "power-estimate90";
    public static final String CHANNEL_ENERGY_ACTUAL = "energy-actual";
    public static final String CHANNEL_ENERGY_REMAIN = "energy-remain";
    public static final String CHANNEL_ENERGY_TODAY = "energy-today";
    public static final String CHANNEL_ENERGY_ESTIMATE = "energy-estimate";
    public static final String CHANNEL_ENERGY_ESTIMATE10 = "energy-estimate10";
    public static final String CHANNEL_ENERGY_ESTIMATE90 = "energy-estimate90";
    public static final String CHANNEL_RAW = "raw";
    public static final int REFRESH_ACTUAL_INTERVAL = 1;

    public static final String SLASH = "/";
    public static final String EMPTY = "";
}
