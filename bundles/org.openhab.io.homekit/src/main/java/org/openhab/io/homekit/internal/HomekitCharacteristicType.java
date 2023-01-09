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

    ACTIVE("Active"),
    ACTIVE_IDENTIFIER("ActiveIdentifier"),
    ACTIVE_STATUS("ActiveStatus"),
    AIR_QUALITY("AirQuality"),
    BATTERY_CHARGING_STATE("BatteryChargingState"),
    BATTERY_LEVEL("BatteryLevel"),
    BATTERY_LOW_STATUS("BatteryLowStatus"),
    BRIGHTNESS("Brightness"),
    CARBON_DIOXIDE_DETECTED_STATE("CarbonDioxideDetectedState"),
    CARBON_DIOXIDE_LEVEL("CarbonDioxideLevel"),
    CARBON_DIOXIDE_PEAK_LEVEL("CarbonDioxidePeakLevel"),
    CARBON_MONOXIDE_DETECTED_STATE("CarbonMonoxideDetectedState"),
    CARBON_MONOXIDE_LEVEL("CarbonMonoxideLevel"),
    CARBON_MONOXIDE_PEAK_LEVEL("CarbonMonoxidePeakLevel"),
    CLOSED_CAPTIONS("ClosedCaptions"),
    COLOR_TEMPERATURE("ColorTemperature"),
    CONFIGURED("Configured"),
    CONFIGURED_NAME("ConfiguredName"),
    CONTACT_SENSOR_STATE("ContactSensorState"),
    COOLING_THRESHOLD_TEMPERATURE("CoolingThresholdTemperature"),
    CURRENT_DOOR_STATE("CurrentDoorState"),
    CURRENT_FAN_STATE("CurrentFanState"),
    CURRENT_HEATER_COOLER_STATE("CurrentHeaterCoolerState"),
    CURRENT_HEATING_COOLING_STATE("CurrentHeatingCoolingMode"),
    CURRENT_HORIZONTAL_TILT_ANGLE("CurrentHorizontalTiltAngle"),
    CURRENT_MEDIA_STATE("CurrentMediaState"),
    CURRENT_POSITION("CurrentPosition"),
    CURRENT_SLAT_STATE("CurrentSlatState"),
    CURRENT_TEMPERATURE("CurrentTemperature"),
    CURRENT_TILT_ANGLE("CurrentTiltAngle"),
    CURRENT_VERTICAL_TILT_ANGLE("CurrentVerticalTiltAngle"),
    CURRENT_VISIBILITY("CurrentVisibility"),
    DURATION("Duration"),
    FAULT_STATUS("FaultStatus"),
    FILTER_CHANGE_INDICATION("FilterChangeIndication"),
    FILTER_LIFE_LEVEL("FilterLifeLevel"),
    FILTER_RESET_INDICATION("FilterResetIndication"),
    FIRMWARE_REVISION("FirmwareRevision"),
    HARDWARE_REVISION("HardwareRevision"),
    HEATING_THRESHOLD_TEMPERATURE("HeatingThresholdTemperature"),
    HOLD_POSITION("HoldPosition"),
    HUE("Hue"),
    IDENTIFIER("Identifier"),
    IDENTIFY("Identify"),
    INPUT_DEVICE_TYPE("InputDeviceType"),
    INPUT_SOURCE_TYPE("InputSourceType"),
    INUSE_STATUS("InUseStatus"),
    ISCONFIGURED("IsConfigured"),
    LEAK_DETECTED_STATE("LeakDetectedState"),
    LIGHT_LEVEL("LightLevel"),
    LOCK_CONTROL("LockControl"),
    LOCK_CURRENT_STATE("LockCurrentState"),
    LOCK_TARGET_STATE("LockTargetState"),
    MANUFACTURER("Manufacturer"),
    MODEL("Model"),
    MOTION_DETECTED_STATE("MotionDetectedState"),
    MUTE("Mute"),
    NAME("Name"),
    NITROGEN_DIOXIDE_DENSITY("NitrogenDioxideDensity"),
    OBSTRUCTION_STATUS("ObstructionStatus"),
    OCCUPANCY_DETECTED_STATE("OccupancyDetectedState"),
    ON_STATE("OnState"),
    OZONE_DENSITY("OzoneDensity"),
    PICTURE_MODE("PictureMode"),
    PM10_DENSITY("PM10Density"),
    PM25_DENSITY("PM25Density"),
    POSITION_STATE("PositionState"),
    POWER_MODE("PowerMode"),
    PROGRAM_MODE("ProgramMode"),
    PROGRAMMABLE_SWITCH_EVENT("ProgrammableSwitchEvent"),
    RELATIVE_HUMIDITY("RelativeHumidity"),
    REMAINING_DURATION("RemainingDuration"),
    REMOTE_KEY("RemoteKey"),
    ROTATION_DIRECTION("RotationDirection"),
    ROTATION_SPEED("RotationSpeed"),
    SATURATION("Saturation"),
    SECURITY_SYSTEM_CURRENT_STATE("CurrentSecuritySystemState"),
    SECURITY_SYSTEM_TARGET_STATE("TargetSecuritySystemState"),
    SERIAL_NUMBER("SerialNumber"),
    SERVICE_INDEX("ServiceIndex"),
    SERVICE_LABEL("ServiceLabel"),
    SLEEP_DISCOVERY_MODE("SleepDiscoveryMode"),
    SMOKE_DETECTED_STATE("SmokeDetectedState"),
    SULPHUR_DIOXIDE_DENSITY("SulphurDioxideDensity"),
    SWING_MODE("SwingMode"),
    TAMPERED_STATUS("TamperedStatus"),
    TARGET_DOOR_STATE("TargetDoorState"),
    TARGET_FAN_STATE("TargetFanState"),
    TARGET_HEATER_COOLER_STATE("TargetHeaterCoolerState"),
    TARGET_HEATING_COOLING_STATE("TargetHeatingCoolingMode"),
    TARGET_HORIZONTAL_TILT_ANGLE("TargetHorizontalTiltAngle"),
    TARGET_MEDIA_STATE("TargetMediaState"),
    TARGET_POSITION("TargetPosition"),
    TARGET_RELATIVE_HUMIDITY("TargetRelativeHumidity"),
    TARGET_TEMPERATURE("TargetTemperature"),
    TARGET_TILT_ANGLE("TargetTiltAngle"),
    TARGET_VERTICAL_TILT_ANGLE("TargetVerticalTiltAngle"),
    TARGET_VISIBILITY_STATE("TargetVisibilityState"),
    TEMPERATURE_UNIT("TemperatureUnit"),
    VOC_DENSITY("VOCDensity"),
    VOLUME("Volume"),
    VOLUME_CONTROL_TYPE("VolumeControlType"),
    VOLUME_SELECTOR("VolumeSelector");

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
