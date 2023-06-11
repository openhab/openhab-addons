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
package org.openhab.binding.opensprinkler.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenSprinklerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Split channels to their own things
 */
@NonNullByDefault
public class OpenSprinklerBindingConstants {
    public static final String BINDING_ID = "opensprinkler";
    public static final String DEFAULT_ADMIN_PASSWORD = "opendoor";
    public static final int DEFAULT_STATION_COUNT = 8;
    public static final String HTTP_REQUEST_URL_PREFIX = "http://";
    public static final String HTTPS_REQUEST_URL_PREFIX = "https://";
    public static final String CMD_ENABLE_MANUAL_MODE = "mm=1";
    public static final String CMD_DISABLE_MANUAL_MODE = "mm=0";
    public static final String CMD_PASSWORD = "pw=";
    public static final String CMD_STATION = "sid=";
    public static final String CMD_STATION_ENABLE = "en=1";
    public static final String CMD_STATION_DISABLE = "en=0";
    public static final String CMD_STATUS_INFO = "jc";
    public static final String CMD_OPTIONS_INFO = "jo";
    public static final String CMD_STATION_INFO = "js";
    public static final String CMD_PROGRAM_DATA = "jp";
    public static final String CMD_STATION_CONTROL = "cm";
    public static final String JSON_OPTION_FIRMWARE_VERSION = "fwv";
    public static final String JSON_OPTION_RAINSENSOR = "rs";
    public static final String JSON_OPTION_STATION = "sn";
    public static final String JSON_OPTION_STATION_COUNT = "nstations";
    public static final String JSON_OPTION_RESULT = "result";
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;
    public static final BigDecimal MAX_TIME_SECONDS = new BigDecimal(64800);

    // List of all Thing ids
    public static final String HTTP_BRIDGE = "http";
    public static final String PI_BRIDGE = "pi";
    public static final String STATION_THING = "station";
    public static final String DEVICE_THING = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID OPENSPRINKLER_HTTP_BRIDGE = new ThingTypeUID(BINDING_ID, HTTP_BRIDGE);
    public static final ThingTypeUID OPENSPRINKLER_STATION = new ThingTypeUID(BINDING_ID, STATION_THING);
    public static final ThingTypeUID OPENSPRINKLER_DEVICE = new ThingTypeUID(BINDING_ID, DEVICE_THING);

    // List of all Channel ids
    public static final String SENSOR_SIGNAL_STRENGTH = "signalStrength";
    public static final String SENSOR_FLOW_COUNT = "flowSensorCount";
    public static final String SENSOR_RAIN = "rainsensor";
    public static final String SENSOR_2 = "sensor2";
    public static final String SENSOR_WATERLEVEL = "waterlevel";
    public static final String SENSOR_CURRENT_DRAW = "currentDraw";
    public static final String CHANNEL_PROGRAMS = "programs";
    public static final String CHANNEL_ENABLE_PROGRAMS = "enablePrograms";
    public static final String CHANNEL_STATIONS = "stations";
    public static final String CHANNEL_RESET_STATIONS = "resetStations";
    public static final String STATION_STATE = "stationState";
    public static final String STATION_QUEUED = "queued";
    public static final String REMAINING_WATER_TIME = "remainingWaterTime";
    public static final String NEXT_DURATION = "nextDuration";
    public static final String CHANNEL_IGNORE_RAIN = "ignoreRain";
    public static final String CHANNEL_RAIN_DELAY = "rainDelay";
}
