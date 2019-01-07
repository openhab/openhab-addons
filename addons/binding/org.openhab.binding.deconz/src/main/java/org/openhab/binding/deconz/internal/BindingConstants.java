/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Graeff - Initial contribution
 */
public class BindingConstants {

    private static final String BINDING_ID = "deconz";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID(BINDING_ID, "deconz");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "presencesensor");
    public static final ThingTypeUID THING_TYPE_POWER_SENSOR = new ThingTypeUID(BINDING_ID, "powersensor");
    public static final ThingTypeUID THING_TYPE_DAYLIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "daylightsensor");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_LIGHT_SENSOR = new ThingTypeUID(BINDING_ID, "lightsensor");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE_SENSOR = new ThingTypeUID(BINDING_ID, "temperaturesensor");
    public static final ThingTypeUID THING_TYPE_HUMIDITY_SENSOR = new ThingTypeUID(BINDING_ID, "humiditysensor");
    public static final ThingTypeUID THING_TYPE_OPENCLOSE_SENSOR = new ThingTypeUID(BINDING_ID, "openclosesensor");

    // List of all Channel ids
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_VALUE = "value";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_DAYLIGHT = "light";
    public static final String CHANNEL_LIGHT_LUX = "lightlux";
    public static final String CHANNEL_BUTTON = "button";
    public static final String CHANNEL_BUTTONEVENT = "buttonevent";
    public static final String CHANNEL_OPENCLOSE = "open";

    // Thing configuration
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_APIKEY = "apikey";

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
