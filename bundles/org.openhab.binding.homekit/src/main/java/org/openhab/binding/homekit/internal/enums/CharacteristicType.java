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
 * Enumeration of HomeKit characteristic types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum CharacteristicType {
    /*
     * According to the Apple specifications the type fields are fully qualified strings such
     * as "public.hap.characteristic.accessory-properties" however we do not need to use the
     * "public.hap.characteristic." prefix in this binding so for brevity it has been removed.
     */
    //@formatter:off
    ACCESSORY_PROPERTIES(0xA6, "accessory-properties"),
    ACTIVE(0xB0, "active"),
    ACTIVE_IDENTIFIER(0xE7, "active-identifier"),
    ADMINISTRATOR_ONLY_ACCESS(0x01, "administrator-only-access"),
    AIR_PARTICULATE_DENSITY(0x64, "air-particulate.density"),
    AIR_PARTICULATE_SIZE(0x65, "air-particulate.size"),
    AIR_PURIFIER_STATE_CURRENT(0xA9, "air-purifier.state.current"),
    AIR_PURIFIER_STATE_TARGET(0xA8, "air-purifier.state.target"),
    AIR_QUALITY(0x95, "air-quality"),
    AUDIO_FEEDBACK(0x05, "audio-feedback"),
    BATTERY_LEVEL(0x68, "battery-level"),
    BRIGHTNESS(0x08, "brightness"),
    BUTTON_EVENT(0x126, "button-event"),
    CARBON_DIOXIDE_DETECTED(0x92, "carbon-dioxide.detected"),
    CARBON_DIOXIDE_LEVEL(0x93, "carbon-dioxide.level"),
    CARBON_DIOXIDE_PEAK_LEVEL(0x94, "carbon-dioxide.peak-level"),
    CARBON_MONOXIDE_DETECTED(0x69, "carbon-monoxide.detected"),
    CARBON_MONOXIDE_LEVEL(0x90, "carbon-monoxide.level"),
    CARBON_MONOXIDE_PEAK_LEVEL(0x91, "carbon-monoxide.peak-level"),
    CHARGING_STATE(0x8F, "charging-state"),
    COLOR_TEMPERATURE(0xCE, "color-temperature"),
    CONTACT_STATE(0x6A, "contact-state"),
    DENSITY_NO2(0xC4, "density.no2"),
    DENSITY_OZONE(0xC3, "density.ozone"),
    DENSITY_PM10(0xC7, "density.pm10"),
    DENSITY_PM2_5(0xC6, "density.pm2_5"),
    DENSITY_SO2(0xC5, "density.so2"),
    DENSITY_VOC(0xC8, "density.voc"),
    DOOR_STATE_CURRENT(0x0E, "door-state.current"),
    DOOR_STATE_TARGET(0x32, "door-state.target"),
    FAN_STATE_CURRENT(0xAF, "fan.state.current"),
    FAN_STATE_TARGET(0xBF, "fan.state.target"),
    FILTER_CHANGE_INDICATION(0xAC, "filter.change-indication"),
    FILTER_LIFE_LEVEL(0xAB, "filter.life-level"),
    FILTER_RESET_INDICATION(0xAD, "filter.reset-indication"),
    FIRMWARE_REVISION(0x52, "firmware.revision"),
    HARDWARE_REVISION(0x53, "hardware.revision"),
    HEATER_COOLER_STATE_CURRENT(0xB1, "heater-cooler.state.current"),
    HEATER_COOLER_STATE_TARGET(0xB2, "heater-cooler.state.target"),
    HEATING_COOLING_CURRENT(0x0F, "heating-cooling.current"),
    HEATING_COOLING_TARGET(0x33, "heating-cooling.target"),
    HORIZONTAL_TILT_CURRENT(0x6C, "horizontal-tilt.current"),
    HORIZONTAL_TILT_TARGET(0x7B, "horizontal-tilt.target"),
    HUE(0x13, "hue"),
    HUMIDIFIER_DEHUMIDIFIER_STATE_CURRENT(0xB3, "humidifier-dehumidifier.state.current"),
    HUMIDIFIER_DEHUMIDIFIER_STATE_TARGET(0xB4, "humidifier-dehumidifier.state.target"),
    IDENTIFY(0x14, "identify"),
    IMAGE_MIRROR(0x11F, "image-mirror"),
    IMAGE_ROTATION(0x11E, "image-rotation"),
    IN_USE(0xD2, "in-use"),
    INPUT_EVENT(0x73, "input-event"),
    IS_CONFIGURED(0xD6, "is-configured"),
    LEAK_DETECTED(0x70, "leak-detected"),
    LIGHT_LEVEL_CURRENT(0x6B, "light-level.current"),
    LOCK_MANAGEMENT_AUTO_SECURE_TIMEOUT(0x1A, "lock-management.auto-secure-timeout"),
    LOCK_MANAGEMENT_CONTROL_POINT(0x19, "lock-management.control-point"),
    LOCK_MECHANISM_CURRENT_STATE(0x1D, "lock-mechanism.current-state"),
    LOCK_MECHANISM_LAST_KNOWN_ACTION(0x1C, "lock-mechanism.last-known-action"),
    LOCK_MECHANISM_TARGET_STATE(0x1E, "lock-mechanism.target-state"),
    LOCK_PHYSICAL_CONTROLS(0xA7, "lock-physical-controls"),
    LOGS(0x1F, "logs"),
    MANUFACTURER(0x20, "manufacturer"),
    MODEL(0x21, "model"),
    MOTION_DETECTED(0x22, "motion-detected"),
    MUTE(0x11A, "mute"),
    NAME(0x23, "name"),
    NIGHT_VISION(0x11B, "night-vision"),
    OBSTRUCTION_DETECTED(0x24, "obstruction-detected"),
    OCCUPANCY_DETECTED(0x71, "occupancy-detected"),
    ON(0x25, "on"),
    OUTLET_IN_USE(0x26, "outlet-in-use"),
    PAIRING_FEATURES(0x4F, "pairing.features"),
    PAIRING_PAIR_SETUP(0x4C, "pairing.pair-setup"),
    PAIRING_PAIR_VERIFY(0x4E, "pairing.pair-verify"),
    PAIRING_PAIRINGS(0x50, "pairing.pairings"),
    POSITION_CURRENT(0x6D, "position.current"),
    POSITION_HOLD(0x6F, "position.hold"),
    POSITION_STATE(0x72, "position.state"),
    POSITION_TARGET(0x7C, "position.target"),
    PROGRAM_MODE(0xD1, "program-mode"),
    RELATIVE_HUMIDITY_CURRENT(0x10, "relative-humidity.current"),
    RELATIVE_HUMIDITY_DEHUMIDIFIER_THRESHOLD(0xC9, "relative-humidity.dehumidifier-threshold"),
    RELATIVE_HUMIDITY_HUMIDIFIER_THRESHOLD(0xCA, "relative-humidity.humidifier-threshold"),
    RELATIVE_HUMIDITY_TARGET(0x34, "relative-humidity.target"),
    REMAINING_DURATION(0xD4, "remaining-duration"),
    ROTATION_DIRECTION(0x28, "rotation.direction"),
    ROTATION_SPEED(0x29, "rotation.speed"),
    SATURATION(0x2F, "saturation"),
    SECURITY_SYSTEM_ALARM_TYPE(0x8E, "security-system.alarm-type"),
    SECURITY_SYSTEM_STATE_CURRENT(0x66, "security-system-state.current"),
    SECURITY_SYSTEM_STATE_TARGET(0x67, "security-system-state.target"),
    SELECTED_AUDIO_STREAM_CONFIGURATION(0x128, "selected-audio-stream-configuration"),
    SELECTED_RTP_STREAM_CONFIGURATION(0x117, "selected-rtp-stream-configuration"),
    SERIAL_NUMBER(0x30, "serial-number"),
    SERVICE_LABEL_INDEX(0xCB, "service-label-index"),
    SERVICE_LABEL_NAMESPACE(0xCD, "service-label-namespace"),
    SET_DURATION(0xD3, "set-duration"),
    SETUP_DATA_STREAM_TRANSPORT(0x131, "setup-data-stream-transport"),
    SETUP_ENDPOINTS(0x118, "setup-endpoints"),
    SIRI_INPUT_TYPE(0x132, "siri-input-type"),
    SLAT_STATE_CURRENT(0xAA, "slat.state.current"),
    SMOKE_DETECTED(0x76, "smoke-detected"),
    STATUS_ACTIVE(0x75, "status-active"),
    STATUS_FAULT(0x77, "status-fault"),
    STATUS_JAMMED(0x78, "status-jammed"),
    STATUS_LO_BATT(0x79, "status-lo-batt"),
    STATUS_TAMPERED(0x7A, "status-tampered"),
    STREAMING_STATUS(0x120, "streaming-status"),
    SUPPORTED_AUDIO_CONFIGURATION(0x115, "supported-audio-configuration"),
    SUPPORTED_DATA_STREAM_TRANSPORT_CONFIGURATION(0x130, "supported-data-stream-transport-configuration"),
    SUPPORTED_RTP_CONFIGURATION(0x116, "supported-rtp-configuration"),
    SUPPORTED_TARGET_CONFIGURATION(0x123, "supported-target-configuration"),
    SUPPORTED_VIDEO_STREAM_CONFIGURATION(0x114, "supported-video-stream-configuration"),
    SWING_MODE(0xB6, "swing-mode"),
    TARGET_LIST(0x124, "target-list"),
    TEMPERATURE_COOLING_THRESHOLD(0x0D, "temperature.cooling-threshold"),
    TEMPERATURE_CURRENT(0x11, "temperature.current"),
    TEMPERATURE_HEATING_THRESHOLD(0x12, "temperature.heating-threshold"),
    TEMPERATURE_TARGET(0x35, "temperature.target"),
    TEMPERATURE_UNITS(0x36, "temperature.units"),
    TILT_CURRENT(0xC1, "tilt.current"),
    TILT_TARGET(0xC2, "tilt.target"),
    TYPE_SLAT(0xC0, "type.slat"),
    VALVE_TYPE(0xD5, "valve-type"),
    VERSION(0x37, "version"),
    VERTICAL_TILT_CURRENT(0x6E, "vertical-tilt.current"),
    VERTICAL_TILT_TARGET(0x7D, "vertical-tilt.target"),
    VOLUME(0x119, "volume"),
    WATER_LEVEL(0xB5, "water-level"),
    ZOOM_DIGITAL(0x11D, "zoom-digital"),
    ZOOM_OPTICAL(0x11C, "zoom-optical"),
    // placeholder for any custom or unsupported characteristic
    CUSTOM_CXX(0xFF, "custom");
    //@formatter:on

    private final int id;
    private final String type;

    CharacteristicType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public static CharacteristicType from(int id) throws IllegalArgumentException {
        for (CharacteristicType value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ID: " + id);
    }

    /**
     * Returns OH type id being a shortened version of the full Homekit type id. e.g. ZOOM_DIGITAL -> zoom-digital
     */
    public String getOpenhabType() {
        return type.replace(".", "-"); // convert to OH channel type format
    }

    /**
     * Returns the full Homekit type id. e.g. ZOOM_DIGITAL -> public.hap.characteristic.zoom-digital
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the name of the enum constant in Title Case. e.g. ZOOM_DIGITAL -> Zoom Digital
     */
    @Override
    public String toString() {
        return Arrays.stream(name().split("_")).map(
                word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Returns the name of the enum constant in Camel Case. e.g. ZOOM_DIGITAL -> zoomDigital
     */
    public String toCamelCase() {
        String[] parts = name().split("_");
        StringBuilder camelCase = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (!part.isEmpty()) {
                camelCase.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return camelCase.toString();
    }
}
