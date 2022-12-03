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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PidTranslationUtils} supporting the translation from pairing IDs into openHAB types
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class PidTranslationUtils {
    private static final Map<String, PIdContainerClass> MAP_TRANSLATOR;

    public static final String PID_VALUETYPE_UNKNOWN = "PID_VALUETYPE_UNKNOWN";
    public static final String PID_VALUETYPE_BOOLEAN = "PID_VALUETYPE_BOOLEAN";
    public static final String PID_VALUETYPE_DECIMAL = "PID_VALUETYPE_DECIMAL";
    public static final String PID_VALUETYPE_INTEGER = "PID_VALUETYPE_INTIGER";
    public static final String PID_VALUETYPE_STRING = "PID_VALUETYPE_STRING";
    public static final String PID_VALUETYPE_SHUTTERMOVEMENT = "PID_VALUETYPE_SHUTTERMOVEMENT";
    public static final String PID_VALUETYPE_ENUM = "PID_VALUETYPE_ENUM";

    public static final String CATEGORY_UNDEFINED = "-";
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
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                        "Movement under consideration of brightness",
                        "Activation of an autonomous switch off function triggered by an movement detector"));
        mapDescObj.put("0x0007", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "Presence",
                "Announces presence triggered by an movement detector to be used by e.g. RTCs. Is independent of brightness and can be used for alerts e.g."));
        mapDescObj.put("0x0010", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Relative Set Value", "Relative dimming value"));
        mapDescObj.put("0x0011", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Absolute Set Value", "Absolute control of the set value"));
        mapDescObj.put("0x0012", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Night", "Toggle between day and night (where day = 0 / night = 1)"));
        mapDescObj.put("0x0013", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, CATEGORY_UNDEFINED, "", "",
                "invalid string id", "Resets load failures / short circuits / etc"));
        mapDescObj.put("0x0015", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "RGB color", "RGB Color coded in three bytes"));
        mapDescObj.put("0x0016", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Color Temperature", "Color temperature"));
        mapDescObj.put("0x0017", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "HSV", "Hue (2 Byte) / Saturation (1 Byte) / Value (1 Byte / brightness)"));
        mapDescObj.put("0x0018", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "HUE", "Hue (2 Byte)"));
        mapDescObj.put("0x0019", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Saturation", "Saturation (1 Byte)"));
        mapDescObj.put("0x0020", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Move Up/Down", "Moves sunblind up (0) and down (1)"));
        mapDescObj.put("0x0021", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Adjust Up/Down", "Stops the sunblind and to step it up/down"));
        mapDescObj.put("0x0023", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "Set Absolute Position Blinds", "Moves the sunblinds into a specified position"));
        mapDescObj.put("0x0024", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "0",
                "100", "Set Absolute Position Slats", "Moves the slats into a specified position"));
        mapDescObj.put("0x0025", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Wind Alarm",
                "State of the wind sensor (sent cyclically and on COV) Moves the sunblind to a secure position and to block it for any further control"));
        mapDescObj.put("0x0026", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Frost Alarm",
                "State of the frost sensor (sent cyclically and on COV) Moves the sunblind to a secure position and to block it for any further control"));
        mapDescObj.put("0x0027", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Rain Alarm", "State of the rain sensor (sent cyclically and on COV)"));
        mapDescObj.put("0x0028", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "Force-position blind", "Forces value dependent high priority up or down state"));
        mapDescObj.put("0x0029", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, CATEGORY_UNDEFINED, "", "",
                "Window/Door position", "Delivers position for Window/Door (Open / Tilted / Closed)"));
        mapDescObj.put("0x0030", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Actuating Value Heating", "Determines the through flow volume of the control valve"));
        mapDescObj.put("0x0031", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Fan Level Heating", "Display value of the fan coil speed. (0=off / 1=lowest - 5=fastest)"));
        mapDescObj.put("0x0032", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Actuating Value Cooling", "Determines the through flow volume of the control valve"));
        mapDescObj.put("0x0033", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Set Value Temperature", "Defines the displayed set point temperature of the system"));
        mapDescObj.put("0x0034", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Relative Set Point Temperature", "Defines the relative set point temperature of the system"));
        mapDescObj.put("0x0035", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_ENUM, CATEGORY_UNDEFINED, "", "",
                "Window/Door", "Open = 1 / closed = 0"));
        mapDescObj.put("0x0036", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Status indication", "states: on/off heating/cooling; eco/comfort; frost/not frost"));
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
        mapDescObj.put("0x0040", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Fan Level Request", "Request for a new manual fan stage"));
        mapDescObj.put("0x0041", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Fan Manual On/Off Request", "WARNING: DO NOT USE!!!! Request for switching fan in manual/auto mode"));
        mapDescObj.put("0x0042", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Controller On/Off Request", "Request for switching controller on or off. Off means protection mode"));
        mapDescObj.put("0x0044", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Eco mode On/Off Request", "Indicates ECO mode"));
        mapDescObj.put("0x0100", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Info On/Off", "Reflects the binary state of the actuator"));
        mapDescObj.put("0x0101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "Force-position info", "Indicates the cause of forced operation (0 = not forced)"));
        mapDescObj.put("0x0105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_UNDEFINED, "", "",
                "SysAP-InfoOnOff", "Reflects the binary state of the actuator group"));
        mapDescObj.put("0x0106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "SysAP-InfoForce", "Indicates whether the actuator group is forced (1) or not forced (0)"));
        mapDescObj.put("0x0110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Info Actual Dimming Value", "Reflects the actual value of the actuator"));
        mapDescObj.put("0x0111", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "Info Error", "Indicates load failures / short circuits / etc"));
        mapDescObj.put("0x0115", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "SysAP-InfoCurrentDimmingValue", "Reflects the actual value of the actuator group"));
        mapDescObj.put("0x0116", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_UNDEFINED, "", "",
                "SysAP-InfoError", "Indicates load failures / short circuits / etc"));
        mapDescObj.put("0x0118", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Color Temperature", "Color temperature"));
        mapDescObj.put("0x011A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "SysAP-Info Color Temperature", "Color temperature"));
        mapDescObj.put("0x011B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info HSV", "Hue (2 Byte) Saturation (1 Byte); Value (1 Byte - brightness)"));
        mapDescObj.put("0x011C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "SysAP Info HSV", "Hue (2 Byte) Saturation (1 Byte); Value (1 Byte - brightness)"));
        mapDescObj.put("0x011D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Color Mode", "hsv or ct"));
        mapDescObj.put("0x011E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "SysAP Info Color Mode", "hsv or ct"));
        mapDescObj.put("0x0120", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_SHUTTERMOVEMENT, CATEGORY_BLINDS,
                "", "", "Info Move Up/Down", "Indicates last moving direction and whether moving currently or not"));
        mapDescObj.put("0x0121",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_BLINDS, "", "",
                        "Current Absolute Position Blinds Percentage",
                        "Indicate the current position of the sunblinds in percentage"));
        mapDescObj.put("0x0122",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "Current Absolute Position Slats Percentage",
                        "Indicate the current position of the slats in percentage"));
        mapDescObj.put("0x0125",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "SysAP-InfoMoveUpDown",
                        "Indicates last moving direction and whether moving currently or not of the actuator group"));
        mapDescObj.put("0x0126",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "SysAP-InfoCurrentAbsoluteBlindsPercentage",
                        "indicate the current position of the sunblinds in percentage of the actuator group"));
        mapDescObj.put("0x0127",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "SysAP-InfoCurrentAbsoluteSlatsPercentage",
                        "indicate the current position of the slats in percentage of the actuator group"));
        mapDescObj.put("0x0130", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Measured Temperature", "Indicates the actual measured temperature"));
        mapDescObj.put("0x0131", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Value Heating", "States the current flow volume of the conrol valve"));
        mapDescObj.put("0x0132", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Info value cooling", "States the current flow volume of the conrol valve"));
        mapDescObj.put("0x0135", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Switchover heating/cooling", "switch between heating and cooling: heating = 0 / cooling = 1"));
        mapDescObj.put("0x0136", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Actuating Fan Stage Heating", "Requests a new manual fan stage from actuator in heating mode"));
        mapDescObj.put("0x0140", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_TEMPERATURE, "7",
                "30", "Absolute setpoint temperature", "Absolute set point temperature input for timer"));
        mapDescObj.put("0x0141", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Additional heating value info", "Feedback"));
        mapDescObj.put("0x0142", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Additional cooling value info", "Feedback"));
        mapDescObj.put("0x0143", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Control value additional heating", ""));
        mapDescObj.put("0x0144", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Control value additional cooling", ""));
        mapDescObj.put("0x0145", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Actuating Fan Stage Heating", "Feedback from FCA"));
        mapDescObj.put("0x0146", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Actuating Fan Manual On/Off Heating", "Feedback from FCA"));
        mapDescObj.put("0x0147", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Actuating Fan Stage Cooling", "Requests a new manual fan stage from actuator in cooling mode"));
        mapDescObj.put("0x0149", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Info Fan Stage Cooling", "Feedback for current fan stage in cooling mode"));
        mapDescObj.put("0x014A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_TEMPERATURE, "",
                "", "Info Fan Manual On/Off Cooling", "Feedback for manual fan control cooling mode"));
        mapDescObj.put("0x014B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Heating active", ""));
        mapDescObj.put("0x014C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Cooling active", ""));
        mapDescObj.put("0x014D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Heating demand", ""));
        mapDescObj.put("0x014E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Cooling demand", ""));
        mapDescObj.put("0x014F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Heating demand feedback signal", ""));
        mapDescObj.put("0x0150", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_INTEGER, CATEGORY_TEMPERATURE, "",
                "", "Cooling demand feedback signal", ""));
        mapDescObj.put("0x0151", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_DECIMAL, CATEGORY_HUMIDITY, "", "",
                "Humidity", "Measured Humidity"));
        mapDescObj.put("0x0152", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Aux On/Off request", "Aux On/Off request"));
        mapDescObj.put("0x0153", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Aux On/Off response", "Aux On/Off response"));
        mapDescObj.put("0x0154", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Heating On/Off request", "Heating On/Off request"));
        mapDescObj.put("0x0155", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Cooling On/Off request", "Cooling On/Off request"));
        mapDescObj.put("0x0156", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Operation mode", ""));
        mapDescObj.put("0x0157", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Swing H/V", ""));
        mapDescObj.put("0x0158", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Supported features", ""));
        mapDescObj.put("0x0159", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Extended Status Indication", ""));
        mapDescObj.put("0x015A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Extended Status Indication", ""));
        mapDescObj.put("0x015B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Aux Heating On Off Request", ""));
        mapDescObj.put("0x015C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Emergency Heating On Off Request", ""));
        mapDescObj.put("0x0160", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Relative fan speed control", "Relative control of the set value"));
        mapDescObj.put("0x0161", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Absolute fan speed control", "Absolute control of the set value"));
        mapDescObj.put("0x0162", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info absolute fan speed", "Reflects the actual value of the actuator"));
        mapDescObj.put("0x0163", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "SysAP-InfoActualFanSpeed", "Reflects the actual value of the actuator"));
        mapDescObj.put("0x01A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Notification flags", "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0x0280", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Power RC", "Bool Value 1"));
        mapDescObj.put("0x0281", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Power RH", "Bool Value 2"));
        mapDescObj.put("0x0282", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Proximity status", "Bool Value 3"));
        mapDescObj.put("0x0290", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Brightness sensor", "Scaling Value 1"));
        mapDescObj.put("0x0291", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Last touch", "Scaling Value 2"));
        mapDescObj.put("0x0292", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "LED backlighting night mode", "Scaling Value 3"));
        mapDescObj.put("0x02C0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Locator beep", "Locator Beep"));
        mapDescObj.put("0x02C1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Switch test alarm", "Switch Test Alarm"));
        mapDescObj.put("0x02C3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Fire alarm active", "Fire-Alarm Active"));
        mapDescObj.put("0x0400", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Outside temperature", "Outdoor Temperature"));
        mapDescObj.put("0x0401", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Wind force", "Wind force"));
        mapDescObj.put("0x0402", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Brightness alarm", "Brightness alarm"));
        mapDescObj.put("0x0403", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Lux value", "Weatherstation brightness level"));
        mapDescObj.put("0x0404", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Wind speed", "Wind speed"));
        mapDescObj.put("0x0405", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Rain detection", ""));
        mapDescObj.put("0x0406", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Rain sensor frequency", ""));
        mapDescObj.put("0x0440", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Play", "Start playing"));
        mapDescObj.put("0x0441", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Pause", "Pause/Stop playing"));
        mapDescObj.put("0x0442", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Next", "Play next title"));
        mapDescObj.put("0x0443", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Previous", "Play previous title"));
        mapDescObj.put("0x0444", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Play mode", "Play mode (shuffle / repeat)"));
        mapDescObj.put("0x0445", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Mute", "Mute (1) and unmute (0) a player"));
        mapDescObj.put("0x0446", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Relative volume control", "Relative volume control. See also relative dimming"));
        mapDescObj.put("0x0447", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Absolute volume control", "Set player volume"));
        mapDescObj.put("0x0448", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Group membership", ""));
        mapDescObj.put("0x0449", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Play favorite", ""));
        mapDescObj.put("0x044A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Play next favorite", ""));
        mapDescObj.put("0x0460", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Playback status", ""));
        mapDescObj.put("0x0461", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Current item metadata info", ""));
        mapDescObj.put("0x0462", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info mute", ""));
        mapDescObj.put("0x0463", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info actual volume", ""));
        mapDescObj.put("0x0464", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Allowed playback actions", ""));
        mapDescObj.put("0x0465", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info group membership", ""));
        mapDescObj.put("0x0466", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info playing favorite", ""));
        mapDescObj.put("0x0467", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Absolute Group Volume Control", ""));
        mapDescObj.put("0x0468", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Absolute Group Volume", ""));
        mapDescObj.put("0x0469", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Media source", ""));
        mapDescObj.put("0x04A0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Solar power production", "Power from the sun"));
        mapDescObj.put("0x04A1", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Inverter output power", "Output power of inverter (pbatt+Psun)"));
        mapDescObj.put("0x04A2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Solar energy (today)", "Produced Energy"));
        mapDescObj.put("0x04A3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Injected energy (today)", "Energy into the grid"));
        mapDescObj.put("0x04A4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Purchased energy (today)", "Energy from the grid"));
        mapDescObj.put("0x04A5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Inverter alarm", "Inverter is working in stand alone mode"));
        mapDescObj.put("0x04A6", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Self-consumption", "production PV/ Total consumption"));
        mapDescObj.put("0x04A7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Self-sufficiency", "Consumption from PV/ Total consumption"));
        mapDescObj.put("0x04A8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Home power consumption", "Power in home (PV and grid)"));
        mapDescObj.put("0x04A9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Power to grid", "Power from and to the grid: Purchased (less than 0), Injection (more than 0)"));
        mapDescObj.put("0x04AA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Consumed energy (today)", "Energy bought from grid per day"));
        mapDescObj.put("0x04AB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Meter alarm", "Meter communication loss"));
        mapDescObj.put("0x04AC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Battery level", "Battery level"));
        mapDescObj.put("0x04AD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Battery power", "Batter power: Discharge (less then 0), Charge (more then 0)"));
        mapDescObj.put("0x04B0", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Boost", "1: Boost enable request, 0: boost disable request"));
        mapDescObj.put("0x04B1",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "Stop charging reuqest",
                        "1: Stop charging session requested, 0: n/a so far, will be resetted when cable is unplugged"));
        mapDescObj.put("0x04B2", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Enable charging reuqest",
                "1: Enable charging when cable is plugged in, 0: Disable next charging session but charge until cable is plugged"));
        mapDescObj.put("0x04B3", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info boost", "1: Boost enabled, 0: boost disabled"));
        mapDescObj.put("0x04B4", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info wallbox status",
                "Wallbox status 00000001: car plugged in, 00000002: Authorization granted, 00000004: Not charging, battery fully loaded, 40000000: charging stopped due to blackout prevention, 80000000: Ground fault error"));
        mapDescObj.put("0x04B5", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info charging", "1: Charging, 0: Not charging"));
        mapDescObj.put("0x04B6",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "Info charging enabled",
                        "1: Charging enabled for next session, 0: Charging disabled for next session"));
        mapDescObj.put("0x04B7", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info installed power", "Installed power (e.g. 20 kW)"));
        mapDescObj.put("0x04B8", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info transmitted energy", "Energy transmitted so far per session (in Wh)"));
        mapDescObj.put("0x04B9", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info car range", "Car range in km per sessions"));
        mapDescObj.put("0x04BA", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info charging duration", "Start of charging session (in minutes in UTC)"));
        mapDescObj.put("0x04BB", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info current limit", "Limit for charger (in kW)"));
        mapDescObj.put("0x04BC", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info current limit for group", "Limit for group of charger (in kW)"));
        mapDescObj.put("0x04BD", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Album cover URL", "Album cover URL"));
        mapDescObj.put("0x0501", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "secure@home Central Unit", "Encrypted control datapoint for domus alarm center"));
        mapDescObj.put("0x0502", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "DomusDisarmCounter", "Info about the next counter to disarm the system"));
        mapDescObj.put("0x0504", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Intrusion Alarm", "Intrusion Alarm"));
        mapDescObj.put("0x0505", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Safety Alarm", "Safety Alarm"));
        mapDescObj.put("0x0507", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "InfoConfigurationStatus", "Domus alarm device negative feedback and configuration info."));
        mapDescObj.put("0x0508", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Enable configuration", "Encrypted control datapoint for entering configuration mode"));
        mapDescObj.put("0x0509", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Disarming LED", "Arm/Disarm a Zone"));
        mapDescObj.put("0x050A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "AES Key", "Manufacturer ID + Serial + AES Key"));
        mapDescObj.put("0x050B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Zone status", "Zone status"));
        mapDescObj.put("0x050E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Time", "Absolute number of seconds when the zone will be armed"));
        mapDescObj.put("0x0600", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Start / Stop", "Starts / Stops operation"));
        mapDescObj.put("0x0601", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Pause / Resume", ""));
        mapDescObj.put("0x0602", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Select program", ""));
        mapDescObj.put("0x0603", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Delayed start time", ""));
        mapDescObj.put("0x0604", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info status", ""));
        mapDescObj.put("0x0605", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info remote start enabled", ""));
        mapDescObj.put("0x0606", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info program", ""));
        mapDescObj.put("0x0607", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info finish time", ""));
        mapDescObj.put("0x0608", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info delayed start", ""));
        mapDescObj.put("0x0609", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info door", ""));
        mapDescObj.put("0x060A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info door alarm", ""));
        mapDescObj.put("0x060B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Switch supercool", ""));
        mapDescObj.put("0x060C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Switch superfreeze", ""));
        mapDescObj.put("0x060D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info switch supercool", ""));
        mapDescObj.put("0x060E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info switch superfreeze", ""));
        mapDescObj.put("0x060F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured Temperature", ""));
        mapDescObj.put("0x0610", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured Temperature", ""));
        mapDescObj.put("0x0611", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Set Value Temperature", ""));
        mapDescObj.put("0x0612", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Set Value Temperature", ""));
        mapDescObj.put("0x0613", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Change operation", ""));
        mapDescObj.put("0x0614", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Detailed status info", ""));
        mapDescObj.put("0x0615", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info remaining time", "Remaining time till status change (start, finish, etc.)"));
        mapDescObj.put("0x0616",
                createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                        "Time of last status change (start, finish, etc.)",
                        "Time of last status change (start, finish, etc.)"));
        mapDescObj.put("0x0618", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Lock/Unlock door command", "Lock/Unlock door command (1 Bit)"));
        mapDescObj.put("0x0619", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info Locked / Unlocked", "Info Lock/Unlock door(1 Bit)"));
        mapDescObj.put("0xF001", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Time", "Current local time"));
        mapDescObj.put("0xF002", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Date", "Curent local date"));
        mapDescObj.put("0xF003", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Notification", "Notification from message center"));
        mapDescObj.put("0xF101", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Switch entity On/Off", "Entity control e.g. activate an alert or timer program"));
        mapDescObj.put("0xF102", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_BOOLEAN, CATEGORY_SWITCH, "", "",
                "Info switch entity On/Off", "Reflects the active state of an entity e.g. alert or timer program"));
        mapDescObj.put("0xF104", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Consistency Tag", "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF105", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Battery Status", "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF106", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Stay awake!", "Notifications of RF devices (e. g. Battery low)"));
        mapDescObj.put("0xF107", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Proxy switch", ""));
        mapDescObj.put("0xF108", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Proxy, 1 byte", ""));
        mapDescObj.put("0xF109", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Proxy, 2 byte", ""));
        mapDescObj.put("0xF10A", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Proxy, 4 byte", ""));
        mapDescObj.put("0xF10B", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Cyclic sleep time", "Time of sleep cycles"));
        mapDescObj.put("0xF10C", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Presence", "SysAP presence"));
        mapDescObj.put("0xF10D", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured temperature 1", "SysAP temperature"));
        mapDescObj.put("0xF10E", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Standby Statistics", "Statistics about standby usage for battery devices"));
        mapDescObj.put("0xF10F", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Heartbeat delay", "Time period between two heartbeats"));
        mapDescObj.put("0xF110", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Info heartbeat delay", "Time period between two heartbeats"));
        mapDescObj.put("0xFF01", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured temperature 1", "For debug purposes"));
        mapDescObj.put("0xFF02", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured temperature 2", "For debug purposes"));
        mapDescObj.put("0xFF03", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured temperature 3", "For debug purposes"));
        mapDescObj.put("0xFF04", createFreeAtHomePairingIdTranslation(PID_VALUETYPE_UNKNOWN, CATEGORY_UNDEFINED, "", "",
                "Measured temperature 4", "For debug purposes"));

        MAP_TRANSLATOR = Collections.unmodifiableMap(mapDescObj);
    }

    @SuppressWarnings("null")
    public static String getShortTextForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.label;
    }

    @SuppressWarnings("null")
    public static String getDescriptionTextForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.descprition;
    }

    @SuppressWarnings("null")
    public static String getValueTypeForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.valueType;
    }

    @SuppressWarnings("null")
    public static String getItemTypeForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.category;
    }

    @SuppressWarnings("null")
    public static String getCategoryForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.category;
    }

    @SuppressWarnings("null")
    public static String getPatternForPairingId(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.category;
    }

    @SuppressWarnings("null")
    public static int getMax(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.max;
    }

    @SuppressWarnings("null")
    public static int getMin(String Key) {
        PIdContainerClass desc = MAP_TRANSLATOR.get(Key);

        return desc.min;
    }
}
