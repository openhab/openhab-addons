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
package org.openhab.binding.automower.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AutomowerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner & calendar data
 */
@NonNullByDefault
public class AutomowerBindingConstants {
    private static final String BINDING_ID = "automower";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "automower");

    // List of all status Channel ids
    public static final String GROUP_STATUS = ""; // no channel group in use at the moment, we'll possibly introduce
                                                  // this in a future release
    public static final String CHANNEL_STATUS_NAME = GROUP_STATUS + "name";
    public static final String CHANNEL_STATUS_MODE = GROUP_STATUS + "mode";
    public static final String CHANNEL_STATUS_ACTIVITY = GROUP_STATUS + "activity";
    public static final String CHANNEL_STATUS_STATE = GROUP_STATUS + "state";
    public static final String CHANNEL_STATUS_LAST_UPDATE = GROUP_STATUS + "last-update";
    public static final String CHANNEL_STATUS_BATTERY = GROUP_STATUS + "battery";
    public static final String CHANNEL_STATUS_ERROR_CODE = GROUP_STATUS + "error-code";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = GROUP_STATUS + "error-timestamp";
    public static final String CHANNEL_PLANNER_NEXT_START = GROUP_STATUS + "planner-next-start";
    public static final String CHANNEL_PLANNER_OVERRIDE_ACTION = GROUP_STATUS + "planner-override-action";
    public static final String CHANNEL_CALENDAR_TASKS = GROUP_STATUS + "calendar-tasks";

    // Position Channels ids
    public static final String GROUP_POSITIONS = ""; // no channel group in use at the moment, we'll possibly
                                                     // introduce
    // this in a future release
    public static final String LAST_POSITION = GROUP_POSITIONS + "last-position";
    public static final ArrayList<String> CHANNEL_POSITIONS = new ArrayList<String>(
            List.of(GROUP_POSITIONS + "position01", GROUP_POSITIONS + "position02", GROUP_POSITIONS + "position03",
                    GROUP_POSITIONS + "position04", GROUP_POSITIONS + "position05", GROUP_POSITIONS + "position06",
                    GROUP_POSITIONS + "position07", GROUP_POSITIONS + "position08", GROUP_POSITIONS + "position09",
                    GROUP_POSITIONS + "position10", GROUP_POSITIONS + "position11", GROUP_POSITIONS + "position12",
                    GROUP_POSITIONS + "position13", GROUP_POSITIONS + "position14", GROUP_POSITIONS + "position15",
                    GROUP_POSITIONS + "position16", GROUP_POSITIONS + "position17", GROUP_POSITIONS + "position18",
                    GROUP_POSITIONS + "position19", GROUP_POSITIONS + "position20", GROUP_POSITIONS + "position21",
                    GROUP_POSITIONS + "position22", GROUP_POSITIONS + "position23", GROUP_POSITIONS + "position24",
                    GROUP_POSITIONS + "position25", GROUP_POSITIONS + "position26", GROUP_POSITIONS + "position27",
                    GROUP_POSITIONS + "position28", GROUP_POSITIONS + "position29", GROUP_POSITIONS + "position30",
                    GROUP_POSITIONS + "position31", GROUP_POSITIONS + "position32", GROUP_POSITIONS + "position33",
                    GROUP_POSITIONS + "position34", GROUP_POSITIONS + "position35", GROUP_POSITIONS + "position36",
                    GROUP_POSITIONS + "position37", GROUP_POSITIONS + "position38", GROUP_POSITIONS + "position39",
                    GROUP_POSITIONS + "position40", GROUP_POSITIONS + "position41", GROUP_POSITIONS + "position42",
                    GROUP_POSITIONS + "position43", GROUP_POSITIONS + "position44", GROUP_POSITIONS + "position45",
                    GROUP_POSITIONS + "position46", GROUP_POSITIONS + "position47", GROUP_POSITIONS + "position48",
                    GROUP_POSITIONS + "position49", GROUP_POSITIONS + "position50"));

    // Command Channel ids
    public static final String GROUP_COMMANDS = ""; // no channel group in use at the moment, we'll possibly introduce
                                                    // this in a future release
    public static final String CHANNEL_COMMAND_START = GROUP_COMMANDS + "start";
    public static final String CHANNEL_COMMAND_RESUME_SCHEDULE = GROUP_COMMANDS + "resume_schedule";
    public static final String CHANNEL_COMMAND_PAUSE = GROUP_COMMANDS + "pause";
    public static final String CHANNEL_COMMAND_PARK = GROUP_COMMANDS + "park";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NEXT_SCHEDULE = GROUP_COMMANDS + "park_until_next_schedule";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NOTICE = GROUP_COMMANDS + "park_until_further_notice";

    // Automower properties
    public static final String AUTOMOWER_ID = "mowerId";
    public static final String AUTOMOWER_NAME = "mowerName";
    public static final String AUTOMOWER_MODEL = "mowerModel";
    public static final String AUTOMOWER_SERIAL_NUMBER = "mowerSerialNumber";
}
