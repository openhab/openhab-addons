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
package org.openhab.binding.nest.internal.sdm.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.annotations.SerializedName;

/**
 * The common SDM traits that are used in the {@link SDMDevice} and {@link SDMEvent} types.
 *
 * @author Wouter Born - Initial contribution
 */
public class SDMTraits {

    /**
     * This trait belongs to any device that supports generation of images from events.
     */
    public static class SDMCameraEventImageTrait extends SDMCameraTrait {
    }

    /**
     * This trait belongs to any device that supports taking images.
     */
    public static class SDMCameraImageTrait extends SDMCameraTrait {
        /**
         * Maximum image resolution that is supported.
         */
        public SDMResolution maxImageResolution;
    }

    /**
     * This trait belongs to any device that supports live streaming.
     */
    public static class SDMCameraLiveStreamTrait extends SDMCameraTrait {
        /**
         * Maximum resolution of the video live stream.
         */
        public SDMResolution maxVideoResolution;

        /**
         * Video codecs supported for the live stream.
         */
        public List<String> videoCodecs;

        /**
         * Audio codecs supported for the live stream.
         */
        public List<String> audioCodecs;

        /**
         * Protocols supported for the live stream.
         */
        public List<String> supportedProtocols;
    }

    /**
     * This trait belongs to any device that supports motion detection events.
     */
    public static class SDMCameraMotionTrait extends SDMCameraTrait {
    }

    /**
     * This trait belongs to any device that supports person detection events.
     */
    public static class SDMCameraPersonTrait extends SDMCameraTrait {
    }

    /**
     * This trait belongs to any device that supports sound detection events.
     */
    public static class SDMCameraSoundTrait extends SDMCameraTrait {
    }

    public static class SDMCameraTrait extends SDMTrait {
    }

    public enum SDMConnectivityStatus {
        OFFLINE,
        ONLINE
    }

    /**
     * This trait belongs to any device that has connectivity information.
     */
    public static class SDMConnectivityTrait extends SDMDeviceTrait {
        /**
         * Device connectivity status.
         */
        public SDMConnectivityStatus status;
    }

    /**
     * This trait belongs to any device for device-related information.
     */
    public static class SDMDeviceInfoTrait extends SDMDeviceTrait {
        /**
         * Custom name of the device. Corresponds to the Label value for a device in the Nest App.
         */
        public String customName;
    }

    /**
     * This trait belongs to any device for device-related settings information.
     */
    public static class SDMDeviceSettingsTrait extends SDMDeviceTrait {
        /**
         * Format of the degrees displayed on a Google Nest Thermostat.
         */
        public SDMTemperatureScale temperatureScale;
    }

    public static class SDMDeviceTrait extends SDMTrait {
    }

    /**
     * This trait belongs to any device that supports a doorbell chime and related press events.
     */
    public static class SDMDoorbellChimeTrait extends SDMDoorbellTrait {
    }

    public static class SDMDoorbellTrait extends SDMTrait {
    }

    public enum SDMThermostatEcoMode {
        MANUAL_ECO,
        OFF
    }

    /**
     * This trait belongs to any device that has the system ability to control the fan.
     */
    public static class SDMFanTrait extends SDMDeviceTrait {
        /**
         * Current timer mode.
         */
        public SDMFanTimerMode timerMode;

        /**
         * Timestamp, in RFC 3339 format, at which timer mode will turn to OFF.
         */
        public ZonedDateTime timerTimeout;
    }

    /**
     * This trait belongs to any device that has a sensor to measure humidity.
     */
    public static class SDMHumidityTrait extends SDMDeviceTrait {
        /**
         * Percent humidity, measured at the device.
         */
        public BigDecimal ambientHumidityPercent;
    }

    public enum SDMHvacStatus {
        OFF,
        HEATING,
        COOLING
    }

    public static class SDMResolution {
        /**
         * Maximum image resolution width.
         */
        public int width;

        /**
         * Maximum image resolution height.
         */
        public int height;
    }

    /**
     * This trait belongs to any room for room-related information.
     */
    public static class SDMRoomInfoTrait extends SDMStructureTrait {
        /**
         * Custom name of the room. Corresponds to the name in the Google Home App.
         */
        public String customName;
    }

    /**
     * This trait belongs to any structure for structure-related information.
     */
    public static class SDMStructureInfoTrait extends SDMStructureTrait {
        /**
         * Custom name of the structure. Corresponds to the name in the Google Home App.
         */
        public String customName;
    }

    public static class SDMStructureTrait extends SDMTrait {
    }

    public enum SDMTemperatureScale {
        CELSIUS,
        FAHRENHEIT;
    }

    /**
     * This trait belongs to any device that has a sensor to measure temperature.
     */
    public static class SDMTemperatureTrait extends SDMDeviceTrait {
        /**
         * Temperature in degrees Celsius, measured at the device.
         */
        public BigDecimal ambientTemperatureCelsius;
    }

    /**
     * This trait belongs to device types of THERMOSTAT that support ECO modes.
     */
    public static class SDMThermostatEcoTrait extends SDMThermostatTrait {
        /**
         * List of supported Eco modes.
         */
        public List<SDMThermostatEcoMode> availableModes;

        /**
         * The current Eco mode of the thermostat.
         */
        public SDMThermostatEcoMode mode;

        /**
         * Lowest temperature in Celsius at which the thermostat begins heating in Eco mode.
         */
        public BigDecimal heatCelsius;

        /**
         * Highest temperature in Celsius at which the thermostat begins cooling in Eco mode.
         */
        public BigDecimal coolCelsius;
    }

    /**
     * This trait belongs to device types of THERMOSTAT that can report HVAC details.
     */
    public static class SDMThermostatHvacTrait extends SDMThermostatTrait {
        /**
         * Current HVAC status of the thermostat.
         */
        public SDMHvacStatus status;
    }

    public enum SDMThermostatMode {
        HEAT,
        COOL,
        HEATCOOL,
        OFF
    }

    /**
     * This trait belongs to device types of THERMOSTAT that support different thermostat modes.
     */
    public static class SDMThermostatModeTrait extends SDMThermostatTrait {
        /**
         * List of supported thermostat modes.
         */
        public List<SDMThermostatMode> availableModes;

        /**
         * The current thermostat mode.
         */
        public SDMThermostatMode mode;
    }

    /**
     * This trait belongs to device types of THERMOSTAT that support setting target temperature and temperature range.
     */
    public static class SDMThermostatTemperatureSetpointTrait extends SDMThermostatTrait {
        /**
         * Target temperature in Celsius for thermostat HEAT and HEATCOOL modes.
         */
        public BigDecimal heatCelsius;

        /**
         * Target temperature in Celsius for thermostat COOL and HEATCOOL modes.
         */
        public BigDecimal coolCelsius;
    }

    public static class SDMThermostatTrait extends SDMTrait {
    }

    public enum SDMFanTimerMode {
        ON,
        OFF
    }

    public static class SDMTrait {
    }

    @SerializedName("sdm.devices.traits.CameraEventImage")
    public SDMCameraEventImageTrait cameraEventImage;

    @SerializedName("sdm.devices.traits.CameraImage")
    public SDMCameraImageTrait cameraImage;

    @SerializedName("sdm.devices.traits.CameraLiveStream")
    public SDMCameraLiveStreamTrait cameraLiveStream;

    @SerializedName("sdm.devices.traits.CameraMotion")
    public SDMCameraMotionTrait cameraMotion;

    @SerializedName("sdm.devices.traits.CameraPerson")
    public SDMCameraPersonTrait cameraPerson;

    @SerializedName("sdm.devices.traits.CameraSound")
    public SDMCameraSoundTrait cameraSound;

    @SerializedName("sdm.devices.traits.Connectivity")
    public SDMConnectivityTrait connectivity;

    @SerializedName("sdm.devices.traits.DoorbellChime")
    public SDMDoorbellChimeTrait doorbellChime;

    @SerializedName("sdm.devices.traits.Fan")
    public SDMFanTrait fan;

    @SerializedName("sdm.devices.traits.Humidity")
    public SDMHumidityTrait humidity;

    @SerializedName("sdm.devices.traits.Info")
    public SDMDeviceInfoTrait deviceInfo;

    @SerializedName("sdm.devices.traits.Settings")
    public SDMDeviceSettingsTrait deviceSettings;

    @SerializedName("sdm.devices.traits.Temperature")
    public SDMTemperatureTrait temperature;

    @SerializedName("sdm.devices.traits.ThermostatEco")
    public SDMThermostatEcoTrait thermostatEco;

    @SerializedName("sdm.devices.traits.ThermostatHvac")
    public SDMThermostatHvacTrait thermostatHvac;

    @SerializedName("sdm.devices.traits.ThermostatMode")
    public SDMThermostatModeTrait thermostatMode;

    @SerializedName("sdm.devices.traits.ThermostatTemperatureSetpoint")
    public SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint;

    @SerializedName("sdm.structures.traits.Info")
    public SDMStructureInfoTrait structureInfo;

    @SerializedName("sdm.structures.traits.RoomInfo")
    public SDMRoomInfoTrait roomInfo;

    public <T> Stream<SDMTrait> traitStream() {
        return Stream.of(cameraEventImage, cameraImage, cameraLiveStream, cameraMotion, cameraPerson, cameraSound,
                connectivity, doorbellChime, fan, humidity, deviceInfo, deviceSettings, temperature, thermostatEco,
                thermostatHvac, thermostatMode, thermostatTemperatureSetpoint, structureInfo, roomInfo)
                .filter(Objects::nonNull);
    }

    public List<SDMTrait> traitList() {
        return traitStream().collect(Collectors.toList());
    }

    public Set<SDMTrait> traitSet() {
        return traitStream().collect(Collectors.toSet());
    }

    public void updateTraits(SDMTraits other) {
        if (other.cameraEventImage != null) {
            cameraEventImage = other.cameraEventImage;
        }
        if (other.cameraImage != null) {
            cameraImage = other.cameraImage;
        }
        if (other.cameraLiveStream != null) {
            cameraLiveStream = other.cameraLiveStream;
        }
        if (other.cameraMotion != null) {
            cameraMotion = other.cameraMotion;
        }
        if (other.cameraPerson != null) {
            cameraPerson = other.cameraPerson;
        }
        if (other.cameraSound != null) {
            cameraSound = other.cameraSound;
        }
        if (other.connectivity != null) {
            connectivity = other.connectivity;
        }
        if (other.doorbellChime != null) {
            doorbellChime = other.doorbellChime;
        }
        if (other.fan != null) {
            fan = other.fan;
        }
        if (other.humidity != null) {
            humidity = other.humidity;
        }
        if (other.deviceInfo != null) {
            deviceInfo = other.deviceInfo;
        }
        if (other.deviceSettings != null) {
            deviceSettings = other.deviceSettings;
        }
        if (other.temperature != null) {
            temperature = other.temperature;
        }
        if (other.thermostatEco != null) {
            thermostatEco = other.thermostatEco;
        }
        if (other.thermostatHvac != null) {
            thermostatHvac = other.thermostatHvac;
        }
        if (other.thermostatMode != null) {
            thermostatMode = other.thermostatMode;
        }
        if (other.thermostatTemperatureSetpoint != null) {
            thermostatTemperatureSetpoint = other.thermostatTemperatureSetpoint;
        }
        if (other.structureInfo != null) {
            structureInfo = other.structureInfo;
        }
        if (other.roomInfo != null) {
            roomInfo = other.roomInfo;
        }
    }
}
