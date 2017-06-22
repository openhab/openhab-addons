/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link XiaomiVacuumBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public final class XiaomiVacuumBindingConstants {

    public static final String BINDING_ID = "xiaomivacuum";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VACUUM = new ThingTypeUID(BINDING_ID, "vacuum");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_VACUUM);

    // List of all Channel IDs
    public static final String CHANNEL_BATTERY = "status#battery";
    public static final String CHANNEL_CLEAN_AREA = "status#clean_area";
    public static final String CHANNEL_CLEAN_TIME = "status#clean_time";
    public static final String CHANNEL_DND_ENABLED = "status#dnd_enabled";
    public static final String CHANNEL_ERROR_CODE = "status#error_code";
    public static final String CHANNEL_FAN_POWER = "status#fan_power";
    public static final String CHANNEL_IN_CLEANING = "status#in_cleaning";
    public static final String CHANNEL_MAP_PRESENT = "status#map_present";
    public static final String CHANNEL_MSG_SEQ = "status#msg_seq";
    public static final String CHANNEL_MSG_VER = "status#msg_ver";
    public static final String CHANNEL_STATE = "status#state";

    public static final String CHANNEL_VACUUM = "actions#vacuum";
    public static final String CHANNEL_SPOT = "actions#spot_clean";
    public static final String CHANNEL_RETURN = "actions#return";
    public static final String CHANNEL_PAUSE = "actions#pause";
    public static final String CHANNEL_COMMAND = "actions#commands";

    public static final String CHANNEL_CONSUMABLE_MAIN = "consumables#main_brush_time";
    public static final String CHANNEL_CONSUMABLE_SIDE = "consumables#side_brush_time";
    public static final String CHANNEL_CONSUMABLE_FILTER = "consumables#filter_time";
    public static final String CHANNEL_CONSUMABLE_SENSOR = "consumables#sensor_dirt_time";

    public static final String CHANNEL_DND_FUNCTION = "dnd#dnd_function";
    public static final String CHANNEL_DND_START = "dnd#dnd_start";
    public static final String CHANNEL_DND_END = "dnd#dnd_end";

    public static final String CHANNEL_HISTORY_TOTALTIME = "history#total_clean_time";
    public static final String CHANNEL_HISTORY_TOTALAREA = "history#total_clean_area";
    public static final String CHANNEL_HISTORY_COUNT = "history#total_clean_count";

    public static final String PROPERTY_HOST_IP = "host";
    public static final String PROPERTY_SERIAL = "serial";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";

    public static final byte[] DISCOVER_STRING = org.openhab.binding.xiaomivacuum.internal.Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    public static final int PORT = 54321;
}
