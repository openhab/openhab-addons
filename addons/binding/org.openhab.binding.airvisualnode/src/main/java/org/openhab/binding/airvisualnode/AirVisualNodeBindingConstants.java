/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airvisualnode;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AirVisualNodeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class AirVisualNodeBindingConstants {

    @NonNull public static final String BINDING_ID = "airvisualnode";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AVNODE = new ThingTypeUID(BINDING_ID, "avnode");

    // List of all Channel ids
    public static final String CHANNEL_CO2_PPM = "co2_ppm";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_AQI_CN = "aqi_cn";
    public static final String CHANNEL_AQI_US = "aqi_us";
    public static final String CHANNEL_PM_25 = "pm_25";
    public static final String CHANNEL_TEMP_CELSIUS = "temp_celsius";
    public static final String CHANNEL_TEMP_FAHRENHEIT = "temp_fahrenheit";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_WIFI_STRENGTH = "wifi_strength";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_USED_MEMORY = "used_memory";

    // List of all supported Thing UIDs
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_AVNODE);

    // List of all supported Channel ids
    public static final Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(CHANNEL_CO2_PPM, CHANNEL_HUMIDITY,
            CHANNEL_AQI_CN, CHANNEL_AQI_US, CHANNEL_PM_25, CHANNEL_TEMP_CELSIUS, CHANNEL_TEMP_FAHRENHEIT,
            CHANNEL_BATTERY_LEVEL, CHANNEL_WIFI_STRENGTH, CHANNEL_TIMESTAMP, CHANNEL_USED_MEMORY);
}
