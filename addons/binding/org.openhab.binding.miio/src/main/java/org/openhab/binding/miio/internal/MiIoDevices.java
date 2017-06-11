/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.miio.MiIoBindingConstants;

/**
 * MiIO Devices
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public enum MiIoDevices {
    AIRCONDITION_A1("aux.aircondition.v1", "AUX Air Conditioner", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    AIRCONDITION_I1("idelan.aircondition.v1", "Idelan Air Conditioner", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M1("midea.aircondition.v1", "Midea Air Conditioner v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    AIRCONDITION_M2("midea.aircondition.v2", "Midea Air Conditioner v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    AIRCONDITION_MXA1("midea.aircondition.xa1", "Midea Air Conditioner xa1",
            MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    AIRMONITOR1("zhimi.airmonitor.v1", "Mi Air Monitor v1", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_HUMIDIFIER_V1("zhimi.humidifier.v1", "Mi Air Humidifier", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_HUMIDIFIER_CA1("zhimi.humidifier.ca1", "Mi Air Humidifier", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER1("zhimi.airpurifier.v1", "Mi Air Purifier v1", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER2("zhimi.airpurifier.v2", "Mi Air Purifier v2", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER3("zhimi.airpurifier.v3", "Mi Air Purifier v3", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER5("zhimi.airpurifier.v5", "Mi Air Purifier v5", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER6("zhimi.airpurifier.v6", "Mi Air Purifier Pro v6", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIER7("zhimi.airpurifier.v7", "Mi Air Purifier Pro v7", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERM("zhimi.airpurifier.m1", "Mi Air Purifier 2 (mini)", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERM2("zhimi.airpurifier.m2", "Mi Air Purifier (mini)", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERMA1("zhimi.airpurifier.ma1", "Mi Air Purifier MS1", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERMA2("zhimi.airpurifier.ma2", "Mi Air Purifier MS2", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERSA1("zhimi.airpurifier.sa1", "Mi Air Purifier Super", MiIoBindingConstants.THING_TYPE_BASIC),
    AIR_PURIFIERSA2("zhimi.airpurifier.sa2", "Mi Air Purifier Super 2", MiIoBindingConstants.THING_TYPE_BASIC),
    CHUANGMI_IR2("chuangmi.ir.v2", "Mi Remote v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER1("chunmi.cooker.normal1", "MiJia Rice Cooker", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER2("chunmi.cooker.normal2", "MiJia Rice Cooker", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER3("hunmi.cooker.normal3", "MiJia Rice Cooker", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER4("chunmi.cooker.normal4", "MiJia Rice Cooker", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER_P1("chunmi.cooker.press1", "MiJia Heating Pressure Rice Cooker",
            MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    COOKER_P2("chunmi.cooker.press2", "MiJia Heating Pressure Rice Cooker",
            MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    FAN1("zhimi.fan.v1", "Mi Smart Fan", MiIoBindingConstants.THING_TYPE_BASIC),
    FAN2("zhimi.fan.v2", "Mi Smart Fan", MiIoBindingConstants.THING_TYPE_BASIC),
    FAN3("zhimi.fan.v3", "Mi Smart Pedestal Fan", MiIoBindingConstants.THING_TYPE_BASIC),
    FAN_SA1("zhimi.fan.sa1", "Xiaomi Mi Smart Pedestal Fan", MiIoBindingConstants.THING_TYPE_BASIC),
    FAN_ZA1("zhimi.fan.za1", "Xiaomi Mi Smart Pedestal Fan", MiIoBindingConstants.THING_TYPE_BASIC),
    GATEWAY1("lumi.gateway.v1", "Mi Smart Home Gateway v1", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    GATEWAY2("lumi.gateway.v2", "Mi Smart Home Gateway v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    GATEWAY3("lumi.gateway.v3", "Mi Smart Home Gateway v3", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    HUMIDIFIER("zhimi.humidifier.v1", "Mi Humdifier", MiIoBindingConstants.THING_TYPE_BASIC),
    LUMI_C11("lumi.ctrl_neutral1.v1", "Light Control (Wall Switch)", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    LUMI_C12("lumi.ctrl_neutral2.v1", "Light Control (Wall Switch)", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    PHILIPS_R1("philips.light.sread1", "Xiaomi Philips Eyecare Smart Lamp 2", MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_C("philips.light.ceiling", "Xiaomi Philips LED Ceiling Lamp", MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_C2("philips.light.zyceiling", "Xiaomi Philips LED Ceiling Lamp", MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_BULB("philips.light.bulb", "Xiaomi Philips Bulb", MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_CANDLE("philips.light.candle", "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp",
            MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_DOWN("philips.light.downlight", "Xiaomi Philips Downlight", MiIoBindingConstants.THING_TYPE_BASIC),
    PHILIPS_MOON("philips.light.moonlight", "Xiaomi Philips ZhiRui bedside lamp",
            MiIoBindingConstants.THING_TYPE_BASIC),
    POWERPLUG("chuangmi.plug.m1", "Mi Power-plug", MiIoBindingConstants.THING_TYPE_BASIC),
    POWERPLUG1("chuangmi.plug.v1", "Mi Power-plug v1", MiIoBindingConstants.THING_TYPE_BASIC),
    POWERPLUG2("chuangmi.plug.v2", "Mi Power-plug v2", MiIoBindingConstants.THING_TYPE_BASIC),
    POWERPLUG3("chuangmi.plug.v3", "Mi Power-plug v3", MiIoBindingConstants.THING_TYPE_BASIC),
    POWERSTRIP("qmi.powerstrip.v1", "Qing Mi Smart Power Strip v1", MiIoBindingConstants.THING_TYPE_BASIC),
    POWERSTRIP2("zimi.powerstrip.v2", "Mi Power-strip v2", MiIoBindingConstants.THING_TYPE_BASIC),
    TOOTHBRUSH("soocare.toothbrush.x3", "Mi Toothbrush", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    VACUUM("rockrobo.vacuum.v1", "Mi Robot Vacuum", MiIoBindingConstants.THING_TYPE_VACUUM),
    VACUUM2("roborock.vacuum.s5", "Mi Robot Vacuum v2", MiIoBindingConstants.THING_TYPE_VACUUM),
    VACUUME2("roborock.vacuum.e2", "Rockrobo Xiaowa Vacuum v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    SWEEPER2("roborock.sweeper.e2v2", "Rockrobo Xiaowa Sweeper v2", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    SWEEPER3("roborock.sweeper.e2v3", "Rockrobo Xiaowa Sweeper v3", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    WATER_PURIFIER2("yunmi.waterpuri.v2", "Mi Water Purifier v2", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIERLX2("yunmi.waterpuri.lx2", "Mi Water Purifier lx2", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIERLX3("yunmi.waterpuri.lx3", "Mi Water Purifier lx3", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIERLX4("yunmi.waterpuri.lx4", "Mi Water Purifier lx4", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIER("yunmi.waterpurifier.v2", "Mi Water Purifier v2", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIER3("yunmi.waterpurifier.v3", "Mi Water Purifier v3", MiIoBindingConstants.THING_TYPE_BASIC),
    WATER_PURIFIER4("yunmi.waterpurifier.v4", "Mi Water Purifier v4", MiIoBindingConstants.THING_TYPE_BASIC),
    WIFI2("xiaomi.repeater.v2", "Xiaomi Wifi Extender", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    WIFISPEAKER("xiaomi.wifispeaker.v1", "Mi Internet Speaker", MiIoBindingConstants.THING_TYPE_UNSUPPORTED),
    YEELIGHT_BS("yeelink.light.bslamp1", "Yeelight Lamp", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_C1("yeelink.light.color1", "Yeelight Color Bulb", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_C2("yeelink.light.color2", "Yeelight Color Bulb YLDP06YL 10W", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_CEIL1("yeelink.light.ceiling1", "Yeelight LED Ceiling Lamp", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_CEIL2("yeelink.light.ceiling2", "Yeelight LED Ceiling Lamp v2", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_CEIL3("yeelink.light.ceiling3", "Yeelight LED Ceiling Lamp v3", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_CEIL4("yeelink.light.ceiling4", "Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB)",
            MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_L1("yeelink.light.lamp1", "Yeelight", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_M1("yeelink.light.mono1", "Yeelight White Bulb", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_M2("yeelink.light.mono2", "Yeelight White Bulb v2", MiIoBindingConstants.THING_TYPE_BASIC),
    YEELIGHT_STRIP("yeelink.light.strip1", "Yeelight Strip", MiIoBindingConstants.THING_TYPE_BASIC),
    UNKNOWN("unknown", "Unknown Mi IO Device", MiIoBindingConstants.THING_TYPE_UNSUPPORTED);

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
