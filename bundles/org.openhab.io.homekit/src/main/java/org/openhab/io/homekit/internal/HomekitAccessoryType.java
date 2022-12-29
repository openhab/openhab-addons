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
    HUMIDITY_SENSOR("HumiditySensor"),
    LIGHTBULB("Lighting"),
    SWITCH("Switchable"),
    TEMPERATURE_SENSOR("TemperatureSensor"),
    THERMOSTAT("Thermostat"),
    CONTACT_SENSOR("ContactSensor"),
    VALVE("Valve"),
    LEAK_SENSOR("LeakSensor"),
    MOTION_SENSOR("MotionSensor"),
    OCCUPANCY_SENSOR("OccupancySensor"),
    WINDOW_COVERING("WindowCovering"),
    DOOR("Door"),
    WINDOW("Window"),
    SMOKE_SENSOR("SmokeSensor"),
    CARBON_MONOXIDE_SENSOR("CarbonMonoxideSensor"),
    CARBON_DIOXIDE_SENSOR("CarbonDioxideSensor"),
    BASIC_FAN("BasicFan"),
    FAN("Fan"),
    LOCK("Lock"),
    SECURITY_SYSTEM("SecuritySystem"),
    OUTLET("Outlet"),
    SPEAKER("Speaker"),
    SMART_SPEAKER("SmartSpeaker"),
    GARAGE_DOOR_OPENER("GarageDoorOpener"),
    HEATER_COOLER("HeaterCooler"),
    LIGHT_SENSOR("LightSensor"),
    AIR_QUALITY_SENSOR("AirQualitySensor"),
    BATTERY("Battery"),
    FILTER_MAINTENANCE("Filter"),
    FAUCET("Faucet"),
    MICROPHONE("Microphone"),
    SLAT("Slat"),
    ACCESSORY_GROUP("AccessoryGroup"),
    DUMMY("Dummy");

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
