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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMCameraImageTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMCameraLiveStreamTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMConnectivityStatus;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMConnectivityTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMDeviceInfoTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMDeviceSettingsTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTimerMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMFanTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMHumidityTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMHvacStatus;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMResolution;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMTemperatureScale;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMTemperatureTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatEcoTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatHvacTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatMode;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatModeTrait;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMThermostatTemperatureSetpointTrait;

/**
 * Tests deserialization of {@link org.openhab.binding.nest.internal.sdm.dto.SDMDevice}s from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMDeviceTest {

    @Test
    public void deserializeThermostatDevice() throws IOException {
        SDMDevice device = getThermostatDevice();
        assertThat(device, is(notNullValue()));

        assertThat(device.name.name, is("enterprises/project-id/devices/thermostat-device-id"));
        assertThat(device.type, is(SDMDeviceType.THERMOSTAT));

        SDMTraits traits = device.traits;
        assertThat(traits, is(notNullValue()));
        assertThat(traits.traitList(), hasSize(10));

        SDMDeviceInfoTrait deviceInfo = traits.deviceInfo;
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo.customName, is(""));

        SDMHumidityTrait humidity = traits.humidity;
        assertThat(humidity, is(notNullValue()));
        assertThat(humidity.ambientHumidityPercent, is(new BigDecimal(26)));

        SDMConnectivityTrait connectivity = traits.connectivity;
        assertThat(connectivity, is(notNullValue()));
        assertThat(connectivity.status, is(SDMConnectivityStatus.ONLINE));

        SDMFanTrait fan = traits.fan;
        assertThat(fan, is(notNullValue()));
        assertThat(fan.timerMode, is(SDMFanTimerMode.ON));
        assertThat(fan.timerTimeout, is(ZonedDateTime.parse("2019-05-10T03:22:54Z")));

        SDMThermostatModeTrait thermostatMode = traits.thermostatMode;
        assertThat(thermostatMode, is(notNullValue()));
        assertThat(thermostatMode.mode, is(SDMThermostatMode.HEAT));
        assertThat(thermostatMode.availableModes, is(List.of(SDMThermostatMode.HEAT, SDMThermostatMode.OFF)));

        SDMThermostatEcoTrait thermostatEco = traits.thermostatEco;
        assertThat(thermostatEco, is(notNullValue()));
        assertThat(thermostatEco.availableModes,
                is(List.of(SDMThermostatEcoMode.OFF, SDMThermostatEcoMode.MANUAL_ECO)));
        assertThat(thermostatEco.mode, is(SDMThermostatEcoMode.OFF));
        assertThat(thermostatEco.heatCelsius, is(new BigDecimal("15.34473")));
        assertThat(thermostatEco.coolCelsius, is(new BigDecimal("24.44443")));

        SDMThermostatHvacTrait thermostatHvac = traits.thermostatHvac;
        assertThat(thermostatHvac, is(notNullValue()));
        assertThat(thermostatHvac.status, is(SDMHvacStatus.OFF));

        SDMDeviceSettingsTrait deviceSettings = traits.deviceSettings;
        assertThat(deviceSettings, is(notNullValue()));
        assertThat(deviceSettings.temperatureScale, is(SDMTemperatureScale.CELSIUS));

        SDMThermostatTemperatureSetpointTrait thermostatTemperatureSetpoint = traits.thermostatTemperatureSetpoint;
        assertThat(thermostatTemperatureSetpoint, is(notNullValue()));
        assertThat(thermostatTemperatureSetpoint.heatCelsius, is(new BigDecimal("14.92249")));
        assertThat(thermostatTemperatureSetpoint.coolCelsius, is(nullValue()));

        SDMTemperatureTrait temperature = traits.temperature;
        assertThat(temperature, is(notNullValue()));
        assertThat(temperature.ambientTemperatureCelsius, is(new BigDecimal("19.73")));

        List<SDMParentRelation> parentRelations = device.parentRelations;
        assertThat(parentRelations, is(notNullValue()));
        assertThat(parentRelations, hasSize(1));

        assertThat(parentRelations.get(0).parent.name,
                is("enterprises/project-id/structures/structure-id/rooms/thermostat-room-id"));
        assertThat(parentRelations.get(0).displayName, is("Thermostat Room Name"));
    }

    protected SDMDevice getThermostatDevice() throws IOException {
        return fromJson("thermostat-device-response.json", SDMDevice.class);
    }

    @Test
    public void deserializeCameraDevice() throws IOException {
        SDMDevice device = getCameraDevice();
        assertThat(device, is(notNullValue()));

        assertThat(device.name.name, is("enterprises/project-id/devices/camera-device-id"));
        assertThat(device.type, is(SDMDeviceType.CAMERA));

        SDMTraits traits = device.traits;
        assertThat(traits, is(notNullValue()));
        assertThat(traits.traitList(), hasSize(7));

        SDMDeviceInfoTrait deviceInfo = traits.deviceInfo;
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo.customName, is(""));

        SDMConnectivityTrait connectivity = traits.connectivity;
        assertThat(connectivity, is(nullValue()));

        SDMCameraLiveStreamTrait cameraLiveStream = traits.cameraLiveStream;
        assertThat(cameraLiveStream, is(notNullValue()));

        SDMResolution maxVideoResolution = cameraLiveStream.maxVideoResolution;
        assertThat(maxVideoResolution, is(notNullValue()));
        assertThat(maxVideoResolution.width, is(640));
        assertThat(maxVideoResolution.height, is(480));

        assertThat(cameraLiveStream.videoCodecs, is(List.of("H264")));
        assertThat(cameraLiveStream.audioCodecs, is(List.of("AAC")));

        SDMCameraImageTrait cameraImage = traits.cameraImage;
        assertThat(cameraImage, is(notNullValue()));

        SDMResolution maxImageResolution = cameraImage.maxImageResolution;
        assertThat(maxImageResolution, is(notNullValue()));
        assertThat(maxImageResolution.width, is(1920));
        assertThat(maxImageResolution.height, is(1200));

        assertThat(traits.cameraPerson, is(notNullValue()));
        assertThat(traits.cameraSound, is(notNullValue()));
        assertThat(traits.cameraMotion, is(notNullValue()));
        assertThat(traits.cameraEventImage, is(notNullValue()));
        assertThat(traits.doorbellChime, is(nullValue()));

        List<SDMParentRelation> parentRelations = device.parentRelations;
        assertThat(parentRelations, is(notNullValue()));
        assertThat(parentRelations, hasSize(1));

        assertThat(parentRelations.get(0).parent.name,
                is("enterprises/project-id/structures/structure-id/rooms/camera-room-id"));
        assertThat(parentRelations.get(0).displayName, is("Camera Room Name"));
    }

    protected SDMDevice getCameraDevice() throws IOException {
        return fromJson("camera-device-response.json", SDMDevice.class);
    }

    @Test
    public void deserializeDisplayDevice() throws IOException {
        SDMDevice device = getDisplayDevice();
        assertThat(device, is(notNullValue()));

        assertThat(device.name.name, is("enterprises/project-id/devices/display-device-id"));
        assertThat(device.type, is(SDMDeviceType.DISPLAY));

        SDMTraits traits = device.traits;
        assertThat(traits, is(notNullValue()));
        assertThat(traits.traitList(), hasSize(7));

        SDMDeviceInfoTrait deviceInfo = traits.deviceInfo;
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo.customName, is(""));

        SDMConnectivityTrait connectivity = traits.connectivity;
        assertThat(connectivity, is(nullValue()));

        SDMCameraLiveStreamTrait cameraLiveStream = traits.cameraLiveStream;
        assertThat(cameraLiveStream, is(notNullValue()));

        SDMResolution maxVideoResolution = cameraLiveStream.maxVideoResolution;
        assertThat(maxVideoResolution, is(notNullValue()));
        assertThat(maxVideoResolution.width, is(640));
        assertThat(maxVideoResolution.height, is(480));

        assertThat(cameraLiveStream.videoCodecs, is(List.of("H264")));
        assertThat(cameraLiveStream.audioCodecs, is(List.of("AAC")));

        SDMCameraImageTrait cameraImage = traits.cameraImage;
        assertThat(cameraImage, is(notNullValue()));

        SDMResolution maxImageResolution = cameraImage.maxImageResolution;
        assertThat(maxImageResolution, is(notNullValue()));
        assertThat(maxImageResolution.width, is(1920));
        assertThat(maxImageResolution.height, is(1200));

        assertThat(traits.cameraPerson, is(notNullValue()));
        assertThat(traits.cameraSound, is(notNullValue()));
        assertThat(traits.cameraMotion, is(notNullValue()));
        assertThat(traits.cameraEventImage, is(notNullValue()));
        assertThat(traits.doorbellChime, is(nullValue()));

        List<SDMParentRelation> parentRelations = device.parentRelations;
        assertThat(parentRelations, is(notNullValue()));
        assertThat(parentRelations, hasSize(1));

        assertThat(parentRelations.get(0).parent.name,
                is("enterprises/project-id/structures/structure-id/rooms/display-room-id"));
        assertThat(parentRelations.get(0).displayName, is("Display Room Name"));
    }

    protected SDMDevice getDisplayDevice() throws IOException {
        return fromJson("display-device-response.json", SDMDevice.class);
    }

    @Test
    public void deserializeDoorbellDevice() throws IOException {
        SDMDevice device = getDoorbellDevice();
        assertThat(device, is(notNullValue()));

        assertThat(device.name.name, is("enterprises/project-id/devices/doorbell-device-id"));
        assertThat(device.type, is(SDMDeviceType.DOORBELL));

        SDMTraits traits = device.traits;
        assertThat(traits, is(notNullValue()));
        assertThat(traits.traitList(), hasSize(8));

        SDMDeviceInfoTrait deviceInfo = traits.deviceInfo;
        assertThat(deviceInfo, is(notNullValue()));
        assertThat(deviceInfo.customName, is(""));

        SDMConnectivityTrait connectivity = traits.connectivity;
        assertThat(connectivity, is(nullValue()));

        SDMCameraLiveStreamTrait cameraLiveStream = traits.cameraLiveStream;
        assertThat(cameraLiveStream, is(notNullValue()));

        SDMResolution maxVideoResolution = cameraLiveStream.maxVideoResolution;
        assertThat(maxVideoResolution, is(notNullValue()));
        assertThat(maxVideoResolution.width, is(640));
        assertThat(maxVideoResolution.height, is(480));

        assertThat(cameraLiveStream.videoCodecs, is(List.of("H264")));
        assertThat(cameraLiveStream.audioCodecs, is(List.of("AAC")));

        SDMCameraImageTrait cameraImage = traits.cameraImage;
        assertThat(cameraImage, is(notNullValue()));

        SDMResolution maxImageResolution = cameraImage.maxImageResolution;
        assertThat(maxImageResolution, is(notNullValue()));
        assertThat(maxImageResolution.width, is(1920));
        assertThat(maxImageResolution.height, is(1200));

        assertThat(traits.cameraPerson, is(notNullValue()));
        assertThat(traits.cameraSound, is(notNullValue()));
        assertThat(traits.cameraMotion, is(notNullValue()));
        assertThat(traits.cameraEventImage, is(notNullValue()));
        assertThat(traits.doorbellChime, is(notNullValue()));

        List<SDMParentRelation> parentRelations = device.parentRelations;
        assertThat(parentRelations, is(notNullValue()));
        assertThat(parentRelations, hasSize(1));

        assertThat(parentRelations.get(0).parent.name,
                is("enterprises/project-id/structures/structure-id/rooms/doorbell-room-id"));
        assertThat(parentRelations.get(0).displayName, is("Doorbell Room Name"));
    }

    protected SDMDevice getDoorbellDevice() throws IOException {
        return fromJson("doorbell-device-response.json", SDMDevice.class);
    }
}
