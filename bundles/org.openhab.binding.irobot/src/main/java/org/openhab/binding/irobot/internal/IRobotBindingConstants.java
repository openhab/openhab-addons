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
package org.openhab.binding.irobot.internal;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IRobotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author hkuhn42 - Initial contribution
 * @author Pavel Fedin - rename and update
 * @author Alexander Falkenstern - Add support for I7 series
 */
@NonNullByDefault
public class IRobotBindingConstants {

    public static final String BINDING_ID = "irobot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ROOMBA = new ThingTypeUID(BINDING_ID, "roomba");

    // Something goes wrong...
    public static final String UNKNOWN = "UNKNOWN";

    // List of all Channel ids
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_CYCLE = "cycle";
    public static final String CHANNEL_PHASE = "phase";
    public static final String CHANNEL_BIN = "bin";
    public static final String CHANNEL_BATTERY = "battery";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_SNR = "snr";
    // iRobot's JSON lists weekdays starting from Saturday
    public static final String CHANNEL_SCHED_SWITCH_PREFIX = "sched_";
    public static final String[] CHANNEL_SCHED_SWITCH = { "sched_sun", "sched_mon", "sched_tue", "sched_wed",
            "sched_thu", "sched_fri", "sched_sat" };
    public static final String CHANNEL_SCHEDULE = "schedule";
    public static final String CHANNEL_EDGE_CLEAN = "edge_clean";
    public static final String CHANNEL_ALWAYS_FINISH = "always_finish";
    public static final String CHANNEL_POWER_BOOST = "power_boost";
    public static final String CHANNEL_CLEAN_PASSES = "clean_passes";
    public static final String CHANNEL_MAP_UPLOAD = "map_upload";
    public static final String CHANNEL_LAST_COMMAND = "last_command";

    public static final String CMD_CLEAN = "clean";
    public static final String CMD_CLEAN_REGIONS = "cleanRegions";
    public static final String CMD_SPOT = "spot";
    public static final String CMD_DOCK = "dock";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_STOP = "stop";

    public static final String BIN_OK = "ok";
    public static final String BIN_FULL = "full";
    public static final String BIN_REMOVED = "removed";

    public static final String BOOST_AUTO = "auto";
    public static final String BOOST_PERFORMANCE = "performance";
    public static final String BOOST_ECO = "eco";

    public static final String PASSES_AUTO = "auto";
    public static final String PASSES_1 = "1";
    public static final String PASSES_2 = "2";

    // Connection and config constants
    public static final int MQTT_PORT = 8883;
    public static final int UDP_PORT = 5678;
    public static final TrustManager[] TRUST_MANAGERS = { TrustAllTrustManager.getInstance() };

    public static final String ROBOT_BLID = "blid";
    public static final String ROBOT_PASSWORD = "password";
}
