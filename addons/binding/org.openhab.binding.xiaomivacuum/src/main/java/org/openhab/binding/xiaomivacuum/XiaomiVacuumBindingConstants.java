/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link XiaomiVacuumBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public final class XiaomiVacuumBindingConstants {

    public static final String BINDING_ID = "xiaomivacuum";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MIIO = new ThingTypeUID(BINDING_ID, "generic");
    public static final ThingTypeUID THING_TYPE_VACUUM = new ThingTypeUID(BINDING_ID, "vacuum");
    public static final ThingTypeUID THING_TYPE_UNSUPPORTED = new ThingTypeUID(BINDING_ID, "unsupported");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_VACUUM,
            THING_TYPE_MIIO);

    // List of all Channel IDs
    public static final String CHANNEL_BATTERY = "status#battery";
    public static final String CHANNEL_CLEAN_AREA = "status#clean_area";
    public static final String CHANNEL_CLEAN_TIME = "status#clean_time";
    public static final String CHANNEL_DND_ENABLED = "status#dnd_enabled";
    public static final String CHANNEL_ERROR_CODE = "status#error_code";
    public static final String CHANNEL_FAN_POWER = "status#fan_power";
    public static final String CHANNEL_IN_CLEANING = "status#in_cleaning";
    public static final String CHANNEL_MAP_PRESENT = "status#map_present";
    public static final String CHANNEL_STATE = "status#state";

    public static final String CHANNEL_CONTROL = "actions#control";
    public static final String CHANNEL_COMMAND = "actions#commands";
    public static final String CHANNEL_FAN_CONTROL = "actions#fan";

    public static final String CHANNEL_SSID = "network#ssid";
    public static final String CHANNEL_BSSID = "network#bssid";
    public static final String CHANNEL_RSSI = "network#rssi";
    public static final String CHANNEL_LIFE = "network#life";

    public static final String CHANNEL_CONSUMABLE_MAIN_PERC = "consumables#main_brush_percent";
    public static final String CHANNEL_CONSUMABLE_SIDE_PERC = "consumables#side_brush_percent";
    public static final String CHANNEL_CONSUMABLE_FILTER_PERC = "consumables#filter_percent";
    public static final String CHANNEL_CONSUMABLE_SENSOR_PERC = "consumables#sensor_dirt_percent";
    public static final String CHANNEL_CONSUMABLE_MAIN_TIME = "consumables#main_brush_time";
    public static final String CHANNEL_CONSUMABLE_SIDE_TIME = "consumables#side_brush_time";
    public static final String CHANNEL_CONSUMABLE_FILTER_TIME = "consumables#filter_time";
    public static final String CHANNEL_CONSUMABLE_SENSOR_TIME = "consumables#sensor_dirt_time";

    public static final String CHANNEL_DND_FUNCTION = "dnd#dnd_function";
    public static final String CHANNEL_DND_START = "dnd#dnd_start";
    public static final String CHANNEL_DND_END = "dnd#dnd_end";

    public static final String CHANNEL_HISTORY_TOTALTIME = "history#total_clean_time";
    public static final String CHANNEL_HISTORY_TOTALAREA = "history#total_clean_area";
    public static final String CHANNEL_HISTORY_COUNT = "history#total_clean_count";

    public static final String PROPERTY_HOST_IP = "host";
    public static final String PROPERTY_DID = "deviceId";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";

    public static final byte[] DISCOVER_STRING = org.openhab.binding.xiaomivacuum.internal.Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    public static final int PORT = 54321;
}
