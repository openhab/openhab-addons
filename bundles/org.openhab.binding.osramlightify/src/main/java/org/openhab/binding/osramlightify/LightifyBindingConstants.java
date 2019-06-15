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
package org.openhab.binding.osramlightify;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyBindingConstants {

    private LightifyBindingConstants() {
    }

    private static final String BINDING_ID = "osramlightify";

    // Probed temperature range is clipped to this due to gateway/wifi
    // firmware 1.2.2.0 permitting the full 0-65535. The prior firmware
    // gave 1501-8000.
    // N.B. Earlier firmware still gave the advertised range. The 1501-8000
    // range uses the colour LEDs to extend the range.
    public static final int MIN_TEMPERATURE = 1501;
    public static final int MAX_TEMPERATURE = 8000;

    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_DIMMABLE = 0x01;
    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_TUNABLE = 0x02;
    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_SOFT_SWITCHABLE = 0x04;
    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_RGB = 0x08;
    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW = 0x0A;
    public static final int LIGHTIFY_DEVICE_TYPE_POWER = 0x10;
    public static final int LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR = 0x20;
    public static final int LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG = 0x40;
    public static final int LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG = 0x41;

    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_EFFECT = "effect";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_ABS_TEMPERATURE = "absTemperature";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_TRIGGERED = "triggered";
    public static final String CHANNEL_BATTERY = "battery";

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_ALLPAIRED = new ThingTypeUID(BINDING_ID, "allpaired");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GROUP = new ThingTypeUID(BINDING_ID, "group");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE = new ThingTypeUID(BINDING_ID, "dimmable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_POWER = new ThingTypeUID(BINDING_ID, "power");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_TUNABLE = new ThingTypeUID(BINDING_ID, "tunable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_RGB = new ThingTypeUID(BINDING_ID, "rgb");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_RGBW = new ThingTypeUID(BINDING_ID, "rgbw");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motionsensor");

    public static final String PROPERTY_CURRENT_ADDRESS = "address";
    public static final String PROPERTY_MINIMUM_WHITE_TEMPERATURE = "minWhiteTemperature";
    public static final String PROPERTY_MAXIMUM_WHITE_TEMPERATURE = "maxWhiteTemperature";
    public static final String PROPERTY_IEEE_ADDRESS = "IEEEaddress";

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(
            THING_TYPE_LIGHTIFY_GATEWAY
        ).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(
            THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE,
            THING_TYPE_LIGHTIFY_LIGHT_RGB, THING_TYPE_LIGHTIFY_LIGHT_RGBW,
            THING_TYPE_LIGHTIFY_POWER
        ).collect(Collectors.toSet()));

    @SuppressWarnings("serial")
    public static final Map<Integer, ThingTypeUID> DEVICE_TYPE_THING_TYPE_UID_MAP = Collections
            .unmodifiableMap(new HashMap<Integer, ThingTypeUID>() {{
                put(LIGHTIFY_DEVICE_TYPE_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE);
                put(LIGHTIFY_DEVICE_TYPE_LIGHT_TUNABLE, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE);
                put(LIGHTIFY_DEVICE_TYPE_LIGHT_SOFT_SWITCHABLE, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE);
                put(LIGHTIFY_DEVICE_TYPE_LIGHT_RGB, THING_TYPE_LIGHTIFY_LIGHT_RGB);
                put(LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW, THING_TYPE_LIGHTIFY_LIGHT_RGBW);
                put(LIGHTIFY_DEVICE_TYPE_POWER, THING_TYPE_LIGHTIFY_POWER);
                put(LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR, THING_TYPE_LIGHTIFY_MOTION_SENSOR);
//              put(LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG, THING_TYPE_LIGHTIFY_SWITCH_2GANG);
//              put(LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG, THING_TYPE_LIGHTIFY_SWITCH_4GANG);
            }});
}
