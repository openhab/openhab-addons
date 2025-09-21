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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
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

    public static final String SALT = "TXdfu$jyZ#TZHsg4";
    public static final int AES_BLOCK_SIZE = 16;

    // Constants used by RoborockWebTargets
    public static final int TIMEOUT_MS = 30000;
    public static final String EU_IOT_BASE_URL = "https://euiot.roborock.com";
    public static final String GET_URL_BY_EMAIL_URI = EU_IOT_BASE_URL + "/api/v1/getUrlByEmail";
    public static final String GET_TOKEN_PATH = "/api/v1/login";
    public static final String GET_HOME_DETAIL_PATH = "/api/v1/getHomeDetail";
    public static final String GET_HOME_DATA_PATH = "/user/homes/";
    public static final String GET_HOME_DATA_V3_PATH = "/v3/user/homes/";
    public static final String GET_ROUTINES_PATH = "/user/scene/device/";
    public static final String SET_ROUTINE_PATH = "/user/scene/";
    public static final String SET_ROUTINE_PATH_SUFFIX = "/execute";

    // Protocol constants
    public static final String MD5_ALGORITHM = "MD5";
    public static final String AES_ECB_PADDING = "AES/ECB/PKCS5Padding";
    public static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
    public static final String VERSION_1_0 = "1.0";
    public static final int SEQ_OFFSET = 3;
    public static final int RANDOM_OFFSET = 7;
    public static final int TIMESTAMP_OFFSET = 11;
    public static final int PROTOCOL_OFFSET = 15;
    public static final int PAYLOAD_OFFSET = 17;
    public static final int HEADER_LENGTH_WITHOUT_CRC = 19; // 3 (version) + 4 (seq) + 4 (random) + 4 (timestamp) + 2
                                                            // (protocol) + 2 (payloadLen)
    public static final int CRC_LENGTH = 4;

    // List of all Channel ids
    public static final String CHANNEL_BATTERY = "status#battery";
    public static final String CHANNEL_CLEAN_AREA = "status#clean-area";
    public static final String CHANNEL_CLEAN_TIME = "status#clean-time";
    public static final String CHANNEL_DND_ENABLED = "status#dnd-enabled";
    public static final String CHANNEL_ERROR_CODE = "status#error-code";
    public static final String CHANNEL_ERROR_ID = "status#error-id";
    public static final String CHANNEL_FAN_POWER = "status#fan-power";
    public static final String CHANNEL_IN_CLEANING = "status#in-cleaning";
    public static final String CHANNEL_MAP_PRESENT = "status#map-present";
    public static final String CHANNEL_STATE_ID = "status#state-id";
    public static final String CHANNEL_DOCK_STATE_ID = "status#dock-state-id";
    public static final String CHANNEL_MOP_DRYING = "status#is-mop-drying";
    public static final String CHANNEL_MOP_TOTAL_DRYTIME = "status#mop-drying-time";

    public static final String CHANNEL_ROUTINES = "info#routine-mapping";

    public static final String CHANNEL_CONTROL = "actions#control";
    public static final String CHANNEL_COMMAND = "actions#commands";
    public static final String CHANNEL_RPC = "actions#rpc";
    public static final String CHANNEL_VACUUM = "actions#vacuum";
    public static final String CHANNEL_FAN_CONTROL = "actions#fan";
    public static final String CHANNEL_TESTCOMMANDS = "actions#testcommands";
    public static final String CHANNEL_TESTMIOT = "actions#testmiot";
    public static final String CHANNEL_POWER = "actions#power";
    public static final String CHANNEL_ROUTINE = "actions#routine";

    public static final String CHANNEL_SSID = "network#ssid";
    public static final String CHANNEL_BSSID = "network#bssid";
    public static final String CHANNEL_RSSI = "network#rssi";
    public static final String CHANNEL_LIFE = "network#life";

    public static final String CHANNEL_CONSUMABLE_MAIN_PERC = "consumables#main-brush-percent";
    public static final String CHANNEL_CONSUMABLE_SIDE_PERC = "consumables#side-brush-percent";
    public static final String CHANNEL_CONSUMABLE_FILTER_PERC = "consumables#filter-percent";
    public static final String CHANNEL_CONSUMABLE_SENSOR_PERC = "consumables#sensor-dirt-percent";
    public static final String CHANNEL_CONSUMABLE_MAIN_TIME = "consumables#main-brush-time";
    public static final String CHANNEL_CONSUMABLE_SIDE_TIME = "consumables#side-brush-time";
    public static final String CHANNEL_CONSUMABLE_FILTER_TIME = "consumables#filter-time";
    public static final String CHANNEL_CONSUMABLE_SENSOR_TIME = "consumables#sensor-dirt-time";
    public static final String CHANNEL_CONSUMABLE_RESET = "consumables#consumable-reset";

    public static final String CHANNEL_DND_FUNCTION = "dnd#dnd-function";
    public static final String CHANNEL_DND_START = "dnd#dnd-start";
    public static final String CHANNEL_DND_END = "dnd#dnd-end";

    public static final String CHANNEL_HISTORY_TOTALTIME = "history#total-clean-time";
    public static final String CHANNEL_HISTORY_TOTALAREA = "history#total-clean-area";
    public static final String CHANNEL_HISTORY_COUNT = "history#total-clean-count";

    public static final String CHANNEL_HISTORY_START_TIME = "cleaning#last-clean-start-time";
    public static final String CHANNEL_HISTORY_END_TIME = "cleaning#last-clean-end-time";
    public static final String CHANNEL_HISTORY_AREA = "cleaning#last-clean-area";
    public static final String CHANNEL_HISTORY_DURATION = "cleaning#last-clean-duration";
    public static final String CHANNEL_HISTORY_ERROR = "cleaning#last-clean-error";
    public static final String CHANNEL_HISTORY_FINISH = "cleaning#last-clean-finish";
    public static final String CHANNEL_HISTORY_FINISHREASON = "cleaning#last-clean-finish-reason";
    public static final String CHANNEL_HISTORY_DUSTCOLLECTION = "cleaning#last-clean-dustcollection-status";
    public static final String CHANNEL_HISTORY_RECORD = "cleaning#last-clean-record";
    public static final String CHANNEL_VACUUM_MAP = "cleaning#map";

    public static final String COMMAND_GET_STATUS = "get_status";
    public static final String COMMAND_GET_CONSUMABLE = "get_consumable";
    public static final String COMMAND_APP_CHARGE = "app_charge";
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
    public static final String COMMAND_SET_MODE = "set_custom_mode";
    public static final String COMMAND_SET_WATERBOX_MODE = "set_water_box_custom_mode";
    public static final String COMMAND_SET_MOP_MODE = "set_mop_mode";
    public static final String COMMAND_START_SEGMENT = "app_segment_clean";
    public static final String COMMAND_CONSUMABLES_RESET = "reset_consumable";
    public static final String COMMAND_SET_COLLECT_DUST = "app_start_collect_dust";
    public static final String COMMAND_SET_CLEAN_MOP_START = "app_start_wash";
    public static final String COMMAND_SET_CLEAN_MOP_STOP = "app_stop_wash";
    public static final String COMMAND_GET_ROOM_MAPPING = "get_room_mapping";
    public static final String COMMAND_GET_NETWORK_INFO = "get_network_info";
    public static final String COMMAND_GET_CLEAN_RECORD = "get_clean_record";
    public static final String COMMAND_GET_CLEAN_SUMMARY = "get_clean_summary";
    public static final String COMMAND_GET_DND_TIMER = "get_dnd_timer";
    public static final String COMMAND_GET_SEGMENT_STATUS = "get_segment_status";
    public static final String COMMAND_GET_MAP_STATUS = "get_map_status";
    public static final String COMMAND_GET_LED_STATUS = "get_led_status";
    public static final String COMMAND_GET_CARPET_MODE = "get_carpet_mode";
    public static final String COMMAND_GET_FW_FEATURES = "get_fw_features";
    public static final String COMMAND_GET_MULTI_MAP_LIST = "get_multi_maps_list";
    public static final String COMMAND_GET_CUSTOMIZE_CLEAN_MODE = "get_customize_clean_mode";
    public static final String COMMAND_GET_MAP = "get_map_v1";

    public static final String THING_CONFIG_DUID = "duid";
    public static final String THING_PROPERTY_SN = "sn";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(ROBOROCK_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(ROBOROCK_ACCOUNT, ROBOROCK_VACUUM);
}
