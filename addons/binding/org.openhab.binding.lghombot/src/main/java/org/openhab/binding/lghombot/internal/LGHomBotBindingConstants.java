/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LGHomBotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class LGHomBotBindingConstants {

    private LGHomBotBindingConstants() {
        throw new IllegalStateException("Utility class");
    }

    private static final String BINDING_ID = "lghombot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LGHOMBOT = new ThingTypeUID(BINDING_ID, "LGHomBot");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_LGHOMBOT);

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_BATTERY = "battery";
    public static final String CHANNEL_CPU_LOAD = "cpuLoad";
    public static final String CHANNEL_SRV_MEM = "srvMem";
    public static final String CHANNEL_CLEAN = "clean";
    public static final String CHANNEL_START = "start";
    public static final String CHANNEL_HOME = "home";
    public static final String CHANNEL_PAUSE = "pause";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_TURBO = "turbo";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_NICKNAME = "nickname";
    public static final String CHANNEL_MOVE = "move";
    public static final String CHANNEL_CAMERA = "camera";
    public static final String CHANNEL_LAST_CLEAN = "lastClean";
    public static final String CHANNEL_MAP = "map";
    public static final String CHANNEL_MONDAY = "monday";
    public static final String CHANNEL_TUESDAY = "tuesday";
    public static final String CHANNEL_WEDNESDAY = "wednesday";
    public static final String CHANNEL_THURSDAY = "thursday";
    public static final String CHANNEL_FRIDAY = "friday";
    public static final String CHANNEL_SATURDAY = "saturday";
    public static final String CHANNEL_SUNDAY = "sunday";

    // List of all HomBot states
    public static final String HBSTATE_UNKNOWN = "UNKNOWN";
    public static final String HBSTATE_WORKING = "WORKING";
    public static final String HBSTATE_BACKMOVING = "BACKMOVING";
    public static final String HBSTATE_BACKMOVING_INIT = "BACKMOVING_INIT";
    public static final String HBSTATE_BACKMOVING_JOY = "BACKMOVING_JOY";
    public static final String HBSTATE_PAUSE = "PAUSE";
    public static final String HBSTATE_STANDBY = "STANDBY";
    public static final String HBSTATE_HOMING = "HOMING";
    public static final String HBSTATE_DOCKING = "DOCKING";
    public static final String HBSTATE_CHARGING = "CHARGING";
    public static final String HBSTATE_DIAGNOSIS = "DIAGNOSIS";
    public static final String HBSTATE_RESERVATION = "RESERVATION";
    public static final String HBSTATE_ERROR = "ERROR";
}
