/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of HomeKit accessory categories with their corresponding numeric IDs and labels.
 * This enum provides a mapping between category IDs used in HomeKit and human-readable labels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum AccessoryCategory {
    OTHER(1, "Other Accessory"),
    BRIDGE(2, "Bridge"),
    FAN(3, "Fan"),
    GARAGE_DOOR(4, "Garage Door"),
    LIGHTING(5, "Lighting"),
    DOOR_LOCK(6, "Door Lock"),
    OUTLET(7, "Outlet"),
    SWITCH(8, "Switch"),
    THERMOSTAT(9, "Thermostat"),
    SENSOR(10, "Sensor"),
    SECURITY_SYSTEM(11, "Security System"),
    DOOR(12, "Door"),
    WINDOW(13, "Window"),
    WINDOW_COVERING(14, "Window Covering"),
    PROGRAMMABLE_SWITCH(15, "Programmable Switch"),
    RANGE_EXTENDER(16, "Range Extender"),
    IP_CAMERA(17, "IP Camera"),
    VIDEO_DOORBELL(18, "Video Doorbell"),
    AIR_PURIFIER(19, "Air Purifier"),
    HEATER(20, "Heater"),
    AIR_CONDITIONER(21, "Air Conditioner"),
    HUMIDIFIER(22, "Humidifier"),
    DEHUMIDIFIER(23, "Dehumidifier"),
    APPLE_TV(24, "Apple TV"),
    SMART_SPEAKER(25, "Smart Speaker"),
    SPEAKER(26, "Speaker"),
    AIRPORT(27, "AirPort"),
    SPRINKLER(28, "Sprinkler"),
    FAUCET(29, "Faucet"),
    SHOWER_HEAD(30, "Shower"),
    TELEVISION(31, "Television"),
    REMOTE(32, "Remote"),
    ROUTER(33, "Router"),
    AUDIO_RECEIVER(34, "Audio Receiver"),
    TV_SET_TOP_BOX(35, "TV Set Top Box"),
    TV_STREAMING_STICK(36, "TV Streaming Stick");

    private final int id;
    private final String label;

    AccessoryCategory(int category, String label) {
        this.id = category;
        this.label = label;
    }

    public static AccessoryCategory from(int id) {
        for (AccessoryCategory value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return OTHER;
    }

    public static AccessoryCategory from(String label) {
        for (AccessoryCategory value : values()) {
            if (label.equals(value.label)) {
                return value;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        return label;
    }
}
