/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanChannelDescription;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTransformationConfig;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_01;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_02;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_03;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_04;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_05;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_06;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_07;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_08;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_09;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_0A;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_0B;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_10;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_11;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_12;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_13;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_14;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_15;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_16;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_17;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_18;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_19;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_1A;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_1B;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_20;
import org.openhab.binding.enocean.internal.eep.A5_02.A5_02_30;
import org.openhab.binding.enocean.internal.eep.A5_04.A5_04_01;
import org.openhab.binding.enocean.internal.eep.A5_04.A5_04_02;
import org.openhab.binding.enocean.internal.eep.A5_04.A5_04_02_Eltako;
import org.openhab.binding.enocean.internal.eep.A5_04.A5_04_03;
import org.openhab.binding.enocean.internal.eep.A5_06.A5_06_01;
import org.openhab.binding.enocean.internal.eep.A5_06.A5_06_01_ELTAKO;
import org.openhab.binding.enocean.internal.eep.A5_07.A5_07_01;
import org.openhab.binding.enocean.internal.eep.A5_07.A5_07_02;
import org.openhab.binding.enocean.internal.eep.A5_07.A5_07_03;
import org.openhab.binding.enocean.internal.eep.A5_08.A5_08_01;
import org.openhab.binding.enocean.internal.eep.A5_08.A5_08_01_FXBH;
import org.openhab.binding.enocean.internal.eep.A5_08.A5_08_02;
import org.openhab.binding.enocean.internal.eep.A5_08.A5_08_03;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_02;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_04;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_05;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_08;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_09;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_0C;
import org.openhab.binding.enocean.internal.eep.A5_09.A5_09_0D;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_01;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_02;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_03;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_04;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_05;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_06;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_07;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_08;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_09;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_0A;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_0B;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_0C;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_0D;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_10;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_11;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_12;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_13;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_14;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_15;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_16;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_17;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_18;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_19;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1A;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1B;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1C;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1D;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1E;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_1F;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_20;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_21;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_22;
import org.openhab.binding.enocean.internal.eep.A5_10.A5_10_23;
import org.openhab.binding.enocean.internal.eep.A5_11.A5_11_03;
import org.openhab.binding.enocean.internal.eep.A5_11.A5_11_04;
import org.openhab.binding.enocean.internal.eep.A5_12.A5_12_00;
import org.openhab.binding.enocean.internal.eep.A5_12.A5_12_01;
import org.openhab.binding.enocean.internal.eep.A5_12.A5_12_02;
import org.openhab.binding.enocean.internal.eep.A5_12.A5_12_03;
import org.openhab.binding.enocean.internal.eep.A5_13.A5_13_01;
import org.openhab.binding.enocean.internal.eep.A5_14.A5_14_01;
import org.openhab.binding.enocean.internal.eep.A5_14.A5_14_01_ELTAKO;
import org.openhab.binding.enocean.internal.eep.A5_14.A5_14_09;
import org.openhab.binding.enocean.internal.eep.A5_14.A5_14_0A;
import org.openhab.binding.enocean.internal.eep.A5_20.A5_20_04;
import org.openhab.binding.enocean.internal.eep.A5_30.A5_30_03_ELTAKO;
import org.openhab.binding.enocean.internal.eep.A5_38.A5_38_08_Blinds;
import org.openhab.binding.enocean.internal.eep.A5_38.A5_38_08_Dimming;
import org.openhab.binding.enocean.internal.eep.A5_38.A5_38_08_Switching;
import org.openhab.binding.enocean.internal.eep.A5_3F.A5_3F_7F_EltakoFRM;
import org.openhab.binding.enocean.internal.eep.A5_3F.A5_3F_7F_EltakoFSB;
import org.openhab.binding.enocean.internal.eep.Base.PTM200Message;
import org.openhab.binding.enocean.internal.eep.Base.UTEResponse;
import org.openhab.binding.enocean.internal.eep.Base._4BSTeachInVariation3Response;
import org.openhab.binding.enocean.internal.eep.D0.D0_06;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_00;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_01;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_02;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_03;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_04;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_05;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_06;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_07;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_08;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_09;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_09_Permundo;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0A;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0B;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0C;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0D;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0E;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0F;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_0F_NodON;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_11;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_12;
import org.openhab.binding.enocean.internal.eep.D2_01.D2_01_12_NodON;
import org.openhab.binding.enocean.internal.eep.D2_03.D2_03_0A;
import org.openhab.binding.enocean.internal.eep.D2_05.D2_05_00;
import org.openhab.binding.enocean.internal.eep.D2_05.D2_05_00_NodON;
import org.openhab.binding.enocean.internal.eep.D2_06.D2_06_01;
import org.openhab.binding.enocean.internal.eep.D2_06.D2_06_50;
import org.openhab.binding.enocean.internal.eep.D2_14.D2_14_30;
import org.openhab.binding.enocean.internal.eep.D2_50.D2_50;
import org.openhab.binding.enocean.internal.eep.D5_00.D5_00_01;
import org.openhab.binding.enocean.internal.eep.F6_01.F6_01_01;
import org.openhab.binding.enocean.internal.eep.F6_02.F6_02_01;
import org.openhab.binding.enocean.internal.eep.F6_02.F6_02_02;
import org.openhab.binding.enocean.internal.eep.F6_05.F6_05_02;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_00;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_00_EltakoFPE;
import org.openhab.binding.enocean.internal.eep.F6_10.F6_10_01;
import org.openhab.binding.enocean.internal.eep.generic.Generic4BS;
import org.openhab.binding.enocean.internal.eep.generic.GenericRPS;
import org.openhab.binding.enocean.internal.eep.generic.GenericVLD;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public enum EEPType {
    Undef(RORG.Unknown, 0, 0, false, EEP.class, null, 0),

    UTEResponse(RORG.UTE, 0, 0, false, UTEResponse.class, null),
    _4BSTeachInVariation3Response(RORG._4BS, 0, 0, false, _4BSTeachInVariation3Response.class, null),

    GenericRPS(RORG.RPS, 0xFF, 0xFF, false, GenericRPS.class, THING_TYPE_GENERICTHING),
    Generic4BS(RORG._4BS, 0xFF, 0xFF, false, Generic4BS.class, THING_TYPE_GENERICTHING, CHANNEL_VIBRATION),
    GenericVLD(RORG.VLD, 0xFF, 0xFF, false, GenericVLD.class, THING_TYPE_GENERICTHING),

    PTM200(RORG.RPS, 0x00, 0x00, false, PTM200Message.class, null, CHANNEL_GENERAL_SWITCHING, CHANNEL_ROLLERSHUTTER,
            CHANNEL_CONTACT),

    PushButton(RORG.RPS, 0x01, 0x01, false, F6_01_01.class, THING_TYPE_PUSHBUTTON, CHANNEL_PUSHBUTTON),
    PushButtonTriState(RORG.VLD, 0x03, 0x0A, false, D2_03_0A.class, THING_TYPE_PUSHBUTTON, CHANNEL_PUSHBUTTON,
            CHANNEL_DOUBLEPRESS, CHANNEL_LONGPRESS, CHANNEL_BATTERY_LEVEL),

    RockerSwitch2RockerStyle1(RORG.RPS, 0x02, 0x01, false, F6_02_01.class, THING_TYPE_ROCKERSWITCH,
            CHANNEL_ROCKERSWITCH_CHANNELA, CHANNEL_ROCKERSWITCH_CHANNELB, CHANNEL_ROCKERSWITCH_ACTION,
            CHANNEL_VIRTUALSWITCHA, CHANNEL_VIRTUALROLLERSHUTTERA, CHANNEL_VIRTUALROCKERSWITCHB,
            CHANNEL_ROCKERSWITCHLISTENERSWITCH, CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER),

    RockerSwitch2RockerStyle2(RORG.RPS, 0x02, 0x02, false, F6_02_02.class, THING_TYPE_ROCKERSWITCH,
            CHANNEL_ROCKERSWITCH_CHANNELA, CHANNEL_ROCKERSWITCH_CHANNELB, CHANNEL_ROCKERSWITCH_ACTION,
            CHANNEL_VIRTUALSWITCHA, CHANNEL_VIRTUALROLLERSHUTTERA, CHANNEL_VIRTUALROCKERSWITCHB,
            CHANNEL_ROCKERSWITCHLISTENERSWITCH, CHANNEL_ROCKERSWITCHLISTENERROLLERSHUTTER),

    MechanicalHandle00(RORG.RPS, 0x10, 0x00, false, F6_10_00.class, THING_TYPE_MECHANICALHANDLE,
            CHANNEL_WINDOWHANDLESTATE, CHANNEL_CONTACT),
    MechanicalHandle01(RORG.RPS, 0x10, 0x01, false, F6_10_01.class, THING_TYPE_MECHANICALHANDLE,
            CHANNEL_WINDOWHANDLESTATE, CHANNEL_CONTACT),
    MechanicalHandle02(RORG._4BS, 0x14, 0x09, false, A5_14_09.class, THING_TYPE_MECHANICALHANDLE,
            CHANNEL_WINDOWHANDLESTATE, CHANNEL_CONTACT, CHANNEL_BATTERY_VOLTAGE),
    MechanicalHandle03(RORG._4BS, 0x14, 0x0A, false, A5_14_0A.class, THING_TYPE_MECHANICALHANDLE,
            CHANNEL_WINDOWHANDLESTATE, CHANNEL_CONTACT, CHANNEL_VIBRATION, CHANNEL_BATTERY_VOLTAGE),
    MechanicalHandle04(RORG.VLD, 0x06, 0x01, false, "Soda", 0x0043, D2_06_01.class, THING_TYPE_MECHANICALHANDLE,
            CHANNEL_WINDOWHANDLESTATE, CHANNEL_WINDOWSASHSTATE, CHANNEL_MOTIONDETECTION, CHANNEL_INDOORAIRTEMPERATURE,
            CHANNEL_HUMIDITY, CHANNEL_ILLUMINATION, CHANNEL_BATTERY_LEVEL, CHANNEL_WINDOWBREACHEVENT,
            CHANNEL_PROTECTIONPLUSEVENT, CHANNEL_PUSHBUTTON, CHANNEL_PUSHBUTTON2, CHANNEL_VACATIONMODETOGGLEEVENT),

    ContactAndSwitch01(RORG._1BS, 0x00, 0x01, false, D5_00_01.class, THING_TYPE_CONTACT, CHANNEL_CONTACT),
    ContactAndSwitch02(RORG._4BS, 0x14, 0x01, false, A5_14_01.class, THING_TYPE_CONTACT, CHANNEL_BATTERY_VOLTAGE,
            CHANNEL_CONTACT),
    ContactAndSwitch03(RORG.RPS, 0x10, 0x00, false, "EltakoFPE", ELTAKOID, F6_10_00_EltakoFPE.class, THING_TYPE_CONTACT,
            CHANNEL_CONTACT),

    SmokeDetection(RORG.RPS, 0x05, 0x02, false, F6_05_02.class, null, CHANNEL_SMOKEDETECTION, CHANNEL_BATTERYLOW),

    BatteryStatus(RORG._4BS, 0x14, 0x01, false, "ELTAKO", ELTAKOID, A5_14_01_ELTAKO.class, THING_TYPE_CONTACT,
            CHANNEL_BATTERY_VOLTAGE, CHANNEL_ENERGY_STORAGE),
    SigBatteryStatus(RORG.SIG, 0x06, 0x00, false, D0_06.class, null, CHANNEL_BATTERY_LEVEL),

    TemperatureSensor_A5_02_01(RORG._4BS, 0x02, 0x01, false, A5_02_01.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_02(RORG._4BS, 0x02, 0x02, false, A5_02_02.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_03(RORG._4BS, 0x02, 0x03, false, A5_02_03.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_04(RORG._4BS, 0x02, 0x04, false, A5_02_04.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_05(RORG._4BS, 0x02, 0x05, false, A5_02_05.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_06(RORG._4BS, 0x02, 0x06, false, A5_02_06.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_07(RORG._4BS, 0x02, 0x07, false, A5_02_07.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_08(RORG._4BS, 0x02, 0x08, false, A5_02_08.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_09(RORG._4BS, 0x02, 0x09, false, A5_02_09.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_0A(RORG._4BS, 0x02, 0x0A, false, A5_02_0A.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_0B(RORG._4BS, 0x02, 0x0B, false, A5_02_0B.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_10(RORG._4BS, 0x02, 0x10, false, A5_02_10.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_11(RORG._4BS, 0x02, 0x11, false, A5_02_11.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_12(RORG._4BS, 0x02, 0x12, false, A5_02_12.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_13(RORG._4BS, 0x02, 0x13, false, A5_02_13.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_14(RORG._4BS, 0x02, 0x14, false, A5_02_14.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_15(RORG._4BS, 0x02, 0x15, false, A5_02_15.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_16(RORG._4BS, 0x02, 0x16, false, A5_02_16.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_17(RORG._4BS, 0x02, 0x17, false, A5_02_17.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_18(RORG._4BS, 0x02, 0x18, false, A5_02_18.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_19(RORG._4BS, 0x02, 0x19, false, A5_02_19.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_1A(RORG._4BS, 0x02, 0x1A, false, A5_02_1A.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_1B(RORG._4BS, 0x02, 0x1B, false, A5_02_1B.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_20(RORG._4BS, 0x02, 0x20, false, A5_02_20.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),
    TemperatureSensor_A5_02_30(RORG._4BS, 0x02, 0x30, false, A5_02_30.class, THING_TYPE_TEMPERATURESENSOR,
            CHANNEL_TEMPERATURE),

    TemperatureHumiditySensor_A5_04_01(RORG._4BS, 0x04, 0x01, false, A5_04_01.class,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY),
    TemperatureHumiditySensor_A5_04_02(RORG._4BS, 0x04, 0x02, false, A5_04_02.class,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY),
    TemperatureHumiditySensor_A5_04_02_Eltako(RORG._4BS, 0x04, 0x02, false, "ELTAKO", ELTAKOID, A5_04_02_Eltako.class,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY, CHANNEL_BATTERY_VOLTAGE),
    TemperatureHumiditySensor_A5_04_03(RORG._4BS, 0x04, 0x03, false, A5_04_03.class,
            THING_TYPE_TEMPERATUREHUMIDITYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY),

    OCCUPANCYSENSOR_A5_07_01(RORG._4BS, 0x07, 0x01, false, A5_07_01.class, THING_TYPE_OCCUPANCYSENSOR,
            CHANNEL_MOTIONDETECTION, CHANNEL_BATTERY_VOLTAGE),
    OCCUPANCYSENSOR_A5_07_02(RORG._4BS, 0x07, 0x02, false, A5_07_02.class, THING_TYPE_OCCUPANCYSENSOR,
            CHANNEL_MOTIONDETECTION, CHANNEL_BATTERY_VOLTAGE),
    OCCUPANCYSENSOR_A5_07_03(RORG._4BS, 0x07, 0x03, false, A5_07_03.class, THING_TYPE_OCCUPANCYSENSOR,
            CHANNEL_ILLUMINATION, CHANNEL_MOTIONDETECTION, CHANNEL_BATTERY_VOLTAGE),

    LightTemperatureOccupancySensor_A5_08_01(RORG._4BS, 0x08, 0x01, false, A5_08_01.class,
            THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_MOTIONDETECTION,
            CHANNEL_ILLUMINATION, CHANNEL_OCCUPANCY),
    LightTemperatureOccupancySensor_A5_08_02(RORG._4BS, 0x08, 0x02, false, A5_08_02.class,
            THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_MOTIONDETECTION,
            CHANNEL_ILLUMINATION, CHANNEL_OCCUPANCY),
    LightTemperatureOccupancySensor_A5_08_03(RORG._4BS, 0x08, 0x03, false, A5_08_03.class,
            THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, CHANNEL_TEMPERATURE, CHANNEL_MOTIONDETECTION,
            CHANNEL_ILLUMINATION, CHANNEL_OCCUPANCY),
    FXBH_A5_08_01(RORG._4BS, 0x08, 0x01, false, "FXBH", ELTAKOID, A5_08_01_FXBH.class,
            THING_TYPE_LIGHTTEMPERATUREOCCUPANCYSENSOR, CHANNEL_MOTIONDETECTION, CHANNEL_ILLUMINATION),

    LightSensor01(RORG._4BS, 0x06, 0x01, false, A5_06_01.class, THING_TYPE_LIGHTSENSOR, CHANNEL_ILLUMINATION),
    LightSensor02(RORG._4BS, 0x06, 0x01, false, "ELTAKO", ELTAKOID, A5_06_01_ELTAKO.class, THING_TYPE_LIGHTSENSOR,
            CHANNEL_ILLUMINATION),

    GasSensor_A5_09_02(RORG._4BS, 0x09, 0x02, false, A5_09_02.class, THING_TYPE_GASSENSOR, CHANNEL_CO,
            CHANNEL_TEMPERATURE, CHANNEL_BATTERY_VOLTAGE),
    GasSensor_A5_09_04(RORG._4BS, 0x09, 0x04, false, A5_09_04.class, THING_TYPE_GASSENSOR, CHANNEL_CO2,
            CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY),
    GasSensor_A5_09_05(RORG._4BS, 0x09, 0x05, false, A5_09_05.class, THING_TYPE_GASSENSOR, CHANNEL_VOC, CHANNEL_VOC_ID),
    GasSensor_A5_09_08(RORG._4BS, 0x09, 0x08, false, A5_09_08.class, THING_TYPE_GASSENSOR, CHANNEL_CO2),
    GasSensor_A5_09_09(RORG._4BS, 0x09, 0x09, false, A5_09_09.class, THING_TYPE_GASSENSOR, CHANNEL_CO2),
    GasSensor_A5_09_0C(RORG._4BS, 0x09, 0x0C, false, A5_09_0C.class, THING_TYPE_GASSENSOR, CHANNEL_VOC, CHANNEL_VOC_ID),
    GasSensor_A5_09_0D(RORG._4BS, 0x09, 0x0D, false, A5_09_0D.class, THING_TYPE_GASSENSOR, CHANNEL_HUMIDITY,
            CHANNEL_TVOC, CHANNEL_TEMPERATURE),

    RoomPanel_A5_10_01(RORG._4BS, 0x10, 0x01, false, A5_10_01.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_FANSPEEDSTAGE, CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_02(RORG._4BS, 0x10, 0x02, false, A5_10_02.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_FANSPEEDSTAGE),
    RoomPanel_A5_10_03(RORG._4BS, 0x10, 0x03, false, A5_10_03.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_04(RORG._4BS, 0x10, 0x04, false, A5_10_04.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_FANSPEEDSTAGE),
    RoomPanel_A5_10_05(RORG._4BS, 0x10, 0x05, false, A5_10_05.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_06(RORG._4BS, 0x10, 0x06, false, A5_10_06.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_07(RORG._4BS, 0x10, 0x07, false, A5_10_07.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_FANSPEEDSTAGE),
    RoomPanel_A5_10_08(RORG._4BS, 0x10, 0x08, false, A5_10_08.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_FANSPEEDSTAGE, CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_09(RORG._4BS, 0x10, 0x09, false, A5_10_09.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_FANSPEEDSTAGE),
    RoomPanel_A5_10_0A(RORG._4BS, 0x10, 0x0A, false, A5_10_0A.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_0B(RORG._4BS, 0x10, 0x0B, false, A5_10_0B.class, THING_TYPE_ROOMOPERATINGPANEL,
            CHANNEL_TEMPERATURE),
    RoomPanel_A5_10_0C(RORG._4BS, 0x10, 0x0C, false, A5_10_0C.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_0D(RORG._4BS, 0x10, 0x0D, false, A5_10_0D.class, THING_TYPE_ROOMOPERATINGPANEL,
            CHANNEL_TEMPERATURE),
    RoomPanel_A5_10_10(RORG._4BS, 0x10, 0x10, false, A5_10_10.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_11(RORG._4BS, 0x10, 0x11, false, A5_10_11.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_12(RORG._4BS, 0x10, 0x12, false, A5_10_12.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_13(RORG._4BS, 0x10, 0x13, false, A5_10_13.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_14(RORG._4BS, 0x10, 0x14, false, A5_10_14.class, THING_TYPE_ROOMOPERATINGPANEL,
            CHANNEL_TEMPERATURE),
    RoomPanel_A5_10_15(RORG._4BS, 0x10, 0x15, false, A5_10_15.class, THING_TYPE_ROOMOPERATINGPANEL,
            CHANNEL_TEMPERATURE),
    RoomPanel_A5_10_16(RORG._4BS, 0x10, 0x16, false, A5_10_16.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_17(RORG._4BS, 0x10, 0x17, false, A5_10_17.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_18(RORG._4BS, 0x10, 0x18, false, A5_10_18.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_19(RORG._4BS, 0x10, 0x19, false, A5_10_19.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1A(RORG._4BS, 0x10, 0x1A, false, A5_10_1A.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1B(RORG._4BS, 0x10, 0x1B, false, A5_10_1B.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1C(RORG._4BS, 0x10, 0x1C, false, A5_10_1C.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1D(RORG._4BS, 0x10, 0x1D, false, A5_10_1D.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1E(RORG._4BS, 0x10, 0x1E, false, A5_10_1E.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY),
    RoomPanel_A5_10_1F(RORG._4BS, 0x10, 0x1F, false, A5_10_1F.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_OCCUPANCY, CHANNEL_SETPOINT, CHANNEL_FANSPEEDSTAGE),
    RoomPanel_A5_10_20(RORG._4BS, 0x10, 0x20, false, A5_10_20.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_21(RORG._4BS, 0x10, 0x21, false, A5_10_21.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_22(RORG._4BS, 0x10, 0x22, false, A5_10_22.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT),
    RoomPanel_A5_10_23(RORG._4BS, 0x10, 0x23, false, A5_10_23.class, THING_TYPE_ROOMOPERATINGPANEL, CHANNEL_TEMPERATURE,
            CHANNEL_SETPOINT, CHANNEL_OCCUPANCY),

    AutomatedMeterReading_00(RORG._4BS, 0x12, 0x00, false, A5_12_00.class, THING_TYPE_AUTOMATEDMETERSENSOR,
            CHANNEL_COUNTER, CHANNEL_CURRENTNUMBER),
    AutomatedMeterReading_01(RORG._4BS, 0x12, 0x01, false, A5_12_01.class, THING_TYPE_AUTOMATEDMETERSENSOR,
            CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    AutomatedMeterReading_02(RORG._4BS, 0x12, 0x02, false, A5_12_02.class, THING_TYPE_AUTOMATEDMETERSENSOR,
            CHANNEL_CUMULATIVEVALUE, CHANNEL_CURRENTFLOW),
    AutomatedMeterReading_03(RORG._4BS, 0x12, 0x03, false, A5_12_03.class, THING_TYPE_AUTOMATEDMETERSENSOR,
            CHANNEL_CUMULATIVEVALUE, CHANNEL_CURRENTFLOW),

    EnvironmentalSensor_01(RORG._4BS, 0x13, 0x01, false, A5_13_01.class, THING_TYPE_ENVIRONMENTALSENSOR,
            CHANNEL_ILLUMINATION, CHANNEL_TEMPERATURE, CHANNEL_WINDSPEED, CHANNEL_RAINSTATUS, CHANNEL_ILLUMINATIONWEST,
            CHANNEL_ILLUMINATIONSOUTHNORTH, CHANNEL_ILLUMINATIONEAST),

    Rollershutter_A5(RORG._4BS, 0x11, 0x03, false, A5_11_03.class, THING_TYPE_ROLLERSHUTTER, CHANNEL_ROLLERSHUTTER,
            CHANNEL_ANGLE),
    ExtendedLight_A5(RORG._4BS, 0x11, 0x04, false, A5_11_04.class, THING_TYPE_CENTRALCOMMAND, CHANNEL_GENERAL_SWITCHING,
            CHANNEL_DIMMER, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER, CHANNEL_COUNTER),

    SmokeDetection4BS(RORG._4BS, 0x30, 0x03, false, "ELTAKO", ELTAKOID, A5_30_03_ELTAKO.class,
            THING_TYPE_MULTFUNCTIONSMOKEDETECTOR, CHANNEL_TEMPERATURE, CHANNEL_SMOKEDETECTION),

    CentralCommandSwitching(RORG._4BS, 0x38, 0x08, false, A5_38_08_Switching.class, THING_TYPE_CENTRALCOMMAND, 0x01,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TEACHINCMD),
    CentralCommandDimming(RORG._4BS, 0x38, 0x08, false, A5_38_08_Dimming.class, THING_TYPE_CENTRALCOMMAND, 0x02,
            CHANNEL_DIMMER, CHANNEL_TEACHINCMD),
    CentralCommandBlinds(RORG._4BS, 0x38, 0x08, false, A5_38_08_Blinds.class, THING_TYPE_ROLLERSHUTTER, 0x07,
            CHANNEL_ROLLERSHUTTER, CHANNEL_ANGLE, CHANNEL_TEACHINCMD),

    // UniversalCommand(RORG._4BS, 0x3f, 0x7f, false, A5_3F_7F_Universal.class, THING_TYPE_UNIVERSALACTUATOR,
    // CHANNEL_GENERIC_ROLLERSHUTTER, CHANNEL_GENERIC_LIGHT_SWITCHING, CHANNEL_GENERIC_DIMMER, CHANNEL_TEACHINCMD),
    EltakoFSB(RORG._4BS, 0x3f, 0x7f, false, false, "EltakoFSB", 0, A5_3F_7F_EltakoFSB.class, THING_TYPE_ROLLERSHUTTER,
            0, new Hashtable<String, Configuration>() {
                private static final long serialVersionUID = 1L;
                {
                    put(CHANNEL_ROLLERSHUTTER, new Configuration());
                    put(CHANNEL_TEACHINCMD, new Configuration() {
                        {
                            put(PARAMETER_CHANNEL_TEACHINMSG, "fff80d80");
                        }
                    });
                }
            }),

    EltakoFRM(RORG._4BS, 0x3f, 0x7f, false, false, "EltakoFRM", 0, A5_3F_7F_EltakoFRM.class, THING_TYPE_ROLLERSHUTTER,
            0, new Hashtable<String, Configuration>() {
                private static final long serialVersionUID = 1L;
                {
                    put(CHANNEL_ROLLERSHUTTER, new Configuration());
                    put(CHANNEL_TEACHINCMD, new Configuration() {
                        {
                            put(PARAMETER_CHANNEL_TEACHINMSG, "fff80d80");
                        }
                    });
                }
            }),

    Thermostat(RORG._4BS, 0x20, 0x04, false, true, A5_20_04.class, THING_TYPE_THERMOSTAT, CHANNEL_VALVE_POSITION,
            CHANNEL_BUTTON_LOCK, CHANNEL_DISPLAY_ORIENTATION, CHANNEL_TEMPERATURE_SETPOINT, CHANNEL_TEMPERATURE,
            CHANNEL_FEED_TEMPERATURE, CHANNEL_MEASUREMENT_CONTROL, CHANNEL_FAILURE_CODE, CHANNEL_WAKEUPCYCLE,
            CHANNEL_SERVICECOMMAND),

    SwitchWithEnergyMeasurment_00(RORG.VLD, 0x01, 0x00, true, D2_01_00.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE),
    SwitchWithEnergyMeasurment_01(RORG.VLD, 0x01, 0x01, true, D2_01_01.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING),
    SwitchWithEnergyMeasurment_02(RORG.VLD, 0x01, 0x02, true, D2_01_02.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_DIMMER, CHANNEL_TOTALUSAGE),
    SwitchWithEnergyMeasurment_03(RORG.VLD, 0x01, 0x03, true, D2_01_03.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_DIMMER),
    SwitchWithEnergyMeasurment_04(RORG.VLD, 0x01, 0x04, true, D2_01_04.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_DIMMER, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_05(RORG.VLD, 0x01, 0x05, true, D2_01_05.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_DIMMER, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_06(RORG.VLD, 0x01, 0x06, true, D2_01_06.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE),
    SwitchWithEnergyMeasurment_07(RORG.VLD, 0x01, 0x07, true, D2_01_07.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING),
    SwitchWithEnergyMeasurment_08(RORG.VLD, 0x01, 0x08, true, D2_01_08.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_DIMMER, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_09(RORG.VLD, 0x01, 0x09, true, D2_01_09.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_09_PERMUNDO(RORG.VLD, 0x01, 0x09, true, "PERMUNDO", PERMUNDOID, D2_01_09_Permundo.class,
            THING_TYPE_MEASUREMENTSWITCH, CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER,
            CHANNEL_ECOMODE, CHANNEL_REPEATERMODE),
    SwitchWithEnergyMeasurment_0A(RORG.VLD, 0x01, 0x0A, true, D2_01_0A.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING),
    SwitchWithEnergyMeasurment_0B(RORG.VLD, 0x01, 0x0B, true, D2_01_0B.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_0C(RORG.VLD, 0x01, 0x0C, true, D2_01_0C.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_0D(RORG.VLD, 0x01, 0x0D, true, D2_01_0D.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING),
    SwitchWithEnergyMeasurment_0E(RORG.VLD, 0x01, 0x0E, true, D2_01_0E.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING, CHANNEL_TOTALUSAGE, CHANNEL_INSTANTPOWER),
    SwitchWithEnergyMeasurment_0F_NODON(RORG.VLD, 0x01, 0x0F, true, "NODON", NODONID, D2_01_0F_NodON.class,
            THING_TYPE_MEASUREMENTSWITCH, CHANNEL_GENERAL_SWITCHING, CHANNEL_REPEATERMODE),
    SwitchWithEnergyMeasurment_0F(RORG.VLD, 0x01, 0x0F, true, D2_01_0F.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHING),
    SwitchWithEnergyMeasurment_11(RORG.VLD, 0x01, 0x11, true, D2_01_11.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHINGA, CHANNEL_GENERAL_SWITCHINGB),
    SwitchWithEnergyMeasurment_12_NODON(RORG.VLD, 0x01, 0x12, true, "NODON", NODONID, D2_01_12_NodON.class,
            THING_TYPE_MEASUREMENTSWITCH, CHANNEL_GENERAL_SWITCHINGA, CHANNEL_GENERAL_SWITCHINGB, CHANNEL_REPEATERMODE),
    SwitchWithEnergyMeasurment_12(RORG.VLD, 0x01, 0x12, true, D2_01_12.class, THING_TYPE_MEASUREMENTSWITCH,
            CHANNEL_GENERAL_SWITCHINGA, CHANNEL_GENERAL_SWITCHINGB),

    Rollershutter_D2_NODON(RORG.VLD, 0x05, 0x00, true, "NODON", NODONID, D2_05_00_NodON.class, THING_TYPE_ROLLERSHUTTER,
            CHANNEL_ROLLERSHUTTER, CHANNEL_REPEATERMODE),
    Rollershutter_D2(RORG.VLD, 0x05, 0x00, true, D2_05_00.class, THING_TYPE_ROLLERSHUTTER, CHANNEL_ROLLERSHUTTER),

    WindowSashHandleSensor_50(RORG.VLD, 0x06, 0x50, false, "Siegenia", 0x005D, D2_06_50.class,
            THING_TYPE_WINDOWSASHHANDLESENSOR, CHANNEL_WINDOWHANDLESTATE, CHANNEL_WINDOWSASHSTATE,
            CHANNEL_BATTERY_LEVEL, CHANNEL_BATTERYLOW, CHANNEL_WINDOWBREACHEVENT, CHANNEL_WINDOWCALIBRATIONSTATE,
            CHANNEL_WINDOWCALIBRATIONSTEP),

    MultiFunctionSensor_30(RORG.VLD, 0x14, 0x30, false, D2_14_30.class, THING_TYPE_MULTFUNCTIONSMOKEDETECTOR,
            CHANNEL_SMOKEDETECTION, CHANNEL_SENSORFAULT, CHANNEL_TIMESINCELASTMAINTENANCE, CHANNEL_BATTERY_LEVEL,
            CHANNEL_REMAININGPLT, CHANNEL_TEMPERATURE, CHANNEL_HUMIDITY, CHANNEL_HYGROCOMFORTINDEX,
            CHANNEL_INDOORAIRANALYSIS),

    HeatRecoveryVentilation_00(RORG.VLD, 0x50, 0x00, false, D2_50.class, THING_TYPE_HEATRECOVERYVENTILATION,
            CHANNEL_VENTILATIONOPERATIONMODE, CHANNEL_SUPPLYAIRFLAPSTATUS, CHANNEL_WEEKLYTIMERPROGRAMSTATUS,
            CHANNEL_EXHAUSTAIRFLAPSTATUS, CHANNEL_DEFROSTMODE, CHANNEL_COOLINGPROTECTIONMODE,
            CHANNEL_OUTDOORAIRHEATERSTATUS, CHANNEL_SUPPLYAIRHEATERSTATUS, CHANNEL_DRAINHEATERSTATUS,
            CHANNEL_TIMEROPERATIONMODE, CHANNEL_MAINTENANCESTATUS, CHANNEL_ROOMTEMPERATURECONTROLSTATUS,
            CHANNEL_AIRQUALITYVALUE1, CHANNEL_AIRQUALITYVALUE2, CHANNEL_OUTDOORAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRTEMPERATURE, CHANNEL_INDOORAIRTEMPERATURE, CHANNEL_EXHAUSTAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRFANAIRFLOWRATE, CHANNEL_EXHAUSTAIRFANAIRFLOWRATE, CHANNEL_SUPPLYFANSPEED,
            CHANNEL_EXHAUSTFANSPEED),
    HeatRecoveryVentilation_01(RORG.VLD, 0x50, 0x01, false, D2_50.class, THING_TYPE_HEATRECOVERYVENTILATION,
            CHANNEL_VENTILATIONOPERATIONMODE, CHANNEL_SUPPLYAIRFLAPSTATUS, CHANNEL_WEEKLYTIMERPROGRAMSTATUS,
            CHANNEL_EXHAUSTAIRFLAPSTATUS, CHANNEL_DEFROSTMODE, CHANNEL_COOLINGPROTECTIONMODE,
            CHANNEL_OUTDOORAIRHEATERSTATUS, CHANNEL_SUPPLYAIRHEATERSTATUS, CHANNEL_DRAINHEATERSTATUS,
            CHANNEL_TIMEROPERATIONMODE, CHANNEL_MAINTENANCESTATUS, CHANNEL_ROOMTEMPERATURECONTROLSTATUS,
            CHANNEL_AIRQUALITYVALUE1, CHANNEL_AIRQUALITYVALUE2, CHANNEL_OUTDOORAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRTEMPERATURE, CHANNEL_INDOORAIRTEMPERATURE, CHANNEL_EXHAUSTAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRFANAIRFLOWRATE, CHANNEL_EXHAUSTAIRFANAIRFLOWRATE, CHANNEL_SUPPLYFANSPEED,
            CHANNEL_EXHAUSTFANSPEED),
    HeatRecoveryVentilation_10(RORG.VLD, 0x50, 0x10, false, D2_50.class, THING_TYPE_HEATRECOVERYVENTILATION,
            CHANNEL_VENTILATIONOPERATIONMODE, CHANNEL_DEFROSTMODE, CHANNEL_COOLINGPROTECTIONMODE,
            CHANNEL_OUTDOORAIRHEATERSTATUS, CHANNEL_SUPPLYAIRHEATERSTATUS, CHANNEL_DRAINHEATERSTATUS,
            CHANNEL_TIMEROPERATIONMODE, CHANNEL_MAINTENANCESTATUS, CHANNEL_ROOMTEMPERATURECONTROLSTATUS,
            CHANNEL_AIRQUALITYVALUE1, CHANNEL_AIRQUALITYVALUE2, CHANNEL_OUTDOORAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRTEMPERATURE, CHANNEL_INDOORAIRTEMPERATURE, CHANNEL_EXHAUSTAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRFANAIRFLOWRATE, CHANNEL_EXHAUSTAIRFANAIRFLOWRATE, CHANNEL_SUPPLYFANSPEED,
            CHANNEL_EXHAUSTFANSPEED, CHANNEL_WEEKLYTIMERPROGRAMSTATUS),
    HeatRecoveryVentilation_11(RORG.VLD, 0x50, 0x11, false, D2_50.class, THING_TYPE_HEATRECOVERYVENTILATION,
            CHANNEL_VENTILATIONOPERATIONMODE, CHANNEL_FIREPLACESAFETYMODE, CHANNEL_HEATEXCHANGERBYPASSSTATUS,
            CHANNEL_DEFROSTMODE, CHANNEL_COOLINGPROTECTIONMODE, CHANNEL_OUTDOORAIRHEATERSTATUS,
            CHANNEL_WEEKLYTIMERPROGRAMSTATUS, CHANNEL_SUPPLYAIRHEATERSTATUS, CHANNEL_DRAINHEATERSTATUS,
            CHANNEL_TIMEROPERATIONMODE, CHANNEL_MAINTENANCESTATUS, CHANNEL_ROOMTEMPERATURECONTROLSTATUS,
            CHANNEL_AIRQUALITYVALUE1, CHANNEL_AIRQUALITYVALUE2, CHANNEL_OUTDOORAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRTEMPERATURE, CHANNEL_INDOORAIRTEMPERATURE, CHANNEL_EXHAUSTAIRTEMPERATURE,
            CHANNEL_SUPPLYAIRFANAIRFLOWRATE, CHANNEL_EXHAUSTAIRFANAIRFLOWRATE, CHANNEL_SUPPLYFANSPEED,
            CHANNEL_EXHAUSTFANSPEED);

    private RORG rorg;
    private int func;
    private int type;
    private int command;
    private Class<? extends EEP> eepClass;

    private String manufactorSuffix;
    private int manufactorId;

    private @Nullable ThingTypeUID thingTypeUID;

    private Hashtable<String, Configuration> channelIdsWithConfig = new Hashtable<>();
    private Hashtable<String, EnOceanChannelDescription> supportedChannels = new Hashtable<>();

    private boolean supportsRefresh;

    private boolean requestsResponse;

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, Class<? extends EEP> eepClass,
            @Nullable ThingTypeUID thingTypeUID, String... channelIds) {
        this(rorg, func, type, supportsRefresh, eepClass, thingTypeUID, -1, channelIds);
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, boolean requestsResponse,
            Class<? extends EEP> eepClass, ThingTypeUID thingTypeUID, String... channelIds) {
        this(rorg, func, type, supportsRefresh, requestsResponse, eepClass, thingTypeUID, -1, channelIds);
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, String manufactorSuffix, int manufId,
            Class<? extends EEP> eepClass, ThingTypeUID thingTypeUID, String... channelIds) {
        this(rorg, func, type, supportsRefresh, false, manufactorSuffix, manufId, eepClass, thingTypeUID, 0,
                channelIds);
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, Class<? extends EEP> eepClass,
            @Nullable ThingTypeUID thingTypeUID, int command, String... channelIds) {
        this(rorg, func, type, supportsRefresh, false, "", 0, eepClass, thingTypeUID, command, channelIds);
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, boolean requestsResponse,
            Class<? extends EEP> eepClass, ThingTypeUID thingTypeUID, int command, String... channelIds) {
        this(rorg, func, type, supportsRefresh, requestsResponse, "", 0, eepClass, thingTypeUID, command, channelIds);
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, boolean requestsResponse, String manufactorSuffix,
            int manufId, Class<? extends EEP> eepClass, @Nullable ThingTypeUID thingTypeUID, int command,
            String... channelIds) {
        this.rorg = rorg;
        this.func = func;
        this.type = type;
        this.eepClass = eepClass;
        this.thingTypeUID = thingTypeUID;
        this.command = command;
        this.manufactorSuffix = manufactorSuffix;
        this.manufactorId = manufId;
        this.supportsRefresh = supportsRefresh;
        this.requestsResponse = requestsResponse;

        for (String id : channelIds) {
            if (id != null) {
                this.channelIdsWithConfig.put(id, new Configuration());
                EnOceanChannelDescription description = CHANNELID2CHANNELDESCRIPTION.get(id);
                if (description != null) {
                    this.supportedChannels.put(id, description);
                }
            }
        }

        addDefaultChannels();
    }

    EEPType(RORG rorg, int func, int type, boolean supportsRefresh, boolean requestsResponse, String manufactorSuffix,
            int manufId, Class<? extends EEP> eepClass, @Nullable ThingTypeUID thingTypeUID, int command,
            Hashtable<String, Configuration> channelConfigs) {
        this.rorg = rorg;
        this.func = func;
        this.type = type;
        this.eepClass = eepClass;
        this.thingTypeUID = thingTypeUID;
        this.command = command;
        this.channelIdsWithConfig = channelConfigs;
        this.manufactorSuffix = manufactorSuffix;
        this.manufactorId = manufId;
        this.supportsRefresh = supportsRefresh;
        this.requestsResponse = requestsResponse;

        for (String id : channelConfigs.keySet()) {
            this.supportedChannels = addChannelDescription(supportedChannels, id, CHANNELID2CHANNELDESCRIPTION.get(id));
        }

        addDefaultChannels();
    }

    private void addDefaultChannels() {
        if (THING_TYPE_GENERICTHING.equals(this.thingTypeUID)) {
            this.channelIdsWithConfig.put(CHANNEL_GENERIC_SWITCH, new EnOceanChannelTransformationConfig());

            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_SWITCH,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_SWITCH));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_ROLLERSHUTTER, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_ROLLERSHUTTER,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_ROLLERSHUTTER));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_DIMMER, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_DIMMER,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_DIMMER));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_NUMBER, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_NUMBER,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_NUMBER));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_STRING, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_STRING,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_STRING));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_COLOR, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_COLOR,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_COLOR));

            this.channelIdsWithConfig.put(CHANNEL_GENERIC_TEACHINCMD, new EnOceanChannelTransformationConfig());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_GENERIC_TEACHINCMD,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_GENERIC_TEACHINCMD));
        }

        this.channelIdsWithConfig.put(CHANNEL_RSSI, new Configuration());
        this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_RSSI,
                CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_RSSI));

        this.channelIdsWithConfig.put(CHANNEL_REPEATCOUNT, new Configuration());
        this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_REPEATCOUNT,
                CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_REPEATCOUNT));

        this.channelIdsWithConfig.put(CHANNEL_LASTRECEIVED, new Configuration());
        this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_LASTRECEIVED,
                CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_LASTRECEIVED));

        if (requestsResponse) {
            this.channelIdsWithConfig.put(CHANNEL_STATUS_REQUEST_EVENT, new Configuration());
            this.supportedChannels = addChannelDescription(this.supportedChannels, CHANNEL_STATUS_REQUEST_EVENT,
                    CHANNELID2CHANNELDESCRIPTION.get(CHANNEL_STATUS_REQUEST_EVENT));
        }
    }

    private static Hashtable<String, EnOceanChannelDescription> addChannelDescription(
            Hashtable<String, EnOceanChannelDescription> channels, @Nullable String id,
            @Nullable EnOceanChannelDescription channelDescription) {
        if (id != null && channelDescription != null) {
            channels.put(id, channelDescription);
        }
        return channels;
    }

    public Class<? extends EEP> getEEPClass() {
        return eepClass;
    }

    public RORG getRORG() {
        return rorg;
    }

    public int getFunc() {
        return func;
    }

    public int getType() {
        return type;
    }

    public boolean getSupportsRefresh() {
        return supportsRefresh;
    }

    public boolean getRequstesResponse() {
        return requestsResponse;
    }

    public Map<String, EnOceanChannelDescription> getSupportedChannels() {
        return Collections.unmodifiableMap(supportedChannels);
    }

    public boolean isChannelSupported(Channel channel) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String id = channelTypeUID == null ? "" : channelTypeUID.getId();

        return isChannelSupported(channel.getUID().getId(), id);
    }

    public boolean isChannelSupported(String channelId, String channelTypeId) {
        return supportedChannels.containsKey(channelId) || VIRTUALCHANNEL_SEND_COMMAND.equals(channelId)
                || supportedChannels.values().stream().anyMatch(c -> c.channelTypeUID.getId().equals(channelTypeId));
    }

    public @Nullable ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public String getId() {
        if (command == -1) {
            return String.format("%02X_%02X_%02X", rorg.getValue(), func, type);
        } else if (command == 0) {
            return String.format("%02X_%02X_%02X_%s", rorg.getValue(), func, type, manufactorSuffix);
        } else {
            return String.format("%02X_%02X_%02X_%02X", rorg.getValue(), func, type, command);
        }
    }

    public Configuration getChannelConfig(String channelId) {
        Configuration c = null;
        if (!channelIdsWithConfig.isEmpty()) {
            c = channelIdsWithConfig.get(channelId);
            if (c != null) {
                return c;
            }
        }
        return new Configuration();
    }

    public static EEPType getType(String receivingEEPId) {
        for (EEPType eep : values()) {
            if (eep.getId().equals(receivingEEPId)) {
                return eep;
            }
        }

        throw new IllegalArgumentException(String.format("EEP with id %s could not be found", receivingEEPId));
    }

    public static EEPType getType(Class<? extends EEP> eepClass) {
        for (EEPType eep : values()) {
            if (eep.eepClass.equals(eepClass)) {
                return eep;
            }
        }

        throw new IllegalArgumentException(String.format("EEP with class %s could not be found", eepClass.getName()));
    }

    public static @Nullable EEPType getType(RORG rorg, int func, int type, int manufId) {
        EEPType fallback = null;

        for (EEPType eep : values()) {
            if (eep.rorg == rorg && eep.func == func && eep.type == type && eep.manufactorId == manufId) {
                return eep;
            } else if (fallback == null && eep.rorg == rorg && eep.func == func && eep.type == type) {
                fallback = eep;
            }
        }

        return fallback;
    }
}
