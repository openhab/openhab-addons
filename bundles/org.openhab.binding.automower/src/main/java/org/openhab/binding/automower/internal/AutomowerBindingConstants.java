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
package org.openhab.binding.automower.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String CHANNEL_STATUS_ERROR_MESSAGE = GROUP_STATUS + "error-message";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = GROUP_STATUS + "error-timestamp";
    public static final String CHANNEL_STATUS_ERROR_CONFIRMABLE = GROUP_STATUS + "error-confirmable";

    // List of all planner Channel ids
    public static final String GROUP_PLANNER = ""; // no channel group in use at the moment, we'll possibly introduce
                                                   // this in a future release
    public static final String CHANNEL_PLANNER_NEXT_START = GROUP_PLANNER + "planner-next-start";
    public static final String CHANNEL_PLANNER_OVERRIDE_ACTION = GROUP_PLANNER + "planner-override-action";
    public static final String CHANNEL_PLANNER_RESTRICTED_REASON = GROUP_PLANNER + "planner-restricted-reason";
    public static final String CHANNEL_PLANNER_EXTERNAL_REASON = GROUP_PLANNER + "planner-external-reason";

    // List of all setting Channel ids
    public static final String GROUP_SETTING = ""; // no channel group in use at the moment, we'll possibly introduce
                                                   // this in a future release
    public static final String CHANNEL_SETTING_CUTTING_HEIGHT = GROUP_SETTING + "setting-cutting-height";
    public static final String CHANNEL_SETTING_HEADLIGHT_MODE = GROUP_SETTING + "setting-headlight-mode";

    // List of all setting Channel ids
    public static final String GROUP_STATISTIC = ""; // no channel group in use at the moment, we'll possibly introduce
                                                     // this in a future release
    public static final String CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME = GROUP_STATISTIC
            + "stat-cutting-blade-usage-time";
    public static final String CHANNEL_STATISTIC_DOWN_TIME = GROUP_STATISTIC + "stat-down-time";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_CHARGING_CYCLES = GROUP_STATISTIC
            + "stat-number-of-charging-cycles";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_COLLISIONS = GROUP_STATISTIC + "stat-number-of-collisions";
    public static final String CHANNEL_STATISTIC_TOTAL_CHARGING_TIME = GROUP_STATISTIC + "stat-total-charging-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_TIME = GROUP_STATISTIC + "stat-total-cutting-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_PERCENT = GROUP_STATISTIC + "stat-total-cutting-percent";
    public static final String CHANNEL_STATISTIC_TOTAL_DRIVE_DISTANCE = GROUP_STATISTIC + "stat-total-drive-distance";
    public static final String CHANNEL_STATISTIC_TOTAL_RUNNING_TIME = GROUP_STATISTIC + "stat-total-running-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_TIME = GROUP_STATISTIC + "stat-total-searching-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_PERCENT = GROUP_STATISTIC
            + "stat-total-searching-percent";
    public static final String CHANNEL_STATISTIC_UP_TIME = GROUP_STATISTIC + "stat-up-time";

    // Calendar Task Channels ids
    public static final String GROUP_CALENDARTASKS = ""; // no channel group in use at the moment, we'll possibly
                                                         // introduce this in a future release
    public static final ArrayList<String> CHANNEL_CALENDARTASKS = new ArrayList<>(List.of(
            GROUP_CALENDARTASKS + "calendartasks01-start", GROUP_CALENDARTASKS + "calendartasks01-duration",
            GROUP_CALENDARTASKS + "calendartasks01-monday", GROUP_CALENDARTASKS + "calendartasks01-tuesday",
            GROUP_CALENDARTASKS + "calendartasks01-wednesday", GROUP_CALENDARTASKS + "calendartasks01-thursday",
            GROUP_CALENDARTASKS + "calendartasks01-friday", GROUP_CALENDARTASKS + "calendartasks01-saturday",
            GROUP_CALENDARTASKS + "calendartasks01-sunday", GROUP_CALENDARTASKS + "calendartasks01-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks01-workArea", GROUP_CALENDARTASKS + "calendartasks02-start",
            GROUP_CALENDARTASKS + "calendartasks02-duration", GROUP_CALENDARTASKS + "calendartasks02-monday",
            GROUP_CALENDARTASKS + "calendartasks02-tuesday", GROUP_CALENDARTASKS + "calendartasks02-wednesday",
            GROUP_CALENDARTASKS + "calendartasks02-thursday", GROUP_CALENDARTASKS + "calendartasks02-friday",
            GROUP_CALENDARTASKS + "calendartasks02-saturday", GROUP_CALENDARTASKS + "calendartasks02-sunday",
            GROUP_CALENDARTASKS + "calendartasks02-workAreaId", GROUP_CALENDARTASKS + "calendartasks02-workArea",
            GROUP_CALENDARTASKS + "calendartasks03-start", GROUP_CALENDARTASKS + "calendartasks03-duration",
            GROUP_CALENDARTASKS + "calendartasks03-monday", GROUP_CALENDARTASKS + "calendartasks03-tuesday",
            GROUP_CALENDARTASKS + "calendartasks03-wednesday", GROUP_CALENDARTASKS + "calendartasks03-thursday",
            GROUP_CALENDARTASKS + "calendartasks03-friday", GROUP_CALENDARTASKS + "calendartasks03-saturday",
            GROUP_CALENDARTASKS + "calendartasks03-sunday", GROUP_CALENDARTASKS + "calendartasks03-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks03-workArea", GROUP_CALENDARTASKS + "calendartasks04-start",
            GROUP_CALENDARTASKS + "calendartasks04-duration", GROUP_CALENDARTASKS + "calendartasks04-monday",
            GROUP_CALENDARTASKS + "calendartasks04-tuesday", GROUP_CALENDARTASKS + "calendartasks04-wednesday",
            GROUP_CALENDARTASKS + "calendartasks04-thursday", GROUP_CALENDARTASKS + "calendartasks04-friday",
            GROUP_CALENDARTASKS + "calendartasks04-saturday", GROUP_CALENDARTASKS + "calendartasks04-sunday",
            GROUP_CALENDARTASKS + "calendartasks04-workAreaId", GROUP_CALENDARTASKS + "calendartasks04-workArea",
            GROUP_CALENDARTASKS + "calendartasks05-start", GROUP_CALENDARTASKS + "calendartasks05-duration",
            GROUP_CALENDARTASKS + "calendartasks05-monday", GROUP_CALENDARTASKS + "calendartasks05-tuesday",
            GROUP_CALENDARTASKS + "calendartasks05-wednesday", GROUP_CALENDARTASKS + "calendartasks05-thursday",
            GROUP_CALENDARTASKS + "calendartasks05-friday", GROUP_CALENDARTASKS + "calendartasks05-saturday",
            GROUP_CALENDARTASKS + "calendartasks05-sunday", GROUP_CALENDARTASKS + "calendartasks05-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks05-workArea", GROUP_CALENDARTASKS + "calendartasks06-start",
            GROUP_CALENDARTASKS + "calendartasks06-duration", GROUP_CALENDARTASKS + "calendartasks06-monday",
            GROUP_CALENDARTASKS + "calendartasks06-tuesday", GROUP_CALENDARTASKS + "calendartasks06-wednesday",
            GROUP_CALENDARTASKS + "calendartasks06-thursday", GROUP_CALENDARTASKS + "calendartasks06-friday",
            GROUP_CALENDARTASKS + "calendartasks06-saturday", GROUP_CALENDARTASKS + "calendartasks06-sunday",
            GROUP_CALENDARTASKS + "calendartasks06-workAreaId", GROUP_CALENDARTASKS + "calendartasks06-workArea",
            GROUP_CALENDARTASKS + "calendartasks07-start", GROUP_CALENDARTASKS + "calendartasks07-duration",
            GROUP_CALENDARTASKS + "calendartasks07-monday", GROUP_CALENDARTASKS + "calendartasks07-tuesday",
            GROUP_CALENDARTASKS + "calendartasks07-wednesday", GROUP_CALENDARTASKS + "calendartasks07-thursday",
            GROUP_CALENDARTASKS + "calendartasks07-friday", GROUP_CALENDARTASKS + "calendartasks07-saturday",
            GROUP_CALENDARTASKS + "calendartasks07-sunday", GROUP_CALENDARTASKS + "calendartasks07-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks07-workArea", GROUP_CALENDARTASKS + "calendartasks08-start",
            GROUP_CALENDARTASKS + "calendartasks08-duration", GROUP_CALENDARTASKS + "calendartasks08-monday",
            GROUP_CALENDARTASKS + "calendartasks08-tuesday", GROUP_CALENDARTASKS + "calendartasks08-wednesday",
            GROUP_CALENDARTASKS + "calendartasks08-thursday", GROUP_CALENDARTASKS + "calendartasks08-friday",
            GROUP_CALENDARTASKS + "calendartasks08-saturday", GROUP_CALENDARTASKS + "calendartasks08-sunday",
            GROUP_CALENDARTASKS + "calendartasks08-workAreaId", GROUP_CALENDARTASKS + "calendartasks08-workArea",
            GROUP_CALENDARTASKS + "calendartasks09-start", GROUP_CALENDARTASKS + "calendartasks09-duration",
            GROUP_CALENDARTASKS + "calendartasks09-monday", GROUP_CALENDARTASKS + "calendartasks09-tuesday",
            GROUP_CALENDARTASKS + "calendartasks09-wednesday", GROUP_CALENDARTASKS + "calendartasks09-thursday",
            GROUP_CALENDARTASKS + "calendartasks09-friday", GROUP_CALENDARTASKS + "calendartasks09-saturday",
            GROUP_CALENDARTASKS + "calendartasks09-sunday", GROUP_CALENDARTASKS + "calendartasks09-workAreaId",
            GROUP_CALENDARTASKS + "calendartasks09-workArea", GROUP_CALENDARTASKS + "calendartasks10-start",
            GROUP_CALENDARTASKS + "calendartasks10-duration", GROUP_CALENDARTASKS + "calendartasks10-monday",
            GROUP_CALENDARTASKS + "calendartasks10-tuesday", GROUP_CALENDARTASKS + "calendartasks10-wednesday",
            GROUP_CALENDARTASKS + "calendartasks10-thursday", GROUP_CALENDARTASKS + "calendartasks10-friday",
            GROUP_CALENDARTASKS + "calendartasks10-saturday", GROUP_CALENDARTASKS + "calendartasks10-sunday",
            GROUP_CALENDARTASKS + "calendartasks10-workAreaId", GROUP_CALENDARTASKS + "calendartasks10-workArea"));

    // Position Channels ids
    public static final String GROUP_POSITIONS = ""; // no channel group in use at the moment, we'll possibly
                                                     // introduce this in a future release
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
                                                        // introduce this in a future release
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
                                                     // introduce this in a future release
    public static final ArrayList<String> CHANNEL_WORKAREAS = new ArrayList<>(
            List.of(GROUP_WORKAREAS + "workarea01-id", GROUP_WORKAREAS + "workarea01-name",
                    GROUP_WORKAREAS + "workarea01-cutting-height", GROUP_WORKAREAS + "workarea01-enabled",
                    GROUP_WORKAREAS + "workarea01-progress", GROUP_WORKAREAS + "workarea01-last-time-completed",

                    GROUP_WORKAREAS + "workarea02-id", GROUP_WORKAREAS + "workarea02-name",
                    GROUP_WORKAREAS + "workarea02-cutting-height", GROUP_WORKAREAS + "workarea02-enabled",
                    GROUP_WORKAREAS + "workarea02-progress", GROUP_WORKAREAS + "workarea02-last-time-completed",

                    GROUP_WORKAREAS + "workarea03-id", GROUP_WORKAREAS + "workarea03-name",
                    GROUP_WORKAREAS + "workarea03-cutting-height", GROUP_WORKAREAS + "workarea03-enabled",
                    GROUP_WORKAREAS + "workarea03-progress", GROUP_WORKAREAS + "workarea03-last-time-completed",

                    GROUP_WORKAREAS + "workarea04-id", GROUP_WORKAREAS + "workarea04-name",
                    GROUP_WORKAREAS + "workarea04-cutting-height", GROUP_WORKAREAS + "workarea04-enabled",
                    GROUP_WORKAREAS + "workarea04-progress", GROUP_WORKAREAS + "workarea04-last-time-completed",

                    GROUP_WORKAREAS + "workarea05-id", GROUP_WORKAREAS + "workarea05-name",
                    GROUP_WORKAREAS + "workarea05-cutting-height", GROUP_WORKAREAS + "workarea05-enabled",
                    GROUP_WORKAREAS + "workarea05-progress", GROUP_WORKAREAS + "workarea05-last-time-completed",

                    GROUP_WORKAREAS + "workarea06-id", GROUP_WORKAREAS + "workarea06-name",
                    GROUP_WORKAREAS + "workarea06-cutting-height", GROUP_WORKAREAS + "workarea06-enabled",
                    GROUP_WORKAREAS + "workarea06-progress", GROUP_WORKAREAS + "workarea06-last-time-completed",

                    GROUP_WORKAREAS + "workarea07-id", GROUP_WORKAREAS + "workarea07-name",
                    GROUP_WORKAREAS + "workarea07-cutting-height", GROUP_WORKAREAS + "workarea07-enabled",
                    GROUP_WORKAREAS + "workarea07-progress", GROUP_WORKAREAS + "workarea07-last-time-completed",

                    GROUP_WORKAREAS + "workarea08-id", GROUP_WORKAREAS + "workarea08-name",
                    GROUP_WORKAREAS + "workarea08-cutting-height", GROUP_WORKAREAS + "workarea08-enabled",
                    GROUP_WORKAREAS + "workarea08-progress", GROUP_WORKAREAS + "workarea08-last-time-completed",

                    GROUP_WORKAREAS + "workarea09-id", GROUP_WORKAREAS + "workarea09-name",
                    GROUP_WORKAREAS + "workarea09-cutting-height", GROUP_WORKAREAS + "workarea09-enabled",
                    GROUP_WORKAREAS + "workarea09-progress", GROUP_WORKAREAS + "workarea09-last-time-completed",

                    GROUP_WORKAREAS + "workarea10-id", GROUP_WORKAREAS + "workarea10-name",
                    GROUP_WORKAREAS + "workarea10-cutting-height", GROUP_WORKAREAS + "workarea10-enabled",
                    GROUP_WORKAREAS + "workarea10-progress", GROUP_WORKAREAS + "workarea10-last-time-completed"));

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
    public static final String AUTOMOWER_CAN_CONFIRM_ERROR = "mowerCanConfirmError";
    public static final String AUTOMOWER_HAS_HEADLIGHTS = "mowerHasHeadlights";
    public static final String AUTOMOWER_HAS_POSITION = "mowerHasPosition";
    public static final String AUTOMOWER_HAS_STAY_OUT_ZONES = "mowerHasStayOutZones";
    public static final String AUTOMOWER_HAS_WORK_AREAS = "mowerHasWorkAreas";

    public static final Map<Integer, String> ERROR = new HashMap<>() {
        {
            put(0, "No message");
            put(1, "Outside working area");
            put(2, "No loop signal");
            put(3, "Wrong loop signal");
            put(4, "Loop sensor problem, front");
            put(5, "Loop sensor problem, rear");
            put(6, "Loop sensor problem, left");
            put(7, "Loop sensor problem, right");
            put(8, "Wrong PIN code");
            put(9, "Trapped");
            put(10, "Upside down");
            put(11, "Low battery");
            put(12, "Empty battery");
            put(13, "No drive");
            put(14, "Mower lifted");
            put(15, "Lifted");
            put(16, "Stuck in charging station");
            put(17, "Charging station blocked");
            put(18, "Collision sensor problem, rear");
            put(19, "Collision sensor problem, front");
            put(20, "Wheel motor blocked, right");
            put(21, "Wheel motor blocked, left");
            put(22, "Wheel drive problem, right");
            put(23, "Wheel drive problem, left");
            put(24, "Cutting system blocked");
            put(25, "Cutting system blocked");
            put(26, "Invalid sub-device combination");
            put(27, "Settings restored");
            put(28, "Memory circuit problem");
            put(29, "Slope too steep");
            put(30, "Charging system problem");
            put(31, "STOP button problem");
            put(32, "Tilt sensor problem");
            put(33, "Mower tilted");
            put(34, "Cutting stopped - slope too steep");
            put(35, "Wheel motor overloaded, right");
            put(36, "Wheel motor overloaded, left");
            put(37, "Charging current too high");
            put(38, "Electronic problem");
            put(39, "Cutting motor problem");
            put(40, "Limited cutting height range");
            put(41, "Unexpected cutting height adj");
            put(42, "Limited cutting height range");
            put(43, "Cutting height problem, drive");
            put(44, "Cutting height problem, curr");
            put(45, "Cutting height problem, dir");
            put(46, "Cutting height blocked");
            put(47, "Cutting height problem");
            put(48, "No response from charger");
            put(49, "Ultrasonic problem");
            put(50, "Guide 1 not found");
            put(51, "Guide 2 not found");
            put(52, "Guide 3 not found");
            put(53, "GPS navigation problem");
            put(54, "Weak GPS signal");
            put(55, "Difficult finding home");
            put(56, "Guide calibration accomplished");
            put(57, "Guide calibration failed");
            put(58, "Temporary battery problem");
            put(59, "Temporary battery problem");
            put(60, "Temporary battery problem");
            put(61, "Temporary battery problem");
            put(62, "Temporary battery problem");
            put(63, "Temporary battery problem");
            put(64, "Temporary battery problem");
            put(65, "Temporary battery problem");
            put(66, "Battery problem");
            put(67, "Battery problem");
            put(68, "Temporary battery problem");
            put(69, "Alarm! Mower switched off");
            put(70, "Alarm! Mower stopped");
            put(71, "Alarm! Mower lifted");
            put(72, "Alarm! Mower tilted");
            put(73, "Alarm! Mower in motion");
            put(74, "Alarm! Outside geofence");
            put(75, "Connection changed");
            put(76, "Connection NOT changed");
            put(77, "Com board not available");
            put(78, "Slipped - Mower has Slipped.Situation not solved with moving pattern");
            put(79, "Invalid battery combination - Invalid combination of different battery types.");
            put(80, "Cutting system imbalance Warning");
            put(81, "Safety function faulty");
            put(82, "Wheel motor blocked, rear right");
            put(83, "Wheel motor blocked, rear left");
            put(84, "Wheel drive problem, rear right");
            put(85, "Wheel drive problem, rear left");
            put(86, "Wheel motor overloaded, rear right");
            put(87, "Wheel motor overloaded, rear left");
            put(88, "Angular sensor problem");
            put(89, "Invalid system configuration");
            put(90, "No power in charging station");
            put(91, "Switch cord problem");
            put(92, "Work area not valid");
            put(93, "No accurate position from satellites");
            put(94, "Reference station communication problem");
            put(95, "Folding sensor activated");
            put(96, "Right brush motor overloaded");
            put(97, "Left brush motor overloaded");
            put(98, "Ultrasonic Sensor 1 defect");
            put(99, "Ultrasonic Sensor 2 defect");
            put(100, "Ultrasonic Sensor 3 defect");
            put(101, "Ultrasonic Sensor 4 defect");
            put(102, "Cutting drive motor 1 defect");
            put(103, "Cutting drive motor 2 defect");
            put(104, "Cutting drive motor 3 defect");
            put(105, "Lift Sensor defect");
            put(106, "Collision sensor defect");
            put(107, "Docking sensor defect");
            put(108, "Folding cutting deck sensor defect");
            put(109, "Loop sensor defect");
            put(110, "Collision sensor error");
            put(111, "No confirmed position");
            put(112, "Cutting system major imbalance");
            put(113, "Complex working area");
            put(114, "Too high discharge current");
            put(115, "Too high internal current");
            put(116, "High charging power loss");
            put(117, "High internal power loss");
            put(118, "Charging system problem");
            put(119, "Zone generator problem");
            put(120, "Internal voltage error");
            put(121, "High internal temerature");
            put(122, "CAN error");
            put(123, "Destination not reachable");
            put(124, "Destination blocked");
            put(125, "Battery needs replacement");
            put(126, "Battery near end of life");
            put(127, "Battery problem");
            put(128, "Multiple reference stations detected");
            put(129, "Auxiliary cutting means blocked");
            put(130, "Imbalanced auxiliary cutting disc detected");
            put(131, "Lifted in link arm");
            put(132, "EPOS accessory missing");
            put(133, "Bluetooth com with CS failed");
            put(134, "Invalid SW configuration");
            put(135, "Radar problem");
            put(136, "Work area tampered");
            put(137, "High temperature in cutting motor, right");
            put(138, "High temperature in cutting motor, center");
            put(139, "High temperature in cutting motor, left");
            put(141, "Wheel brush motor problem");
            put(143, "Accessory power problem");
            put(144, "Boundary wire problem");
            put(701, "Connectivity problem");
            put(702, "Connectivity settings restored");
            put(703, "Connectivity problem");
            put(704, "Connectivity problem");
            put(705, "Connectivity problem");
            put(706, "Poor signal quality");
            put(707, "SIM card requires PIN");
            put(708, "SIM card locked");
            put(709, "SIM card not found");
            put(710, "SIM card locked");
            put(711, "SIM card locked");
            put(712, "SIM card locked");
            put(713, "Geofence problem");
            put(714, "Geofence problem");
            put(715, "Connectivity problem");
            put(716, "Connectivity problem");
            put(717, "SMS could not be sent");
            put(724, "Communication circuit board SW must be updated");
        }
    };
}
