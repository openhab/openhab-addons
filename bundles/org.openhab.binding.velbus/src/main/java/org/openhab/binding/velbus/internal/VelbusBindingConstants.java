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
package org.openhab.binding.velbus.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VelbusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusBindingConstants {

    public static final String BINDING_ID = "velbus";

    // bridges
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID NETWORK_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "networkbridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_VMB1BL = new ThingTypeUID(BINDING_ID, "vmb1bl");
    public static final ThingTypeUID THING_TYPE_VMB1BLS = new ThingTypeUID(BINDING_ID, "vmb1bls");
    public static final ThingTypeUID THING_TYPE_VMB1DM = new ThingTypeUID(BINDING_ID, "vmb1dm");
    public static final ThingTypeUID THING_TYPE_VMB1LED = new ThingTypeUID(BINDING_ID, "vmb1led");
    public static final ThingTypeUID THING_TYPE_VMB1RY = new ThingTypeUID(BINDING_ID, "vmb1ry");
    public static final ThingTypeUID THING_TYPE_VMB1RYNO = new ThingTypeUID(BINDING_ID, "vmb1ryno");
    public static final ThingTypeUID THING_TYPE_VMB1RYNOS = new ThingTypeUID(BINDING_ID, "vmb1rynos");
    public static final ThingTypeUID THING_TYPE_VMB1RYS = new ThingTypeUID(BINDING_ID, "vmb1rys");
    public static final ThingTypeUID THING_TYPE_VMB1TS = new ThingTypeUID(BINDING_ID, "vmb1ts");
    public static final ThingTypeUID THING_TYPE_VMB2BL = new ThingTypeUID(BINDING_ID, "vmb2bl");
    public static final ThingTypeUID THING_TYPE_VMB2BLE = new ThingTypeUID(BINDING_ID, "vmb2ble");
    public static final ThingTypeUID THING_TYPE_VMB2PBN = new ThingTypeUID(BINDING_ID, "vmb2pbn");
    public static final ThingTypeUID THING_TYPE_VMB4AN = new ThingTypeUID(BINDING_ID, "vmb4an");
    public static final ThingTypeUID THING_TYPE_VMB4DC = new ThingTypeUID(BINDING_ID, "vmb4dc");
    public static final ThingTypeUID THING_TYPE_VMB4RY = new ThingTypeUID(BINDING_ID, "vmb4ry");
    public static final ThingTypeUID THING_TYPE_VMB4RYLD = new ThingTypeUID(BINDING_ID, "vmb4ryld");
    public static final ThingTypeUID THING_TYPE_VMB4RYNO = new ThingTypeUID(BINDING_ID, "vmb4ryno");
    public static final ThingTypeUID THING_TYPE_VMB6IN = new ThingTypeUID(BINDING_ID, "vmb6in");
    public static final ThingTypeUID THING_TYPE_VMB6PBN = new ThingTypeUID(BINDING_ID, "vmb6pbn");
    public static final ThingTypeUID THING_TYPE_VMB7IN = new ThingTypeUID(BINDING_ID, "vmb7in");
    public static final ThingTypeUID THING_TYPE_VMB8IR = new ThingTypeUID(BINDING_ID, "vmb8ir");
    public static final ThingTypeUID THING_TYPE_VMB8PB = new ThingTypeUID(BINDING_ID, "vmb8pb");
    public static final ThingTypeUID THING_TYPE_VMB8PBU = new ThingTypeUID(BINDING_ID, "vmb8pbu");
    public static final ThingTypeUID THING_TYPE_VMBDME = new ThingTypeUID(BINDING_ID, "vmbdme");
    public static final ThingTypeUID THING_TYPE_VMBDMI = new ThingTypeUID(BINDING_ID, "vmbdmi");
    public static final ThingTypeUID THING_TYPE_VMBDMIR = new ThingTypeUID(BINDING_ID, "vmbdmir");
    public static final ThingTypeUID THING_TYPE_VMBEL1 = new ThingTypeUID(BINDING_ID, "vmbel1");
    public static final ThingTypeUID THING_TYPE_VMBEL2 = new ThingTypeUID(BINDING_ID, "vmbel2");
    public static final ThingTypeUID THING_TYPE_VMBEL4 = new ThingTypeUID(BINDING_ID, "vmbel4");
    public static final ThingTypeUID THING_TYPE_VMBELO = new ThingTypeUID(BINDING_ID, "vmbelo");
    public static final ThingTypeUID THING_TYPE_VMBELPIR = new ThingTypeUID(BINDING_ID, "vmbelpir");
    public static final ThingTypeUID THING_TYPE_VMBGP1 = new ThingTypeUID(BINDING_ID, "vmbgp1");
    public static final ThingTypeUID THING_TYPE_VMBGP1_2 = new ThingTypeUID(BINDING_ID, "vmbgp1-2");
    public static final ThingTypeUID THING_TYPE_VMBGP2 = new ThingTypeUID(BINDING_ID, "vmbgp2");
    public static final ThingTypeUID THING_TYPE_VMBGP2_2 = new ThingTypeUID(BINDING_ID, "vmbgp2-2");
    public static final ThingTypeUID THING_TYPE_VMBGP4 = new ThingTypeUID(BINDING_ID, "vmbgp4");
    public static final ThingTypeUID THING_TYPE_VMBGP4_2 = new ThingTypeUID(BINDING_ID, "vmbgp4-2");
    public static final ThingTypeUID THING_TYPE_VMBGP4PIR = new ThingTypeUID(BINDING_ID, "vmbgp4pir");
    public static final ThingTypeUID THING_TYPE_VMBGP4PIR_2 = new ThingTypeUID(BINDING_ID, "vmbgp4pir-2");
    public static final ThingTypeUID THING_TYPE_VMBGPO = new ThingTypeUID(BINDING_ID, "vmbgpo");
    public static final ThingTypeUID THING_TYPE_VMBGPOD = new ThingTypeUID(BINDING_ID, "vmbgpod");
    public static final ThingTypeUID THING_TYPE_VMBGPOD_2 = new ThingTypeUID(BINDING_ID, "vmbgpod-2");
    public static final ThingTypeUID THING_TYPE_VMBMETEO = new ThingTypeUID(BINDING_ID, "vmbmeteo");
    public static final ThingTypeUID THING_TYPE_VMBPIRC = new ThingTypeUID(BINDING_ID, "vmbpirc");
    public static final ThingTypeUID THING_TYPE_VMBPIRM = new ThingTypeUID(BINDING_ID, "vmbpirm");
    public static final ThingTypeUID THING_TYPE_VMBPIRO = new ThingTypeUID(BINDING_ID, "vmbpiro");
    public static final ThingTypeUID THING_TYPE_VMBRFR8S = new ThingTypeUID(BINDING_ID, "vmbrfr8s");
    public static final ThingTypeUID THING_TYPE_VMBVP1 = new ThingTypeUID(BINDING_ID, "vmbvp1");
    public static final ThingTypeUID THING_TYPE_VMBKP = new ThingTypeUID(BINDING_ID, "vmbkp");
    public static final ThingTypeUID THING_TYPE_VMBIN = new ThingTypeUID(BINDING_ID, "vmbin");
    public static final ThingTypeUID THING_TYPE_VMB4PB = new ThingTypeUID(BINDING_ID, "vmb4pb");
    public static final ThingTypeUID THING_TYPE_VMBDALI = new ThingTypeUID(BINDING_ID, "vmbdali");
    public static final ThingTypeUID THING_TYPE_VMB4RYLD_10 = new ThingTypeUID(BINDING_ID, "vmb4ryld-10");
    public static final ThingTypeUID THING_TYPE_VMB4RYNO_10 = new ThingTypeUID(BINDING_ID, "vmb4ryno-10");
    public static final ThingTypeUID THING_TYPE_VMB2BLE_10 = new ThingTypeUID(BINDING_ID, "vmb2ble-10");
    public static final ThingTypeUID THING_TYPE_VMB6PB_20 = new ThingTypeUID(BINDING_ID, "vmb6pb-20");
    public static final ThingTypeUID THING_TYPE_VMBEL1_20 = new ThingTypeUID(BINDING_ID, "vmbel1-20");
    public static final ThingTypeUID THING_TYPE_VMBEL2_20 = new ThingTypeUID(BINDING_ID, "vmbel2-20");
    public static final ThingTypeUID THING_TYPE_VMBEL4_20 = new ThingTypeUID(BINDING_ID, "vmbel4-20");
    public static final ThingTypeUID THING_TYPE_VMBELO_20 = new ThingTypeUID(BINDING_ID, "vmbelo-20");
    public static final ThingTypeUID THING_TYPE_VMBGP1_20 = new ThingTypeUID(BINDING_ID, "vmbgp1-20");
    public static final ThingTypeUID THING_TYPE_VMBGP2_20 = new ThingTypeUID(BINDING_ID, "vmbgp2-20");
    public static final ThingTypeUID THING_TYPE_VMBGP4_20 = new ThingTypeUID(BINDING_ID, "vmbgp4-20");
    public static final ThingTypeUID THING_TYPE_VMBGPO_20 = new ThingTypeUID(BINDING_ID, "vmbgpo-20");
    public static final ThingTypeUID THING_TYPE_VMBDALI_20 = new ThingTypeUID(BINDING_ID, "vmbdali-20");
    public static final ThingTypeUID THING_TYPE_VMBEL4PIR_20 = new ThingTypeUID(BINDING_ID, "vmbel4pir-20");
    public static final ThingTypeUID THING_TYPE_VMBGP4PIR_20 = new ThingTypeUID(BINDING_ID, "vmbgp4pir-20");

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(BRIDGE_THING_TYPE,
            NETWORK_BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_VMB1BL, THING_TYPE_VMB1BLS,
            THING_TYPE_VMB1DM, THING_TYPE_VMB1LED, THING_TYPE_VMB1RY, THING_TYPE_VMB1RYNO, THING_TYPE_VMB1RYNOS,
            THING_TYPE_VMB1RYS, THING_TYPE_VMB1TS, THING_TYPE_VMB2BL, THING_TYPE_VMB2BLE, THING_TYPE_VMB2PBN,
            THING_TYPE_VMB4AN, THING_TYPE_VMB4DC, THING_TYPE_VMB4RY, THING_TYPE_VMB4RYLD, THING_TYPE_VMB4RYNO,
            THING_TYPE_VMB6IN, THING_TYPE_VMB6PBN, THING_TYPE_VMB7IN, THING_TYPE_VMB8IR, THING_TYPE_VMB8PB,
            THING_TYPE_VMB8PBU, THING_TYPE_VMBDME, THING_TYPE_VMBDMI, THING_TYPE_VMBDMIR, THING_TYPE_VMBEL1,
            THING_TYPE_VMBEL2, THING_TYPE_VMBEL4, THING_TYPE_VMBELO, THING_TYPE_VMBELPIR, THING_TYPE_VMBGP1,
            THING_TYPE_VMBGP1_2, THING_TYPE_VMBGP2, THING_TYPE_VMBGP2_2, THING_TYPE_VMBGP4, THING_TYPE_VMBGP4_2,
            THING_TYPE_VMBGP4PIR, THING_TYPE_VMBGP4PIR_2, THING_TYPE_VMBGPO, THING_TYPE_VMBGPOD, THING_TYPE_VMBGPOD_2,
            THING_TYPE_VMBMETEO, THING_TYPE_VMBPIRC, THING_TYPE_VMBPIRM, THING_TYPE_VMBPIRO, THING_TYPE_VMBRFR8S,
            THING_TYPE_VMBVP1, THING_TYPE_VMBKP, THING_TYPE_VMBIN, THING_TYPE_VMB4PB, THING_TYPE_VMBDALI,
            THING_TYPE_VMB4RYLD_10, THING_TYPE_VMB4RYNO_10, THING_TYPE_VMB2BLE_10, THING_TYPE_VMB6PB_20,
            THING_TYPE_VMBEL1_20, THING_TYPE_VMBEL2_20, THING_TYPE_VMBEL4_20, THING_TYPE_VMBELO_20,
            THING_TYPE_VMBGP1_20, THING_TYPE_VMBGP2_20, THING_TYPE_VMBGP4_20, THING_TYPE_VMBGPO_20,
            THING_TYPE_VMBDALI_20, THING_TYPE_VMBEL4PIR_20, THING_TYPE_VMBGP4PIR_20);

    // Velbus module types
    public static final byte MODULE_TYPE_VMB8PB = 0x01;
    public static final byte MODULE_TYPE_VMB1RY = 0x02;
    public static final byte MODULE_TYPE_VMB1BL = 0x03;
    public static final byte MODULE_TYPE_VMB6IN = 0x05;
    public static final byte MODULE_TYPE_VMB1DM = 0x07;
    public static final byte MODULE_TYPE_VMB4RY = 0x08;
    public static final byte MODULE_TYPE_VMB2BL = 0x09;
    public static final byte MODULE_TYPE_VMB8IR = 0x0A;
    public static final byte MODULE_TYPE_VMB1TS = 0x0C;
    public static final byte MODULE_TYPE_VMB1LED = 0x0F;
    public static final byte MODULE_TYPE_VMB4RYLD = 0x10;
    public static final byte MODULE_TYPE_VMB4RYNO = 0x11;
    public static final byte MODULE_TYPE_VMB4DC = 0x12;
    public static final byte MODULE_TYPE_VMBLCDWB = 0x13;
    public static final byte MODULE_TYPE_VMBDME = 0x14;
    public static final byte MODULE_TYPE_VMBDMI = 0x15;
    public static final byte MODULE_TYPE_VMB8PBU = 0x16;
    public static final byte MODULE_TYPE_VMB6PBN = 0x17;
    public static final byte MODULE_TYPE_VMB2PBN = 0x18;
    public static final byte MODULE_TYPE_VMB1RYNO = 0x1B;
    public static final byte MODULE_TYPE_VMB2BLE = 0x1D;
    public static final byte MODULE_TYPE_VMBGP1 = 0x1E;
    public static final byte MODULE_TYPE_VMBGP2 = 0x1F;
    public static final byte MODULE_TYPE_VMBGP4 = 0x20;
    public static final byte MODULE_TYPE_VMBGPO = 0x21;
    public static final byte MODULE_TYPE_VMB7IN = 0x22;
    public static final byte MODULE_TYPE_VMBGPOD = 0x28;
    public static final byte MODULE_TYPE_VMB1RYNOS = 0x29;
    public static final byte MODULE_TYPE_VMBPIRM = 0x2A;
    public static final byte MODULE_TYPE_VMBPIRC = 0x2B;
    public static final byte MODULE_TYPE_VMBPIRO = 0x2C;
    public static final byte MODULE_TYPE_VMBGP4PIR = 0x2D;
    public static final byte MODULE_TYPE_VMB1BLS = 0x2E;
    public static final byte MODULE_TYPE_VMBDMIR = 0x2F;
    public static final byte MODULE_TYPE_VMBRFR8S = 0x30;
    public static final byte MODULE_TYPE_VMBMETEO = 0x31;
    public static final byte MODULE_TYPE_VMB4AN = 0x32;
    public static final byte MODULE_TYPE_VMBVP1 = 0x33;
    public static final byte MODULE_TYPE_VMBEL1 = 0x34;
    public static final byte MODULE_TYPE_VMBEL2 = 0x35;
    public static final byte MODULE_TYPE_VMBEL4 = 0x36;
    public static final byte MODULE_TYPE_VMBELO = 0x37;
    public static final byte MODULE_TYPE_VMBELPIR = 0x38;
    public static final byte MODULE_TYPE_VMBGP1_2 = 0x3A;
    public static final byte MODULE_TYPE_VMBGP2_2 = 0x3B;
    public static final byte MODULE_TYPE_VMBGP4_2 = 0x3C;
    public static final byte MODULE_TYPE_VMBGPOD_2 = 0x3D;
    public static final byte MODULE_TYPE_VMBGP4PIR_2 = 0x3E;
    public static final byte MODULE_TYPE_VMB1RYS = 0x41;
    public static final byte MODULE_TYPE_VMBKP = 0x42;
    public static final byte MODULE_TYPE_VMBIN = 0x43;
    public static final byte MODULE_TYPE_VMB4PB = 0x44;
    public static final byte MODULE_TYPE_VMBDALI = 0x45;
    public static final byte MODULE_TYPE_VMB4RYLD_10 = 0x48;
    public static final byte MODULE_TYPE_VMB4RYNO_10 = 0x49;
    public static final byte MODULE_TYPE_VMB2BLE_10 = 0x4A;
    public static final byte MODULE_TYPE_VMB6PB_20 = 0x4C;
    public static final byte MODULE_TYPE_VMBEL1_20 = 0x4F;
    public static final byte MODULE_TYPE_VMBEL2_20 = 0x50;
    public static final byte MODULE_TYPE_VMBEL4_20 = 0x51;
    public static final byte MODULE_TYPE_VMBELO_20 = 0x52;
    public static final byte MODULE_TYPE_VMBGP1_20 = 0x54;
    public static final byte MODULE_TYPE_VMBGP2_20 = 0x55;
    public static final byte MODULE_TYPE_VMBGP4_20 = 0x56;
    public static final byte MODULE_TYPE_VMBGPO_20 = 0x57;
    public static final byte MODULE_TYPE_VMBDALI_20 = 0x5A;
    public static final byte MODULE_TYPE_VMBEL4PIR_20 = 0x5C;
    public static final byte MODULE_TYPE_VMBGP4PIR_20 = 0x5F;

    // Velbus commands
    public static final byte COMMAND_PUSH_BUTTON_STATUS = 0x00;
    public static final byte COMMAND_SWITCH_RELAY_OFF = 0x01;
    public static final byte COMMAND_SWITCH_RELAY_ON = 0x02;
    public static final byte COMMAND_SWITCH_BLIND_OFF = 0x04;
    public static final byte COMMAND_BLIND_UP = 0x05;
    public static final byte COMMAND_BLIND_DOWN = 0x06;
    public static final byte COMMAND_SET_VALUE = 0x07;
    public static final byte COMMAND_SET_DIMVALUE = 0x07;
    public static final byte COMMAND_SLIDER_STATUS = 0x0F;
    public static final byte COMMAND_RESTORE_LAST_DIMVALUE = 0x11;
    public static final byte COMMAND_FORCED_OFF = 0x12;
    public static final byte COMMAND_CANCEL_FORCED_OFF = 0x13;
    public static final byte COMMAND_FORCED_ON = 0x14;
    public static final byte COMMAND_CANCEL_FORCED_ON = 0x15;
    public static final byte COMMAND_INHIBIT = 0x16;
    public static final byte COMMAND_CANCEL_INHIBIT = 0x17;
    public static final byte COMMAND_BLIND_POS = 0x1C;
    public static final byte COMMAND_SET_DIMSCENE = (byte) 0x1D;
    public static final byte COMMAND_SET_COLOR = (byte) 0x1E;
    public static final byte COMMAND_DIMVALUE_STATUS = (byte) 0xA5;
    public static final byte COMMAND_SUBTYPE_3 = (byte) 0xA6;
    public static final byte COMMAND_SUBTYPE_2 = (byte) 0xA7;
    public static final byte COMMAND_SENSOR_RAW_DATA = (byte) 0xA9;
    public static final byte COMMAND_LIGHT_VALUE_REQUEST = (byte) 0xAA;
    public static final byte COMMAND_POWER_UP = (byte) 0xAB;
    public static final byte COMMAND_TEXT = (byte) 0xAC;
    public static final byte COMMAND_DAYLIGHT_SAVING_STATUS = (byte) 0xAF;
    public static final byte COMMAND_SUBTYPE = (byte) 0xB0;
    public static final byte COMMAND_SET_REALTIME_DATE = (byte) 0xB7;
    public static final byte COMMAND_DIMMERCONTROLLER_STATUS = (byte) 0xB8;
    public static final byte COMMAND_TEMP_SENSOR_SETTINGS_PART4 = (byte) 0xB9;
    public static final byte COMMAND_COUNTER_STATUS_REQUEST = (byte) 0xBD;
    public static final byte COMMAND_COUNTER_STATUS = (byte) 0xBE;
    public static final byte COMMAND_SET_ALARM_CLOCK = (byte) 0xC3;
    public static final byte COMMAND_TEMP_SENSOR_SETTINGS_PART3 = (byte) 0xC6;
    public static final byte COMMAND_READ_MEMORY_BLOCK = (byte) 0xC9;
    public static final byte COMMAND_MEMORY_DATA_BLOCK = (byte) 0xCC;
    public static final byte COMMAND_SET_REALTIME_CLOCK = (byte) 0xD8;
    public static final byte COMMAND_SWITCH_TO_COMFORT_MODE = (byte) 0xDB;
    public static final byte COMMAND_SWITCH_TO_DAY_MODE = (byte) 0xDC;
    public static final byte COMMAND_SWITCH_TO_NIGHT_MODE = (byte) 0xDD;
    public static final byte COMMAND_SWITCH_TO_SAFE_MODE = (byte) 0xDE;
    public static final byte COMMAND_SET_COOLING_MODE = (byte) 0xDF;
    public static final byte COMMAND_SET_HEATING_MODE = (byte) 0xE0;
    public static final byte COMMAND_SET_TEMP = (byte) 0xE4;
    public static final byte COMMAND_SENSOR_READOUT_REQUEST = (byte) 0xE5;
    public static final byte COMMAND_SENSOR_TEMPERATURE = (byte) 0xE6;
    public static final byte COMMAND_TEMP_SENSOR_SETTINGS_REQUEST = (byte) 0xE7;
    public static final byte COMMAND_TEMP_SENSOR_SETTINGS_PART1 = (byte) 0xE8;
    public static final byte COMMAND_TEMP_SENSOR_SETTINGS_PART2 = (byte) 0xE9;
    public static final byte COMMAND_TEMP_SENSOR_STATUS = (byte) 0xEA;
    public static final byte COMMAND_BLIND_STATUS = (byte) 0xEC;
    public static final byte COMMAND_MODULE_STATUS = (byte) 0xED;
    public static final byte COMMAND_DIMMER_STATUS = (byte) 0xEE;
    public static final byte COMMAND_MODULE_NAME_REQUEST = (byte) 0xEF;
    public static final byte COMMAND_MODULE_NAME_PART1 = (byte) 0xF0;
    public static final byte COMMAND_MODULE_NAME_PART2 = (byte) 0xF1;
    public static final byte COMMAND_MODULE_NAME_PART3 = (byte) 0xF2;
    public static final byte COMMAND_CLEAR_LED = (byte) 0xF5;
    public static final byte COMMAND_SET_LED = (byte) 0xF6;
    public static final byte COMMAND_SLOW_BLINK_LED = (byte) 0xF7;
    public static final byte COMMAND_FAST_BLINK_LED = (byte) 0xF8;
    public static final byte COMMAND_VERY_FAST_BLINK_LED = (byte) 0xF9;
    public static final byte COMMAND_STATUS_REQUEST = (byte) 0xFA;
    public static final byte COMMAND_RELAY_STATUS = (byte) 0xFB;
    public static final byte COMMAND_WRITE_DATA_TO_MEMORY = (byte) 0xFC;
    public static final byte COMMAND_READ_DATA_FROM_MEMORY = (byte) 0xFD;
    public static final byte COMMAND_MEMORY_DATA = (byte) 0xFE;
    public static final byte COMMAND_MODULE_TYPE = (byte) 0xFF;

    // Useful values
    public static final byte ALL_CHANNELS = (byte) 0xFF;
    public static final byte ALL_DALI_CHANNELS = (byte) 0x51;
    public static final byte SUB_ADDRESS_DISABLED = (byte) 0xFF;
    public static final byte VALUE_UNCHANGED = (byte) 0xFF;
    public static final byte DALI_SETTING_ACTUAL_LEVEL = (byte) 0x1A;
    public static final byte DALI_MAX_VALUE = (byte) 0xFE;

    // Module properties
    public static final String PORT = "port";
    public static final String ADDRESS = "address";
    public static final String REFRESH_INTERVAL = "refresh";
    public static final String COUNTER1_PULSE_MULTIPLIER = "COUNTER1_PULSE_MULTIPLIER";
    public static final String COUNTER2_PULSE_MULTIPLIER = "COUNTER2_PULSE_MULTIPLIER";
    public static final String COUNTER3_PULSE_MULTIPLIER = "COUNTER3_PULSE_MULTIPLIER";
    public static final String COUNTER4_PULSE_MULTIPLIER = "COUNTER4_PULSE_MULTIPLIER";
    public static final String RECONNECTION_INTERVAL = "reconnectionInterval";
    public static final String TIME_UPDATE_INTERVAL = "timeUpdateInterval";
    public static final String MODULE_SERIAL_NUMBER = "serial number";
    public static final String MODULE_MEMORY_MAP_VERSION = "memory map version";
    public static final String MODULE_BUILD = "build";
    public static final String CHANNEL = "CH";
    public static final String DALI_ADDRESS = "A";
    public static final String VIRTUAL_LIGHT = "VL";
    public static final String SUB_ADDRESS = "subaddress";
    public static final String DIMSPEED = "dimspeed";

    // Channel group ids
    public static final String CHANNEL_GROUP_BRIDGE_CLOCK_ALARM = "bridgeClockAlarm";
    public static final String CHANNEL_GROUP_MODULE_CLOCK_ALARM = "clockAlarm";
    public static final String CHANNEL_GROUP_INPUT = "input";
    public static final String CHANNEL_GROUP_FEEDBACK = "feedback";
    public static final String CHANNEL_GROUP_BUTTON = "button";
    public static final String CHANNEL_GROUP_OUTPUT = "output";
    public static final String CHANNEL_GROUP_THERMOSTAT = "thermostat";
    public static final String CHANNEL_GROUP_COLOR = "color";
    public static final String CHANNEL_GROUP_BRIGHTNESS = "brightness";
    public static final String CHANNEL_GROUP_WHITE = "white";
    public static final String CHANNEL_GROUP_SCENE = "scene";
    public static final String CHANNEL_GROUP_VIRTUAL_LIGHT = "virtual-light";

    // Channels ids
    public static final String CHANNEL_CLOCK_ALARM1_TYPE = "clockAlarm1Type";
    public static final String CHANNEL_CLOCK_ALARM1_ENABLED = "clockAlarm1Enabled";
    public static final String CHANNEL_CLOCK_ALARM1_WAKEUP_HOUR = "clockAlarm1WakeupHour";
    public static final String CHANNEL_CLOCK_ALARM1_WAKEUP_MINUTE = "clockAlarm1WakeupMinute";
    public static final String CHANNEL_CLOCK_ALARM1_BEDTIME_HOUR = "clockAlarm1BedtimeHour";
    public static final String CHANNEL_CLOCK_ALARM1_BEDTIME_MINUTE = "clockAlarm1BedtimeMinute";
    public static final String CHANNEL_CLOCK_ALARM2_TYPE = "clockAlarm2Type";
    public static final String CHANNEL_CLOCK_ALARM2_ENABLED = "clockAlarm2Enabled";
    public static final String CHANNEL_CLOCK_ALARM2_WAKEUP_HOUR = "clockAlarm2WakeupHour";
    public static final String CHANNEL_CLOCK_ALARM2_WAKEUP_MINUTE = "clockAlarm2WakeupMinute";
    public static final String CHANNEL_CLOCK_ALARM2_BEDTIME_HOUR = "clockAlarm2BedtimeHour";
    public static final String CHANNEL_CLOCK_ALARM2_BEDTIME_MINUTE = "clockAlarm2BedtimeMinute";

    public static final String CHANNEL_THERMOSTAT_CURRENT_TEMPERATURE = "currentTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_HEATING_COMFORT = "heatingModeComfortTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_HEATING_DAY = "heatingModeDayTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_HEATING_NIGHT = "heatingModeNightTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_HEATING_ANTI_FROST = "heatingModeAntiFrostTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_COOLING_COMFORT = "coolingModeComfortTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_COOLING_DAY = "coolingModeDayTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_COOLING_NIGHT = "coolingModeNightTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_COOLING_SAFE = "coolingModeSafeTemperatureSetpoint";
    public static final String CHANNEL_THERMOSTAT_OPERATING_MODE = "operatingMode";
    public static final String CHANNEL_THERMOSTAT_MODE = "mode";
    public static final String CHANNEL_THERMOSTAT_HEATER = "heater";
    public static final String CHANNEL_THERMOSTAT_BOOST = "boost";
    public static final String CHANNEL_THERMOSTAT_PUMP = "pump";
    public static final String CHANNEL_THERMOSTAT_COOLER = "cooler";
    public static final String CHANNEL_THERMOSTAT_ALARM1 = "alarm1";
    public static final String CHANNEL_THERMOSTAT_ALARM2 = "alarm2";
    public static final String CHANNEL_THERMOSTAT_ALARM3 = "alarm3";
    public static final String CHANNEL_THERMOSTAT_ALARM4 = "alarm4";

    public static final String CHANNEL_OUTPUT = "output";

    // Delay
    public static final Integer DELAY_SEND_CLOCK_ALARM_UPDATE = 10000;
}
