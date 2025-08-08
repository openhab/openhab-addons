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
import org.openhab.core.thing.type.ChannelTypeUID;

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
    public static final String GROUP_STATUS = "status#";

    public static final String CHANNEL_STATUS_NAME = GROUP_STATUS + "name";
    public static final String CHANNEL_STATUS_MODE = GROUP_STATUS + "mode";
    public static final String CHANNEL_STATUS_ACTIVITY = GROUP_STATUS + "activity";
    public static final String CHANNEL_STATUS_INACTIVE_REASON = GROUP_STATUS + "inactive-reason";
    public static final String CHANNEL_STATUS_STATE = GROUP_STATUS + "state";
    public static final String CHANNEL_STATUS_LAST_UPDATE = GROUP_STATUS + "last-update";
    public static final String CHANNEL_STATUS_LAST_POLL_UPDATE = GROUP_STATUS + "last-poll-update";
    public static final String CHANNEL_STATUS_POLL_UPDATE = GROUP_STATUS + "poll-update";
    public static final String CHANNEL_STATUS_WORK_AREA_ID = GROUP_STATUS + "work-area-id";
    public static final String CHANNEL_STATUS_WORK_AREA = GROUP_STATUS + "work-area";
    public static final String CHANNEL_STATUS_BATTERY = GROUP_STATUS + "battery";
    public static final String CHANNEL_STATUS_ERROR_CODE = GROUP_STATUS + "error-code";
    public static final String CHANNEL_STATUS_ERROR_MESSAGE = GROUP_STATUS + "error-message";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = GROUP_STATUS + "error-timestamp";
    public static final String CHANNEL_STATUS_ERROR_CONFIRMABLE = GROUP_STATUS + "error-confirmable";
    public static final String CHANNEL_STATUS_NEXT_START = GROUP_STATUS + "next-start";
    public static final String CHANNEL_STATUS_OVERRIDE_ACTION = GROUP_STATUS + "override-action";
    public static final String CHANNEL_STATUS_RESTRICTED_REASON = GROUP_STATUS + "restricted-reason";
    public static final String CHANNEL_STATUS_EXTERNAL_REASON = GROUP_STATUS + "external-reason";
    public static final String CHANNEL_STATUS_POSITION = GROUP_STATUS + "position";

    // List of all setting Channel ids
    public static final String GROUP_SETTING = "setting#";

    public static final String CHANNEL_SETTING_CUTTING_HEIGHT = GROUP_SETTING + "cutting-height";
    public static final String CHANNEL_SETTING_HEADLIGHT_MODE = GROUP_SETTING + "headlight-mode";

    // List of all setting Channel ids
    public static final String GROUP_STATISTIC = "statistic#";

    public static final String CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME = GROUP_STATISTIC
            + "cutting-blade-usage-time";
    public static final String CHANNEL_STATISTIC_DOWN_TIME = GROUP_STATISTIC + "down-time";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_CHARGING_CYCLES = GROUP_STATISTIC
            + "number-of-charging-cycles";
    public static final String CHANNEL_STATISTIC_NUMBER_OF_COLLISIONS = GROUP_STATISTIC + "number-of-collisions";
    public static final String CHANNEL_STATISTIC_TOTAL_CHARGING_TIME = GROUP_STATISTIC + "total-charging-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_TIME = GROUP_STATISTIC + "total-cutting-time";
    public static final String CHANNEL_STATISTIC_TOTAL_CUTTING_PERCENT = GROUP_STATISTIC + "total-cutting-percent";
    public static final String CHANNEL_STATISTIC_TOTAL_DRIVE_DISTANCE = GROUP_STATISTIC + "total-drive-distance";
    public static final String CHANNEL_STATISTIC_TOTAL_RUNNING_TIME = GROUP_STATISTIC + "total-running-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_TIME = GROUP_STATISTIC + "total-searching-time";
    public static final String CHANNEL_STATISTIC_TOTAL_SEARCHING_PERCENT = GROUP_STATISTIC + "total-searching-percent";
    public static final String CHANNEL_STATISTIC_UP_TIME = GROUP_STATISTIC + "up-time";

    // Calendar Task Channel ids
    public static final String GROUP_CALENDARTASK = "calendartask#";

    public static final String CHANNEL_CALENDARTASK_START = "task-start";
    public static final String CHANNEL_CALENDARTASK_DURATION = "task-duration";
    public static final String CHANNEL_CALENDARTASK_MONDAY = "task-monday";
    public static final String CHANNEL_CALENDARTASK_TUEDAY = "task-tuesday";
    public static final String CHANNEL_CALENDARTASK_WEDNESDAY = "task-wednesday";
    public static final String CHANNEL_CALENDARTASK_THURSRAY = "task-thursday";
    public static final String CHANNEL_CALENDARTASK_FRIDAY = "task-friday";
    public static final String CHANNEL_CALENDARTASK_SATURDAY = "task-saturday";
    public static final String CHANNEL_CALENDARTASK_SUNDAY = "task-sunday";
    public static final String CHANNEL_CALENDARTASK_WORKAREAID = "task-workareaid";
    public static final String CHANNEL_CALENDARTASK_WORKAREA = "task-workarea";

    public static final ArrayList<String> CHANNEL_CALENDARTASK = new ArrayList<>(
            List.of(CHANNEL_CALENDARTASK_START, CHANNEL_CALENDARTASK_DURATION, CHANNEL_CALENDARTASK_MONDAY,
                    CHANNEL_CALENDARTASK_TUEDAY, CHANNEL_CALENDARTASK_WEDNESDAY, CHANNEL_CALENDARTASK_THURSRAY,
                    CHANNEL_CALENDARTASK_FRIDAY, CHANNEL_CALENDARTASK_SATURDAY, CHANNEL_CALENDARTASK_SUNDAY,
                    CHANNEL_CALENDARTASK_WORKAREAID, CHANNEL_CALENDARTASK_WORKAREA));

    public static final ArrayList<ChannelTypeUID> CHANNEL_TYPE_CALENDARTASK = new ArrayList<>(
            List.of(new ChannelTypeUID(BINDING_ID, "calendarTaskStartType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskDurationType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskMondayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskTuesdayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskWednesdayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskThursdayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskFridayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskSaturdayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskSundayType"),
                    new ChannelTypeUID(BINDING_ID, "calendarTaskWorkAreaIdType"),
                    new ChannelTypeUID(BINDING_ID, "workareaNameType")));

    // Stayout Zones Channel ids
    public static final String GROUP_STAYOUTZONE = "stayoutzone#";

    public static final String CHANNEL_STAYOUTZONE_DIRTY = GROUP_STAYOUTZONE + "dirty";
    public static final String CHANNEL_STAYOUTZONE_ENABLED = "zone-enabled";
    public static final ArrayList<String> CHANNEL_STAYOUTZONE = new ArrayList<>(
            List.of("zone-id", "zone-name", CHANNEL_STAYOUTZONE_ENABLED));

    public static final ArrayList<ChannelTypeUID> CHANNEL_TYPE_STAYOUTZONE = new ArrayList<>(
            List.of(new ChannelTypeUID(BINDING_ID, "zoneIdType"), new ChannelTypeUID(BINDING_ID, "zoneNameType"),
                    new ChannelTypeUID(BINDING_ID, "zoneEnabledType")));

    // Work Areas Channel ids
    public static final String GROUP_WORKAREA = "workarea#";

    public static final String CHANNEL_WORKAREA_CUTTING_HEIGHT = "area-cutting-height";
    public static final String CHANNEL_WORKAREA_ENABLED = "area-enabled";
    public static final ArrayList<String> CHANNEL_WORKAREA = new ArrayList<>(List.of("area-id", "area-name",
            CHANNEL_WORKAREA_CUTTING_HEIGHT, CHANNEL_WORKAREA_ENABLED, "area-progress", "area-last-time-completed"));

    public static final ArrayList<ChannelTypeUID> CHANNEL_TYPE_WORKAREA = new ArrayList<>(List.of(
            new ChannelTypeUID(BINDING_ID, "workareaIdType"), new ChannelTypeUID(BINDING_ID, "workareaNameType"),
            new ChannelTypeUID(BINDING_ID, "workareaCuttingHeightType"),
            new ChannelTypeUID(BINDING_ID, "workareaEnabledType"),
            new ChannelTypeUID(BINDING_ID, "workareaProgressType"),
            new ChannelTypeUID(BINDING_ID, "workareaLastTimeCompletedType")));

    // Messages Channel ids
    public static final String GROUP_MESSAGE = "message#";

    public static final String CHANNEL_MESSAGE_TIMESTAMP = GROUP_MESSAGE + "msg-timestamp";
    public static final String CHANNEL_MESSAGE_CODE = GROUP_MESSAGE + "msg-code";
    public static final String CHANNEL_MESSAGE_TEXT = GROUP_MESSAGE + "msg-text";
    public static final String CHANNEL_MESSAGE_SEVERITY = GROUP_MESSAGE + "msg-severity";
    public static final String CHANNEL_MESSAGE_GPS_POSITION = GROUP_MESSAGE + "msg-gps-position";

    // Command Channel ids
    public static final String GROUP_COMMAND = "command#";

    public static final String CHANNEL_COMMAND_START = GROUP_COMMAND + "start";
    public static final String CHANNEL_COMMAND_START_IN_WORK_AREA = GROUP_COMMAND + "start_in_work_area";
    public static final String CHANNEL_COMMAND_RESUME_SCHEDULE = GROUP_COMMAND + "resume_schedule";
    public static final String CHANNEL_COMMAND_PAUSE = GROUP_COMMAND + "pause";
    public static final String CHANNEL_COMMAND_PARK = GROUP_COMMAND + "park";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NEXT_SCHEDULE = GROUP_COMMAND + "park_until_next_schedule";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NOTICE = GROUP_COMMAND + "park_until_further_notice";

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

    // Channel Types of dynamic channels
    public static final ChannelTypeUID CHANNEL_TYPE_STATUS_WORK_AREA_ID = new ChannelTypeUID(BINDING_ID,
            "workAreaIdType");
    public static final ChannelTypeUID CHANNEL_TYPE_STATUS_WORK_AREA = new ChannelTypeUID(BINDING_ID, "workAreaType");
    public static final ChannelTypeUID CHANNEL_TYPE_STATUS_ERROR_CONFIRMABLE = new ChannelTypeUID(BINDING_ID,
            "errorConfirmableType");
    public static final ChannelTypeUID CHANNEL_TYPE_STATUS_POSITION = new ChannelTypeUID(BINDING_ID, "positionType");
    public static final ChannelTypeUID CHANNEL_TYPE_SETTING_HEADLIGHT_MODE = new ChannelTypeUID(BINDING_ID,
            "settingHeadlightModeType");

    public static final ChannelTypeUID CHANNEL_TYPE_STAYOUTZONES_DIRTY = new ChannelTypeUID(BINDING_ID,
            "zoneDirtyType");

    public static final Map<Integer, String> ERROR = new HashMap<>() {
        private static final long serialVersionUID = 1L;
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
            put(78, "Slipped - Mower has Slipped. Situation not solved with moving pattern");
            put(79, "Invalid battery combination - Invalid combination of different battery types");
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
