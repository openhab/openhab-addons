/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
    public static final int LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW = 0x0A;
    public static final int LIGHTIFY_DEVICE_TYPE_POWER = 0x10;
    public static final int LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR = 0x20;
    public static final int LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG = 0x40;
    public static final int LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG = 0x41;

    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_ABS_TEMPERATURE = "absTemperature";
    public static final String CHANNEL_ENABLED = "enabled";
    public static final String CHANNEL_TRIGGERED = "triggered";
    public static final String CHANNEL_BATTERY = "battery";

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GROUP = new ThingTypeUID(BINDING_ID, "group");

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE = new ThingTypeUID(BINDING_ID, "dimmable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_POWER = new ThingTypeUID(BINDING_ID, "power");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_TUNABLE = new ThingTypeUID(BINDING_ID, "tunable");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_LIGHT_RGBW = new ThingTypeUID(BINDING_ID, "rgbw");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_MOTION_SENSOR = new ThingTypeUID(BINDING_ID, "motionsensor");

    public static final String PROPERTY_CURRENT_ADDRESS = "Current address";
    public static final String PROPERTY_MINIMUM_WHITE_TEMPERATURE = "Minimum white temperature";
    public static final String PROPERTY_MAXIMUM_WHITE_TEMPERATURE = "Maximum white temperature";
    public static final String PROPERTY_IEEE_ADDRESS = "IEEE address";

    @SuppressWarnings("unchecked")
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(
        THING_TYPE_LIGHTIFY_GATEWAY
    );

    @SuppressWarnings("unchecked")
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(
        THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE, THING_TYPE_LIGHTIFY_LIGHT_RGBW,
        THING_TYPE_LIGHTIFY_POWER
    );

    @SuppressWarnings("unchecked")
    public static final Map<Integer, ThingTypeUID> DEVICE_TYPE_THING_TYPE_UID_MAP = ImmutableMap
        .<Integer, ThingTypeUID>builder()
        .put(LIGHTIFY_DEVICE_TYPE_LIGHT_DIMMABLE, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE)
        .put(LIGHTIFY_DEVICE_TYPE_LIGHT_TUNABLE, THING_TYPE_LIGHTIFY_LIGHT_TUNABLE)
        .put(LIGHTIFY_DEVICE_TYPE_LIGHT_SOFT_SWITCHABLE, THING_TYPE_LIGHTIFY_LIGHT_DIMMABLE)
        .put(LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW, THING_TYPE_LIGHTIFY_LIGHT_RGBW)
        .put(LIGHTIFY_DEVICE_TYPE_POWER, THING_TYPE_LIGHTIFY_POWER)
        .put(LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR, THING_TYPE_LIGHTIFY_MOTION_SENSOR)
//      .put(LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG, THING_TYPE_LIGHTIFY_SWITCH_2GANG)
//      .put(LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG, THING_TYPE_LIGHTIFY_SWITCH_4GANG)
        .build();
}
