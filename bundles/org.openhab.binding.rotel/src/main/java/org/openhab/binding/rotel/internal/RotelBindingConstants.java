/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RotelBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelBindingConstants {

    private static final String BINDING_ID = "rotel";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RSP1066 = new ThingTypeUID(BINDING_ID, "rsp1066");
    public static final ThingTypeUID THING_TYPE_RSP1068 = new ThingTypeUID(BINDING_ID, "rsp1068");
    public static final ThingTypeUID THING_TYPE_RSP1069 = new ThingTypeUID(BINDING_ID, "rsp1069");
    public static final ThingTypeUID THING_TYPE_RSP1098 = new ThingTypeUID(BINDING_ID, "rsp1098");
    public static final ThingTypeUID THING_TYPE_RSP1570 = new ThingTypeUID(BINDING_ID, "rsp1570");
    public static final ThingTypeUID THING_TYPE_RSP1572 = new ThingTypeUID(BINDING_ID, "rsp1572");
    public static final ThingTypeUID THING_TYPE_RSX1055 = new ThingTypeUID(BINDING_ID, "rsx1055");
    public static final ThingTypeUID THING_TYPE_RSX1056 = new ThingTypeUID(BINDING_ID, "rsx1056");
    public static final ThingTypeUID THING_TYPE_RSX1057 = new ThingTypeUID(BINDING_ID, "rsx1057");
    public static final ThingTypeUID THING_TYPE_RSX1058 = new ThingTypeUID(BINDING_ID, "rsx1058");
    public static final ThingTypeUID THING_TYPE_RSX1065 = new ThingTypeUID(BINDING_ID, "rsx1065");
    public static final ThingTypeUID THING_TYPE_RSX1067 = new ThingTypeUID(BINDING_ID, "rsx1067");
    public static final ThingTypeUID THING_TYPE_RSX1550 = new ThingTypeUID(BINDING_ID, "rsx1550");
    public static final ThingTypeUID THING_TYPE_RSX1560 = new ThingTypeUID(BINDING_ID, "rsx1560");
    public static final ThingTypeUID THING_TYPE_RSX1562 = new ThingTypeUID(BINDING_ID, "rsx1562");
    public static final ThingTypeUID THING_TYPE_A11 = new ThingTypeUID(BINDING_ID, "a11");
    public static final ThingTypeUID THING_TYPE_A12 = new ThingTypeUID(BINDING_ID, "a12");
    public static final ThingTypeUID THING_TYPE_A14 = new ThingTypeUID(BINDING_ID, "a14");
    public static final ThingTypeUID THING_TYPE_CD11 = new ThingTypeUID(BINDING_ID, "cd11");
    public static final ThingTypeUID THING_TYPE_CD14 = new ThingTypeUID(BINDING_ID, "cd14");
    public static final ThingTypeUID THING_TYPE_RA11 = new ThingTypeUID(BINDING_ID, "ra11");
    public static final ThingTypeUID THING_TYPE_RA12 = new ThingTypeUID(BINDING_ID, "ra12");
    public static final ThingTypeUID THING_TYPE_RA1570 = new ThingTypeUID(BINDING_ID, "ra1570");
    public static final ThingTypeUID THING_TYPE_RA1572 = new ThingTypeUID(BINDING_ID, "ra1572");
    public static final ThingTypeUID THING_TYPE_RA1592 = new ThingTypeUID(BINDING_ID, "ra1592");
    public static final ThingTypeUID THING_TYPE_RAP1580 = new ThingTypeUID(BINDING_ID, "rap1580");
    public static final ThingTypeUID THING_TYPE_RC1570 = new ThingTypeUID(BINDING_ID, "rc1570");
    public static final ThingTypeUID THING_TYPE_RC1572 = new ThingTypeUID(BINDING_ID, "rc1572");
    public static final ThingTypeUID THING_TYPE_RC1590 = new ThingTypeUID(BINDING_ID, "rc1590");
    public static final ThingTypeUID THING_TYPE_RCD1570 = new ThingTypeUID(BINDING_ID, "rcd1570");
    public static final ThingTypeUID THING_TYPE_RCD1572 = new ThingTypeUID(BINDING_ID, "rcd1572");
    public static final ThingTypeUID THING_TYPE_RCX1500 = new ThingTypeUID(BINDING_ID, "rcx1500");
    public static final ThingTypeUID THING_TYPE_RDD1580 = new ThingTypeUID(BINDING_ID, "rdd1580");
    public static final ThingTypeUID THING_TYPE_RDG1520 = new ThingTypeUID(BINDING_ID, "rdg1520");
    public static final ThingTypeUID THING_TYPE_RSP1576 = new ThingTypeUID(BINDING_ID, "rsp1576");
    public static final ThingTypeUID THING_TYPE_RSP1582 = new ThingTypeUID(BINDING_ID, "rsp1582");
    public static final ThingTypeUID THING_TYPE_RT09 = new ThingTypeUID(BINDING_ID, "rt09");
    public static final ThingTypeUID THING_TYPE_RT11 = new ThingTypeUID(BINDING_ID, "rt11");
    public static final ThingTypeUID THING_TYPE_RT1570 = new ThingTypeUID(BINDING_ID, "rt1570");
    public static final ThingTypeUID THING_TYPE_T11 = new ThingTypeUID(BINDING_ID, "t11");
    public static final ThingTypeUID THING_TYPE_T14 = new ThingTypeUID(BINDING_ID, "t14");

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
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_MAIN_MUTE = "mainZone#mute";
    public static final String CHANNEL_BASS = "bass";
    public static final String CHANNEL_MAIN_BASS = "mainZone#bass";
    public static final String CHANNEL_TREBLE = "treble";
    public static final String CHANNEL_MAIN_TREBLE = "mainZone#treble";
    public static final String CHANNEL_PLAY_CONTROL = "playControl";
    public static final String CHANNEL_TRACK = "track";
    public static final String CHANNEL_LINE1 = "mainZone#line1";
    public static final String CHANNEL_LINE2 = "mainZone#line2";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ZONE2_POWER = "zone2#power";
    public static final String CHANNEL_ZONE2_SOURCE = "zone2#source";
    public static final String CHANNEL_ZONE2_VOLUME = "zone2#volume";
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
