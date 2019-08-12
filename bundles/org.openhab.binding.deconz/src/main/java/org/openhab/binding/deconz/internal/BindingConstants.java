/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    public static final String BINDING_ID = "deconz";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "deconz");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "presencesensor");
    public static final ThingTypeUID THING_TYPE_POWER_SENSOR = new ThingTypeUID(BINDING_ID, "powersensor");
    public static final ThingTypeUID THING_TYPE_CONSUMPTION_SENSOR = new ThingTypeUID(BINDING_ID, "consumptionsensor");
    public static final ThingTypeUID THING_TYPE_DAYLIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "daylightsensor");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "lightsensor");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "temperaturesensor");
    public static final ThingTypeUID THING_TYPE_HUMIDITY_SENSOR = new ThingTypeUID(BINDING_ID, "humiditysensor");
    public static final ThingTypeUID THING_TYPE_PRESSURE_SENSOR = new ThingTypeUID(BINDING_ID, "pressuresensor");
    public static final ThingTypeUID THING_TYPE_OPENCLOSE_SENSOR = new ThingTypeUID(BINDING_ID, "openclosesensor");
    public static final ThingTypeUID THING_TYPE_WATERLEAKAGE_SENSOR = new ThingTypeUID(BINDING_ID,
            "waterleakagesensor");
    public static final ThingTypeUID THING_TYPE_ALARM_SENSOR = new ThingTypeUID(BINDING_ID, "alarmsensor");
    public static final ThingTypeUID THING_TYPE_VIBRATION_SENSOR = new ThingTypeUID(BINDING_ID, "vibrationsensor");

    // List of all Channel ids
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_LAST_UPDATED = "last_updated";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_LIGHT_LUX = "lightlux";
    public static final String CHANNEL_LIGHT_LEVEL = "light_level";
    public static final String CHANNEL_DARK = "dark";
    public static final String CHANNEL_DAYLIGHT = "daylight";
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_BUTTONEVENT = "buttonevent";
    public static final String CHANNEL_OPENCLOSE = "open";
    public static final String CHANNEL_WATERLEAKAGE = "waterleakage";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_TAMPERED = "tampered";
    public static final String CHANNEL_VIBRATION = "vibration";
    public static final String CHANNEL_BATTERY_LEVEL = "battery_level";
    public static final String CHANNEL_BATTERY_LOW = "battery_low";

    // Thing configuration
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_APIKEY = "apikey";

    public static final String UNIQUE_ID = "uid";

    public static String url(String host, @Nullable String apikey, @Nullable String endpointType,
            @Nullable String endpointID) {
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(host);
        url.append("/api/");
        if (apikey != null) {
            url.append(apikey);
        }
        if (endpointType != null) {
            url.append("/");
            url.append(endpointType);
            url.append("/");
        }
        if (endpointID != null) {
            url.append(endpointID);
        }
        return url.toString();
    }
}
