/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
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
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "meteostick_bridge");
    public final static ThingTypeUID THING_TYPE_DAVIS = new ThingTypeUID(BINDING_ID, "meteostick_davis_iss");

    // List of all Channel ids
    public final static String CHANNEL_INDOOR_TEMPERATURE = "indoor_temperature";
    public final static String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor_temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_RAIN_RAW = "rain-raw";
    public final static String CHANNEL_RAIN_CURRENTHOUR = "rain-currenthour";
    public final static String CHANNEL_RAIN_LASTHOUR = "rain-lasthour";
    public final static String CHANNEL_WIND_SPEED = "wind-speed";
    public final static String CHANNEL_WIND_DIRECTION = "wind-direction";
    public final static String CHANNEL_SOLAR_POWER = "solar-power";
    public final static String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public final static String CHANNEL_LOW_BATTERY = "low-battery";

    // List of parameters
    public final static String PARAMETER_CHANNEL = "channel";
}
