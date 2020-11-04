/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Mi IO Devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum MiIoDevices {
    AIRCONDITION_A1("aux.aircondition.v1", "AUX Air Conditioner", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_I1("idelan.aircondition.v1", "Idelan Air Conditioner", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M1("midea.aircondition.v1", "Midea Air Conditioner v2", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M2("midea.aircondition.v2", "Midea Air Conditioner v2", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_MXA1("midea.aircondition.xa1", "Midea Air Conditioner xa1", THING_TYPE_UNSUPPORTED),
    AIRMONITOR1("zhimi.airmonitor.v1", "Mi Air Monitor v1", THING_TYPE_BASIC),
    AIRMONITOR_B1("cgllc.airmonitor.b1", "Mi Air Quality Monitor 2gen", THING_TYPE_BASIC),
    AIRMONITOR_S1("cgllc.airmonitor.s1", "Mi Air Quality Monitor S1", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_V1("zhimi.humidifier.v1", "Mi Air Humidifier", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_CA1("zhimi.humidifier.ca1", "Mi Air Humidifier", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_CB1("zhimi.humidifier.cb1", "Mi Air Humidifier 2", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_MJJSQ("deerma.humidifier.mjjsq", "Mija Smart humidifier", THING_TYPE_BASIC),
    AIR_PURIFIER1("zhimi.airpurifier.v1", "Mi Air Purifier v1", THING_TYPE_BASIC),
    AIR_PURIFIER2("zhimi.airpurifier.v2", "Mi Air Purifier v2", THING_TYPE_BASIC),
    AIR_PURIFIER3("zhimi.airpurifier.v3", "Mi Air Purifier v3", THING_TYPE_BASIC),
    AIR_PURIFIER5("zhimi.airpurifier.v5", "Mi Air Purifier v5", THING_TYPE_BASIC),
    AIR_PURIFIER6("zhimi.airpurifier.v6", "Mi Air Purifier Pro v6", THING_TYPE_BASIC),
    AIR_PURIFIER7("zhimi.airpurifier.v7", "Mi Air Purifier Pro v7", THING_TYPE_BASIC),
    AIR_PURIFIERM("zhimi.airpurifier.m1", "Mi Air Purifier 2 (mini)", THING_TYPE_BASIC),
    AIR_PURIFIERM2("zhimi.airpurifier.m2", "Mi Air Purifier (mini)", THING_TYPE_BASIC),
    AIR_PURIFIERMA1("zhimi.airpurifier.ma1", "Mi Air Purifier MS1", THING_TYPE_BASIC),
    AIR_PURIFIERMA2("zhimi.airpurifier.ma2", "Mi Air Purifier MS2", THING_TYPE_BASIC),
    AIR_PURIFIERMA4("zhimi.airpurifier.ma4", "Mi Air Purifier 3", THING_TYPE_BASIC),
    AIR_PURIFIERMMB3("zhimi.airpurifier.mb3", "Mi Air Purifier 3", THING_TYPE_BASIC),
    AIR_PURIFIERSA1("zhimi.airpurifier.sa1", "Mi Air Purifier Super", THING_TYPE_BASIC),
    AIR_PURIFIERSA2("zhimi.airpurifier.sa2", "Mi Air Purifier Super 2", THING_TYPE_BASIC),
    AIRFRESH_T2017("dmaker.airfresh.t2017", "Mi Fresh Air Ventilator", THING_TYPE_BASIC),
    AIRFRESH_A1("dmaker.airfresh.a1", "Mi Fresh Air Ventilator A1", THING_TYPE_BASIC),
    ALARM_CLOCK_MYK01("zimi.clock.myk01", "Xiao AI Smart Alarm Clock", THING_TYPE_UNSUPPORTED),
    BATHHEATER_V2("yeelight.bhf_light.v2", "Yeelight Smart Bath Heater", THING_TYPE_UNSUPPORTED),
    CUCOPLUG_CP1("cuco.plug.cp1", "Gosund Plug", THING_TYPE_BASIC),
    DEHUMIDIFIER_FW1("nwt.derh.wdh318efw1", "XIAOMI MIJIA WIDETECH WDH318EFW1 Dehumidifier", THING_TYPE_UNSUPPORTED),
    ZHIMI_AIRPURIFIER_MB1("zhimi.airpurifier.mb1", "Mi Air Purifier mb1", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_MC1("zhimi.airpurifier.mc1", "Mi Air Purifier 2S", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_MC2("zhimi.airpurifier.mc2", "Mi Air Purifier 2S", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_VIRTUAL("zhimi.airpurifier.virtual", "Mi Air Purifier virtual", THING_TYPE_UNSUPPORTED),
    ZHIMI_AIRPURIFIER_VTL_M1("zhimi.airpurifier.vtl_m1", "Mi Air Purifier vtl m1", THING_TYPE_UNSUPPORTED),
    CHUANGMI_IR2("chuangmi.ir.v2", "Mi Remote v2", THING_TYPE_UNSUPPORTED),
    CHUANGMI_V2("chuangmi.remote.v2", "Xiaomi IR Remote", THING_TYPE_UNSUPPORTED),
    COOKER1("chunmi.cooker.normal1", "MiJia Rice Cooker", THING_TYPE_UNSUPPORTED),
    COOKER2("chunmi.cooker.normal2", "MiJia Rice Cooker", THING_TYPE_UNSUPPORTED),
    COOKER3("hunmi.cooker.normal3", "MiJia Rice Cooker", THING_TYPE_UNSUPPORTED),
    COOKER4("chunmi.cooker.normal4", "MiJia Rice Cooker", THING_TYPE_UNSUPPORTED),
    COOKER_P1("chunmi.cooker.press1", "MiJia Heating Pressure Rice Cooker", THING_TYPE_UNSUPPORTED),
    COOKER_P2("chunmi.cooker.press2", "MiJia Heating Pressure Rice Cooker", THING_TYPE_UNSUPPORTED),
    FAN1("zhimi.fan.v1", "Mi Smart Fan", THING_TYPE_BASIC),
    FAN2("zhimi.fan.v2", "Mi Smart Fan", THING_TYPE_BASIC),
    FAN3("zhimi.fan.v3", "Mi Smart Pedestal Fan", THING_TYPE_BASIC),
    FAN_SA1("zhimi.fan.sa1", "Xiaomi Mi Smart Pedestal Fan", THING_TYPE_BASIC),
    FAN_ZA1("zhimi.fan.za1", "Xiaomi Mi Smart Pedestal Fan", THING_TYPE_BASIC),
    FAN_ZA4("zhimi.fan.za4", "Xiaomi Mi Smart Pedestal Fan", THING_TYPE_BASIC),
    FAN_1C("dmaker.fan.1c", "Xiaomi Mijia Smart Tower Fan", THING_TYPE_BASIC),
    FAN_P5("dmaker.fan.p5", "Xiaomi Mijia Smart Tower Fan", THING_TYPE_BASIC),
    FAN_P8("dmaker.fan.p8", "Xiaomi Mijia Smart Tower Fan", THING_TYPE_BASIC),
    FAN_P9("dmaker.fan.p9", "Xiaomi Mijia Smart Tower Fan", THING_TYPE_BASIC),
    FAN_P10("dmaker.fan.p10", "Xiaomi Mijia Smart Tower Fan", THING_TYPE_BASIC),
    FRIDGE_V3("viomi.fridge.v3", "Viomi Internet refrigerator iLive", THING_TYPE_UNSUPPORTED),
    GATEWAY1("lumi.gateway.v1", "Mi Smart Home Gateway v1", THING_TYPE_BASIC),
    GATEWAY2("lumi.gateway.v2", "Mi Smart Home Gateway v2", THING_TYPE_BASIC),
    GATEWAY3("lumi.gateway.v3", "Mi Smart Home Gateway v3", THING_TYPE_BASIC),
    GATEWAY_MGL3("lumi.gateway.mgl03", "Xiaomi Mi Mijia Gateway V3 ZNDMWG03LM", THING_TYPE_BASIC),
    HUMIDIFIER("zhimi.humidifier.v1", "Mi Humdifier", THING_TYPE_BASIC),
    LUMI_C11("lumi.ctrl_neutral1.v1", "Light Control (Wall Switch)", THING_TYPE_UNSUPPORTED),
    LUMI_C12("lumi.ctrl_neutral2.v1", "Light Control (Wall Switch)", THING_TYPE_UNSUPPORTED),
    MRBOND_AIRER_M1PRO("mrbond.airer.m1pro", "Mr Bond M1 Pro Smart Clothes Dryer", THING_TYPE_BASIC),
    MRBOND_AIRER_M1S("mrbond.airer.m1s", "Mr Bond M1 Smart Clothes Dryer", THING_TYPE_BASIC),
    MRBOND_AIRER_M1SUPER("mrbond.airer.m1super", "Mr Bond M1 Super Smart Clothes Dryer", THING_TYPE_BASIC),
    PHILIPS_SR1("philips.light.sread1", "Xiaomi Philips Eyecare Smart Lamp 2", THING_TYPE_BASIC),
    PHILIPS_SR2("philips.light.sread2", "Xiaomi Philips Eyecare Smart Lamp 2", THING_TYPE_BASIC),
    PHILIPS_C("philips.light.ceiling", "Xiaomi Philips LED Ceiling Lamp", THING_TYPE_BASIC),
    PHILIPS_C2("philips.light.zyceiling", "Xiaomi Philips LED Ceiling Lamp", THING_TYPE_BASIC),
    PHILIPS_BULB("philips.light.bulb", "Xiaomi Philips Bulb", THING_TYPE_BASIC),
    PHILIPS_HBULB("philips.light.hbulb", "Xiaomi Philips Wi-Fi Bulb E27 White", THING_TYPE_BASIC),
    PHILIPS_CANDLE("philips.light.candle", "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp", THING_TYPE_BASIC),
    PHILIPS_DOWN("philips.light.downlight", "Xiaomi Philips Downlight", THING_TYPE_BASIC),
    PHILIPS_MOON("philips.light.moonlight", "Xiaomi Philips ZhiRui bedside lamp", THING_TYPE_BASIC),
    PHILIPS_LIGHT_BCEILING1("philips.light.bceiling1", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_BCEILING2("philips.light.bceiling2", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_CBULB("philips.light.cbulb", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_CBULBS("philips.light.cbulbs", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_DCOLOR("philips.light.dcolor", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_RWREAD("philips.light.rwread", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_LNBLIGHT1("philips.light.lnblight1", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_LNBLIGHT2("philips.light.lnblight2", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_LNLRLIGHT("philips.light.lnlrlight", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_LRCEILING("philips.light.lrceiling", "Philips Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_CANDLE2("philips.light.candle2", "Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal",
            THING_TYPE_BASIC),
    PHILIPS_LIGHT_MONO1("philips.light.mono1", "philips.light.mono1", THING_TYPE_BASIC),
    PHILIPS_LIGHT_DLIGHT("philips.light.dlight", "Philips Down Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_MCEIL("philips.light.mceil", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_MCEILM("philips.light.mceilm", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_MCEILS("philips.light.mceils", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_OBCEIL("philips.light.obceil", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_OBCEIM("philips.light.obceim", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_OBCEIS("philips.light.obceis", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_SCEIL("philips.light.sceil", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_SCEILM("philips.light.sceilm", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_SCEILS("philips.light.sceils", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_XZCEIL("philips.light.xzceil", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_XZCEIM("philips.light.xzceim", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_XZCEIS("philips.light.xzceis", "Philips Ceiling Light", THING_TYPE_BASIC),
    PHILIPS_LIGHT_VIRTUAL("philips.light.virtual", "philips.light.virtual", THING_TYPE_BASIC),
    PHILIPS_LIGHT_ZYSREAD("philips.light.zysread", "philips.light.zysread", THING_TYPE_BASIC),
    PHILIPS_LIGHT_ZYSTRIP("philips.light.zystrip", "philips.light.zystrip", THING_TYPE_BASIC),
    POWERPLUG("chuangmi.plug.m1", "Mi Power-plug", THING_TYPE_BASIC),
    POWERPLUG1("chuangmi.plug.v1", "Mi Power-plug v1", THING_TYPE_BASIC),
    POWERPLUG2("chuangmi.plug.v2", "Mi Power-plug v2", THING_TYPE_BASIC),
    POWERPLUG3("chuangmi.plug.v3", "Mi Power-plug v3", THING_TYPE_BASIC),
    POWERPLUGM3("chuangmi.plug.m3", "Mi Power-plug", THING_TYPE_BASIC),
    POWERPLUG_HMI205("chuangmi.plug.hmi205", "Mi Smart Plug", THING_TYPE_BASIC),
    CHUANGMI_PLUG_HMI206("chuangmi.plug.hmi206", "Mi Smart Plug", THING_TYPE_BASIC),
    CHUANGMI_PLUG_HMI208("chuangmi.plug.hmi208", "Mi Smart Plug", THING_TYPE_BASIC),
    POWERSTRIP("qmi.powerstrip.v1", "Qing Mi Smart Power Strip v1", THING_TYPE_BASIC),
    POWERSTRIP2("zimi.powerstrip.v2", "Mi Power-strip v2", THING_TYPE_BASIC),
    TOOTHBRUSH("soocare.toothbrush.x3", "Mi Toothbrush", THING_TYPE_UNSUPPORTED),
    VACUUM("rockrobo.vacuum.v1", "Mi Robot Vacuum", THING_TYPE_VACUUM),
    VACUUM_C1("roborock.vacuum.c1", "Mi Xiaowa Vacuum c1", THING_TYPE_VACUUM),
    VACUUM_A08("roborock.vacuum.a08", "Roborock Vacuum S6 pure", THING_TYPE_VACUUM),
    VACUUM_A09("roborock.vacuum.a09", "Roborock S6 MaxV / T7 Pro", THING_TYPE_VACUUM),
    VACUUM_A10("roborock.vacuum.a10", "Roborock S6 MaxV / T7 Pro", THING_TYPE_VACUUM),
    VACUUM_A11("roborock.vacuum.a11", "Roborock S6 MaxV / T7 Pro", THING_TYPE_VACUUM),
    VACUUM_P5("roborock.vacuum.p5", "Roborock Vacuum S6 pure", THING_TYPE_VACUUM),
    VACUUM2("roborock.vacuum.s5", "Mi Robot Vacuum v2", THING_TYPE_VACUUM),
    VACUUM1S("roborock.vacuum.m1s", "Mi Robot Vacuum 1S", THING_TYPE_VACUUM),
    VACUUMS4("roborock.vacuum.s4", "Mi Robot Vacuum S4", THING_TYPE_VACUUM),
    VACUUMSTS4V2("roborock.vacuum.s4v2", "Roborock Vacuum S4v2", THING_TYPE_VACUUM),
    VACUUMST6("roborock.vacuum.t6", "Roborock Vacuum T6", THING_TYPE_VACUUM),
    VACUUMST6V2("roborock.vacuum.t6v2", "Roborock Vacuum T6 v2", THING_TYPE_VACUUM),
    VACUUMST6V3("roborock.vacuum.t6v3", "Roborock Vacuum T6 v3", THING_TYPE_VACUUM),
    VACUUMST4("roborock.vacuum.t4", "Roborock Vacuum T4", THING_TYPE_VACUUM),
    VACUUMST4V2("roborock.vacuum.t4v2", "Roborock Vacuum T4 v2", THING_TYPE_VACUUM),
    VACUUMST4V3("roborock.vacuum.t4v3", "Roborock Vacuum T4 v3", THING_TYPE_VACUUM),
    VACUUMST7("roborock.vacuum.t7", "Roborock Vacuum T7", THING_TYPE_VACUUM),
    VACUUMST7V2("roborock.vacuum.t7v2", "Roborock Vacuum T7 v2", THING_TYPE_VACUUM),
    VACUUMST7V3("roborock.vacuum.t7v3", "Roborock Vacuum T7 v3", THING_TYPE_VACUUM),
    VACUUMST7P("roborock.vacuum.t7p", "Roborock Vacuum T7p", THING_TYPE_VACUUM),
    VACUUMST7PV2("roborock.vacuum.t7pv2", "Roborock Vacuum T7 v2", THING_TYPE_VACUUM),
    VACUUMST7PV3("roborock.vacuum.t7pv3", "Roborock Vacuum T7 v3", THING_TYPE_VACUUM),
    VACUUMS5MAX("roborock.vacuum.s5e", "Roborock Vacuum S5 Max", THING_TYPE_VACUUM),
    VACUUMSS6("rockrobo.vacuum.s6", "Roborock Vacuum S6", THING_TYPE_VACUUM),
    VACUUMSS62("roborock.vacuum.s6", "Roborock Vacuum S6", THING_TYPE_VACUUM),
    VACUUME2("roborock.vacuum.e2", "Rockrobo Xiaowa Vacuum v2", THING_TYPE_UNSUPPORTED),
    VACUUME_V6("viomi.vacuum.v6", "Xiaomi Mijia vacuum V-RVCLM21B", THING_TYPE_BASIC),
    VACUUME_V7("viomi.vacuum.v7", "Xiaomi Mijia vacuum mop STYJ02YM", THING_TYPE_BASIC),
    VACUUME_V8("viomi.vacuum.v8", "Xiaomi Mijia vacuum mop STYJ02YM v2", THING_TYPE_BASIC),
    VACUUM_MC1808("dreame.vacuum.mc1808", "Vacuum 1C STYTJ01ZHM", THING_TYPE_BASIC),
    ROBOROCK_VACUUM_C1("roborock.vacuum.c1", "roborock.vacuum.c1", THING_TYPE_UNSUPPORTED),
    SWEEPER2("roborock.sweeper.e2v2", "Rockrobo Xiaowa Sweeper v2", THING_TYPE_UNSUPPORTED),
    SWEEPER3("roborock.sweeper.e2v3", "Rockrobo Xiaowa Sweeper v3", THING_TYPE_UNSUPPORTED),
    SWITCH01("090615.switch.xswitch01", " Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch", THING_TYPE_BASIC),
    SWITCH02("090615.switch.xswitch02", " Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch", THING_TYPE_BASIC),
    SWITCH03("090615.switch.xswitch03", " Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch", THING_TYPE_BASIC),
    WATER_PURIFIER1("yunmi.waterpurifier.v1", "Mi Water Purifier v1", THING_TYPE_BASIC),
    WATER_PURIFIER2("yunmi.waterpurifier.v2", "Mi Water Purifier v2", THING_TYPE_BASIC),
    WATER_PURIFIER3("yunmi.waterpurifier.v3", "Mi Water Purifier v3", THING_TYPE_BASIC),
    WATER_PURIFIER4("yunmi.waterpurifier.v4", "Mi Water Purifier v4", THING_TYPE_BASIC),
    WATER_PURIFIER_LX2("yunmi.waterpuri.lx2", "Mi Water Purifier lx2", THING_TYPE_BASIC),
    WATER_PURIFIER_LX3("yunmi.waterpuri.lx3", "Mi Water Purifier lx3", THING_TYPE_BASIC),
    WATER_PURIFIER_LX4("yunmi.waterpuri.lx4", "Mi Water Purifier lx4", THING_TYPE_BASIC),
    WATER_PURIFIER_LX5("yunmi.waterpuri.lx5", "Mi Water Purifier lx5", THING_TYPE_BASIC),
    WATER_PURIFIER_LX6("yunmi.waterpuri.lx6", "Mi Water Purifier lx6", THING_TYPE_BASIC),
    WATER_PURIFIER_LX7("yunmi.waterpuri.lx7", "Mi Water Purifier lx7", THING_TYPE_BASIC),
    WATER_PURIFIER_LX8("yunmi.waterpuri.lx8", "Mi Water Purifier lx8", THING_TYPE_BASIC),
    WATER_PURIFIER_LX9("yunmi.waterpuri.lx9", "Mi Water Purifier lx9", THING_TYPE_BASIC),
    WATER_PURIFIER_LX10("yunmi.waterpuri.lx10", "Mi Water Purifier lx10", THING_TYPE_BASIC),
    WATER_PURIFIER_LX11("yunmi.waterpuri.lx11", "Mi Water Purifier lx11", THING_TYPE_BASIC),
    WATER_PURIFIER_LX12("yunmi.waterpuri.lx12", "Mi Water Purifier lx12", THING_TYPE_BASIC),
    WIFI2("xiaomi.repeater.v2", "Xiaomi Wifi Extender", THING_TYPE_UNSUPPORTED),
    WIFISPEAKER("xiaomi.wifispeaker.v1", "Mi Internet Speaker", THING_TYPE_UNSUPPORTED),
    XJX_TOILET_PRO("xjx.toilet.pro", "Xiaomi Mijia Whale Smart Toilet Cover", THING_TYPE_BASIC),
    XJX_TOILET_RELAX("xjx.toilet.relax", "Xiaomi Mijia Smart Toilet Cover", THING_TYPE_BASIC),
    XJX_TOILET_PURE("xjx.toilet.pure", "Xiaomi Mijia Smart Toilet Cover", THING_TYPE_BASIC),
    XJX_TOILET_ZERO("xjx.toilet.zero", "Xiaomi Mijia Smart Toilet Cover", THING_TYPE_BASIC),
    YEELIGHT_BSLAMP("yeelink.light.bslamp1", "Yeelight Lamp", THING_TYPE_BASIC),
    YEELIGHT_BSLAMP2("yeelink.light.bslamp2", "Yeelight Lamp", THING_TYPE_BASIC),
    YEELIGHT_BSLAMP3("yeelink.light.bslamp3", "Yeelight Lamp", THING_TYPE_BASIC),
    YEELIGHT_BHFLIGHT1("yeelink.bhf_light.v1", "Yeelight BadHeater", THING_TYPE_BASIC),
    YEELIGHT_BHFLIGHT2("yeelink.bhf_light.v2", "Yeelight BadHeater", THING_TYPE_BASIC),
    YEELIGHT_CEIL1("yeelink.light.ceiling1", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL2("yeelink.light.ceiling2", "Yeelight LED Ceiling Lamp v2", THING_TYPE_BASIC),
    YEELIGHT_CEIL3("yeelink.light.ceiling3", "Yeelight LED Ceiling Lamp v3", THING_TYPE_BASIC),
    YEELIGHT_CEIL4("yeelink.light.ceiling4", "Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB)", THING_TYPE_BASIC),
    YEELIGHT_CEIL4A("yeelink.light.ceiling4.ambi", "Yeelight LED Ceiling Lamp v4", THING_TYPE_BASIC),
    YEELIGHT_CEIL5("yeelink.light.ceiling5", "Yeelight LED Ceiling Lamp v5", THING_TYPE_BASIC),
    YEELIGHT_CEIL6("yeelink.light.ceiling6", "Yeelight LED Ceiling Lamp v6", THING_TYPE_BASIC),
    YEELIGHT_CEIL7("yeelink.light.ceiling7", "Yeelight LED Ceiling Lamp v7", THING_TYPE_BASIC),
    YEELIGHT_CEIL8("yeelink.light.ceiling8", "Yeelight LED Ceiling Lamp v8", THING_TYPE_BASIC),
    YEELIGHT_CEIL9("yeelink.light.ceiling9", "Yeelight LED Ceiling Lamp v9", THING_TYPE_BASIC),
    YEELIGHT_CEIL10("yeelink.light.ceiling10", "Yeelight LED Meteorite lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL11("yeelink.light.ceiling11", "Yeelight LED Ceiling Lamp v11", THING_TYPE_BASIC),
    YEELIGHT_CEIL12("yeelink.light.ceiling12", "Yeelight LED Ceiling Lamp v12", THING_TYPE_BASIC),
    YEELIGHT_CEIL13("yeelink.light.ceiling13", "Yeelight LED Ceiling Lamp v13", THING_TYPE_BASIC),
    YEELIGHT_CEIL14("yeelink.light.ceiling14", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL15("yeelink.light.ceiling15", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL16("yeelink.light.ceiling16", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL17("yeelink.light.ceiling17", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL18("yeelink.light.ceiling18", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL19("yeelink.light.ceiling19", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL20("yeelink.light.ceiling20", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL21("yeelink.light.ceiling21", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL22("yeelink.light.ceiling22", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL23("yeelink.light.ceiling23", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL4_A("yeelink.light.ceiling4.ambi", "Yeelight LED Ceiling Ambi Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL10_A("yeelink.light.ceiling10.ambi", "Yeelight LED Ceiling Ambi Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL19_A("yeelink.light.ceiling19.ambi", "Yeelight LED Ceiling Ambi Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL20_A("yeelink.light.ceiling20.ambi", "Yeelight LED Ceiling Ambi Lamp", THING_TYPE_BASIC),
    YEELIGHT_CT2("yeelink.light.ct2", "Yeelight ct2", THING_TYPE_BASIC),
    YEELIGHT_DOLPHIN("yeelink.light.mono1", "Yeelight White Bulb", THING_TYPE_BASIC),
    YEELIGHT_DOLPHIN2("yeelink.light.mono2", "Yeelight White Bulb v2", THING_TYPE_BASIC),
    YEELIGHT_FLUTE("yeelink.light.mono5", "Yeelight White", THING_TYPE_BASIC),
    YEELIGHT_DONUT("yeelink.wifispeaker.v1", "Yeelight Wifi Speaker", THING_TYPE_UNSUPPORTED),
    YEELIGHT_MANGO("yeelink.light.lamp1", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO2("yeelink.light.lamp2", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO3("yeelink.light.lamp3", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO4("yeelink.light.lamp4", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO5("yeelink.light.lamp5", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO6("yeelink.light.lamp6", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO7("yeelink.light.lamp7", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO8("yeelink.light.lamp8", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_PANEL1("yeelink.light.panel1", "Yeelight Panel", THING_TYPE_BASIC),
    YEELIGHT_STRIP("yeelink.light.strip1", "Yeelight Strip", THING_TYPE_BASIC),
    YEELIGHT_STRIP2("yeelink.light.strip2", "Yeelight Strip", THING_TYPE_BASIC),
    YEELIGHT_STRIP4("yeelink.light.strip4", "Yeelight Strip", THING_TYPE_BASIC),
    YEELIGHT_VIRT("yeelink.light.virtual", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_C1("yeelink.light.color1", "Yeelight Color Bulb", THING_TYPE_BASIC),
    YEELIGHT_C2("yeelink.light.color2", "Yeelight Color Bulb YLDP06YL 10W", THING_TYPE_BASIC),
    YEELIGHT_C3("yeelink.light.color3", "Yeelight Color Bulb YLDP02YL 9W", THING_TYPE_BASIC),
    YEELIGHT_C4("yeelink.light.color4", "Yeelight Bulb YLDP13YL (8,5W)", THING_TYPE_BASIC),
    YL_CEILING1("yilai.light.ceiling1", "Yeelight yilai ceiling", THING_TYPE_BASIC),
    YL_CEILING2("yilai.light.ceiling2", "Yeelight yilai ceiling", THING_TYPE_BASIC),
    YL_CEILING3("yilai.light.ceiling3", "Yeelight yilai ceiling", THING_TYPE_BASIC),
    ZHIMI_HEATER_ZA1("zhimi.heater.za1", "Zhimi Heater", THING_TYPE_BASIC),
    UNKNOWN("unknown", "Unknown Mi IO Device", THING_TYPE_UNSUPPORTED);

    public static MiIoDevices getType(String modelString) {
        for (MiIoDevices mioDev : MiIoDevices.values()) {
            if (mioDev.getModel().equals(modelString)) {
                return mioDev;
            }
        }
        return UNKNOWN;
    }

    private final String model;
    private final String description;

    private final ThingTypeUID thingType;

    MiIoDevices(String model, String description, ThingTypeUID thingType) {
        this.model = model;
        this.description = description;
        this.thingType = thingType;
    }

    public String getDescription() {
        return description;
    }

    public String getModel() {
        return model;
    }

    public ThingTypeUID getThingType() {
        return thingType;
    }

    @Override
    public String toString() {
        return description + " (" + model + ")";
    }
}
