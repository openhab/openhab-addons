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
package org.openhab.binding.worxlandroid.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WorxLandroidBindingConstants} class defines datCommon constants, which are
 * used across the whole binding.
 *
 * @author Nils Billing - Initial contribution
 */
@NonNullByDefault
public class WorxLandroidBindingConstants {

    public static final String BINDING_ID = "worxlandroid";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_MOWER = new ThingTypeUID(BINDING_ID, "mower");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_MOWER);

    // Channel group ids
    public static final String GROUP_COMMON = "common";
    public static final String GROUP_CONFIG = "config";
    public static final String GROUP_MULTI_ZONES = "multi-zones";
    public static final String GROUP_SCHEDULE = "schedule";
    public static final String GROUP_ONE_TIME = "one-time";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_ORIENTATION = "orientation";
    public static final String GROUP_METRICS = "metrics";
    public static final String GROUP_RAIN = "rain";
    public static final String GROUP_WIFI = "wifi";
    public static final String GROUP_AWS = "aws";

    // List channel ids
    // common
    public static final String CHANNEL_ONLINE_TIMESTAMP = "online-timestamp";
    public static final String CHANNEL_ACTION = "action";
    public static final String CHANNEL_ENABLE = "enable";
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_RSSI = "rssi";

    // AWS
    public static final String CHANNEL_POLL = "poll";
    public static final String CHANNEL_CONNECTED = "connected";

    // cfgCommon
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_DELAY = "delay";

    // cfgSc
    public static final String CHANNEL_TIME_EXTENSION = "time-extension";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_START = "next-start";
    public static final String CHANNEL_STOP = "next-stop";

    // cfgScXXXday
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_EDGECUT = "edgecut";
    public static final String CHANNEL_TIME = "time";

    // datCommon
    public static final String CHANNEL_WIFI_QUALITY = "wifi-quality";
    public static final String CHANNEL_LAST_ZONE = "last-zone";
    public static final String CHANNEL_STATUS_CODE = "status";
    public static final String CHANNEL_ERROR_CODE = "error";

    // datBattery
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_CHARGE_CYCLES = "charge-cycles";
    public static final String CHANNEL_CHARGE_CYCLES_TOTAL = "charge-cycles-total";
    public static final String CHANNEL_CHARGING = "charging";

    // datDmp
    public static final String CHANNEL_PITCH = "pitch";
    public static final String CHANNEL_ROLL = "roll";
    public static final String CHANNEL_YAW = "yaw";

    // datSt
    public static final String CHANNEL_BLADE_TIME = "blade-time";
    public static final String CHANNEL_BLADE_TIME_TOTAL = "blade-time-total";
    public static final String CHANNEL_DISTANCE = "distance";
    public static final String CHANNEL_TOTAL_TIME = "total-time";

    // datRain
    public static final String CHANNEL_RAIN_STATE = "state";
    public static final String CHANNEL_RAIN_COUNTER = "counter";

    public static final String CHANNEL_PREFIX_ALLOCATION = "allocation-%d";
    public static final String CHANNEL_PREFIX_ZONE = "zone-%d";
}
