/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meteostick;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link meteostickBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
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
    public static final String CHANNEL_SOLAR_POWER = "solar-power";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";

    // List of parameters
    public static final String PARAMETER_CHANNEL = "channel";
}
