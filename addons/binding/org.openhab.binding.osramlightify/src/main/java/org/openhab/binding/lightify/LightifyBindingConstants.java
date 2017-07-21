/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyBindingConstants {

    private LightifyBindingConstants() {
    }

    private static final String BINDING_ID = "osramlightify";

    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_ABS_TEMPERATURE = "absTemperature";

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GROUP = new ThingTypeUID(BINDING_ID, "group");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE = new ThingTypeUID(BINDING_ID, "dimmable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_POWER = new ThingTypeUID(BINDING_ID, "power");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_TUNABLE = new ThingTypeUID(BINDING_ID, "tunable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_RGBW = new ThingTypeUID(BINDING_ID, "rgbw");

    public static final String PROPERTY_CURRENT_ADDRESS = "Current address";
    public static final String PROPERTY_MINIMUM_WHITE_TEMPERATURE = "Minimum white temperature";
    public static final String PROPERTY_MAXIMUM_WHITE_TEMPERATURE = "Maximum white temperature";
    public static final String PROPERTY_WIFI_FIRMWARE_VERSION = "Wifi firmware version";
    public static final String PROPERTY_IEEE_ADDRESS = "IEEE address";

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(
        THING_TYPE_LIGHTIFY_GATEWAY
    );

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(
        THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE, THING_TYPE_LIGHTIFY_LIGHT_RGBW,
        THING_TYPE_LIGHTIFY_POWER,
        THING_TYPE_LIGHTIFY_GROUP
    );

    public static final Map<Integer, ThingTypeUID> DEVICE_TYPE_THING_TYPE_UID_MAP = ImmutableMap
        .<Integer, ThingTypeUID>builder()
        .put(0x01, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE) // not soft-switchable
        .put(0x02, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE)
        .put(0x04, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE) // soft-switchable
        .put(0x0A, THING_TYPE_LIGHTIFY_LIGHT_RGBW)
        .put(0x10, THING_TYPE_LIGHTIFY_POWER)
//      .put(0x20, THING_TYPE_LIGHTIFY_MOTION_SENSOR)
//      .put(0x40, THING_TYPE_LIGHTIFY_SWITCH_2GANG)
//      .put(0x41, THING_TYPE_LIGHTIFY_SWITCH_4GANG)
        .build();
}
