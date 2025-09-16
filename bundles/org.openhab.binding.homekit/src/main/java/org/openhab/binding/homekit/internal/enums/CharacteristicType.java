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
 * Enumeration of HomeKit characteristic types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum CharacteristicType {
    //@formatter:off
    ACCESSORY_PROPERTIES(0xA6, "public.hap.characteristic.accessory-properties"),
    ACTIVE(0xB0, "public.hap.characteristic.active"),
    ACTIVE_IDENTIFIER(0xE7, "public.hap.characteristic.active-identifier"),
    ADMINISTRATOR_ONLY_ACCESS(0x01, "public.hap.characteristic.administrator-only-access"),
    AIR_PARTICULATE_DENSITY(0x64, "public.hap.characteristic.air-particulate.density"),
    AIR_PARTICULATE_SIZE(0x65, "public.hap.characteristic.air-particulate.size"),
    AIR_PURIFIER_STATE_CURRENT(0xA9, "public.hap.characteristic.air-purifier.state.current"),
    AIR_PURIFIER_STATE_TARGET(0xA8, "public.hap.characteristic.air-purifier.state.target"),
    AIR_QUALITY(0x95, "public.hap.characteristic.air-quality"),
    AUDIO_FEEDBACK(0x05, "public.hap.characteristic.audio-feedback"),
    BATTERY_LEVEL(0x68, "public.hap.characteristic.battery-level"),
    BRIGHTNESS(0x08, "public.hap.characteristic.brightness"),
    BUTTON_EVENT(0x126, "public.hap.characteristic.button-event"),
    CARBON_DIOXIDE_DETECTED(0x92, "public.hap.characteristic.carbon-dioxide.detected"),
    CARBON_DIOXIDE_LEVEL(0x93, "public.hap.characteristic.carbon-dioxide.level"),
    CARBON_DIOXIDE_PEAK_LEVEL(0x94, "public.hap.characteristic.carbon-dioxide.peak-level"),
    CARBON_MONOXIDE_DETECTED(0x69, "public.hap.characteristic.carbon-monoxide.detected"),
    CARBON_MONOXIDE_LEVEL(0x90, "public.hap.characteristic.carbon-monoxide.level"),
    CARBON_MONOXIDE_PEAK_LEVEL(0x91, "public.hap.characteristic.carbon-monoxide.peak-level"),
    CHARGING_STATE(0x8F, "public.hap.characteristic.charging-state"),
    COLOR_TEMPERATURE(0xCE, "public.hap.characteristic.color-temperature"),
    CONTACT_STATE(0x6A, "public.hap.characteristic.contact-state"),
    DENSITY_NO2(0xC4, "public.hap.characteristic.density.no2"),
    DENSITY_OZONE(0xC3, "public.hap.characteristic.density.ozone"),
    DENSITY_PM10(0xC7, "public.hap.characteristic.density.pm10"),
    DENSITY_PM2_5(0xC6, "public.hap.characteristic.density.pm2_5"),
    DENSITY_SO2(0xC5, "public.hap.characteristic.density.so2"),
    DENSITY_VOC(0xC8, "public.hap.characteristic.density.voc"),
    DOOR_STATE_CURRENT(0x0E, "public.hap.characteristic.door-state.current"),
    DOOR_STATE_TARGET(0x32, "public.hap.characteristic.door-state.target"),
    FAN_STATE_CURRENT(0xAF, "public.hap.characteristic.fan.state.current"),
    FAN_STATE_TARGET(0xBF, "public.hap.characteristic.fan.state.target"),
    FILTER_CHANGE_INDICATION(0xAC, "public.hap.characteristic.filter.change-indication"),
    FILTER_LIFE_LEVEL(0xAB, "public.hap.characteristic.filter.life-level"),
    FILTER_RESET_INDICATION(0xAD, "public.hap.characteristic.filter.reset-indication"),
    FIRMWARE_REVISION(0x52, "public.hap.characteristic.firmware.revision"),
    HARDWARE_REVISION(0x53, "public.hap.characteristic.hardware.revision"),
    HEATER_COOLER_STATE_CURRENT(0xB1, "public.hap.characteristic.heater-cooler.state.current"),
    HEATER_COOLER_STATE_TARGET(0xB2, "public.hap.characteristic.heater-cooler.state.target"),
    HEATING_COOLING_CURRENT(0x0F, "public.hap.characteristic.heating-cooling.current"),
    HEATING_COOLING_TARGET(0x33, "public.hap.characteristic.heating-cooling.target"),
    HORIZONTAL_TILT_CURRENT(0x6C, "public.hap.characteristic.horizontal-tilt.current"),
    HORIZONTAL_TILT_TARGET(0x7B, "public.hap.characteristic.horizontal-tilt.target"),
    HUE(0x13, "public.hap.characteristic.hue"),
    HUMIDIFIER_DEHUMIDIFIER_STATE_CURRENT(0xB3, "public.hap.characteristic.humidifier-dehumidifier.state.current"),
    HUMIDIFIER_DEHUMIDIFIER_STATE_TARGET(0xB4, "public.hap.characteristic.humidifier-dehumidifier.state.target"),
    IDENTIFY(0x14, "public.hap.characteristic.identify"),
    IMAGE_MIRROR(0x11F, "public.hap.characteristic.image-mirror"),
    IMAGE_ROTATION(0x11E, "public.hap.characteristic.image-rotation"),
    IN_USE(0xD2, "public.hap.characteristic.in-use"),
    INPUT_EVENT(0x73, "public.hap.characteristic.input-event"),
    IS_CONFIGURED(0xD6, "public.hap.characteristic.is-configured"),
    LEAK_DETECTED(0x70, "public.hap.characteristic.leak-detected"),
    LIGHT_LEVEL_CURRENT(0x6B, "public.hap.characteristic.light-level.current"),
    LOCK_MANAGEMENT_AUTO_SECURE_TIMEOUT(0x1A, "public.hap.characteristic.lock-management.auto-secure-timeout"),
    LOCK_MANAGEMENT_CONTROL_POINT(0x19, "public.hap.characteristic.lock-management.control-point"),
    LOCK_MECHANISM_CURRENT_STATE(0x1D, "public.hap.characteristic.lock-mechanism.current-state"),
    LOCK_MECHANISM_LAST_KNOWN_ACTION(0x1C, "public.hap.characteristic.lock-mechanism.last-known-action"),
    LOCK_MECHANISM_TARGET_STATE(0x1E, "public.hap.characteristic.lock-mechanism.target-state"),
    LOCK_PHYSICAL_CONTROLS(0xA7, "public.hap.characteristic.lock-physical-controls"),
    LOGS(0x1F, "public.hap.characteristic.logs"),
    MANUFACTURER(0x20, "public.hap.characteristic.manufacturer"),
    MODEL(0x21, "public.hap.characteristic.model"),
    MOTION_DETECTED(0x22, "public.hap.characteristic.motion-detected"),
    MUTE(0x11A, "public.hap.characteristic.mute"),
    NAME(0x23, "public.hap.characteristic.name"),
    NIGHT_VISION(0x11B, "public.hap.characteristic.night-vision"),
    OBSTRUCTION_DETECTED(0x24, "public.hap.characteristic.obstruction-detected"),
    OCCUPANCY_DETECTED(0x71, "public.hap.characteristic.occupancy-detected"),
    ON(0x25, "public.hap.characteristic.on"),
    OUTLET_IN_USE(0x26, "public.hap.characteristic.outlet-in-use"),
    PAIRING_FEATURES(0x4F, "public.hap.characteristic.pairing.features"),
    PAIRING_PAIR_SETUP(0x4C, "public.hap.characteristic.pairing.pair-setup"),
    PAIRING_PAIR_VERIFY(0x4E, "public.hap.characteristic.pairing.pair-verify"),
    PAIRING_PAIRINGS(0x50, "public.hap.characteristic.pairing.pairings"),
    POSITION_CURRENT(0x6D, "public.hap.characteristic.position.current"),
    POSITION_HOLD(0x6F, "public.hap.characteristic.position.hold"),
    POSITION_STATE(0x72, "public.hap.characteristic.position.state"),
    POSITION_TARGET(0x7C, "public.hap.characteristic.position.target"),
    PROGRAM_MODE(0xD1, "public.hap.characteristic.program-mode"),
    RELATIVE_HUMIDITY_CURRENT(0x10, "public.hap.characteristic.relative-humidity.current"),
    RELATIVE_HUMIDITY_DEHUMIDIFIER_THRESHOLD(0xC9, "public.hap.characteristic.relative-humidity.dehumidifier-threshold"),
    RELATIVE_HUMIDITY_HUMIDIFIER_THRESHOLD(0xCA, "public.hap.characteristic.relative-humidity.humidifier-threshold"),
    RELATIVE_HUMIDITY_TARGET(0x34, "public.hap.characteristic.relative-humidity.target"),
    REMAINING_DURATION(0xD4, "public.hap.characteristic.remaining-duration"),
    ROTATION_DIRECTION(0x28, "public.hap.characteristic.rotation.direction"),
    ROTATION_SPEED(0x29, "public.hap.characteristic.rotation.speed"),
    SATURATION(0x2F, "public.hap.characteristic.saturation"),
    SECURITY_SYSTEM_ALARM_TYPE(0x8E, "public.hap.characteristic.security-system.alarm-type"),
    SECURITY_SYSTEM_STATE_CURRENT(0x66, "public.hap.characteristic.security-system-state.current"),
    SECURITY_SYSTEM_STATE_TARGET(0x67, "public.hap.characteristic.security-system-state.target"),
    SELECTED_AUDIO_STREAM_CONFIGURATION(0x128, "public.hap.characteristic.selected-audio-stream-configuration"),
    SELECTED_RTP_STREAM_CONFIGURATION(0x117, "public.hap.characteristic.selected-rtp-stream-configuration"),
    SERIAL_NUMBER(0x30, "public.hap.characteristic.serial-number"),
    SERVICE_LABEL_INDEX(0xCB, "public.hap.characteristic.service-label-index"),
    SERVICE_LABEL_NAMESPACE(0xCD, "public.hap.characteristic.service-label-namespace"),
    SET_DURATION(0xD3, "public.hap.characteristic.set-duration"),
    SETUP_DATA_STREAM_TRANSPORT(0x131, "public.hap.characteristic.setup-data-stream-transport"),
    SETUP_ENDPOINTS(0x118, "public.hap.characteristic.setup-endpoints"),
    SIRI_INPUT_TYPE(0x132, "public.hap.characteristic.siri-input-type"),
    SLAT_STATE_CURRENT(0xAA, "public.hap.characteristic.slat.state.current"),
    SMOKE_DETECTED(0x76, "public.hap.characteristic.smoke-detected"),
    STATUS_ACTIVE(0x75, "public.hap.characteristic.status-active"),
    STATUS_FAULT(0x77, "public.hap.characteristic.status-fault"),
    STATUS_JAMMED(0x78, "public.hap.characteristic.status-jammed"),
    STATUS_LO_BATT(0x79, "public.hap.characteristic.status-lo-batt"),
    STATUS_TAMPERED(0x7A, "public.hap.characteristic.status-tampered"),
    STREAMING_STATUS(0x120, "public.hap.characteristic.streaming-status"),
    SUPPORTED_AUDIO_CONFIGURATION(0x115, "public.hap.characteristic.supported-audio-configuration"),
    SUPPORTED_DATA_STREAM_TRANSPORT_CONFIGURATION(0x130, "public.hap.characteristic.supported-data-stream-transport-configuration"),
    SUPPORTED_RTP_CONFIGURATION(0x116, "public.hap.characteristic.supported-rtp-configuration"),
    SUPPORTED_TARGET_CONFIGURATION(0x123, "public.hap.characteristic.supported-target-configuration"),
    SUPPORTED_VIDEO_STREAM_CONFIGURATION(0x114, "public.hap.characteristic.supported-video-stream-configuration"),
    SWING_MODE(0xB6, "public.hap.characteristic.swing-mode"),
    TARGET_LIST(0x124, "public.hap.characteristic.target-list"),
    TEMPERATURE_COOLING_THRESHOLD(0x0D, "public.hap.characteristic.temperature.cooling-threshold"),
    TEMPERATURE_CURRENT(0x11, "public.hap.characteristic.temperature.current"),
    TEMPERATURE_HEATING_THRESHOLD(0x12, "public.hap.characteristic.temperature.heating-threshold"),
    TEMPERATURE_TARGET(0x35, "public.hap.characteristic.temperature.target"),
    TEMPERATURE_UNITS(0x36, "public.hap.characteristic.temperature.units"),
    TILT_CURRENT(0xC1, "public.hap.characteristic.tilt.current"),
    TILT_TARGET(0xC2, "public.hap.characteristic.tilt.target"),
    TYPE_SLAT(0xC0, "public.hap.characteristic.type.slat"),
    VALVE_TYPE(0xD5, "public.hap.characteristic.valve-type"),
    VERSION(0x37, "public.hap.characteristic.version"),
    VERTICAL_TILT_CURRENT(0x6E, "public.hap.characteristic.vertical-tilt.current"),
    VERTICAL_TILT_TARGET(0x7D, "public.hap.characteristic.vertical-tilt.target"),
    VOLUME(0x119, "public.hap.characteristic.volume"),
    WATER_LEVEL(0xB5, "public.hap.characteristic.water-level"),
    ZOOM_DIGITAL(0x11D, "public.hap.characteristic.zoom-digital"),
    ZOOM_OPTICAL(0x11C, "public.hap.characteristic.zoom-optical");
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

    public String getGroupTypeId() {
        return type.replace("-", "_").replace(".", "-"); // convert to OH channel-group-type format
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
