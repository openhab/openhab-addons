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
package org.openhab.binding.miio.internal;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Mi IO Devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum MiIoDevices {
    AIRCONDITION_A1("aux.aircondition.v1", "AUX Air Conditioner", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_I1("idelan.aircondition.v1", "Idelan Air Conditioner", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M1("midea.aircondition.v1", "Midea Air Conditioner v2", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M2("midea.aircondition.v2", "Midea Air Conditioner v2", THING_TYPE_UNSUPPORTED),
    AIRCONDITION_MXA1("midea.aircondition.xa1", "Midea Air Conditioner xa1", THING_TYPE_UNSUPPORTED),
    AIRMONITOR1("zhimi.airmonitor.v1", "Mi Air Monitor v1", THING_TYPE_BASIC),
    AIRMONITOR_B1("cgllc.airmonitor.b1", "Mi Air Quality Monitor 2gen", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_V1("zhimi.humidifier.v1", "Mi Air Humidifier", THING_TYPE_BASIC),
    AIR_HUMIDIFIER_CA1("zhimi.humidifier.ca1", "Mi Air Humidifier", THING_TYPE_BASIC),
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
    AIR_PURIFIERSA1("zhimi.airpurifier.sa1", "Mi Air Purifier Super", THING_TYPE_BASIC),
    AIR_PURIFIERSA2("zhimi.airpurifier.sa2", "Mi Air Purifier Super 2", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_MB1("zhimi.airpurifier.mb1", "Mi Air Purifier mb1", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_MC1("zhimi.airpurifier.mc1", "Mi Air Purifier mc1", THING_TYPE_BASIC),
    ZHIMI_AIRPURIFIER_VIRTUAL("zhimi.airpurifier.virtual", "Mi Air Purifier virtual", THING_TYPE_UNSUPPORTED),
    ZHIMI_AIRPURIFIER_VTL_M1("zhimi.airpurifier.vtl_m1", "Mi Air Purifier vtl m1", THING_TYPE_UNSUPPORTED),
    CHUANGMI_IR2("chuangmi.ir.v2", "Mi Remote v2", THING_TYPE_UNSUPPORTED),
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
    GATEWAY1("lumi.gateway.v1", "Mi Smart Home Gateway v1", THING_TYPE_UNSUPPORTED),
    GATEWAY2("lumi.gateway.v2", "Mi Smart Home Gateway v2", THING_TYPE_UNSUPPORTED),
    GATEWAY3("lumi.gateway.v3", "Mi Smart Home Gateway v3", THING_TYPE_UNSUPPORTED),
    HUMIDIFIER("zhimi.humidifier.v1", "Mi Humdifier", THING_TYPE_BASIC),
    LUMI_C11("lumi.ctrl_neutral1.v1", "Light Control (Wall Switch)", THING_TYPE_UNSUPPORTED),
    LUMI_C12("lumi.ctrl_neutral2.v1", "Light Control (Wall Switch)", THING_TYPE_UNSUPPORTED),
    PHILIPS_R1("philips.light.sread1", "Xiaomi Philips Eyecare Smart Lamp 2", THING_TYPE_BASIC),
    PHILIPS_C("philips.light.ceiling", "Xiaomi Philips LED Ceiling Lamp", THING_TYPE_BASIC),
    PHILIPS_C2("philips.light.zyceiling", "Xiaomi Philips LED Ceiling Lamp", THING_TYPE_BASIC),
    PHILIPS_BULB("philips.light.bulb", "Xiaomi Philips Bulb", THING_TYPE_BASIC),
    PHILIPS_CANDLE("philips.light.candle", "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp", THING_TYPE_BASIC),
    PHILIPS_DOWN("philips.light.downlight", "Xiaomi Philips Downlight", THING_TYPE_BASIC),
    PHILIPS_MOON("philips.light.moonlight", "Xiaomi Philips ZhiRui bedside lamp", THING_TYPE_BASIC),
    PHILIPS_LIGHT_CANDLE2("philips.light.candle2", "Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal",
            THING_TYPE_BASIC),
    PHILIPS_LIGHT_MONO1("philips.light.mono1", "philips.light.mono1", THING_TYPE_BASIC),
    PHILIPS_LIGHT_VIRTUAL("philips.light.virtual", "philips.light.virtual", THING_TYPE_BASIC),
    PHILIPS_LIGHT_ZYSREAD("philips.light.zysread", "philips.light.zysread", THING_TYPE_BASIC),
    PHILIPS_LIGHT_ZYSTRIP("philips.light.zystrip", "philips.light.zystrip", THING_TYPE_BASIC),
    POWERPLUG("chuangmi.plug.m1", "Mi Power-plug", THING_TYPE_BASIC),
    POWERPLUG1("chuangmi.plug.v1", "Mi Power-plug v1", THING_TYPE_BASIC),
    POWERPLUG2("chuangmi.plug.v2", "Mi Power-plug v2", THING_TYPE_BASIC),
    POWERPLUG3("chuangmi.plug.v3", "Mi Power-plug v3", THING_TYPE_BASIC),
    POWERPLUGM3("chuangmi.plug.m3", "Mi Power-plug", THING_TYPE_BASIC),
    POWERPLUG_HMI205("chuangmi.plug.hmi205", "Mi Smart Plug", THING_TYPE_BASIC),
    POWERSTRIP("qmi.powerstrip.v1", "Qing Mi Smart Power Strip v1", THING_TYPE_BASIC),
    POWERSTRIP2("zimi.powerstrip.v2", "Mi Power-strip v2", THING_TYPE_BASIC),
    TOOTHBRUSH("soocare.toothbrush.x3", "Mi Toothbrush", THING_TYPE_UNSUPPORTED),
    VACUUM("rockrobo.vacuum.v1", "Mi Robot Vacuum", THING_TYPE_VACUUM),
    VACUUM2("roborock.vacuum.s5", "Mi Robot Vacuum v2", THING_TYPE_VACUUM),
    VACUUME2("roborock.vacuum.e2", "Rockrobo Xiaowa Vacuum v2", THING_TYPE_UNSUPPORTED),
    ROBOROCK_VACUUM_C1("roborock.vacuum.c1", "roborock.vacuum.c1", THING_TYPE_UNSUPPORTED),
    SWEEPER2("roborock.sweeper.e2v2", "Rockrobo Xiaowa Sweeper v2", THING_TYPE_UNSUPPORTED),
    SWEEPER3("roborock.sweeper.e2v3", "Rockrobo Xiaowa Sweeper v3", THING_TYPE_UNSUPPORTED),
    WATER_PURIFIER2("yunmi.waterpuri.v2", "Mi Water Purifier v2", THING_TYPE_BASIC),
    WATER_PURIFIERLX2("yunmi.waterpuri.lx2", "Mi Water Purifier lx2", THING_TYPE_BASIC),
    WATER_PURIFIERLX3("yunmi.waterpuri.lx3", "Mi Water Purifier lx3", THING_TYPE_BASIC),
    WATER_PURIFIERLX4("yunmi.waterpuri.lx4", "Mi Water Purifier lx4", THING_TYPE_BASIC),
    WATER_PURIFIER("yunmi.waterpurifier.v2", "Mi Water Purifier v2", THING_TYPE_BASIC),
    WATER_PURIFIER3("yunmi.waterpurifier.v3", "Mi Water Purifier v3", THING_TYPE_BASIC),
    WATER_PURIFIER4("yunmi.waterpurifier.v4", "Mi Water Purifier v4", THING_TYPE_BASIC),
    WIFI2("xiaomi.repeater.v2", "Xiaomi Wifi Extender", THING_TYPE_UNSUPPORTED),
    WIFISPEAKER("xiaomi.wifispeaker.v1", "Mi Internet Speaker", THING_TYPE_UNSUPPORTED),
    YEELIGHT_BSLAMP("yeelink.light.bslamp1", "Yeelight Lamp", THING_TYPE_BASIC),
    YEELIGHT_BSLAMP2("yeelink.light.bslamp2", "Yeelight Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL1("yeelink.light.ceiling1", "Yeelight LED Ceiling Lamp", THING_TYPE_BASIC),
    YEELIGHT_CEIL2("yeelink.light.ceiling2", "Yeelight LED Ceiling Lamp v2", THING_TYPE_BASIC),
    YEELIGHT_CEIL3("yeelink.light.ceiling3", "Yeelight LED Ceiling Lamp v3", THING_TYPE_BASIC),
    YEELIGHT_CEIL4("yeelink.light.ceiling4", "Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB)", THING_TYPE_BASIC),
    YEELIGHT_CEIL4A("yeelink.light.ceiling4.ambi", "Yeelight LED Ceiling Lamp v4", THING_TYPE_BASIC),
    YEELIGHT_CEIL5("yeelink.light.ceiling5", "Yeelight LED Ceiling Lamp v5", THING_TYPE_BASIC),
    YEELIGHT_CEIL6("yeelink.light.ceiling6", "Yeelight LED Ceiling Lamp v6", THING_TYPE_BASIC),
    YEELIGHT_CEIL7("yeelink.light.ceiling7", "Yeelight LED Ceiling Lamp v7", THING_TYPE_BASIC),
    YEELIGHT_CEIL8("yeelink.light.ceiling8", "Yeelight LED Ceiling Lamp v8", THING_TYPE_BASIC),
    YEELIGHT_CT2("yeelink.light.ct2", "Yeelight ct2", THING_TYPE_BASIC),
    YEELIGHT_DOLPHIN("yeelink.light.mono1", "Yeelight White Bulb", THING_TYPE_BASIC),
    YEELIGHT_DOLPHIN2("yeelink.light.mono2", "Yeelight White Bulb v2", THING_TYPE_BASIC),
    YEELIGHT_DONUT("yeelink.wifispeaker.v1", "Yeelight Wifi Speaker", THING_TYPE_UNSUPPORTED),
    YEELIGHT_MANGO("yeelink.light.lamp1", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO2("yeelink.light.lamp2", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_MANGO3("yeelink.light.lamp3", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_STRIP("yeelink.light.strip1", "Yeelight Strip", THING_TYPE_BASIC),
    YEELIGHT_STRIP2("yeelink.light.strip2", "Yeelight Strip", THING_TYPE_BASIC),
    YEELIGHT_VIRT("yeelink.light.virtual", "Yeelight", THING_TYPE_BASIC),
    YEELIGHT_C1("yeelink.light.color1", "Yeelight Color Bulb", THING_TYPE_BASIC),
    YEELIGHT_C2("yeelink.light.color2", "Yeelight Color Bulb YLDP06YL 10W", THING_TYPE_BASIC),
    YEELIGHT_WONDER("yeelink.light.color3", "Yeelight Color Bulb", THING_TYPE_BASIC),
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
