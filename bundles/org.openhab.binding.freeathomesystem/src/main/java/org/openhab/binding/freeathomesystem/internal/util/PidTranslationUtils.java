/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeathomesystem.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link PidTranslationUtils} supporting the translation from pairing IDs into openHAB types
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
public class PidTranslationUtils {
    private static final Map<String, PIdContainerClass> mapTranslator;

    public static final String PID_VALUETYPE_BOOLEAN = "PID_VALUETYPE_BOOLEAN";
    public static final String PID_VALUETYPE_DECIMAL = "PID_VALUETYPE_DECIMAL";
    public static final String PID_VALUETYPE_INTEGER = "PID_VALUETYPE_INTIGER";
    public static final String PID_VALUETYPE_STRING = "PID_VALUETYPE_STRING";
    public static final String PID_VALUETYPE_SHUTTERMOVEMENT = "PID_VALUETYPE_SHUTTERMOVEMENT";
    public static final String PID_VALUETYPE_ENUM = "PID_VALUETYPE_ENUM";

    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_MOTION = "Motion";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_WATER = "Water";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_BLINDS = "Blinds";
    public static final String CATEGORY_CONTACT = "Contact";
    public static final String CATEGORY_SWITCH = "Switch";

    private static PIdContainerClass createFreeAtHomePairingIdTranslation(String p1, String p2, String p3, String p4,
            String p5, String p6) {
        return new PIdContainerClass(p1, p2, p3, p4, p5, p6);
    }

    static {
        Map<String, PIdContainerClass> mapDescObj = new HashMap<String, PIdContainerClass>();

        mapDescObj.put("0x0001", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "0", "1",
                "Switch On/Off", "Binary Switch value"));
        mapDescObj.put("0x0002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Timed Start/Stop", "For staircase lighning or movement detection"));
        mapDescObj.put("0x0003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Force-position", "Forces value dependent high priority on or off state"));
        mapDescObj.put("0x0004", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Scene Control", "Recall or learn the set value related to encoded scene number"));
        mapDescObj.put("0x0006",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, null, "", "",
                        "Movement under consideration of brightness",
                        "Activation of an autonomous switch off function triggered by an movement detector"));
        mapDescObj.put("0x0007", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, null, "", "", "Presence",
                "Announces presence triggered by an movement detector to be used by e.g. RTCs. Is independent of brightness and can be used for alerts e.g."));
        mapDescObj.put("0x0010", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Relative Set Value", "Relative dimming value"));
        mapDescObj.put("0x0011", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Absolute Set Value", "Absolute control of the set value"));
        mapDescObj.put("0x0012", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "", "Night",
                "Toggle between day and night (where day = 0 / night = 1)"));
        mapDescObj.put("0x0013", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, null, "", "",
                "invalid string id", "Resets load failures / short circuits / etc"));
        mapDescObj.put("0x0015", createFreeAtHomePairingIdTranslation(null, null, "", "", "RGB color",
                "RGB Color coded in three bytes"));
        mapDescObj.put("0x0016",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Color Temperature", "Color temperature"));
        mapDescObj.put("0x0017", createFreeAtHomePairingIdTranslation(null, null, "", "", "HSV",
                "Hue (2 Byte) / Saturation (1 Byte) / Value (1 Byte / brightness)"));
        mapDescObj.put("0x0018", createFreeAtHomePairingIdTranslation(null, null, "", "", "HUE", "Hue (2 Byte)"));
        mapDescObj.put("0x0019",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Saturation", "Saturation (1 Byte)"));
        mapDescObj.put("0x0020", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Move Up/Down", "Moves sunblind up (0) and down (1)"));
        mapDescObj.put("0x0021", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Adjust Up/Down", "Stops the sunblind and to step it up/down"));
        mapDescObj.put("0x0023", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "Set Absolute Position Blinds", "Moves the sunblinds into a specified position"));
        mapDescObj.put("0x0024", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "Set Absolute Position Slats", "Moves the slats into a specified position"));
        mapDescObj.put("0x0025", createFreeAtHomePairingIdTranslation(null, null, "", "", "Wind Alarm",
                "State of the wind sensor (sent cyclically and on COV) Moves the sunblind to a secure position and to block it for any further control"));
        mapDescObj.put("0x0026", createFreeAtHomePairingIdTranslation(null, null, "", "", "Frost Alarm",
                "State of the frost sensor (sent cyclically and on COV) Moves the sunblind to a secure position and to block it for any further control"));
        mapDescObj.put("0x0027", createFreeAtHomePairingIdTranslation(null, null, "", "", "Rain Alarm",
                "State of the rain sensor (sent cyclically and on COV)"));
        mapDescObj.put("0x0028", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, null, "", "",
                "Force-position blind", "Forces value dependent high priority up or down state"));
        mapDescObj.put("0x0029", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, null, "", "",
                "Window/Door position", "Delivers position for Window/Door (Open / Tilted / Closed)"));
        mapDescObj.put("0x0030", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Actuating Value Heating", "Determines the through flow volume of the control valve"));
        mapDescObj.put("0x0031", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Fan Level Heating", "Display value of the fan coil speed. (0=off / 1=lowest - 5=fastest)"));
        mapDescObj.put("0x0032", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Actuating Value Cooling", "Determines the through flow volume of the control valve"));
        mapDescObj.put("0x0033", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Set Value Temperature", "Defines the displayed set point temperature of the system"));
        mapDescObj.put("0x0034", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Relative Set Point Temperature", "Defines the relative set point temperature of the system"));
        mapDescObj.put("0x0035", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, null, "", "", "Window/Door",
                "Open = 1 / closed = 0"));
        mapDescObj.put("0x0036", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Status indication", "states: on/off heating/cooling; eco/comfort; frost/not frost"));
        mapDescObj.put("0x0037", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Fan Manual Heating On/Off", "Switches Fan in manual control mode (master to slave)"));
        mapDescObj.put("0x0038", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Controller On/Off", "Switches controller on or off. Off means protection mode"));
        mapDescObj.put("0x0039", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Relative Set Point Request", "Request for a new relative set point value"));
        mapDescObj.put("0x003A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Eco mode On/Off Request", "Switches eco mode on or off"));
        mapDescObj.put("0x003B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Comfort Temperature", "Sends the current comfort temperature"));
        mapDescObj.put("0x0040", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Fan Level Request", "Request for a new manual fan stage"));
        mapDescObj.put("0x0041", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Fan Manual On/Off Request", "WARNING: DO NOT USE!!!! Request for switching fan in manual/auto mode"));
        mapDescObj.put("0x0042", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Controller On/Off Request", "Request for switching controller on or off. Off means protection mode"));
        mapDescObj.put("0x0044", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Eco mode On/Off Request", "Indicates ECO mode"));
        mapDescObj.put("0x0100", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Info On/Off", "Reflects the binary state of the actuator"));
        mapDescObj.put("0x0101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, null, "", "",
                "Force-position info", "Indicates the cause of forced operation (0 = not forced)"));
        mapDescObj.put("0x0105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, null, "", "",
                "SysAP-InfoOnOff", "Reflects the binary state of the actuator group"));
        mapDescObj.put("0x0106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "SysAP-InfoForce", "Indicates whether the actuator group is forced (1) or not forced (0)"));
        mapDescObj.put("0x0110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "Info Actual Dimming Value", "Reflects the actual value of the actuator"));
        mapDescObj.put("0x0111", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "", "Info Error",
                "Indicates load failures / short circuits / etc"));
        mapDescObj.put("0x0115", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "SysAP-InfoCurrentDimmingValue", "Reflects the actual value of the actuator group"));
        mapDescObj.put("0x0116", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "",
                "SysAP-InfoError", "Indicates load failures / short circuits / etc"));
        mapDescObj.put("0x0118", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Color Temperature",
                "Color temperature"));
        mapDescObj.put("0x011A", createFreeAtHomePairingIdTranslation(null, null, "", "",
                "SysAP-Info Color Temperature", "Color temperature"));
        mapDescObj.put("0x011B", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info HSV",
                "Hue (2 Byte) Saturation (1 Byte); Value (1 Byte - brightness)"));
        mapDescObj.put("0x011C", createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP Info HSV",
                "Hue (2 Byte) Saturation (1 Byte); Value (1 Byte - brightness)"));
        mapDescObj.put("0x011D",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Color Mode", "hsv or ct"));
        mapDescObj.put("0x011E",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP Info Color Mode", "hsv or ct"));
        mapDescObj.put("0x0120", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Info Move Up/Down", "Indicates last moving direction and whether moving currently or not"));
        mapDescObj.put("0x0121",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "", "",
                        "Current Absolute Position Blinds Percentage",
                        "Indicate the current position of the sunblinds in percentage"));
        mapDescObj.put("0x0122",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Current Absolute Position Slats Percentage",
                        "Indicate the current position of the slats in percentage"));
        mapDescObj.put("0x0125", createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP-InfoMoveUpDown",
                "Indicates last moving direction and whether moving currently or not of the actuator group"));
        mapDescObj.put("0x0126",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP-InfoCurrentAbsoluteBlindsPercentage",
                        "indicate the current position of the sunblinds in percentage of the actuator group"));
        mapDescObj.put("0x0127",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP-InfoCurrentAbsoluteSlatsPercentage",
                        "indicate the current position of the slats in percentage of the actuator group"));
        mapDescObj.put("0x0130", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Measured Temperature", "Indicates the actual measured temperature"));
        mapDescObj.put("0x0131", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Value Heating",
                "States the current flow volume of the conrol valve"));
        mapDescObj.put("0x0132", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info value cooling",
                "States the current flow volume of the conrol valve"));
        mapDescObj.put("0x0135", createFreeAtHomePairingIdTranslation(null, null, "", "", "Switchover heating/cooling",
                "switch between heating and cooling: heating = 0 / cooling = 1"));
        mapDescObj.put("0x0136", createFreeAtHomePairingIdTranslation(null, null, "", "", "Actuating Fan Stage Heating",
                "Requests a new manual fan stage from actuator in heating mode"));
        mapDescObj.put("0x0140", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Absolute setpoint temperature", "Absolute set point temperature input for timer"));
        mapDescObj.put("0x0141",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Additional heating value info", "Feedback"));
        mapDescObj.put("0x0142",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Additional cooling value info", "Feedback"));
        mapDescObj.put("0x0143",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Control value additional heating", ""));
        mapDescObj.put("0x0144",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Control value additional cooling", ""));
        mapDescObj.put("0x0145", createFreeAtHomePairingIdTranslation(null, null, "", "",
                "Info Actuating Fan Stage Heating", "Feedback from FCA"));
        mapDescObj.put("0x0146", createFreeAtHomePairingIdTranslation(null, null, "", "",
                "Info Actuating Fan Manual On/Off Heating", "Feedback from FCA"));
        mapDescObj.put("0x0147", createFreeAtHomePairingIdTranslation(null, null, "", "", "Actuating Fan Stage Cooling",
                "Requests a new manual fan stage from actuator in cooling mode"));
        mapDescObj.put("0x0149", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Fan Stage Cooling",
                "Feedback for current fan stage in cooling mode"));
        mapDescObj.put("0x014A", createFreeAtHomePairingIdTranslation(null, null, "", "",
                "Info Fan Manual On/Off Cooling", "Feedback for manual fan control cooling mode"));
        mapDescObj.put("0x014B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Heating active", ""));
        mapDescObj.put("0x014C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Cooling active", ""));
        mapDescObj.put("0x014D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Heating demand", ""));
        mapDescObj.put("0x014E",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, null, "", "", "Cooling demand", ""));
        mapDescObj.put("0x014F",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Heating demand feedback signal", ""));
        mapDescObj.put("0x0150",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Cooling demand feedback signal", ""));
        mapDescObj.put("0x0151",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Humidity", "Measured Humidity"));
        mapDescObj.put("0x0152",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Aux On/Off request", "Aux On/Off request"));
        mapDescObj.put("0x0153",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Aux On/Off response", "Aux On/Off response"));
        mapDescObj.put("0x0154", createFreeAtHomePairingIdTranslation(null, null, "", "", "Heating On/Off request",
                "Heating On/Off request"));
        mapDescObj.put("0x0155", createFreeAtHomePairingIdTranslation(null, null, "", "", "Cooling On/Off request",
                "Cooling On/Off request"));
        mapDescObj.put("0x0156", createFreeAtHomePairingIdTranslation(null, null, "", "", "Operation mode", ""));
        mapDescObj.put("0x0157", createFreeAtHomePairingIdTranslation(null, null, "", "", "Swing H/V", ""));
        mapDescObj.put("0x0158", createFreeAtHomePairingIdTranslation(null, null, "", "", "Supported features", ""));
        mapDescObj.put("0x0159",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Extended Status Indication", ""));
        mapDescObj.put("0x015A",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Extended Status Indication", ""));
        mapDescObj.put("0x015B",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Aux Heating On Off Request", ""));
        mapDescObj.put("0x015C",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Emergency Heating On Off Request", ""));
        mapDescObj.put("0x0160", createFreeAtHomePairingIdTranslation(null, null, "", "", "Relative fan speed control",
                "Relative control of the set value"));
        mapDescObj.put("0x0161", createFreeAtHomePairingIdTranslation(null, null, "", "", "Absolute fan speed control",
                "Absolute control of the set value"));
        mapDescObj.put("0x0162", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info absolute fan speed",
                "Reflects the actual value of the actuator"));
        mapDescObj.put("0x0163", createFreeAtHomePairingIdTranslation(null, null, "", "", "SysAP-InfoActualFanSpeed",
                "Reflects the actual value of the actuator"));
        mapDescObj.put("0x01A0", createFreeAtHomePairingIdTranslation(null, null, "", "", "Notification flags",
                "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0x0280", createFreeAtHomePairingIdTranslation(null, null, "", "", "Power RC", "Bool Value 1"));
        mapDescObj.put("0x0281", createFreeAtHomePairingIdTranslation(null, null, "", "", "Power RH", "Bool Value 2"));
        mapDescObj.put("0x0282",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Proximity status", "Bool Value 3"));
        mapDescObj.put("0x0290",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Brightness sensor", "Scaling Value 1"));
        mapDescObj.put("0x0291",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Last touch", "Scaling Value 2"));
        mapDescObj.put("0x0292", createFreeAtHomePairingIdTranslation(null, null, "", "", "LED backlighting night mode",
                "Scaling Value 3"));
        mapDescObj.put("0x02C0",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Locator beep", "Locator Beep"));
        mapDescObj.put("0x02C1",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Switch test alarm", "Switch Test Alarm"));
        mapDescObj.put("0x02C3",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Fire alarm active", "Fire-Alarm Active"));
        mapDescObj.put("0x0400",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Outside temperature", "Outdoor Temperature"));
        mapDescObj.put("0x0401", createFreeAtHomePairingIdTranslation(null, null, "", "", "Wind force", "Wind force"));
        mapDescObj.put("0x0402",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Brightness alarm", "Brightness alarm"));
        mapDescObj.put("0x0403", createFreeAtHomePairingIdTranslation(null, null, "", "", "Lux value",
                "Weatherstation brightness level"));
        mapDescObj.put("0x0404", createFreeAtHomePairingIdTranslation(null, null, "", "", "Wind speed", "Wind speed"));
        mapDescObj.put("0x0405", createFreeAtHomePairingIdTranslation(null, null, "", "", "Rain detection", ""));
        mapDescObj.put("0x0406", createFreeAtHomePairingIdTranslation(null, null, "", "", "Rain sensor frequency", ""));
        mapDescObj.put("0x0440", createFreeAtHomePairingIdTranslation(null, null, "", "", "Play", "Start playing"));
        mapDescObj.put("0x0441",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Pause", "Pause/Stop playing"));
        mapDescObj.put("0x0442", createFreeAtHomePairingIdTranslation(null, null, "", "", "Next", "Play next title"));
        mapDescObj.put("0x0443",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Previous", "Play previous title"));
        mapDescObj.put("0x0444",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Play mode", "Play mode (shuffle / repeat)"));
        mapDescObj.put("0x0445",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Mute", "Mute (1) and unmute (0) a player"));
        mapDescObj.put("0x0446", createFreeAtHomePairingIdTranslation(null, null, "", "", "Relative volume control",
                "Relative volume control. See also relative dimming"));
        mapDescObj.put("0x0447", createFreeAtHomePairingIdTranslation(null, null, "", "", "Absolute volume control",
                "Set player volume"));
        mapDescObj.put("0x0448", createFreeAtHomePairingIdTranslation(null, null, "", "", "Group membership", ""));
        mapDescObj.put("0x0449", createFreeAtHomePairingIdTranslation(null, null, "", "", "Play favorite", ""));
        mapDescObj.put("0x044A", createFreeAtHomePairingIdTranslation(null, null, "", "", "Play next favorite", ""));
        mapDescObj.put("0x0460", createFreeAtHomePairingIdTranslation(null, null, "", "", "Playback status", ""));
        mapDescObj.put("0x0461",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Current item metadata info", ""));
        mapDescObj.put("0x0462", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info mute", ""));
        mapDescObj.put("0x0463", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info actual volume", ""));
        mapDescObj.put("0x0464",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Allowed playback actions", ""));
        mapDescObj.put("0x0465", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info group membership", ""));
        mapDescObj.put("0x0466", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info playing favorite", ""));
        mapDescObj.put("0x0467",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Absolute Group Volume Control", ""));
        mapDescObj.put("0x0468",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Absolute Group Volume", ""));
        mapDescObj.put("0x0469", createFreeAtHomePairingIdTranslation(null, null, "", "", "Media source", ""));
        mapDescObj.put("0x04A0", createFreeAtHomePairingIdTranslation(null, null, "", "", "Solar power production",
                "Power from the sun"));
        mapDescObj.put("0x04A1", createFreeAtHomePairingIdTranslation(null, null, "", "", "Inverter output power",
                "Output power of inverter (pbatt+Psun)"));
        mapDescObj.put("0x04A2",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Solar energy (today)", "Produced Energy"));
        mapDescObj.put("0x04A3", createFreeAtHomePairingIdTranslation(null, null, "", "", "Injected energy (today)",
                "Energy into the grid"));
        mapDescObj.put("0x04A4", createFreeAtHomePairingIdTranslation(null, null, "", "", "Purchased energy (today)",
                "Energy from the grid"));
        mapDescObj.put("0x04A5", createFreeAtHomePairingIdTranslation(null, null, "", "", "Inverter alarm",
                "Inverter is working in stand alone mode"));
        mapDescObj.put("0x04A6", createFreeAtHomePairingIdTranslation(null, null, "", "", "Self-consumption",
                "production PV/ Total consumption"));
        mapDescObj.put("0x04A7", createFreeAtHomePairingIdTranslation(null, null, "", "", "Self-sufficiency",
                "Consumption from PV/ Total consumption"));
        mapDescObj.put("0x04A8", createFreeAtHomePairingIdTranslation(null, null, "", "", "Home power consumption",
                "Power in home (PV and grid)"));
        mapDescObj.put("0x04A9", createFreeAtHomePairingIdTranslation(null, null, "", "", "Power to grid",
                "Power from and to the grid: Purchased (less than 0), Injection (more than 0)"));
        mapDescObj.put("0x04AA", createFreeAtHomePairingIdTranslation(null, null, "", "", "Consumed energy (today)",
                "Energy bought from grid per day"));
        mapDescObj.put("0x04AB",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Meter alarm", "Meter communication loss"));
        mapDescObj.put("0x04AC",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Battery level", "Battery level"));
        mapDescObj.put("0x04AD", createFreeAtHomePairingIdTranslation(null, null, "", "", "Battery power",
                "Batter power: Discharge (less then 0), Charge (more then 0)"));
        mapDescObj.put("0x04B0", createFreeAtHomePairingIdTranslation(null, null, "", "", "Boost",
                "1: Boost enable request, 0: boost disable request"));
        mapDescObj.put("0x04B1", createFreeAtHomePairingIdTranslation(null, null, "", "", "Stop charging reuqest",
                "1: Stop charging session requested, 0: n/a so far, will be resetted when cable is unplugged"));
        mapDescObj.put("0x04B2", createFreeAtHomePairingIdTranslation(null, null, "", "", "Enable charging reuqest",
                "1: Enable charging when cable is plugged in, 0: Disable next charging session but charge until cable is plugged"));
        mapDescObj.put("0x04B3", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info boost",
                "1: Boost enabled, 0: boost disabled"));
        mapDescObj.put("0x04B4", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info wallbox status",
                "Wallbox status 00000001: car plugged in, 00000002: Authorization granted, 00000004: Not charging, battery fully loaded, 40000000: charging stopped due to blackout prevention, 80000000: Ground fault error"));
        mapDescObj.put("0x04B5", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info charging",
                "1: Charging, 0: Not charging"));
        mapDescObj.put("0x04B6", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info charging enabled",
                "1: Charging enabled for next session, 0: Charging disabled for next session"));
        mapDescObj.put("0x04B7", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info installed power",
                "Installed power (e.g. 20 kW)"));
        mapDescObj.put("0x04B8", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info transmitted energy",
                "Energy transmitted so far per session (in Wh)"));
        mapDescObj.put("0x04B9", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info car range",
                "Car range in km per sessions"));
        mapDescObj.put("0x04BA", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info charging duration",
                "Start of charging session (in minutes in UTC)"));
        mapDescObj.put("0x04BB", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info current limit",
                "Limit for charger (in kW)"));
        mapDescObj.put("0x04BC", createFreeAtHomePairingIdTranslation(null, null, "", "",
                "Info current limit for group", "Limit for group of charger (in kW)"));
        mapDescObj.put("0x04BD",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Album cover URL", "Album cover URL"));
        mapDescObj.put("0x0501", createFreeAtHomePairingIdTranslation(null, null, "", "", "secure@home Central Unit",
                "Encrypted control datapoint for domus alarm center"));
        mapDescObj.put("0x0502", createFreeAtHomePairingIdTranslation(null, null, "", "", "DomusDisarmCounter",
                "Info about the next counter to disarm the system"));
        mapDescObj.put("0x0504",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Intrusion Alarm", "Intrusion Alarm"));
        mapDescObj.put("0x0505",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Safety Alarm", "Safety Alarm"));
        mapDescObj.put("0x0507", createFreeAtHomePairingIdTranslation(null, null, "", "", "InfoConfigurationStatus",
                "Domus alarm device negative feedback and configuration info."));
        mapDescObj.put("0x0508", createFreeAtHomePairingIdTranslation(null, null, "", "", "Enable configuration",
                "Encrypted control datapoint for entering configuration mode"));
        mapDescObj.put("0x0509",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Disarming LED", "Arm/Disarm a Zone"));
        mapDescObj.put("0x050A", createFreeAtHomePairingIdTranslation(null, null, "", "", "AES Key",
                "Manufacturer ID + Serial + AES Key"));
        mapDescObj.put("0x050B",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Zone status", "Zone status"));
        mapDescObj.put("0x050E", createFreeAtHomePairingIdTranslation(null, null, "", "", "Time",
                "Absolute number of seconds when the zone will be armed"));
        mapDescObj.put("0x0600",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Start / Stop", "Starts / Stops operation"));
        mapDescObj.put("0x0601", createFreeAtHomePairingIdTranslation(null, null, "", "", "Pause / Resume", ""));
        mapDescObj.put("0x0602", createFreeAtHomePairingIdTranslation(null, null, "", "", "Select program", ""));
        mapDescObj.put("0x0603", createFreeAtHomePairingIdTranslation(null, null, "", "", "Delayed start time", ""));
        mapDescObj.put("0x0604", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info status", ""));
        mapDescObj.put("0x0605",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Info remote start enabled", ""));
        mapDescObj.put("0x0606", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info program", ""));
        mapDescObj.put("0x0607", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info finish time", ""));
        mapDescObj.put("0x0608", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info delayed start", ""));
        mapDescObj.put("0x0609", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info door", ""));
        mapDescObj.put("0x060A", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info door alarm", ""));
        mapDescObj.put("0x060B", createFreeAtHomePairingIdTranslation(null, null, "", "", "Switch supercool", ""));
        mapDescObj.put("0x060C", createFreeAtHomePairingIdTranslation(null, null, "", "", "Switch superfreeze", ""));
        mapDescObj.put("0x060D", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info switch supercool", ""));
        mapDescObj.put("0x060E",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Info switch superfreeze", ""));
        mapDescObj.put("0x060F", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured Temperature", ""));
        mapDescObj.put("0x0610", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured Temperature", ""));
        mapDescObj.put("0x0611", createFreeAtHomePairingIdTranslation(null, null, "", "", "Set Value Temperature", ""));
        mapDescObj.put("0x0612", createFreeAtHomePairingIdTranslation(null, null, "", "", "Set Value Temperature", ""));
        mapDescObj.put("0x0613", createFreeAtHomePairingIdTranslation(null, null, "", "", "Change operation", ""));
        mapDescObj.put("0x0614", createFreeAtHomePairingIdTranslation(null, null, "", "", "Detailed status info", ""));
        mapDescObj.put("0x0615", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info remaining time",
                "Remaining time till status change (start, finish, etc.)"));
        mapDescObj.put("0x0616",
                createFreeAtHomePairingIdTranslation(null, null, "", "",
                        "Time of last status change (start, finish, etc.)",
                        "Time of last status change (start, finish, etc.)"));
        mapDescObj.put("0x0618", createFreeAtHomePairingIdTranslation(null, null, "", "", "Lock/Unlock door command",
                "Lock/Unlock door command (1 Bit)"));
        mapDescObj.put("0x0619", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info Locked / Unlocked",
                "Info Lock/Unlock door(1 Bit)"));
        mapDescObj.put("0xF001",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Time", "Current local time"));
        mapDescObj.put("0xF002", createFreeAtHomePairingIdTranslation(null, null, "", "", "Date", "Curent local date"));
        mapDescObj.put("0xF003", createFreeAtHomePairingIdTranslation(null, null, "", "", "Notification",
                "Notification from message center"));
        mapDescObj.put("0xF101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Switch entity On/Off", "Entity control e.g. activate an alert or timer program"));
        mapDescObj.put("0xF102", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Info switch entity On/Off", "Reflects the active state of an entity e.g. alert or timer program"));
        mapDescObj.put("0xF104", createFreeAtHomePairingIdTranslation(null, null, "", "", "Consistency Tag",
                "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF105", createFreeAtHomePairingIdTranslation(null, null, "", "", "Battery Status",
                "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF106", createFreeAtHomePairingIdTranslation(null, null, "", "", "Stay awake!",
                "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF107", createFreeAtHomePairingIdTranslation(null, null, "", "", "Proxy switch", ""));
        mapDescObj.put("0xF108", createFreeAtHomePairingIdTranslation(null, null, "", "", "Proxy, 1 byte", ""));
        mapDescObj.put("0xF109", createFreeAtHomePairingIdTranslation(null, null, "", "", "Proxy, 2 byte", ""));
        mapDescObj.put("0xF10A", createFreeAtHomePairingIdTranslation(null, null, "", "", "Proxy, 4 byte", ""));
        mapDescObj.put("0xF10B",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Cyclic sleep time", "Time of sleep cycles"));
        mapDescObj.put("0xF10C",
                createFreeAtHomePairingIdTranslation(null, null, "", "", "Presence", "SysAP presence"));
        mapDescObj.put("0xF10D", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured temperature 1",
                "SysAP temperature"));
        mapDescObj.put("0xF10E", createFreeAtHomePairingIdTranslation(null, null, "", "", "Standby Statistics",
                "Statistics about standby usage for battery devices"));
        mapDescObj.put("0xF10F", createFreeAtHomePairingIdTranslation(null, null, "", "", "Heartbeat delay",
                "Time period between two heartbeats"));
        mapDescObj.put("0xF110", createFreeAtHomePairingIdTranslation(null, null, "", "", "Info heartbeat delay",
                "Time period between two heartbeats"));
        mapDescObj.put("0xFF01", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured temperature 1",
                "For debug purposes"));
        mapDescObj.put("0xFF02", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured temperature 2",
                "For debug purposes"));
        mapDescObj.put("0xFF03", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured temperature 3",
                "For debug purposes"));
        mapDescObj.put("0xFF04", createFreeAtHomePairingIdTranslation(null, null, "", "", "Measured temperature 4",
                "For debug purposes"));

        mapTranslator = Collections.unmodifiableMap(mapDescObj);
    }

    public static String getShortTextForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.Label;
    }

    public static String getDescriptionTextForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.Descprition;
    }

    public static String getValueTypeForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.valueType;
    }

    public static String getItemTypeForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.category;
    }

    public static String getCategoryForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.category;
    }

    public static String getPatternForPairingId(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.category;
    }

    public static int getMax(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.max;
    }

    public static int getMin(String Key) {
        PIdContainerClass desc = mapTranslator.get(Key);

        return desc.min;
    }
}
