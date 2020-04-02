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
package org.openhab.binding.sagercaster.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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

    // Output channel ids
    public static final String CHANNEL_FORECAST = "output#forecast";
    public static final String CHANNEL_VELOCITY = "output#velocity";
    public static final String CHANNEL_VELOCITY_BEAUFORT = "output#velocity-beaufort";
    public static final String CHANNEL_WINDFROM = "output#wind-from";
    public static final String CHANNEL_WINDTO = "output#wind-to";
    public static final String CHANNEL_WINDEVOLUTION = "output#wind-evolution";
    public static final String CHANNEL_PRESSURETREND = "output#pressure-trend";
    public static final String CHANNEL_TEMPERATURETREND = "output#temperature-trend";
    // Input channel ids
    public static final String CHANNEL_CLOUDINESS = "input#cloudiness";
    public static final String CHANNEL_IS_RAINING = "input#is-raining";
    public static final String CHANNEL_WIND_SPEED = "input#wind-speed-beaufort";
    public static final String CHANNEL_TEMPERATURE = "input#temperature";
    public static final String CHANNEL_PRESSURE = "input#pressure";
    public static final String CHANNEL_WIND_ANGLE = "input#wind-angle";
}
