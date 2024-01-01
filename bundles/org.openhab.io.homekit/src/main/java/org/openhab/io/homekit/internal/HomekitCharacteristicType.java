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
 * Characteristics are used by complex accessories that can't be represented by
 * a single item (i.e. a thermostat)
 *
 * @author Andy Lintner - Initial contribution
 */
public enum HomekitCharacteristicType {
    /*
     * It is illegal to have a characteristic type also be a device type
     */
    EMPTY("Empty"), // used in case only accessory type but no characteristic provided
    NAME("Name"),
    BATTERY_LOW_STATUS("BatteryLowStatus"),
    ACTIVE_STATUS("ActiveStatus"),
    ISCONFIGURED("IsConfigured"),
    SERVICELABELIDX("ServiceLabelIndex"),
    INUSE_STATUS("InUseStatus"),
    FAULT_STATUS("FaultStatus"),
    TAMPERED_STATUS("TamperedStatus"),
    OBSTRUCTION_STATUS("ObstructionStatus"),
    ON_STATE("OnState"),
    CONTACT_SENSOR_STATE("ContactSensorState"),

    MOTION_DETECTED_STATE("MotionDetectedState"),
    OCCUPANCY_DETECTED_STATE("OccupancyDetectedState"),
    SMOKE_DETECTED_STATE("SmokeDetectedState"),

    CARBON_MONOXIDE_DETECTED_STATE("CarbonMonoxideDetectedState"),
    CARBON_MONOXIDE_LEVEL("CarbonMonoxideLevel"),
    CARBON_MONOXIDE_PEAK_LEVEL("CarbonMonoxidePeakLevel"),

    CARBON_DIOXIDE_DETECTED_STATE("CarbonDioxideDetectedState"),
    CARBON_DIOXIDE_LEVEL("CarbonDioxideLevel"),
    CARBON_DIOXIDE_PEAK_LEVEL("CarbonDioxidePeakLevel"),

    RELATIVE_HUMIDITY("RelativeHumidity"),
    LEAK_DETECTED_STATE("LeakDetectedState"),
    HOLD_POSITION("HoldPosition"),

    TARGET_POSITION("TargetPosition"),
    CURRENT_POSITION("CurrentPosition"),
    POSITION_STATE("PositionState"),
    CURRENT_HORIZONTAL_TILT_ANGLE("CurrentHorizontalTiltAngle"),
    CURRENT_VERTICAL_TILT_ANGLE("CurrentVerticalTiltAngle"),
    TARGET_HORIZONTAL_TILT_ANGLE("TargetHorizontalTiltAngle"),
    TARGET_VERTICAL_TILT_ANGLE("TargetVerticalTiltAngle"),
    CURRENT_TILT_ANGLE("CurrentTiltAngle"),
    TARGET_TILT_ANGLE("TargetTiltAngle"),

    HUE("Hue"),
    BRIGHTNESS("Brightness"),
    SATURATION("Saturation"),
    COLOR_TEMPERATURE("ColorTemperature"),

    CURRENT_FAN_STATE("CurrentFanState"),
    TARGET_FAN_STATE("TargetFanState"),
    ROTATION_DIRECTION("RotationDirection"),
    ROTATION_SPEED("RotationSpeed"),
    SWING_MODE("SwingMode"),
    LOCK_CONTROL("LockControl"),

    CURRENT_TEMPERATURE("CurrentTemperature"),
    TARGET_HEATING_COOLING_STATE("TargetHeatingCoolingMode"),
    CURRENT_HEATING_COOLING_STATE("CurrentHeatingCoolingMode"),
    TARGET_TEMPERATURE("TargetTemperature"),
    TEMPERATURE_UNIT("TemperatureUnit"),

    LOCK_CURRENT_STATE("LockCurrentState"),
    LOCK_TARGET_STATE("LockTargetState"),

    DURATION("Duration"),
    REMAINING_DURATION("RemainingDuration"),

    SECURITY_SYSTEM_CURRENT_STATE("CurrentSecuritySystemState"),
    SECURITY_SYSTEM_TARGET_STATE("TargetSecuritySystemState"),

    VOLUME("Volume"),
    MUTE("Mute"),
    LIGHT_LEVEL("LightLevel"),

    CURRENT_DOOR_STATE("CurrentDoorState"),
    TARGET_DOOR_STATE("TargetDoorState"),
    TARGET_HEATER_COOLER_STATE("TargetHeaterCoolerState"),
    CURRENT_HEATER_COOLER_STATE("CurrentHeaterCoolerState"),
    COOLING_THRESHOLD_TEMPERATURE("CoolingThresholdTemperature"),
    HEATING_THRESHOLD_TEMPERATURE("HeatingThresholdTemperature"),

    AIR_QUALITY("AirQuality"),
    OZONE_DENSITY("OzoneDensity"),
    NITROGEN_DIOXIDE_DENSITY("NitrogenDioxideDensity"),
    SULPHUR_DIOXIDE_DENSITY("SulphurDioxideDensity"),
    PM25_DENSITY("PM25Density"),
    PM10_DENSITY("PM10Density"),
    VOC_DENSITY("VOCDensity"),

    BATTERY_LEVEL("BatteryLevel"),
    BATTERY_CHARGING_STATE("BatteryChargingState"),

    CURRENT_SLAT_STATE("CurrentSlatState"),

    CURRENT_MEDIA_STATE("CurrentMediaState"),
    TARGET_MEDIA_STATE("TargetMediaState"),
    CONFIGURED_NAME("ConfiguredName"),

    ACTIVE("Active"),

    FILTER_CHANGE_INDICATION("FilterChangeIndication"),
    FILTER_LIFE_LEVEL("FilterLifeLevel"),
    FILTER_RESET_INDICATION("FilterResetIndication"),

    ACTIVE_IDENTIFIER("ActiveIdentifier"),
    REMOTE_KEY("RemoteKey"),
    SLEEP_DISCOVERY_MODE("SleepDiscoveryMode"),
    POWER_MODE("PowerMode"),
    CLOSED_CAPTIONS("ClosedCaptions"),
    PICTURE_MODE("PictureMode"),

    CONFIGURED("Configured"),
    INPUT_SOURCE_TYPE("InputSourceType"),
    CURRENT_VISIBILITY("CurrentVisibility"),
    IDENTIFIER("Identifier"),
    INPUT_DEVICE_TYPE("InputDeviceType"),
    TARGET_VISIBILITY_STATE("TargetVisibilityState"),

    VOLUME_SELECTOR("VolumeSelector"),
    VOLUME_CONTROL_TYPE("VolumeControlType"),

    PROGRAM_MODE("ProgramMode"),
    SERVICE_LABEL("ServiceLabel"),
    SERVICE_INDEX("ServiceIndex");

    private static final Map<String, HomekitCharacteristicType> TAG_MAP = new HashMap<>();

    static {
        for (HomekitCharacteristicType type : HomekitCharacteristicType.values()) {
            TAG_MAP.put(type.tag, type);
        }
    }

    private final String tag;

    private HomekitCharacteristicType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    /**
     * get characteristicType from String
     *
     * @param tag the tag string
     * @return characteristicType or null if not found
     */
    public static Optional<HomekitCharacteristicType> valueOfTag(String tag) {
        return Optional.ofNullable(TAG_MAP.get(tag));
    }

    @Override
    public String toString() {
        return tag;
    }
}
