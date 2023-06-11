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
package org.openhab.binding.mystrom.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyStromBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Frederic Chastagnol - Add constants for myStrom bulb support
 */
@NonNullByDefault
public class MyStromBindingConstants {

    public static final int DEFAULT_REFRESH_RATE_SECONDS = 10;
    public static final int DEFAULT_BACKOFF_TIME_SECONDS = 10;

    private static final String BINDING_ID = "mystrom";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PLUG = new ThingTypeUID(BINDING_ID, "mystromplug");
    public static final ThingTypeUID THING_TYPE_BULB = new ThingTypeUID(BINDING_ID, "mystrombulb");
    public static final ThingTypeUID THING_TYPE_PIR = new ThingTypeUID(BINDING_ID, "mystrompir");

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_RAMP = "ramp";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_LIGHT = "light";

    // Config
    public static final String CONFIG_MAC = "mac";

    // List of all Properties
    public static final String PROPERTY_MAC = "mac";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_SSID = "ssid";
    public static final String PROPERTY_IP = "ip";
    public static final String PROPERTY_MASK = "mask";
    public static final String PROPERTY_GW = "gw";
    public static final String PROPERTY_DNS = "dns";
    public static final String PROPERTY_STATIC = "static";
    public static final String PROPERTY_CONNECTED = "connected";
    public static final String PROPERTY_LAST_REFRESH = "lastRefresh";

    // myStrom Bulb modes
    public static final String RGB = "rgb";
    public static final String HSV = "hsv";
    public static final String MONO = "mono";
}
