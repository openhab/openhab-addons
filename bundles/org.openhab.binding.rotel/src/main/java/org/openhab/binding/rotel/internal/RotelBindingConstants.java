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

    // List of all properties
    public static final String PROPERTY_PROTOCOL = "protocol";
}
