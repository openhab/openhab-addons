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
package org.openhab.binding.rotel.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RotelBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelBindingConstants {

    private static final String BINDING_ID = "rotel";

    // List of all Thing Type IDs
    public static final String THING_TYPE_ID_RSP1066 = "rsp1066";
    public static final String THING_TYPE_ID_RSP1068 = "rsp1068";
    public static final String THING_TYPE_ID_RSP1069 = "rsp1069";
    public static final String THING_TYPE_ID_RSP1098 = "rsp1098";
    public static final String THING_TYPE_ID_RSP1570 = "rsp1570";
    public static final String THING_TYPE_ID_RSP1572 = "rsp1572";
    public static final String THING_TYPE_ID_RSX1055 = "rsx1055";
    public static final String THING_TYPE_ID_RSX1056 = "rsx1056";
    public static final String THING_TYPE_ID_RSX1057 = "rsx1057";
    public static final String THING_TYPE_ID_RSX1058 = "rsx1058";
    public static final String THING_TYPE_ID_RSX1065 = "rsx1065";
    public static final String THING_TYPE_ID_RSX1067 = "rsx1067";
    public static final String THING_TYPE_ID_RSX1550 = "rsx1550";
    public static final String THING_TYPE_ID_RSX1560 = "rsx1560";
    public static final String THING_TYPE_ID_RSX1562 = "rsx1562";
    public static final String THING_TYPE_ID_RX1052 = "rx1052";
    public static final String THING_TYPE_ID_A11 = "a11";
    public static final String THING_TYPE_ID_A12 = "a12";
    public static final String THING_TYPE_ID_A14 = "a14";
    public static final String THING_TYPE_ID_CD11 = "cd11";
    public static final String THING_TYPE_ID_CD14 = "cd14";
    public static final String THING_TYPE_ID_RA11 = "ra11";
    public static final String THING_TYPE_ID_RA12 = "ra12";
    public static final String THING_TYPE_ID_RA1570 = "ra1570";
    public static final String THING_TYPE_ID_RA1572 = "ra1572";
    public static final String THING_TYPE_ID_RA1592 = "ra1592";
    public static final String THING_TYPE_ID_RAP1580 = "rap1580";
    public static final String THING_TYPE_ID_RC1570 = "rc1570";
    public static final String THING_TYPE_ID_RC1572 = "rc1572";
    public static final String THING_TYPE_ID_RC1590 = "rc1590";
    public static final String THING_TYPE_ID_RCD1570 = "rcd1570";
    public static final String THING_TYPE_ID_RCD1572 = "rcd1572";
    public static final String THING_TYPE_ID_RCX1500 = "rcx1500";
    public static final String THING_TYPE_ID_RDD1580 = "rdd1580";
    public static final String THING_TYPE_ID_RDG1520 = "rdg1520";
    public static final String THING_TYPE_ID_RSP1576 = "rsp1576";
    public static final String THING_TYPE_ID_RSP1582 = "rsp1582";
    public static final String THING_TYPE_ID_RT09 = "rt09";
    public static final String THING_TYPE_ID_RT11 = "rt11";
    public static final String THING_TYPE_ID_RT1570 = "rt1570";
    public static final String THING_TYPE_ID_T11 = "t11";
    public static final String THING_TYPE_ID_T14 = "t14";
    public static final String THING_TYPE_ID_C8 = "c8";
    public static final String THING_TYPE_ID_M8 = "m8";
    public static final String THING_TYPE_ID_P5 = "p5";
    public static final String THING_TYPE_ID_S5 = "s5";
    public static final String THING_TYPE_ID_X3 = "x3";
    public static final String THING_TYPE_ID_X5 = "x5";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RSP1066 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1066);
    public static final ThingTypeUID THING_TYPE_RSP1068 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1068);
    public static final ThingTypeUID THING_TYPE_RSP1069 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1069);
    public static final ThingTypeUID THING_TYPE_RSP1098 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1098);
    public static final ThingTypeUID THING_TYPE_RSP1570 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1570);
    public static final ThingTypeUID THING_TYPE_RSP1572 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1572);
    public static final ThingTypeUID THING_TYPE_RSX1055 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1055);
    public static final ThingTypeUID THING_TYPE_RSX1056 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1056);
    public static final ThingTypeUID THING_TYPE_RSX1057 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1057);
    public static final ThingTypeUID THING_TYPE_RSX1058 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1058);
    public static final ThingTypeUID THING_TYPE_RSX1065 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1065);
    public static final ThingTypeUID THING_TYPE_RSX1067 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1067);
    public static final ThingTypeUID THING_TYPE_RSX1550 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1550);
    public static final ThingTypeUID THING_TYPE_RSX1560 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1560);
    public static final ThingTypeUID THING_TYPE_RSX1562 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSX1562);
    public static final ThingTypeUID THING_TYPE_RX1052 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RX1052);
    public static final ThingTypeUID THING_TYPE_A11 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_A11);
    public static final ThingTypeUID THING_TYPE_A12 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_A12);
    public static final ThingTypeUID THING_TYPE_A14 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_A14);
    public static final ThingTypeUID THING_TYPE_CD11 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_CD11);
    public static final ThingTypeUID THING_TYPE_CD14 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_CD14);
    public static final ThingTypeUID THING_TYPE_RA11 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RA11);
    public static final ThingTypeUID THING_TYPE_RA12 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RA12);
    public static final ThingTypeUID THING_TYPE_RA1570 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RA1570);
    public static final ThingTypeUID THING_TYPE_RA1572 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RA1572);
    public static final ThingTypeUID THING_TYPE_RA1592 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RA1592);
    public static final ThingTypeUID THING_TYPE_RAP1580 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RAP1580);
    public static final ThingTypeUID THING_TYPE_RC1570 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RC1570);
    public static final ThingTypeUID THING_TYPE_RC1572 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RC1572);
    public static final ThingTypeUID THING_TYPE_RC1590 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RC1590);
    public static final ThingTypeUID THING_TYPE_RCD1570 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RCD1570);
    public static final ThingTypeUID THING_TYPE_RCD1572 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RCD1572);
    public static final ThingTypeUID THING_TYPE_RCX1500 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RCX1500);
    public static final ThingTypeUID THING_TYPE_RDD1580 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RDD1580);
    public static final ThingTypeUID THING_TYPE_RDG1520 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RDG1520);
    public static final ThingTypeUID THING_TYPE_RSP1576 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1576);
    public static final ThingTypeUID THING_TYPE_RSP1582 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RSP1582);
    public static final ThingTypeUID THING_TYPE_RT09 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RT09);
    public static final ThingTypeUID THING_TYPE_RT11 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RT11);
    public static final ThingTypeUID THING_TYPE_RT1570 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_RT1570);
    public static final ThingTypeUID THING_TYPE_T11 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_T11);
    public static final ThingTypeUID THING_TYPE_T14 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_T14);
    public static final ThingTypeUID THING_TYPE_C8 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_C8);
    public static final ThingTypeUID THING_TYPE_M8 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_M8);
    public static final ThingTypeUID THING_TYPE_P5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_P5);
    public static final ThingTypeUID THING_TYPE_S5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_S5);
    public static final ThingTypeUID THING_TYPE_X3 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_X3);
    public static final ThingTypeUID THING_TYPE_X5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_X5);

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SOURCE = "source";
    public static final String CHANNEL_RECORD_SOURCE = "recordSource";
    public static final String CHANNEL_DSP = "dsp";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_VOLUME_UP_DOWN = "volumeUpDown";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_BASS = "bass";
    public static final String CHANNEL_TREBLE = "treble";
    public static final String CHANNEL_PLAY_CONTROL = "playControl";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_RANDOM = "random";
    public static final String CHANNEL_REPEAT = "repeat";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_RADIO_PRESET = "radioPreset";
    public static final String CHANNEL_LINE1 = "mainZone#line1";
    public static final String CHANNEL_LINE2 = "mainZone#line2";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_TCBYPASS = "tcbypass";
    public static final String CHANNEL_BALANCE = "balance";
    public static final String CHANNEL_SPEAKER_A = "speakera";
    public static final String CHANNEL_SPEAKER_B = "speakerb";
    public static final String CHANNEL_OTHER_COMMAND = "otherCommand";

    public static final String CHANNEL_GROUP_ALL_ZONES = "allZones";
    public static final String CHANNEL_ALL_POWER = CHANNEL_GROUP_ALL_ZONES + "#" + CHANNEL_POWER;
    public static final String CHANNEL_ALL_BRIGHTNESS = CHANNEL_GROUP_ALL_ZONES + "#" + CHANNEL_BRIGHTNESS;

    public static final String CHANNEL_GROUP_MAIN_ZONE = "mainZone";
    public static final String CHANNEL_MAIN_POWER = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_POWER;
    public static final String CHANNEL_MAIN_SOURCE = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_SOURCE;
    public static final String CHANNEL_MAIN_RECORD_SOURCE = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_RECORD_SOURCE;
    public static final String CHANNEL_MAIN_DSP = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_DSP;
    public static final String CHANNEL_MAIN_VOLUME = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_VOLUME;
    public static final String CHANNEL_MAIN_VOLUME_UP_DOWN = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_VOLUME_UP_DOWN;
    public static final String CHANNEL_MAIN_MUTE = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_MUTE;
    public static final String CHANNEL_MAIN_BASS = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_BASS;
    public static final String CHANNEL_MAIN_TREBLE = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_TREBLE;
    public static final String CHANNEL_MAIN_SPEAKER_A = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_SPEAKER_A;
    public static final String CHANNEL_MAIN_SPEAKER_B = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_SPEAKER_B;
    public static final String CHANNEL_MAIN_OTHER_COMMAND = CHANNEL_GROUP_MAIN_ZONE + "#" + CHANNEL_OTHER_COMMAND;

    public static final String CHANNEL_GROUP_ZONE1 = "zone1";
    public static final String CHANNEL_ZONE1_SOURCE = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_SOURCE;
    public static final String CHANNEL_ZONE1_VOLUME = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_VOLUME;
    public static final String CHANNEL_ZONE1_MUTE = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_MUTE;
    public static final String CHANNEL_ZONE1_BASS = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_BASS;
    public static final String CHANNEL_ZONE1_TREBLE = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_TREBLE;
    public static final String CHANNEL_ZONE1_BALANCE = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_BALANCE;
    public static final String CHANNEL_ZONE1_FREQUENCY = CHANNEL_GROUP_ZONE1 + "#" + CHANNEL_FREQUENCY;

    public static final String CHANNEL_GROUP_ZONE2 = "zone2";
    public static final String CHANNEL_ZONE2_POWER = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_POWER;
    public static final String CHANNEL_ZONE2_SOURCE = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_SOURCE;
    public static final String CHANNEL_ZONE2_VOLUME = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_VOLUME;
    public static final String CHANNEL_ZONE2_VOLUME_UP_DOWN = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_VOLUME_UP_DOWN;
    public static final String CHANNEL_ZONE2_MUTE = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_MUTE;
    public static final String CHANNEL_ZONE2_BASS = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_BASS;
    public static final String CHANNEL_ZONE2_TREBLE = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_TREBLE;
    public static final String CHANNEL_ZONE2_BALANCE = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_BALANCE;
    public static final String CHANNEL_ZONE2_FREQUENCY = CHANNEL_GROUP_ZONE2 + "#" + CHANNEL_FREQUENCY;

    public static final String CHANNEL_GROUP_ZONE3 = "zone3";
    public static final String CHANNEL_ZONE3_POWER = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_POWER;
    public static final String CHANNEL_ZONE3_SOURCE = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_SOURCE;
    public static final String CHANNEL_ZONE3_VOLUME = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_VOLUME;
    public static final String CHANNEL_ZONE3_MUTE = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_MUTE;
    public static final String CHANNEL_ZONE3_BASS = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_BASS;
    public static final String CHANNEL_ZONE3_TREBLE = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_TREBLE;
    public static final String CHANNEL_ZONE3_BALANCE = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_BALANCE;
    public static final String CHANNEL_ZONE3_FREQUENCY = CHANNEL_GROUP_ZONE3 + "#" + CHANNEL_FREQUENCY;

    public static final String CHANNEL_GROUP_ZONE4 = "zone4";
    public static final String CHANNEL_ZONE4_POWER = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_POWER;
    public static final String CHANNEL_ZONE4_SOURCE = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_SOURCE;
    public static final String CHANNEL_ZONE4_VOLUME = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_VOLUME;
    public static final String CHANNEL_ZONE4_MUTE = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_MUTE;
    public static final String CHANNEL_ZONE4_BASS = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_BASS;
    public static final String CHANNEL_ZONE4_TREBLE = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_TREBLE;
    public static final String CHANNEL_ZONE4_BALANCE = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_BALANCE;
    public static final String CHANNEL_ZONE4_FREQUENCY = CHANNEL_GROUP_ZONE4 + "#" + CHANNEL_FREQUENCY;

    // List of all properties
    public static final String PROPERTY_PROTOCOL = "protocol";

    // Message types (HEX protocol)
    public static final byte PRIMARY_CMD = (byte) 0x10;
    public static final byte MAIN_ZONE_CMD = (byte) 0x14;
    public static final byte RECORD_SRC_CMD = (byte) 0x15;
    public static final byte ZONE2_CMD = (byte) 0x16;
    public static final byte ZONE3_CMD = (byte) 0x17;
    public static final byte ZONE4_CMD = (byte) 0x18;
    public static final byte VOLUME_CMD = (byte) 0x30;
    public static final byte ZONE2_VOLUME_CMD = (byte) 0x32;
    public static final byte ZONE3_VOLUME_CMD = (byte) 0x33;
    public static final byte ZONE4_VOLUME_CMD = (byte) 0x34;
    public static final byte TRIGGER_CMD = (byte) 0x40;
    public static final byte STANDARD_RESPONSE = (byte) 0x20;
    public static final byte TRIGGER_STATUS = (byte) 0x21;
    public static final byte SMART_DISPLAY_DATA_1 = (byte) 0x22;
    public static final byte SMART_DISPLAY_DATA_2 = (byte) 0x23;

    // Common (output) keys used by the HEX and ASCII protocols
    public static final String KEY_POWER = "power";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_VOLUME_ZONE2 = "volume_zone2";
    public static final String KEY_VOLUME_ZONE3 = "volume_zone3";
    public static final String KEY_VOLUME_ZONE4 = "volume_zone4";
    public static final String KEY_MUTE = "mute";
    public static final String KEY_MUTE_ZONE2 = "mute_zone2";
    public static final String KEY_MUTE_ZONE3 = "mute_zone3";
    public static final String KEY_MUTE_ZONE4 = "mute_zone4";
    public static final String KEY_BASS = "bass";
    public static final String KEY_TREBLE = "treble";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_DSP_MODE = "dsp_mode";
    public static final String KEY_ERROR = "error";
    // Keys only used by the ASCII protocol
    public static final String KEY_POWER_MODE = "power_mode";
    public static final String KEY_INPUT = "input";
    public static final String KEY_INPUT_ZONE1 = "input_zone1";
    public static final String KEY_INPUT_ZONE2 = "input_zone2";
    public static final String KEY_INPUT_ZONE3 = "input_zone3";
    public static final String KEY_INPUT_ZONE4 = "input_zone4";
    public static final String KEY_VOLUME_ZONE1 = "volume_zone1";
    public static final String KEY_MUTE_ZONE1 = "mute_zone1";
    public static final String KEY_BASS_ZONE1 = "bass_zone1";
    public static final String KEY_BASS_ZONE2 = "bass_zone2";
    public static final String KEY_BASS_ZONE3 = "bass_zone3";
    public static final String KEY_BASS_ZONE4 = "bass_zone4";
    public static final String KEY_TREBLE_ZONE1 = "treble_zone1";
    public static final String KEY_TREBLE_ZONE2 = "treble_zone2";
    public static final String KEY_TREBLE_ZONE3 = "treble_zone3";
    public static final String KEY_TREBLE_ZONE4 = "treble_zone4";
    public static final String KEY_UPDATE_MODE = "update_mode";
    public static final String KEY_DISPLAY_UPDATE = "display_update";
    public static final String KEY_VOLUME_MIN = "volume_min";
    public static final String KEY_VOLUME_MAX = "volume_max";
    public static final String KEY_TONE_MAX = "tone_max";
    public static final String KEY1_PLAY_STATUS = "play_status";
    public static final String KEY2_PLAY_STATUS = "status";
    public static final String KEY_DISC_NAME = "disc_name";
    public static final String KEY_DISC_TYPE = "disc_type";
    public static final String KEY_TRACK = "track";
    public static final String KEY_TRACK_NAME = "track_name";
    public static final String KEY_TIME = "time";
    public static final String KEY_RANDOM = "rnd";
    public static final String KEY_REPEAT = "rpt";
    public static final String KEY_PRESET_FM = "preset_fm";
    public static final String KEY_FM_PRESET = "fm_preset_";
    public static final String KEY_FM_ALL_PRESET = "fm_allpreset_";
    public static final String KEY_FM = "fm";
    public static final String KEY_FM_MONO = "fm_mono";
    public static final String KEY_FM_RDS = "fm_rds";
    public static final String KEY_FM_FREQ = "fm_freq";
    public static final String KEY_PRESET_DAB = "preset_dab";
    public static final String KEY_DAB_PRESET = "dab_preset_";
    public static final String KEY_DAB_ALL_PRESET = "dab_allpreset_";
    public static final String KEY_DAB = "dab";
    public static final String KEY_DAB_STATION = "dab_station";
    public static final String KEY_PRESET_IRADIO = "preset_iradio";
    public static final String KEY_IRADIO_PRESET = "iradio_preset_";
    public static final String KEY_IRADIO_ALL_PRESET = "iradio_allpreset_";
    public static final String KEY_CURRENT_STATION = "current_station";
    public static final String KEY_SIGNAL_STRENGTH = "signal_strength";
    public static final String KEY_DIMMER = "dimmer";
    public static final String KEY_FREQ = "freq";
    public static final String KEY_FREQ_ZONE1 = "freq_zone1";
    public static final String KEY_FREQ_ZONE2 = "freq_zone2";
    public static final String KEY_FREQ_ZONE3 = "freq_zone3";
    public static final String KEY_FREQ_ZONE4 = "freq_zone4";
    public static final String KEY_TONE = "tone";
    public static final String KEY_TCBYPASS = "bypass";
    public static final String KEY_BALANCE = "balance";
    public static final String KEY_BALANCE_ZONE1 = "balance_zone1";
    public static final String KEY_BALANCE_ZONE2 = "balance_zone2";
    public static final String KEY_BALANCE_ZONE3 = "balance_zone3";
    public static final String KEY_BALANCE_ZONE4 = "balance_zone4";
    public static final String KEY_SPEAKER = "speaker";
    public static final String KEY_SUB_LEVEL = "subwoofer_level";
    public static final String KEY_CENTER_LEVEL = "center_level";
    public static final String KEY_SURROUND_RIGHT_LEVEL = "surround_right";
    public static final String KEY_SURROUND_LEFT_LEVEL = "surround_left";
    public static final String KEY_CENTER_BACK_RIGHT_LEVEL = "center_back_right";
    public static final String KEY_CENTER_BACK_LEFT_LEVEL = "center_back_left";
    public static final String KEY_CEILING_FRONT_RIGHT_LEVEL = "ceiling_front_right";
    public static final String KEY_CEILING_FRONT_LEFT_LEVEL = "ceiling_front_left";
    public static final String KEY_CEILING_REAR_RIGHT_LEVEL = "ceiling_rear_right";
    public static final String KEY_CEILING_REAR_LEFT_LEVEL = "ceiling_rear_left";
    public static final String KEY_PCUSB_CLASS = "pcusb_class";
    public static final String KEY_PRODUCT_TYPE = "product_type";
    public static final String KEY_MODEL = "model";
    public static final String KEY_PRODUCT_VERSION = "product_version";
    public static final String KEY_VERSION = "version";
    public static final String KEY_TC_VERSION = "tc_version";
    public static final String KEY_DISPLAY = "display";
    public static final String KEY_DISPLAY1 = "display1";
    public static final String KEY_DISPLAY2 = "display2";
    public static final String KEY_DISPLAY3 = "display3";
    public static final String KEY_DISPLAY4 = "display4";
    // Output keys only used by the HEX protocol
    public static final String KEY_LINE1 = "line1";
    public static final String KEY_LINE2 = "line2";
    public static final String KEY_RECORD = "record";
    public static final String KEY_RECORD_SEL = "record_sel";
    public static final String KEY_ZONE = "zone";
    public static final String KEY_POWER_ZONE2 = "power_zone2";
    public static final String KEY_POWER_ZONE3 = "power_zone3";
    public static final String KEY_POWER_ZONE4 = "power_zone4";
    public static final String KEY_POWER_ZONES = "power_zones";
    public static final String KEY_SOURCE_ZONE2 = "source_zone2";
    public static final String KEY_SOURCE_ZONE3 = "source_zone3";
    public static final String KEY_SOURCE_ZONE4 = "source_zone4";

    // Specific values for keys
    public static final String MSG_VALUE_OFF = "off";
    public static final String MSG_VALUE_ON = "on";
    public static final String MSG_VALUE_NONE = "none";
    public static final String MSG_VALUE_OVER = "over";
    public static final String POWER_ON = "on";
    public static final String POWER_QUICK = "quick";
    public static final String POWER_NORMAL = "normal";
    public static final String STANDBY = "standby";
    public static final String POWER_OFF_DELAYED = "off_delayed";
    public static final String MSG_VALUE_SPEAKER_A = "a";
    public static final String MSG_VALUE_SPEAKER_B = "b";
    public static final String MSG_VALUE_SPEAKER_AB = "a_b";
    public static final String MSG_VALUE_MIN = "min";
    public static final String MSG_VALUE_MAX = "max";
    public static final String MSG_VALUE_FIX = "fix";
    public static final String AUTO = "auto";
    public static final String MANUAL = "manual";
    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    public static final String TRACK = "track";
    public static final String DISC = "disc";

    public static final int MAX_NUMBER_OF_ZONES = 4;
}
