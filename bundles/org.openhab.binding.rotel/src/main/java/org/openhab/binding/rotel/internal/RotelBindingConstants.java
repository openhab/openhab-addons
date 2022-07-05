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
    public static final ThingTypeUID THING_TYPE_M8 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_M8);
    public static final ThingTypeUID THING_TYPE_P5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_P5);
    public static final ThingTypeUID THING_TYPE_S5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_S5);
    public static final ThingTypeUID THING_TYPE_X3 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_X3);
    public static final ThingTypeUID THING_TYPE_X5 = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_X5);

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MAIN_POWER = "mainZone#power";
    public static final String CHANNEL_SOURCE = "source";
    public static final String CHANNEL_MAIN_SOURCE = "mainZone#source";
    public static final String CHANNEL_MAIN_RECORD_SOURCE = "mainZone#recordSource";
    public static final String CHANNEL_DSP = "dsp";
    public static final String CHANNEL_MAIN_DSP = "mainZone#dsp";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MAIN_VOLUME = "mainZone#volume";
    public static final String CHANNEL_MAIN_VOLUME_UP_DOWN = "mainZone#volumeUpDown";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_MAIN_MUTE = "mainZone#mute";
    public static final String CHANNEL_BASS = "bass";
    public static final String CHANNEL_MAIN_BASS = "mainZone#bass";
    public static final String CHANNEL_TREBLE = "treble";
    public static final String CHANNEL_MAIN_TREBLE = "mainZone#treble";
    public static final String CHANNEL_PLAY_CONTROL = "playControl";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_FREQUENCY = "frequency";
    public static final String CHANNEL_LINE1 = "mainZone#line1";
    public static final String CHANNEL_LINE2 = "mainZone#line2";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_SOURCE = "zone2#source";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
    public static final String CHANNEL_ZONE2_VOLUME_UP_DOWN = "zone2#volumeUpDown";
    public static final String CHANNEL_ZONE2_MUTE = "zone2#mute";
    public static final String CHANNEL_ZONE3_POWER = "zone3#power";
    public static final String CHANNEL_ZONE3_SOURCE = "zone3#source";
    public static final String CHANNEL_ZONE3_VOLUME = "zone3#volume";
    public static final String CHANNEL_ZONE3_MUTE = "zone3#mute";
    public static final String CHANNEL_ZONE4_POWER = "zone4#power";
    public static final String CHANNEL_ZONE4_SOURCE = "zone4#source";
    public static final String CHANNEL_ZONE4_VOLUME = "zone4#volume";
    public static final String CHANNEL_ZONE4_MUTE = "zone4#mute";
    public static final String CHANNEL_TCBYPASS = "tcbypass";
    public static final String CHANNEL_BALANCE = "balance";
    public static final String CHANNEL_SPEAKER_A = "speakera";
    public static final String CHANNEL_SPEAKER_B = "speakerb";

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
    public static final String KEY_MUTE = "mute";
    public static final String KEY_BASS = "bass";
    public static final String KEY_TREBLE = "treble";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_DSP_MODE = "dsp_mode";
    public static final String KEY_ERROR = "error";
    // Keys only used by the ASCII protocol
    public static final String KEY_UPDATE_MODE = "update_mode";
    public static final String KEY_DISPLAY_UPDATE = "display_update";
    public static final String KEY_VOLUME_MIN = "volume_min";
    public static final String KEY_VOLUME_MAX = "volume_max";
    public static final String KEY_TONE_MAX = "tone_max";
    public static final String KEY1_PLAY_STATUS = "play_status";
    public static final String KEY2_PLAY_STATUS = "status";
    public static final String KEY_TRACK = "track";
    public static final String KEY_DIMMER = "dimmer";
    public static final String KEY_FREQ = "freq";
    public static final String KEY_TONE = "tone";
    public static final String KEY_TCBYPASS = "bypass";
    public static final String KEY_BALANCE = "balance";
    public static final String KEY_SPEAKER = "speaker";
    // Output keys only used by the HEX protocol
    public static final String KEY_LINE1 = "line1";
    public static final String KEY_LINE2 = "line2";
    public static final String KEY_RECORD = "record";
    public static final String KEY_RECORD_SEL = "record_sel";
    public static final String KEY_ZONE = "zone";
    public static final String KEY_POWER_ZONE2 = "power_zone2";
    public static final String KEY_POWER_ZONE3 = "power_zone3";
    public static final String KEY_POWER_ZONE4 = "power_zone4";
    public static final String KEY_SOURCE_ZONE2 = "source_zone2";
    public static final String KEY_SOURCE_ZONE3 = "source_zone3";
    public static final String KEY_SOURCE_ZONE4 = "source_zone4";
    public static final String KEY_VOLUME_ZONE2 = "volume_zone2";
    public static final String KEY_VOLUME_ZONE3 = "volume_zone3";
    public static final String KEY_VOLUME_ZONE4 = "volume_zone4";
    public static final String KEY_MUTE_ZONE2 = "mute_zone2";
    public static final String KEY_MUTE_ZONE3 = "mute_zone3";
    public static final String KEY_MUTE_ZONE4 = "mute_zone4";

    // Specific values for keys
    public static final String MSG_VALUE_OFF = "off";
    public static final String MSG_VALUE_ON = "on";
    public static final String POWER_ON = "on";
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
}
