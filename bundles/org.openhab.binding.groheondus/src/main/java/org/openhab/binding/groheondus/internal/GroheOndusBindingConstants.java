/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.groheondus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusBindingConstants {

    private static final String BINDING_ID = "groheondus";

    public static final ThingTypeUID THING_TYPE_BRIDGE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_SENSEGUARD = new ThingTypeUID(BINDING_ID, "senseguard");
    public static final ThingTypeUID THING_TYPE_SENSE = new ThingTypeUID(BINDING_ID, "sense");

    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_TEMPERATURE_GUARD = "temperature_guard";
    public static final String CHANNEL_VALVE_OPEN = "valve_open";
    public static final String CHANNEL_WATERCONSUMPTION = "waterconsumption";
    public static final String CHANNEL_WATERCONSUMPTION_SINCE_MIDNIGHT = "waterconsumption_since_midnight";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_BATTERY = "battery";

    public static final String CHANNEL_CONFIG_TIMEFRAME = "timeframe";
}
