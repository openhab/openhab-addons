/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lghombot.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LGHomBotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public final class LGHomBotBindingConstants {

    private static final String BINDING_ID = "lghombot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LGHOMBOT = new ThingTypeUID(BINDING_ID, "LGHomBot");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_LGHOMBOT);

    // List of all Channel ids
    static final String CHANNEL_STATE = "state";
    static final String CHANNEL_BATTERY = "battery";
    static final String CHANNEL_CPU_LOAD = "cpuLoad";
    static final String CHANNEL_SRV_MEM = "srvMem";
    static final String CHANNEL_CLEAN = "clean";
    static final String CHANNEL_START = "start";
    static final String CHANNEL_HOME = "home";
    static final String CHANNEL_PAUSE = "pause";
    static final String CHANNEL_MODE = "mode";
    static final String CHANNEL_TURBO = "turbo";
    static final String CHANNEL_REPEAT = "repeat";
    static final String CHANNEL_NICKNAME = "nickname";
    static final String CHANNEL_MOVE = "move";
    static final String CHANNEL_CAMERA = "camera";
    static final String CHANNEL_LAST_CLEAN = "lastClean";
    static final String CHANNEL_MAP = "map";
    static final String CHANNEL_MONDAY = "monday";
    static final String CHANNEL_TUESDAY = "tuesday";
    static final String CHANNEL_WEDNESDAY = "wednesday";
    static final String CHANNEL_THURSDAY = "thursday";
    static final String CHANNEL_FRIDAY = "friday";
    static final String CHANNEL_SATURDAY = "saturday";
    static final String CHANNEL_SUNDAY = "sunday";

    // List of all HomBot states
    static final String HBSTATE_UNKNOWN = "UNKNOWN";
    static final String HBSTATE_WORKING = "WORKING";
    static final String HBSTATE_BACKMOVING = "BACKMOVING";
    static final String HBSTATE_BACKMOVING_INIT = "BACKMOVING_INIT";
    static final String HBSTATE_BACKMOVING_JOY = "BACKMOVING_JOY";
    static final String HBSTATE_PAUSE = "PAUSE";
    static final String HBSTATE_STANDBY = "STANDBY";
    static final String HBSTATE_HOMING = "HOMING";
    static final String HBSTATE_DOCKING = "DOCKING";
    static final String HBSTATE_CHARGING = "CHARGING";
    static final String HBSTATE_DIAGNOSIS = "DIAGNOSIS";
    static final String HBSTATE_RESERVATION = "RESERVATION";
    static final String HBSTATE_ERROR = "ERROR";

    /**
     * Default port number HomBot uses.
     */
    public static final int DEFAULT_HOMBOT_PORT = 6260;

    private LGHomBotBindingConstants() {
        // No need to instance this class.
    }
}
