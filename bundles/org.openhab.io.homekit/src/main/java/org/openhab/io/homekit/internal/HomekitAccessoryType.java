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
package org.openhab.io.homekit.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enum of the possible device types. The defined tag string can be used
 * as a tag on an item to enable it for Homekit.
 *
 * @author Andy Lintner - Initial contribution
 */
public enum HomekitAccessoryType {
    ACCESSORY_GROUP("AccessoryGroup"),
    DUMMY("Dummy"),

    AIR_QUALITY_SENSOR("AirQualitySensor"),
    BASIC_FAN("BasicFan"),
    BATTERY("Battery"),
    CARBON_DIOXIDE_SENSOR("CarbonDioxideSensor"),
    CARBON_MONOXIDE_SENSOR("CarbonMonoxideSensor"),
    CONTACT_SENSOR("ContactSensor"),
    DOOR("Door"),
    DOORBELL("Doorbell"),
    FAN("Fan"),
    FAUCET("Faucet"),
    FILTER_MAINTENANCE("Filter"),
    GARAGE_DOOR_OPENER("GarageDoorOpener"),
    HEATER_COOLER("HeaterCooler"),
    HUMIDITY_SENSOR("HumiditySensor"),
    INPUT_SOURCE("InputSource"),
    IRRIGATION_SYSTEM("IrrigationSystem"),
    LEAK_SENSOR("LeakSensor"),
    LIGHT_SENSOR("LightSensor"),
    LIGHTBULB("Lighting"),
    LOCK("Lock"),
    MICROPHONE("Microphone"),
    MOTION_SENSOR("MotionSensor"),
    OCCUPANCY_SENSOR("OccupancySensor"),
    OUTLET("Outlet"),
    SECURITY_SYSTEM("SecuritySystem"),
    SLAT("Slat"),
    SMART_SPEAKER("SmartSpeaker"),
    SMOKE_SENSOR("SmokeSensor"),
    SPEAKER("Speaker"),
    STATELESS_PROGRAMMABLE_SWITCH("StatelessProgrammableSwitch"),
    SWITCH("Switchable"),
    TELEVISION("Television"),
    TELEVISION_SPEAKER("TelevisionSpeaker"),
    TEMPERATURE_SENSOR("TemperatureSensor"),
    THERMOSTAT("Thermostat"),
    VALVE("Valve"),
    WINDOW("Window"),
    WINDOW_COVERING("WindowCovering");

    private static final Map<String, HomekitAccessoryType> TAG_MAP = new HashMap<>();

    static {
        for (HomekitAccessoryType type : HomekitAccessoryType.values()) {
            TAG_MAP.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitAccessoryType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * get accessoryType from String
     *
     * @param tag the tag string
     * @return accessoryType or Optional.empty if no accessory type for the tag was found
     */
    public static Optional<HomekitAccessoryType> valueOfTag(String tag) {
        return Optional.ofNullable(TAG_MAP.get(tag));
    }
}
