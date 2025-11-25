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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of HomeKit service types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ServiceType {
    /*
     * According to the Apple specifications the type fields are fully qualified strings
     * such as "public.hap.service.accessory-information" however we do not need to use
     * the "public.hap.service." prefix in this binding so for brevity it has been removed.
     */
    ACCESSORY_INFORMATION(0x3E, "accessory-information"),
    AIR_PURIFIER(0xBB, "air-purifier"),
    AUDIO_STREAM_MANAGEMENT(0x127, "audio-stream-management"),
    BATTERY(0x96, "battery"),
    CAMERA_RTP_STREAM_MANAGEMENT(0x110, "camera-rtp-stream-management"),
    DATA_STREAM_TRANSPORT_MANAGEMENT(0x129, "data-stream-transport-management"),
    DOOR(0x81, "door"),
    DOORBELL(0x121, "doorbell"),
    FAN(0x40, "fan"),
    FANV2(0xB7, "fanv2"),
    FAUCET(0xD7, "faucet"),
    FILTER_MAINTENANCE(0xBA, "filter-maintenance"),
    GARAGE_DOOR_OPENER(0x41, "garage-door-opener"),
    HEATER_COOLER(0xBC, "heater-cooler"),
    HUMIDIFIER_DEHUMIDIFIER(0xBD, "humidifier-dehumidifier"),
    INPUT_SOURCE(0xD9, "input-source"),
    IRRIGATION_SYSTEM(0xCF, "irrigation-system"),
    LIGHT_BULB(0x43, "lightbulb"),
    LOCK_MANAGEMENT(0x44, "lock-management"),
    LOCK_MECHANISM(0x45, "lock-mechanism"),
    MICROPHONE(0x112, "microphone"),
    OUTLET(0x47, "outlet"),
    PAIRING(0x55, "pairing"),
    PROTOCOL_INFORMATION_SERVICE(0xA2, "protocol.information.service"),
    SECURITY_SYSTEM(0x7E, "security-system"),
    SENSOR_AIR_QUALITY(0x8D, "sensor.air-quality"),
    SENSOR_CARBON_DIOXIDE(0x97, "sensor.carbon-dioxide"),
    SENSOR_CARBON_MONOXIDE(0x7F, "sensor.carbon-monoxide"),
    SENSOR_CONTACT(0x80, "sensor.contact"),
    SENSOR_HUMIDITY(0x82, "sensor.humidity"),
    SENSOR_LEAK(0x83, "sensor.leak"),
    SENSOR_LIGHT(0x84, "sensor.light"),
    SENSOR_MOTION(0x85, "sensor.motion"),
    SENSOR_OCCUPANCY(0x86, "sensor.occupancy"),
    SENSOR_SMOKE(0x87, "sensor.smoke"),
    SENSOR_TEMPERATURE(0x8A, "sensor.temperature"),
    SERVICE_LABEL(0xCC, "service-label"),
    SIRI(0x133, "siri"),
    SMART_SPEAKER(0x228, "smart-speaker"),
    SPEAKER(0x113, "speaker"),
    STATELESS_PROGRAMMABLE_SWITCH(0x89, "stateless-programmable-switch"),
    SWITCH(0x49, "switch"),
    TARGET_CONTROL(0x125, "target-control"),
    TARGET_CONTROL_MANAGEMENT(0x122, "target-control-management"),
    TELEVISION(0xD8, "television"),
    THERMOSTAT(0x4A, "thermostat"),
    VALVE(0xD0, "valve"),
    VERTICAL_SLAT(0xB9, "vertical-slat"),
    WINDOW(0x8B, "window"),
    WINDOW_COVERING(0x8C, "window-covering");

    private final int id;
    private final String type;

    ServiceType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public static ServiceType from(int type) throws IllegalArgumentException {
        for (ServiceType value : values()) {
            if (value.id == type) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ID: " + type);
    }

    public String getOpenhabType() {
        return type.replace(".", "-"); // convert to OH channel type format
    }

    public String getType() {
        return type;
    }

    /**
     * Returns the name of the enum constant in Title Case.
     */
    @Override
    public String toString() {
        return Arrays.stream(name().split("_")).map(
                word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
