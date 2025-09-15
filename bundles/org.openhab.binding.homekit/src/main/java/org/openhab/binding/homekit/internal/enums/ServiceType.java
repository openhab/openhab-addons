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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of HomeKit service types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ServiceType {
    ACCESSORY_INFORMATION(0x3E, "public.hap.service.accessory-information"),
    AIR_PURIFIER(0xBB, "public.hap.service.air-purifier"),
    AUDIO_STREAM_MANAGEMENT(0x127, "public.hap.service.audio-stream-management"),
    BATTERY(0x96, "public.hap.service.battery"),
    CAMERA_RTP_STREAM_MANAGEMENT(0x110, "public.hap.service.camera-rtp-stream-management"),
    DATA_STREAM_TRANSPORT_MANAGEMENT(0x129, "public.hap.service.data-stream-transport-management"),
    DOOR(0x81, "public.hap.service.door"),
    DOORBELL(0x121, "public.hap.service.doorbell"),
    FANV2(0xB7, "public.hap.service.fanv2"),
    FAUCET(0xD7, "public.hap.service.faucet"),
    FILTER_MAINTENANCE(0xBA, "public.hap.service.filter-maintenance"),
    GARAGE_DOOR_OPENER(0x41, "public.hap.service.garage-door-opener"),
    HEATER_COOLER(0xBC, "public.hap.service.heater-cooler"),
    HUMIDIFIER_DEHUMIDIFIER(0xBD, "public.hap.service.humidifier-dehumidifier"),
    IRRIGATION_SYSTEM(0xCF, "public.hap.service.irrigation-system"),
    LIGHTBULB(0x43, "public.hap.service.lightbulb"),
    LOCK_MANAGEMENT(0x44, "public.hap.service.lock-management"),
    LOCK_MECHANISM(0x45, "public.hap.service.lock-mechanism"),
    MICROPHONE(0x112, "public.hap.service.microphone"),
    OUTLET(0x47, "public.hap.service.outlet"),
    PAIRING(0x55, "public.hap.service.pairing"),
    PROTOCOL_INFORMATION_SERVICE(0xA2, "public.hap.service.protocol.information.service"),
    SECURITY_SYSTEM(0x7E, "public.hap.service.security-system"),
    SENSOR_AIR_QUALITY(0x8D, "public.hap.service.sensor.air-quality"),
    SENSOR_CARBON_DIOXIDE(0x97, "public.hap.service.sensor.carbon-dioxide"),
    SENSOR_CARBON_MONOXIDE(0x7F, "public.hap.service.sensor.carbon-monoxide"),
    SENSOR_CONTACT(0x80, "public.hap.service.sensor.contact"),
    SENSOR_HUMIDITY(0x82, "public.hap.service.sensor.humidity"),
    SENSOR_LEAK(0x83, "public.hap.service.sensor.leak"),
    SENSOR_LIGHT(0x84, "public.hap.service.sensor.light"),
    SENSOR_MOTION(0x85, "public.hap.service.sensor.motion"),
    SENSOR_OCCUPANCY(0x86, "public.hap.service.sensor.occupancy"),
    SENSOR_SMOKE(0x87, "public.hap.service.sensor.smoke"),
    SENSOR_TEMPERATURE(0x8A, "public.hap.service.sensor.temperature"),
    SERVICE_LABEL(0xCC, "public.hap.service.service-label"),
    SIRI(0x133, "public.hap.service.siri"),
    SPEAKER(0x113, "public.hap.service.speaker"),
    STATELESS_PROGRAMMABLE_SWITCH(0x89, "public.hap.service.stateless-programmable-switch"),
    SWITCH(0x49, "public.hap.service.switch"),
    TARGET_CONTROL(0x125, "public.hap.service.target-control"),
    TARGET_CONTROL_MANAGEMENT(0x122, "public.hap.service.target-control-management"),
    THERMOSTAT(0x4A, "public.hap.service.thermostat"),
    VALVE(0xD0, "public.hap.service.valve"),
    VERTICAL_SLAT(0xB9, "public.hap.service.vertical-slat"),
    WINDOW(0x8B, "public.hap.service.window"),
    WINDOW_COVERING(0x8C, "public.hap.service.window-covering");

    private final int id;
    private final String type;

    ServiceType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public static ServiceType from(int id) throws IllegalArgumentException {
        for (ServiceType value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ID: " + id);
    }

    public String getChannelTypeId() {
        return type.replace("-", "_").replace(".", "-"); // convert to OH channel type format
    }

    public String getType() {
        return type;
    }

    /**
     * Returns the name of the enum constant in `First Letter Capitals`.
     */
    @Override
    public String toString() {
        String[] parts = name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            builder.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return builder.toString();
    }
}
