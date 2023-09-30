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
package org.openhab.binding.meteostick.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MeteostickBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class MeteostickBindingConstants {

    public static final String BINDING_ID = "meteostick";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "meteostick_bridge");
    public static final ThingTypeUID THING_TYPE_DAVIS = new ThingTypeUID(BINDING_ID, "meteostick_davis_iss");

    // List of all Channel ids
    public static final String CHANNEL_INDOOR_TEMPERATURE = "indoor-temperature";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_RAIN_RAW = "rain-raw";
    public static final String CHANNEL_RAIN_CURRENTHOUR = "rain-currenthour";
    public static final String CHANNEL_RAIN_LASTHOUR = "rain-lasthour";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_WIND_SPEED_LAST2MIN_AVERAGE = "wind-speed-last2min-average";
    public static final String CHANNEL_WIND_SPEED_LAST2MIN_MAXIMUM = "wind-speed-last2min-maximum";
    public static final String CHANNEL_WIND_DIRECTION_LAST2MIN_AVERAGE = "wind-direction-last2min-average";
    public static final String CHANNEL_SOLAR_POWER = "solar-power";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";

    // List of parameters
    public static final String PARAMETER_CHANNEL = "channel";
    public static final String PARAMETER_SPOON = "spoon";
    public static final String PARAMETER_SPOON_DEFAULT = "0.254";

    // Miscellaneous constants
    public static final long HOUR_IN_SEC = 60 * 60;
    public static final long HOUR_IN_MSEC = HOUR_IN_SEC * 1000;
}
