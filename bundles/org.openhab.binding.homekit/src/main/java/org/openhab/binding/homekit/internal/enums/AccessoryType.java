/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

/**
 * Enumeration of HomeKit accessory categories with their corresponding numeric IDs and labels.
 * This enum provides a mapping between category IDs used in HomeKit and human-readable labels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public enum AccessoryType {
    // TODO manually check the Homekit specification pdf to ensure all types are covered
    OTHER(1, "Other"),
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
    RESERVED(16, "Reserved"),
    IP_CAMERA(17, "IP Camera"),
    VIDEO_DOORBELL(18, "Video Doorbell"),
    AIR_PURIFIER(19, "Air Purifier"),
    HEATER(20, "Heater"),
    AIR_CONDITIONER(21, "Air Conditioner"),
    HUMIDIFIER(22, "Humidifier"),
    DEHUMIDIFIER(23, "Dehumidifier"),
    APPLE_TV(24, "Apple TV"),
    SPEAKER(26, "Speaker"),
    AIRPORT(27, "AirPort"),
    SPRINKLER(28, "Sprinkler"),
    FAUCET(29, "Faucet"),
    SHOWER_HEAD(30, "Shower"),
    TELEVISION(31, "Television"),
    REMOTE(32, "Remote");

    private final int id;
    private final String label;

    AccessoryType(int category, String label) {
        this.id = category;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static AccessoryType from(int id) throws IllegalArgumentException {
        for (AccessoryType value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return OTHER;
    }
}
