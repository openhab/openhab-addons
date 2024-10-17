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
 * @author Marcin Czeczko - Added support for planner and calendar data
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
    public static final String CHANNEL_STATUS_INACTIVE_REASON = GROUP_STATUS + "inactive-reason";
    public static final String CHANNEL_STATUS_STATE = GROUP_STATUS + "state";
    public static final String CHANNEL_STATUS_LAST_UPDATE = GROUP_STATUS + "last-update";
    public static final String CHANNEL_STATUS_WORK_AREA_ID = GROUP_STATUS + "work-area-id";
    public static final String CHANNEL_STATUS_WORK_AREA = GROUP_STATUS + "work-area";
    public static final String CHANNEL_STATUS_BATTERY = GROUP_STATUS + "battery";
    public static final String CHANNEL_STATUS_ERROR_CODE = GROUP_STATUS + "error-code";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = GROUP_STATUS + "error-timestamp";
    public static final String CHANNEL_STATUS_ERROR_CONFIRMABLE = GROUP_STATUS + "error-confirmable";
    public static final String CHANNEL_PLANNER_NEXT_START = GROUP_STATUS + "planner-next-start";
    public static final String CHANNEL_PLANNER_OVERRIDE_ACTION = GROUP_STATUS + "planner-override-action";
    public static final String CHANNEL_PLANNER_RESTRICTED_REASON = GROUP_STATUS + "planner-restricted-reason";
    public static final String CHANNEL_PLANNER_EXTERNAL_REASON = GROUP_STATUS + "planner-external-reason";
    public static final String CHANNEL_SETTING_CUTTING_HEIGHT = GROUP_STATUS + "setting-cutting-height";
    public static final String CHANNEL_SETTING_HEADLIGHT_MODE = GROUP_STATUS + "setting-headlight-mode";
    public static final String CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME = GROUP_STATUS
            + "stat-cutting-blade-usage-time";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_CHARGING_CYCLES = GROUP_STATUS
            + "stat-number-of-charging-cycles";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_COLLISIONS = GROUP_STATUS + "stat-number-of-collisions";
    public static final String CHANNEL_STATISTIC_TOTAL_CHARGING_TIME = GROUP_STATUS + "stat-total-charging-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_TIME = GROUP_STATUS + "stat-total-cutting-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_PERCENT = GROUP_STATUS + "stat-total-cutting-percent";
    public static final String CHANNEL_STATISTIC_TOTAL_DRIVE_DISTANCE = GROUP_STATUS + "stat-total-drive-distance";
    public static final String CHANNEL_STATISTIC_TOTAL_RUNNING_TIME = GROUP_STATUS + "stat-total-running-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_TIME = GROUP_STATUS + "stat-total-searching-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_PERCENT = GROUP_STATUS
            + "stat-total-searching-percent";

    // Calendar Task Channels ids
    public static final String GROUP_CALENDARTASKS = ""; // no channel group in use at the moment, we'll possibly
    // introduce
    // this in a future release
    public static final ArrayList<String> CHANNEL_CALENDARTASKS = new ArrayList<>(List.of(
            GROUP_CALENDARTASKS + "calendartasks01-start", GROUP_CALENDARTASKS + "calendartasks01-duration",
            GROUP_CALENDARTASKS + "calendartasks01-monday", GROUP_CALENDARTASKS + "calendartasks01-tuesday",
            GROUP_CALENDARTASKS + "calendartasks01-wednesday", GROUP_CALENDARTASKS + "calendartasks01-thursday",
            GROUP_CALENDARTASKS + "calendartasks01-friday", GROUP_CALENDARTASKS + "calendartasks01-saturday",
            GROUP_CALENDARTASKS + "calendartasks01-sunday", GROUP_CALENDARTASKS + "calendartasks01-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks02-start", GROUP_CALENDARTASKS + "calendartasks02-duration",
            GROUP_CALENDARTASKS + "calendartasks02-monday", GROUP_CALENDARTASKS + "calendartasks02-tuesday",
            GROUP_CALENDARTASKS + "calendartasks02-wednesday", GROUP_CALENDARTASKS + "calendartasks02-thursday",
            GROUP_CALENDARTASKS + "calendartasks02-friday", GROUP_CALENDARTASKS + "calendartasks02-saturday",
            GROUP_CALENDARTASKS + "calendartasks02-sunday", GROUP_CALENDARTASKS + "calendartasks02-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks03-start", GROUP_CALENDARTASKS + "calendartasks03-duration",
            GROUP_CALENDARTASKS + "calendartasks03-monday", GROUP_CALENDARTASKS + "calendartasks03-tuesday",
            GROUP_CALENDARTASKS + "calendartasks03-wednesday", GROUP_CALENDARTASKS + "calendartasks03-thursday",
            GROUP_CALENDARTASKS + "calendartasks03-friday", GROUP_CALENDARTASKS + "calendartasks03-saturday",
            GROUP_CALENDARTASKS + "calendartasks03-sunday", GROUP_CALENDARTASKS + "calendartasks03-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks04-start", GROUP_CALENDARTASKS + "calendartasks04-duration",
            GROUP_CALENDARTASKS + "calendartasks04-monday", GROUP_CALENDARTASKS + "calendartasks04-tuesday",
            GROUP_CALENDARTASKS + "calendartasks04-wednesday", GROUP_CALENDARTASKS + "calendartasks04-thursday",
            GROUP_CALENDARTASKS + "calendartasks04-friday", GROUP_CALENDARTASKS + "calendartasks04-saturday",
            GROUP_CALENDARTASKS + "calendartasks04-sunday", GROUP_CALENDARTASKS + "calendartasks04-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks05-start", GROUP_CALENDARTASKS + "calendartasks05-duration",
            GROUP_CALENDARTASKS + "calendartasks05-monday", GROUP_CALENDARTASKS + "calendartasks05-tuesday",
            GROUP_CALENDARTASKS + "calendartasks05-wednesday", GROUP_CALENDARTASKS + "calendartasks05-thursday",
            GROUP_CALENDARTASKS + "calendartasks05-friday", GROUP_CALENDARTASKS + "calendartasks05-saturday",
            GROUP_CALENDARTASKS + "calendartasks05-sunday", GROUP_CALENDARTASKS + "calendartasks05-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks06-start", GROUP_CALENDARTASKS + "calendartasks06-duration",
            GROUP_CALENDARTASKS + "calendartasks06-monday", GROUP_CALENDARTASKS + "calendartasks06-tuesday",
            GROUP_CALENDARTASKS + "calendartasks06-wednesday", GROUP_CALENDARTASKS + "calendartasks06-thursday",
            GROUP_CALENDARTASKS + "calendartasks06-friday", GROUP_CALENDARTASKS + "calendartasks06-saturday",
            GROUP_CALENDARTASKS + "calendartasks06-sunday", GROUP_CALENDARTASKS + "calendartasks06-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks07-start", GROUP_CALENDARTASKS + "calendartasks07-duration",
            GROUP_CALENDARTASKS + "calendartasks07-monday", GROUP_CALENDARTASKS + "calendartasks07-tuesday",
            GROUP_CALENDARTASKS + "calendartasks07-wednesday", GROUP_CALENDARTASKS + "calendartasks07-thursday",
            GROUP_CALENDARTASKS + "calendartasks07-friday", GROUP_CALENDARTASKS + "calendartasks07-saturday",
            GROUP_CALENDARTASKS + "calendartasks07-sunday", GROUP_CALENDARTASKS + "calendartasks07-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks08-start", GROUP_CALENDARTASKS + "calendartasks08-duration",
            GROUP_CALENDARTASKS + "calendartasks08-monday", GROUP_CALENDARTASKS + "calendartasks08-tuesday",
            GROUP_CALENDARTASKS + "calendartasks08-wednesday", GROUP_CALENDARTASKS + "calendartasks08-thursday",
            GROUP_CALENDARTASKS + "calendartasks08-friday", GROUP_CALENDARTASKS + "calendartasks08-saturday",
            GROUP_CALENDARTASKS + "calendartasks08-sunday", GROUP_CALENDARTASKS + "calendartasks08-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks09-start", GROUP_CALENDARTASKS + "calendartasks09-duration",
            GROUP_CALENDARTASKS + "calendartasks09-monday", GROUP_CALENDARTASKS + "calendartasks09-tuesday",
            GROUP_CALENDARTASKS + "calendartasks09-wednesday", GROUP_CALENDARTASKS + "calendartasks09-thursday",
            GROUP_CALENDARTASKS + "calendartasks09-friday", GROUP_CALENDARTASKS + "calendartasks09-saturday",
            GROUP_CALENDARTASKS + "calendartasks09-sunday", GROUP_CALENDARTASKS + "calendartasks09-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks10-start", GROUP_CALENDARTASKS + "calendartasks10-duration",
            GROUP_CALENDARTASKS + "calendartasks10-monday", GROUP_CALENDARTASKS + "calendartasks10-tuesday",
            GROUP_CALENDARTASKS + "calendartasks10-wednesday", GROUP_CALENDARTASKS + "calendartasks10-thursday",
            GROUP_CALENDARTASKS + "calendartasks10-friday", GROUP_CALENDARTASKS + "calendartasks10-saturday",
            GROUP_CALENDARTASKS + "calendartasks10-sunday", GROUP_CALENDARTASKS + "calendartasks10-workAreaId"));

    // Position Channels ids
    public static final String GROUP_POSITIONS = ""; // no channel group in use at the moment, we'll possibly
                                                     // introduce
    // this in a future release
    public static final String LAST_POSITION = GROUP_POSITIONS + "last-position";
    public static final ArrayList<String> CHANNEL_POSITIONS = new ArrayList<>(
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

    // Stayout Zones Channels ids
    public static final String GROUP_STAYOUTZONES = ""; // no channel group in use at the moment, we'll possibly
                                                        // introduce
    // this in a future release
    public static final String CHANNEL_STAYOUTZONES_DIRTY = GROUP_STAYOUTZONES + "dirty";
    public static final ArrayList<String> CHANNEL_STAYOUTZONES = new ArrayList<>(List.of(
            GROUP_STAYOUTZONES + "zone01-id", GROUP_STAYOUTZONES + "zone01-name", GROUP_STAYOUTZONES + "zone01-enabled",
            GROUP_STAYOUTZONES + "zone02-id", GROUP_STAYOUTZONES + "zone02-name", GROUP_STAYOUTZONES + "zone02-enabled",
            GROUP_STAYOUTZONES + "zone03-id", GROUP_STAYOUTZONES + "zone03-name", GROUP_STAYOUTZONES + "zone03-enabled",
            GROUP_STAYOUTZONES + "zone04-id", GROUP_STAYOUTZONES + "zone04-name", GROUP_STAYOUTZONES + "zone04-enabled",
            GROUP_STAYOUTZONES + "zone05-id", GROUP_STAYOUTZONES + "zone05-name", GROUP_STAYOUTZONES + "zone05-enabled",
            GROUP_STAYOUTZONES + "zone06-id", GROUP_STAYOUTZONES + "zone06-name", GROUP_STAYOUTZONES + "zone06-enabled",
            GROUP_STAYOUTZONES + "zone07-id", GROUP_STAYOUTZONES + "zone07-name", GROUP_STAYOUTZONES + "zone07-enabled",
            GROUP_STAYOUTZONES + "zone08-id", GROUP_STAYOUTZONES + "zone08-name", GROUP_STAYOUTZONES + "zone08-enabled",
            GROUP_STAYOUTZONES + "zone09-id", GROUP_STAYOUTZONES + "zone09-name", GROUP_STAYOUTZONES + "zone09-enabled",
            GROUP_STAYOUTZONES + "zone10-id", GROUP_STAYOUTZONES + "zone10-name",
            GROUP_STAYOUTZONES + "zone10-enabled"));

    // Work Areas Channels ids
    public static final String GROUP_WORKAREAS = ""; // no channel group in use at the moment, we'll possibly
                                                     // introduce
    // this in a future release
    public static final ArrayList<String> CHANNEL_WORKAREAS = new ArrayList<>(
            List.of(GROUP_WORKAREAS + "workareas01-id", GROUP_WORKAREAS + "workareas01-name",
                    GROUP_WORKAREAS + "workareas01-cutting-height", GROUP_WORKAREAS + "workareas01-enabled",
                    GROUP_WORKAREAS + "workareas01-progress", GROUP_WORKAREAS + "workareas01-last-time-completed",

                    GROUP_WORKAREAS + "workareas02-id", GROUP_WORKAREAS + "workareas02-name",
                    GROUP_WORKAREAS + "workareas02-cutting-height", GROUP_WORKAREAS + "workareas02-enabled",
                    GROUP_WORKAREAS + "workareas02-progress", GROUP_WORKAREAS + "workareas02-last-time-completed",

                    GROUP_WORKAREAS + "workareas03-id", GROUP_WORKAREAS + "workareas03-name",
                    GROUP_WORKAREAS + "workareas03-cutting-height", GROUP_WORKAREAS + "workareas03-enabled",
                    GROUP_WORKAREAS + "workareas03-progress", GROUP_WORKAREAS + "workareas03-last-time-completed",

                    GROUP_WORKAREAS + "workareas04-id", GROUP_WORKAREAS + "workareas04-name",
                    GROUP_WORKAREAS + "workareas04-cutting-height", GROUP_WORKAREAS + "workareas04-enabled",
                    GROUP_WORKAREAS + "workareas04-progress", GROUP_WORKAREAS + "workareas04-last-time-completed",

                    GROUP_WORKAREAS + "workareas05-id", GROUP_WORKAREAS + "workareas05-name",
                    GROUP_WORKAREAS + "workareas05-cutting-height", GROUP_WORKAREAS + "workareas05-enabled",
                    GROUP_WORKAREAS + "workareas05-progress", GROUP_WORKAREAS + "workareas05-last-time-completed",

                    GROUP_WORKAREAS + "workareas06-id", GROUP_WORKAREAS + "workareas06-name",
                    GROUP_WORKAREAS + "workareas06-cutting-height", GROUP_WORKAREAS + "workareas06-enabled",
                    GROUP_WORKAREAS + "workareas06-progress", GROUP_WORKAREAS + "workareas06-last-time-completed",

                    GROUP_WORKAREAS + "workareas07-id", GROUP_WORKAREAS + "workareas07-name",
                    GROUP_WORKAREAS + "workareas07-cutting-height", GROUP_WORKAREAS + "workareas07-enabled",
                    GROUP_WORKAREAS + "workareas07-progress", GROUP_WORKAREAS + "workareas07-last-time-completed",

                    GROUP_WORKAREAS + "workareas08-id", GROUP_WORKAREAS + "workareas08-name",
                    GROUP_WORKAREAS + "workareas08-cutting-height", GROUP_WORKAREAS + "workareas08-enabled",
                    GROUP_WORKAREAS + "workareas08-progress", GROUP_WORKAREAS + "workareas08-last-time-completed",

                    GROUP_WORKAREAS + "workareas09-id", GROUP_WORKAREAS + "workareas09-name",
                    GROUP_WORKAREAS + "workareas09-cutting-height", GROUP_WORKAREAS + "workareas09-enabled",
                    GROUP_WORKAREAS + "workareas09-progress", GROUP_WORKAREAS + "workareas09-last-time-completed",

                    GROUP_WORKAREAS + "workareas10-id", GROUP_WORKAREAS + "workareas10-name",
                    GROUP_WORKAREAS + "workareas10-cutting-height", GROUP_WORKAREAS + "workareas10-enabled",
                    GROUP_WORKAREAS + "workareas10-progress", GROUP_WORKAREAS + "workareas10-last-time-completed"));

    // Command Channel ids
    public static final String GROUP_COMMANDS = ""; // no channel group in use at the moment, we'll possibly introduce
                                                    // this in a future release
    public static final String CHANNEL_COMMAND_START = GROUP_COMMANDS + "start";
    public static final String CHANNEL_COMMAND_RESUME_SCHEDULE = GROUP_COMMANDS + "resume_schedule";
    public static final String CHANNEL_COMMAND_PAUSE = GROUP_COMMANDS + "pause";
    public static final String CHANNEL_COMMAND_PARK = GROUP_COMMANDS + "park";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NEXT_SCHEDULE = GROUP_COMMANDS + "park_until_next_schedule";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NOTICE = GROUP_COMMANDS + "park_until_further_notice";
    public static final String CHANNEL_COMMAND_CONFIRM_ERROR = GROUP_COMMANDS + "confirm_error";

    // Automower properties
    public static final String AUTOMOWER_ID = "mowerId";
    public static final String AUTOMOWER_NAME = "mowerName";
    public static final String AUTOMOWER_MODEL = "mowerModel";
    public static final String AUTOMOWER_SERIAL_NUMBER = "mowerSerialNumber";
    public static final String AUTOMOWER_CAN_CONFIRM_ERROR = "mowerCanConfirmError";
    public static final String AUTOMOWER_HAS_HEADLIGHTS = "mowerHasHeadlights";
    public static final String AUTOMOWER_HAS_POSITION = "mowerHasPosition";
    public static final String AUTOMOWER_HAS_STAY_OUT_ZONES = "mowerHasStayOutZones";
    public static final String AUTOMOWER_HAS_WORK_AREAS = "mowerHasWorkAreas";
}
