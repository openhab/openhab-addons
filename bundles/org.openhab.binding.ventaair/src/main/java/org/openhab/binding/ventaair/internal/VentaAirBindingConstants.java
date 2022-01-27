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
package org.openhab.binding.ventaair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VentaAirBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class VentaAirBindingConstants {

    private static final String BINDING_ID = "ventaair";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LW60T = new ThingTypeUID(BINDING_ID, "lw60t");
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_TARGET_HUMIDITY = "targetHumidity";
    public static final String CHANNEL_TIMER = "timer";
    public static final String CHANNEL_SLEEP_MODE = "sleepMode";
    public static final String CHANNEL_BOOST = "boost";
    public static final String CHANNEL_CHILD_LOCK = "childLock";
    public static final String CHANNEL_AUTOMATIC = "automatic";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PM25 = "pm25";
    public static final String CHANNEL_WATERLEVEL = "waterLevel";
    public static final String CHANNEL_FAN_RPM = "fanRPM";
    public static final String CHANNEL_CLEAN_MODE = "cleanMode";
    public static final String CHANNEL_OPERATION_TIME = "operationTime";
    public static final String CHANNEL_DISC_REPLACE_TIME = "discReplaceTime";
    public static final String CHANNEL_CLEANING_TIME = "cleaningTime";
    public static final String CHANNEL_TIMER_TIME_PASSED = "timerTimePassed";
    public static final String CHANNEL_SERVICE_TIME = "serviceTime";

    public static final int PORT = 48000;
}
