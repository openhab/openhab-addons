/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal;

import java.io.File;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RoborockBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class RoborockBindingConstants {

    private static final String BINDING_ID = "roborock";

    // List of all Thing Type UIDs
    public static final ThingTypeUID ROBOROCK_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID ROBOROCK_VACUUM = new ThingTypeUID(BINDING_ID, "vacuum");

    // The device definitions file name
    public static final String FILENAME_LOGINDATA = OpenHAB.getUserDataFolder() + File.separator + "roborock"
            + File.separator + "login.json";
    public static final String FILENAME_HOMEDETAIL = OpenHAB.getUserDataFolder() + File.separator + "roborock"
            + File.separator + "homedetail.json";

    // List of all Channel ids
    public static final String CHANNEL_BATTERY = "status#battery";
    public static final String CHANNEL_CLEAN_AREA = "status#clean_area";
    public static final String CHANNEL_CLEAN_TIME = "status#clean_time";
    public static final String CHANNEL_DND_ENABLED = "status#dnd_enabled";
    public static final String CHANNEL_ERROR_CODE = "status#error_code";
    public static final String CHANNEL_ERROR_ID = "status#error_id";
    public static final String CHANNEL_FAN_POWER = "status#fan_power";
    public static final String CHANNEL_IN_CLEANING = "status#in_cleaning";
    public static final String CHANNEL_MAP_PRESENT = "status#map_present";
    public static final String CHANNEL_STATE = "status#state";
    public static final String CHANNEL_STATE_ID = "status#state_id";
    public static final String CHANNEL_DOCK_STATE = "status#dock_state";
    public static final String CHANNEL_DOCK_STATE_ID = "status#dock_state_id";
    public static final String CHANNEL_MOP_DRYING = "status#is_mop_drying";
    public static final String CHANNEL_MOP_TOTAL_DRYTIME = "status#mop_drying_time";

    public static final String CHANNEL_CONTROL = "actions#control";
    public static final String CHANNEL_COMMAND = "actions#commands";
    public static final String CHANNEL_RPC = "actions#rpc";
    public static final String CHANNEL_VACUUM = "actions#vacuum";
    public static final String CHANNEL_FAN_CONTROL = "actions#fan";
    public static final String CHANNEL_TESTCOMMANDS = "actions#testcommands";
    public static final String CHANNEL_TESTMIOT = "actions#testmiot";
    public static final String CHANNEL_POWER = "actions#power";

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
    public static final String CHANNEL_CONSUMABLE_RESET = "consumables#consumable_reset";

    public static final String CHANNEL_DND_FUNCTION = "dnd#dnd_function";
    public static final String CHANNEL_DND_START = "dnd#dnd_start";
    public static final String CHANNEL_DND_END = "dnd#dnd_end";

    public static final String CHANNEL_HISTORY_TOTALTIME = "history#total_clean_time";
    public static final String CHANNEL_HISTORY_TOTALAREA = "history#total_clean_area";
    public static final String CHANNEL_HISTORY_COUNT = "history#total_clean_count";

    public static final String CHANNEL_MOP_TOTALDRYTIME = "status#mop_drying_time";

    public static final String CHANNEL_HISTORY_START_TIME = "cleaning#last_clean_start_time";
    public static final String CHANNEL_HISTORY_END_TIME = "cleaning#last_clean_end_time";
    public static final String CHANNEL_HISTORY_AREA = "cleaning#last_clean_area";
    public static final String CHANNEL_HISTORY_DURATION = "cleaning#last_clean_duration";
    public static final String CHANNEL_HISTORY_ERROR = "cleaning#last_clean_error";
    public static final String CHANNEL_HISTORY_FINISH = "cleaning#last_clean_finish";
    public static final String CHANNEL_HISTORY_FINISHREASON = "cleaning#last_clean_finish_reason";
    public static final String CHANNEL_HISTORY_DUSTCOLLECTION = "cleaning#last_clean_dustcollection_status";
    public static final String CHANNEL_HISTORY_RECORD = "cleaning#last_clean_record";
    public static final String CHANNEL_VACUUM_MAP = "cleaning#map";

    public static final String COMMAND_GET_STATUS = "get_status";
    public static final String COMMAND_GET_CONSUMABLE = "get_consumable";
    public static final String COMMAND_APP_PAUSE = "app_pause";
    public static final String COMMAND_APP_SPOT = "app_spot";
    public static final String COMMAND_APP_START = "app_start";
    public static final String COMMAND_APP_START_BUILD_MAP = "app_start_build_map";
    public static final String COMMAND_APP_START_COLLECT_DUST = "app_start_collect_dust";
    public static final String COMMAND_APP_START_EASTER_EGG = "app_start_easter_egg";
    public static final String COMMAND_APP_START_PATROL = "app_start_patrol";
    public static final String COMMAND_APP_START_PET_PATROL = "app_start_pet_patrol";
    public static final String COMMAND_APP_START_WASH = "app_start_wash";
    public static final String COMMAND_APP_STAT = "app_stat";
    public static final String COMMAND_APP_STOP = "app_stop";
    public static final String COMMAND_APP_STOP_COLLECT_DUST = "app_stop_collect_dust";
    public static final String COMMAND_APP_STOP_WASH = "app_stop_wash";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(ROBOROCK_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(ROBOROCK_ACCOUNT, ROBOROCK_VACUUM);
}
