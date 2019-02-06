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
package org.openhab.binding.hdl;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link HdlBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author stigla - Initial contribution
 */
public class HdlBindingConstants {

    public static final String BINDING_ID = "hdl";

    public static final String PROPERTY_IP = "Ip";
    public static final String PROPERTY_PORT = "Port";

    public static final String PROPERTY_SUBNET = "Subnet";
    public static final String PROPERTY_DEVICEID = "DeviceID";
    public static final String PROPERTY_CHANNEL = "Channel";
    public static final String PROPERTY_REFRESHRATE = "refreshInterval";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final ThingTypeUID THING_TYPE_ML01 = new ThingTypeUID(BINDING_ID, "ML01"); // Dimmer
    public static final ThingTypeUID THING_TYPE_MDT0601_233 = new ThingTypeUID(BINDING_ID, "MDT0601_233"); // 6 Ch Uni
                                                                                                           // Dim
    public static final ThingTypeUID THING_TYPE_MPL8_48_FH = new ThingTypeUID(BINDING_ID, "MPL8_48_FH"); // DLP Bryter
                                                                                                         // panel
    public static final ThingTypeUID THING_TYPE_MPT04_48 = new ThingTypeUID(BINDING_ID, "MPT04_48"); // 4 Knapper
    public static final ThingTypeUID THING_TYPE_MR1216_233 = new ThingTypeUID(BINDING_ID, "MR1216_233"); // 12 Rele på
                                                                                                         // 16 amp
    public static final ThingTypeUID THING_TYPE_MRDA0610_432 = new ThingTypeUID(BINDING_ID, "MRDA0610_432"); // 6
                                                                                                             // kanaler
                                                                                                             // 0-10 V
    public static final ThingTypeUID THING_TYPE_MRDA06 = new ThingTypeUID(BINDING_ID, "MRDA06"); // 6 kanaler 0-10 V
    public static final ThingTypeUID THING_TYPE_MW02 = new ThingTypeUID(BINDING_ID, "MW02_231"); // Gardin kontroller
    public static final ThingTypeUID THING_TYPE_MS12_2C = new ThingTypeUID(BINDING_ID, "MS12_2C"); // 12 i 1
    public static final ThingTypeUID THING_TYPE_MS08MN_2C = new ThingTypeUID(BINDING_ID, "MS08Mn_2C"); // 8 i 1 sensor
    public static final ThingTypeUID THING_TYPE_MS24 = new ThingTypeUID(BINDING_ID, "MS24"); // Sensor Input Module

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATUR = "temperature";
    public static final String CHANNEL_TIME = "time";
    public static final String CHANNEL_BRIGHTNESS = "Brightness";
    public static final String CHANNEL_MOTIONSSENSOR = "MotionSensor";
    public static final String CHANNEL_SONIC = "Sonic";
    public static final String CHANNEL_FHNORMALTEMPSET = "FHNormalTempSet";
    public static final String CHANNEL_FHDAYTEMPSET = "FHDayTempSet";
    public static final String CHANNEL_FHNIGHTTEMPSET = "FHNightTempSet";
    public static final String CHANNEL_FHAWAYTEMPSET = "FHAwayTempSet";
    public static final String CHANNEL_FHCURRENTTEMPSET = "FHCurrentTempSet";
    public static final String CHANNEL_FHMODE = "FHMode";
    public static final String CHANNEL_ACMODE = "ACMode";
    public static final String CHANNEL_ACFANSPEED = "ACFanSpeed";
    public static final String CHANNEL_ACCOOLINGTEMPSET = "ACCoolingTempSet";
    public static final String CHANNEL_ACHEATTEMPSET = "ACHeatTempSet";
    public static final String CHANNEL_ACAUTOTEMPSET = "ACAutoTempSet";
    public static final String CHANNEL_ACDRYTEMPSET = "ACDryTempSet";
    public static final String CHANNEL_ACCURRENTTEMPSET = "ACCurrentTempSet";
    // public static final String CHANNEL_SHUTTER1UPDOWN = "Shutter1UpDown";
    // public static final String CHANNEL_SHUTTER2UPDOWN = "Shutter2UpDown";
    public static final String CHANNEL_SHUTTER1CONTROL = "Shutter1Control";
    public static final String CHANNEL_SHUTTER2CONTROL = "Shutter2Control";

    public static final String CHANNEL_DRYCONTACT1 = "DryContact1Status";
    public static final String CHANNEL_DRYCONTACT2 = "DryContact2Status";
    public static final String CHANNEL_DRYCONTACT3 = "DryContact3Status";
    public static final String CHANNEL_DRYCONTACT4 = "DryContact4Status";
    public static final String CHANNEL_DRYCONTACT5 = "DryContact5Status";
    public static final String CHANNEL_DRYCONTACT6 = "DryContact6Status";
    public static final String CHANNEL_DRYCONTACT7 = "DryContact7Status";
    public static final String CHANNEL_DRYCONTACT8 = "DryContact8Status";
    public static final String CHANNEL_DRYCONTACT9 = "DryContact9Status";
    public static final String CHANNEL_DRYCONTACT10 = "DryContac10Status";
    public static final String CHANNEL_DRYCONTACT11 = "DryContac11Status";
    public static final String CHANNEL_DRYCONTACT12 = "DryContac12Status";
    public static final String CHANNEL_DRYCONTACT13 = "DryContac13Status";
    public static final String CHANNEL_DRYCONTACT14 = "DryContac14Status";
    public static final String CHANNEL_DRYCONTACT15 = "DryContac15Status";
    public static final String CHANNEL_DRYCONTACT16 = "DryContac16Status";
    public static final String CHANNEL_DRYCONTACT17 = "DryContac17Status";
    public static final String CHANNEL_DRYCONTACT18 = "DryContac18Status";
    public static final String CHANNEL_DRYCONTACT19 = "DryContac19Status";
    public static final String CHANNEL_DRYCONTACT20 = "DryContact20Status";
    public static final String CHANNEL_DRYCONTACT21 = "DryContact21Status";
    public static final String CHANNEL_DRYCONTACT22 = "DryContact22Status";
    public static final String CHANNEL_DRYCONTACT23 = "DryContact23Status";
    public static final String CHANNEL_DRYCONTACT24 = "DryContact24Status";
    public static final String CHANNEL_DIMCHANNEL1 = "DimChannel1";
    public static final String CHANNEL_DIMCHANNEL2 = "DimChannel2";
    public static final String CHANNEL_DIMCHANNEL3 = "DimChannel3";
    public static final String CHANNEL_DIMCHANNEL4 = "DimChannel4";
    public static final String CHANNEL_DIMCHANNEL5 = "DimChannel5";
    public static final String CHANNEL_DIMCHANNEL6 = "DimChannel6";
    public static final String CHANNEL_RELAYCH1 = "RelayCh1";
    public static final String CHANNEL_RELAYCH2 = "RelayCh2";
    public static final String CHANNEL_RELAYCH3 = "RelayCh3";
    public static final String CHANNEL_RELAYCH4 = "RelayCh4";
    public static final String CHANNEL_RELAYCH5 = "RelayCh5";
    public static final String CHANNEL_RELAYCH6 = "RelayCh6";
    public static final String CHANNEL_RELAYCH7 = "RelayCh7";
    public static final String CHANNEL_RELAYCH8 = "RelayCh8";
    public static final String CHANNEL_RELAYCH9 = "RelayCh9";
    public static final String CHANNEL_RELAYCH10 = "RelayCh10";
    public static final String CHANNEL_RELAYCH11 = "RelayCh11";
    public static final String CHANNEL_RELAYCH12 = "RelayCh12";
    public static final String CHANNEL_UVSWITCH1 = "UVSwitch1";
    public static final String CHANNEL_UVSWITCH2 = "UVSwitch2";
    public static final String CHANNEL_UVSWITCH3 = "UVSwitch3";
    public static final String CHANNEL_UVSWITCH4 = "UVSwitch4";
    public static final String CHANNEL_UVSWITCH5 = "UVSwitch5";
    public static final String CHANNEL_UVSWITCH6 = "UVSwitch6";
    public static final String CHANNEL_UVSWITCH200 = "UVSwitch200";
    public static final String CHANNEL_UVSWITCH201 = "UVSwitch201";
    public static final String CHANNEL_UVSWITCH202 = "UVSwitch202";
    public static final String CHANNEL_UVSWITCH203 = "UVSwitch203";
    public static final String CHANNEL_UVSWITCH204 = "UVSwitch204";
    public static final String CHANNEL_UVSWITCH205 = "UVSwitch205";
    public static final String CHANNEL_UVSWITCH206 = "UVSwitch206";
    public static final String CHANNEL_UVSWITCH207 = "UVSwitch207";
    public static final String CHANNEL_UVSWITCH208 = "UVSwitch208";
    public static final String CHANNEL_UVSWITCH209 = "UVSwitch209";
    public static final String CHANNEL_UVSWITCH210 = "UVSwitch210";
    public static final String CHANNEL_UVSWITCH211 = "UVSwitch211";
    public static final String CHANNEL_UVSWITCH212 = "UVSwitch212";
    public static final String CHANNEL_UVSWITCH213 = "UVSwitch213";
    public static final String CHANNEL_UVSWITCH214 = "UVSwitch214";
    public static final String CHANNEL_UVSWITCH215 = "UVSwitch215";
    public static final String CHANNEL_UVSWITCH216 = "UVSwitch216";
    public static final String CHANNEL_UVSWITCH217 = "UVSwitch217";
    public static final String CHANNEL_UVSWITCH218 = "UVSwitch218";
    public static final String CHANNEL_UVSWITCH219 = "UVSwitch219";
    public static final String CHANNEL_UVSWITCH220 = "UVSwitch220";
    public static final String CHANNEL_UVSWITCH221 = "UVSwitch221";
    public static final String CHANNEL_UVSWITCH222 = "UVSwitch222";
    public static final String CHANNEL_UVSWITCH223 = "UVSwitch223";
    public static final String CHANNEL_UVSWITCH224 = "UVSwitch224";
    public static final String CHANNEL_UVSWITCH225 = "UVSwitch225";
    public static final String CHANNEL_UVSWITCH226 = "UVSwitch226";
    public static final String CHANNEL_UVSWITCH227 = "UVSwitch227";
    public static final String CHANNEL_UVSWITCH228 = "UVSwitch228";
    public static final String CHANNEL_UVSWITCH229 = "UVSwitch229";
    public static final String CHANNEL_UVSWITCH230 = "UVSwitch230";
    public static final String CHANNEL_UVSWITCH231 = "UVSwitch231";
    public static final String CHANNEL_UVSWITCH232 = "UVSwitch232";
    public static final String CHANNEL_UVSWITCH233 = "UVSwitch233";
    public static final String CHANNEL_UVSWITCH234 = "UVSwitch234";
    public static final String CHANNEL_UVSWITCH235 = "UVSwitch235";
    public static final String CHANNEL_UVSWITCH236 = "UVSwitch236";
    public static final String CHANNEL_UVSWITCH237 = "UVSwitch237";
    public static final String CHANNEL_UVSWITCH238 = "UVSwitch238";
    public static final String CHANNEL_UVSWITCH239 = "UVSwitch239";
    public static final String CHANNEL_UVSWITCH240 = "UVSwitch240";

    public enum emumFHMode {
        Normal,
        Day,
        Night,
        Away,
        Timer
    }

    public enum DryContactNr {
        DryContact1(1),
        DryContact2(2),
        DryContact3(3),
        DryContact4(4),
        DryContact5(5),
        DryContact6(6),
        DryContact7(7),
        DryContact8(8),
        DryContact9(9),
        DryContact10(10),
        DryContact11(11),
        DryContact12(12),
        DryContact13(13),
        DryContact14(14),
        DryContact15(15),
        DryContact16(16),
        DryContact17(17),
        DryContact18(18),
        DryContact19(19),
        DryContact20(20),
        DryContact21(21),
        DryContact22(22),
        DryContact23(23),
        DryContact24(24);

        private int value;

        private DryContactNr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum CurtainNr {
        Shutter1Control(1),
        Shutter2Control(2);

        private int value;

        private CurtainNr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public enum UVSwitchNr {
        UVSwitch1(1),
        UVSwitch2(2),
        UVSwitch3(3),
        UVSwitch4(4),
        UVSwitch5(5),
        UVSwitch6(6),
        UVSwitch200(200),
        UVSwitch201(201),
        UVSwitch202(202),
        UVSwitch203(203),
        UVSwitch204(204),
        UVSwitch205(205),
        UVSwitch206(206),
        UVSwitch207(207),
        UVSwitch208(208),
        UVSwitch209(209),
        UVSwitch210(210),
        UVSwitch211(211),
        UVSwitch212(212),
        UVSwitch213(213),
        UVSwitch214(214),
        UVSwitch215(215),
        UVSwitch216(216),
        UVSwitch217(217),
        UVSwitch218(218),
        UVSwitch219(219),
        UVSwitch220(220),
        UVSwitch221(221),
        UVSwitch222(222),
        UVSwitch223(223),
        UVSwitch224(224),
        UVSwitch225(225),
        UVSwitch226(226),
        UVSwitch227(227),
        UVSwitch228(228),
        UVSwitch229(229),
        UVSwitch230(230),
        UVSwitch231(231),
        UVSwitch232(232),
        UVSwitch233(233),
        UVSwitch234(234),
        UVSwitch235(235),
        UVSwitch236(236),
        UVSwitch237(237),
        UVSwitch238(238),
        UVSwitch239(239),
        UVSwitch240(240);

        private int value;

        private UVSwitchNr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public enum DimChannelNr {
        DimChannel1(1),
        DimChannel2(2),
        DimChannel3(3),
        DimChannel4(4),
        DimChannel5(5),
        DimChannel6(6);

        private int value;

        private DimChannelNr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum RelayChannelNr {
        RelayCh1(1),
        RelayCh2(2),
        RelayCh3(3),
        RelayCh4(4),
        RelayCh5(5),
        RelayCh6(6),
        RelayCh7(7),
        RelayCh8(8),
        RelayCh9(9),
        RelayCh10(10),
        RelayCh11(11),
        RelayCh12(12);

        private int value;

        private RelayChannelNr(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    //
    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_BRIDGE,
            THING_TYPE_ML01, THING_TYPE_MDT0601_233, THING_TYPE_MPL8_48_FH, THING_TYPE_MPT04_48, THING_TYPE_MR1216_233,
            THING_TYPE_MRDA0610_432, THING_TYPE_MW02, THING_TYPE_MS12_2C, THING_TYPE_MS08MN_2C, THING_TYPE_MS24,
            THING_TYPE_MRDA06);

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ML01,
            THING_TYPE_MDT0601_233, THING_TYPE_MPL8_48_FH, THING_TYPE_MPT04_48, THING_TYPE_MR1216_233,
            THING_TYPE_MRDA0610_432, THING_TYPE_MW02, THING_TYPE_MS12_2C, THING_TYPE_MS08MN_2C, THING_TYPE_MS24,
            THING_TYPE_MRDA06);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE);

}
