/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sagercaster.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SagerCasterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SagerCasterBindingConstants {

    public static final String BINDING_ID = "sagercaster";
    public static final String LOCAL = "local";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAGERCASTER = new ThingTypeUID(BINDING_ID, "sagercaster");

    // Configuration elements
    public static final String CONFIG_LOCATION = "location";
    public static final String CONFIG_PERIOD = "observation-period";

    // List of all Channel Groups Group Channel ids
    public static final String GROUP_OUTPUT = "output";

    // Output channel ids
    public static final String CHANNEL_FORECAST = "forecast";
    public static final String CHANNEL_VELOCITY = "velocity";
    public static final String CHANNEL_VELOCITY_BEAUFORT = "velocity-beaufort";
    public static final String CHANNEL_WINDFROM = "wind-from";
    public static final String CHANNEL_WINDTO = "wind-to";
    public static final String CHANNEL_WINDEVOLUTION = "wind-evolution";
    public static final String CHANNEL_PRESSURETREND = "pressure-trend";
    public static final String CHANNEL_TEMPERATURETREND = "temperature-trend";
    public static final String CHANNEL_TIMESTAMP = "timestamp";

    // Input channel ids
    public static final String CHANNEL_CLOUDINESS = "cloudiness";
    public static final String CHANNEL_IS_RAINING = "is-raining";
    public static final String CHANNEL_RAIN_QTTY = "rain-qtty";
    public static final String CHANNEL_WIND_SPEED = "wind-speed-beaufort";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_WIND_ANGLE = "wind-angle";

    // Some algorythms constants
    public static final String FORECAST_PENDING = "0";
    public static final Set<String> SHOWERS = Set.of("G", "K", "L", "R", "S", "T", "U", "W");
}
