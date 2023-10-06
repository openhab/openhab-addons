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
package org.openhab.binding.miio.internal;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MiIoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class MiIoBindingConstants {

    public static final String BINDING_ID = "miio";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MIIO = new ThingTypeUID(BINDING_ID, "generic");
    public static final ThingTypeUID THING_TYPE_BASIC = new ThingTypeUID(BINDING_ID, "basic");
    public static final ThingTypeUID THING_TYPE_LUMI = new ThingTypeUID(BINDING_ID, "lumi");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_VACUUM = new ThingTypeUID(BINDING_ID, "vacuum");
    public static final ThingTypeUID THING_TYPE_UNSUPPORTED = new ThingTypeUID(BINDING_ID, "unsupported");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MIIO, THING_TYPE_BASIC, THING_TYPE_LUMI, THING_TYPE_GATEWAY,
                    THING_TYPE_VACUUM, THING_TYPE_UNSUPPORTED).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> NONGENERIC_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_BASIC, THING_TYPE_LUMI, THING_TYPE_GATEWAY, THING_TYPE_VACUUM, THING_TYPE_UNSUPPORTED)
                    .collect(Collectors.toSet()));

    // List of all Channel IDs
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

    public static final String PROPERTY_HOST_IP = "host";
    public static final String PROPERTY_DID = "deviceId";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";
    public static final String PROPERTY_TIMEOUT = "timeout";
    public static final String PROPERTY_CLOUDSERVER = "cloudServer";

    public static final Set<String> PERSISTENT_CHANNELS = Collections.unmodifiableSet(
            Stream.of(CHANNEL_COMMAND, CHANNEL_RPC, CHANNEL_SSID, CHANNEL_BSSID, CHANNEL_RSSI, CHANNEL_LIFE)
                    .collect(Collectors.toSet()));

    public static final byte[] DISCOVER_STRING = org.openhab.binding.miio.internal.Utils
            .hexStringToByteArray("21310020ffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    public static final int PORT = 54321;
    public static final Set<String> IGNORED_TOKENS = Collections.unmodifiableSet(Stream
            .of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "00000000000000000000000000000000").collect(Collectors.toSet()));

    public static final String DATABASE_PATH = "database/";
    public static final String BINDING_DATABASE_PATH = OpenHAB.getConfigFolder() + File.separator + "misc"
            + File.separator + BINDING_ID;
    public static final String BINDING_USERDATA_PATH = OpenHAB.getUserDataFolder() + File.separator
            + MiIoBindingConstants.BINDING_ID;

    public static final String I18N_THING_PREFIX = "thing.";
    public static final String I18N_CHANNEL_PREFIX = "ch.";
    public static final String I18N_OPTION_PREFIX = "option.";
}
