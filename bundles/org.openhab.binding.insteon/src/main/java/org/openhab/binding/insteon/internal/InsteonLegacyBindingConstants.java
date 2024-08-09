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
package org.openhab.binding.insteon.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonLegacyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Rob Nielsen - Initial contribution
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class InsteonLegacyBindingConstants {
    // List of all Channel ids
    public static final String AC_DELAY = "acDelay";
    public static final String BACKLIGHT_DURATION = "backlightDuration";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String BATTERY_PERCENT = "batteryPercent";
    public static final String BATTERY_WATERMARK_LEVEL = "batteryWatermarkLevel";
    public static final String BEEP = "beep";
    public static final String BOTTOM_OUTLET = "bottomOutlet";
    public static final String BUTTON_A = "buttonA";
    public static final String BUTTON_B = "buttonB";
    public static final String BUTTON_C = "buttonC";
    public static final String BUTTON_D = "buttonD";
    public static final String BUTTON_E = "buttonE";
    public static final String BUTTON_F = "buttonF";
    public static final String BUTTON_G = "buttonG";
    public static final String BUTTON_H = "buttonH";
    public static final String BROADCAST_ON_OFF = "broadcastOnOff";
    public static final String CONTACT = "contact";
    public static final String COOL_SET_POINT = "coolSetPoint";
    public static final String DIMMER = "dimmer";
    public static final String FAN = "fan";
    public static final String FAN_MODE = "fanMode";
    public static final String FAST_ON_OFF = "fastOnOff";
    public static final String FAST_ON_OFF_BUTTON_A = "fastOnOffButtonA";
    public static final String FAST_ON_OFF_BUTTON_B = "fastOnOffButtonB";
    public static final String FAST_ON_OFF_BUTTON_C = "fastOnOffButtonC";
    public static final String FAST_ON_OFF_BUTTON_D = "fastOnOffButtonD";
    public static final String FAST_ON_OFF_BUTTON_E = "fastOnOffButtonE";
    public static final String FAST_ON_OFF_BUTTON_F = "fastOnOffButtonF";
    public static final String FAST_ON_OFF_BUTTON_G = "fastOnOffButtonG";
    public static final String FAST_ON_OFF_BUTTON_H = "fastOnOffButtonH";
    public static final String HEAT_SET_POINT = "heatSetPoint";
    public static final String HUMIDITY = "humidity";
    public static final String HUMIDITY_HIGH = "humidityHigh";
    public static final String HUMIDITY_LOW = "humidityLow";
    public static final String IS_COOLING = "isCooling";
    public static final String IS_HEATING = "isHeating";
    public static final String KEYPAD_BUTTON_A = "keypadButtonA";
    public static final String KEYPAD_BUTTON_B = "keypadButtonB";
    public static final String KEYPAD_BUTTON_C = "keypadButtonC";
    public static final String KEYPAD_BUTTON_D = "keypadButtonD";
    public static final String KEYPAD_BUTTON_E = "keypadButtonE";
    public static final String KEYPAD_BUTTON_F = "keypadButtonF";
    public static final String KEYPAD_BUTTON_G = "keypadButtonG";
    public static final String KEYPAD_BUTTON_H = "keypadButtonH";
    public static final String KWH = "kWh";
    public static final String LAST_HEARD_FROM = "lastHeardFrom";
    public static final String LED_BRIGHTNESS = "ledBrightness";
    public static final String LED_ONOFF = "ledOnOff";
    public static final String LIGHT_DIMMER = "lightDimmer";
    public static final String LIGHT_LEVEL = "lightLevel";
    public static final String LIGHT_LEVEL_ABOVE_THRESHOLD = "lightLevelAboveThreshold";
    public static final String LOAD_DIMMER = "loadDimmer";
    public static final String LOAD_SWITCH = "loadSwitch";
    public static final String LOAD_SWITCH_FAST_ON_OFF = "loadSwitchFastOnOff";
    public static final String LOAD_SWITCH_MANUAL_CHANGE = "loadSwitchManualChange";
    public static final String LOWBATTERY = "lowBattery";
    public static final String MANUAL_CHANGE = "manualChange";
    public static final String MANUAL_CHANGE_BUTTON_A = "manualChangeButtonA";
    public static final String MANUAL_CHANGE_BUTTON_B = "manualChangeButtonB";
    public static final String MANUAL_CHANGE_BUTTON_C = "manualChangeButtonC";
    public static final String MANUAL_CHANGE_BUTTON_D = "manualChangeButtonD";
    public static final String MANUAL_CHANGE_BUTTON_E = "manualChangeButtonE";
    public static final String MANUAL_CHANGE_BUTTON_F = "manualChangeButtonF";
    public static final String MANUAL_CHANGE_BUTTON_G = "manualChangeButtonG";
    public static final String MANUAL_CHANGE_BUTTON_H = "manualChangeButtonH";
    public static final String NOTIFICATION = "notification";
    public static final String ON_LEVEL = "onLevel";
    public static final String RAMP_DIMMER = "rampDimmer";
    public static final String RAMP_RATE = "rampRate";
    public static final String RESET = "reset";
    public static final String STAGE1_DURATION = "stage1Duration";
    public static final String SWITCH = "switch";
    public static final String SYSTEM_MODE = "systemMode";
    public static final String TAMPER_SWITCH = "tamperSwitch";
    public static final String TEMPERATURE = "temperature";
    public static final String TEMPERATURE_LEVEL = "temperatureLevel";
    public static final String TOP_OUTLET = "topOutlet";
    public static final String UPDATE = "update";
    public static final String WATTS = "watts";

    public static final Set<String> ALL_CHANNEL_IDS = Set.of(AC_DELAY, BACKLIGHT_DURATION, BATTERY_LEVEL,
            BATTERY_PERCENT, BATTERY_WATERMARK_LEVEL, BEEP, BOTTOM_OUTLET, BUTTON_A, BUTTON_B, BUTTON_C, BUTTON_D,
            BUTTON_E, BUTTON_F, BUTTON_G, BUTTON_H, BROADCAST_ON_OFF, CONTACT, COOL_SET_POINT, DIMMER, FAN, FAN_MODE,
            FAST_ON_OFF, FAST_ON_OFF_BUTTON_A, FAST_ON_OFF_BUTTON_B, FAST_ON_OFF_BUTTON_C, FAST_ON_OFF_BUTTON_D,
            FAST_ON_OFF_BUTTON_E, FAST_ON_OFF_BUTTON_F, FAST_ON_OFF_BUTTON_G, FAST_ON_OFF_BUTTON_H, HEAT_SET_POINT,
            HUMIDITY, HUMIDITY_HIGH, HUMIDITY_LOW, IS_COOLING, IS_HEATING, KEYPAD_BUTTON_A, KEYPAD_BUTTON_B,
            KEYPAD_BUTTON_C, KEYPAD_BUTTON_D, KEYPAD_BUTTON_E, KEYPAD_BUTTON_F, KEYPAD_BUTTON_G, KEYPAD_BUTTON_H, KWH,
            LAST_HEARD_FROM, LED_BRIGHTNESS, LED_ONOFF, LIGHT_DIMMER, LIGHT_LEVEL, LIGHT_LEVEL_ABOVE_THRESHOLD,
            LOAD_DIMMER, LOAD_SWITCH, LOAD_SWITCH_FAST_ON_OFF, LOAD_SWITCH_MANUAL_CHANGE, LOWBATTERY, MANUAL_CHANGE,
            MANUAL_CHANGE_BUTTON_A, MANUAL_CHANGE_BUTTON_B, MANUAL_CHANGE_BUTTON_C, MANUAL_CHANGE_BUTTON_D,
            MANUAL_CHANGE_BUTTON_E, MANUAL_CHANGE_BUTTON_F, MANUAL_CHANGE_BUTTON_G, MANUAL_CHANGE_BUTTON_H,
            NOTIFICATION, ON_LEVEL, RAMP_DIMMER, RAMP_RATE, RESET, STAGE1_DURATION, SWITCH, SYSTEM_MODE, TAMPER_SWITCH,
            TEMPERATURE, TEMPERATURE_LEVEL, TOP_OUTLET, UPDATE, WATTS);

    public static final String BROADCAST_GROUPS = "broadcastGroups";
    public static final String CMD = "cmd";
    public static final String CMD_RESET = "reset";
    public static final String CMD_UPDATE = "update";
    public static final String DATA = "data";
    public static final String FIELD = "field";
    public static final String FIELD_BATTERY_LEVEL = "battery_level";
    public static final String FIELD_BATTERY_PERCENTAGE = "battery_percentage";
    public static final String FIELD_BATTERY_WATERMARK_LEVEL = "battery_watermark_level";
    public static final String FIELD_KWH = "kwh";
    public static final String FIELD_LIGHT_LEVEL = "light_level";
    public static final String FIELD_TEMPERATURE_LEVEL = "temperature_level";
    public static final String FIELD_WATTS = "watts";
    public static final String GROUP = "group";
    public static final String METER = "meter";

    public static final String HIDDEN_DOOR_SENSOR_PRODUCT_KEY = "F00.00.03";
    public static final String MOTION_SENSOR_II_PRODUCT_KEY = "F00.00.24";
    public static final String MOTION_SENSOR_PRODUCT_KEY = "0x00004A";
    public static final String PLM_PRODUCT_KEY = "0x000045";
    public static final String POWER_METER_PRODUCT_KEY = "F00.00.17";
}
