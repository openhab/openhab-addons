/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public final class LightifyConstants {

    private LightifyConstants() {
    }

    private static final String BINDING_ID = "lightify";

    public static final ThingTypeUID THING_TYPE_LIGHTIFY_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_BULB_SB = new ThingTypeUID(BINDING_ID, "bulb-sb");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_BULB_TW = new ThingTypeUID(BINDING_ID, "bulb-tw");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_BULB_RGBW = new ThingTypeUID(BINDING_ID, "bulb-rgbw");
    public static final ThingTypeUID THING_TYPE_LIGHTIFY_ZONE = new ThingTypeUID(BINDING_ID, "zone");

    public static final String PROPERTY_ADDRESS = "ipAddress";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_DEVICE_NAME = "device-name";
    public static final String PROPERTY_DEVICE_ADDRESS = "device-address";
    public static final String PROPERTY_ZONE_ID = "zone-id";

    public static final String SB_CHANNEL_ID_POWER = "sb#power";
    public static final String SB_CHANNEL_ID_DIMMER = "sb#dimmer";

    public static final String TW_CHANNEL_ID_POWER = "tw#power";
    public static final String TW_CHANNEL_ID_TEMPERATURE = "tw#temperature";
    public static final String TW_CHANNEL_ID_DIMMER = "tw#dimmer";

    public static final String RGBW_CHANNEL_ID_POWER = "rgbw#power";
    public static final String RGBW_CHANNEL_ID_TEMPERATURE = "rgbw#temperature";
    public static final String RGBW_CHANNEL_ID_DIMMER = "rgbw#dimmer";
    public static final String RGBW_CHANNEL_ID_COLOR = "rgbw#color";

    public static final int BITMASK_DIMMING = 1 << 0;
    public static final int BITMASK_TUNABLE_WHITE = 1 << 1;
    public static final int BITMASK_PURE_WHITE = 1 << 2;
    public static final int BITMASK_RGB = 1 << 3;
    public static final int BITMASK_UNK2 = 1 << 4;
    public static final int BITMASK_MOTION_SENSOR = 1 << 5;
    public static final int BITMASK_SWITCHING = 1 << 6;
    public static final int BITMASK_UNK3 = 1 << 7;

    public static final int BITMASK_COMBO_RGBW = BITMASK_TUNABLE_WHITE | BITMASK_RGB;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_GATEWAY);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_BULB_TW);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_BULB_RGBW);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LIGHTIFY_ZONE);
    }

}
