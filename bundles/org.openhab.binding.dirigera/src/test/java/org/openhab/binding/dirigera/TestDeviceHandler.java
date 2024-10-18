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
package org.openhab.binding.dirigera;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.AirQualityHandler;
import org.openhab.binding.dirigera.internal.handler.ColorLightHandler;
import org.openhab.binding.dirigera.internal.handler.ContactSensorHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.LightSensorHandler;
import org.openhab.binding.dirigera.internal.handler.MotionLightSensorHandler;
import org.openhab.binding.dirigera.internal.handler.MotionSensorHandler;
import org.openhab.binding.dirigera.internal.handler.RepeaterHandler;
import org.openhab.binding.dirigera.internal.handler.SceneHandler;
import org.openhab.binding.dirigera.internal.handler.SmartPlugHandler;
import org.openhab.binding.dirigera.internal.handler.SpeakerHandler;
import org.openhab.binding.dirigera.internal.handler.TemperatureLightHandler;
import org.openhab.binding.dirigera.internal.handler.WaterSensorHandler;
import org.openhab.binding.dirigera.mock.CallbackMock;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.State;

/**
 * {@link TestDeviceHandler} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestDeviceHandler {
    private static final TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {

        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };

    /**
     * Prepare operational bridge where device handlers can be connected to
     *
     * @return Bridge
     */
    Bridge prepareBridge() {
        /**
         * Prepare https replies
         */
        String ipAddress = "1.2.3.4";
        HttpClient httpMock = mock(HttpClient.class);
        Request requestMock = mock(Request.class);
        when(httpMock.newRequest(String.format(HOME_URL, ipAddress))).thenReturn(requestMock);
        when(requestMock.timeout(10, TimeUnit.SECONDS)).thenReturn(requestMock);
        when(requestMock.header(HttpHeader.AUTHORIZATION, "Bearer 1234")).thenReturn(requestMock);
        String modelString = FileReader.readFileInString("src/test/resources/home/home.json");
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn(modelString);
        try {
            when(requestMock.send()).thenReturn(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail();
        }
        ContentResponse imageResponse = mock(ContentResponse.class);
        Path path = Paths.get("src/test/resources/coverart/sonos-radio-cocktail-hour.avif");
        try {
            byte[] imageData = Files.readAllBytes(path);
            when(httpMock.GET(
                    "http://192.168.1.95:1400/getaa?s=1&u=x-sonos-http%3acloudcast%253a2179434714.unknown%3fsid%3d181%26flags%3d0%26sn%3d3"))
                    .thenReturn(imageResponse);
            when(imageResponse.getStatus()).thenReturn(200);
            when(imageResponse.getMediaType()).thenReturn(null);
            when(imageResponse.getContent()).thenReturn(imageData);
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            fail("getting image");
        }

        /**
         * Prepare persistence
         */
        // prepare persistence data
        JSONObject storageObject = new JSONObject();
        JSONArray knownDevices = new JSONArray();
        knownDevices.put("594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1");
        storageObject.put(PROPERTY_DEVICES, knownDevices.toString());
        storageObject.put(PROPERTY_TOKEN, "1234");
        // now mock it
        Storage<String> mockStorage = mock(Storage.class);
        when(mockStorage.get("594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1")).thenReturn(storageObject.toString());

        // prepare instances
        BridgeImpl hubBridge = new BridgeImpl(THING_TYPE_GATEWAY, new ThingUID(BINDING_ID + ":" + "gateway:9876"));
        hubBridge.setBridgeUID(new ThingUID(BINDING_ID + ":" + "gateway:9876"));
        DirigeraHandler hubHandler = new DirigeraHandler(hubBridge, httpMock, mockStorage,
                mock(DirigeraDiscoveryManager.class), timeZoneProvider);
        hubBridge.setHandler(hubHandler);
        CallbackMock bridgeCallback = new CallbackMock();
        hubHandler.setCallback(bridgeCallback);

        // set handler to full configured with token, ipAddress and if
        Map<String, Object> config = new HashMap<>();
        config.put("ipAddress", ipAddress);
        config.put("id", "594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1");
        hubHandler.handleConfigurationUpdate(config);

        hubHandler.initialize();
        bridgeCallback.waitForOnline();
        return hubBridge;
    }

    @Test
    void testWrongHandlerForId() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_CONTACT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ContactSensorHandler handler = new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "5ac5e131-44a4-4d75-be78-759a095d31fb_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        ThingStatusInfo status = callback.getStatus();
        assertEquals(ThingStatus.OFFLINE, status.getStatus(), "OFFLINE");
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getStatusDetail(), "Config Error");
        String description = status.getDescription();
        assertNotNull(description);
        assertTrue(description.contains("doesn't match with model"), "Description");
    }

    @Test
    void testContactDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_CONTACT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ContactSensorHandler handler = new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "07cca6c2-f2b6-4f57-bfd9-a788a16d1eef_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State batteryState = callback.getState("dirigera:contact-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(84, ((QuantityType) batteryState).intValue(), "Battery level");
        State openCloseState = callback.getState("dirigera:contact-sensor:test-device:state");
        assertNotNull(openCloseState);
        assertTrue(openCloseState instanceof OpenClosedType);
        assertTrue(OpenClosedType.CLOSED.equals((openCloseState)), "Closed");
    }

    @Test
    void testMotionSensorDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_MOTION_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        MotionSensorHandler handler = new MotionSensorHandler(thing, MOTION_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "ee61c57f-8efa-44f4-ba8a-d108ae054138_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State batteryState = callback.getState("dirigera:motion-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(20, ((QuantityType) batteryState).intValue(), "Battery level");
        State onOffState = callback.getState("dirigera:motion-sensor:test-device:detection");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Off");
    }

    @Test
    void testMotionLightSensorDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_MOTION_LIGHT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        MotionLightSensorHandler handler = new MotionLightSensorHandler(thing, MOTION_LIGHT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "5ac5e131-44a4-4d75-be78-759a095d31fb_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State batteryState = callback.getState("dirigera:motion-light-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(85, ((QuantityType) batteryState).intValue(), "Battery level");
        State onOffState = callback.getState("dirigera:motion-light-sensor:test-device:detection");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Off");
        State luxState = callback.getState("dirigera:motion-light-sensor:test-device:illuminance");
        assertNotNull(luxState);
        assertTrue(luxState instanceof QuantityType);
        assertTrue(((QuantityType) luxState).getUnit().equals(Units.LUX));
        assertEquals(1, ((QuantityType) luxState).intValue(), "Lux level");
    }

    @Test
    void testColorLightDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_COLOR_LIGHT, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ColorLightHandler handler = new ColorLightHandler(thing, COLOR_LIGHT_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "3c8b0049-eb5c-4ea1-9da3-cdedc50366ef_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State onOffState = callback.getState("dirigera:color-light:test-device:state");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals((onOffState)), "On");
        State hsbState = callback.getState("dirigera:color-light:test-device:hsb");
        assertNotNull(hsbState);
        assertTrue(hsbState instanceof HSBType);
        assertEquals(119, ((HSBType) hsbState).getHue().intValue(), "Hue");
        assertEquals(70, ((HSBType) hsbState).getSaturation().intValue(), "Saturation");
        assertEquals(92, ((HSBType) hsbState).getBrightness().intValue(), "Brightness");

        // test ota
        State otaStatus = callback.getState("dirigera:color-light:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:color-light:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:color-light:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");
    }

    @Test
    void testTemperatureLightDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_TEMPERATURE_LIGHT, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        TemperatureLightHandler handler = new TemperatureLightHandler(thing, TEMPERATURE_LIGHT_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "a1e1eacc-2dcf-45bd-9f93-62a436b6a7ed_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State onOffState = callback.getState("dirigera:temperature-light:test-device:state");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals((onOffState)), "On");

        State temperatureState = callback.getState("dirigera:temperature-light:test-device:temperature");
        assertNotNull(temperatureState);
        assertTrue(temperatureState instanceof PercentType);
        assertEquals(0, ((PercentType) temperatureState).intValue(), "Temperature");

        State brightnessState = callback.getState("dirigera:temperature-light:test-device:brightness");
        assertNotNull(brightnessState);
        assertTrue(brightnessState instanceof PercentType);
        assertEquals(49, ((PercentType) brightnessState).intValue(), "Brightness");

        // test ota
        State otaStatus = callback.getState("dirigera:temperature-light:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:temperature-light:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:temperature-light:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");
    }

    @Test
    void testLightSensorDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_LIGHT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        LightSensorHandler handler = new LightSensorHandler(thing, LIGHT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "ca856a7d-a715-42f7-84a1-7caae41e6ff2_3");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State luxState = callback.getState("dirigera:light-sensor:test-device:illuminance");
        assertNotNull(luxState);
        assertTrue(luxState instanceof QuantityType);
        assertTrue(((QuantityType) luxState).getUnit().equals(Units.LUX));
        assertEquals(1, ((QuantityType) luxState).intValue(), "Lux level");
    }

    @Test
    void testSmartPlugDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_SMART_PLUG, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        SmartPlugHandler handler = new SmartPlugHandler(thing, SMART_PLUG_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "ec549fa8-4e35-4f27-90e9-bb67e68311f2_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        // TODO tbd

        // State luxState = callback.getState("dirigera:light-sensor:test-device:illuminance");
        // assertNotNull(luxState);
        // assertTrue(luxState instanceof QuantityType);
        // assertTrue(((QuantityType) luxState).getUnit().equals(Units.LUX));
        // assertEquals(1, ((QuantityType) luxState).intValue(), "Lux level");
    }

    @Test
    void testSpeakerDevice() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_SPEAKER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        SpeakerHandler handler = new SpeakerHandler(thing, SPEAKER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "338bb721-35bb-4775-8cd0-ba70fc37ab10_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        // TODO tbd

        // State luxState = callback.getState("dirigera:light-sensor:test-device:illuminance");
        // assertNotNull(luxState);
        // assertTrue(luxState instanceof QuantityType);
        // assertTrue(((QuantityType) luxState).getUnit().equals(Units.LUX));
        // assertEquals(1, ((QuantityType) luxState).intValue(), "Lux level");
    }

    @Test
    void testSceneHandler() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_SCENE, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        SceneHandler handler = new SceneHandler(thing, SPEAKER_MAP, timeZoneProvider);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "086f4a37-ebe8-4fd4-9a25-a0220a1e5f58");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        // TODO tbd

        State dateTimeState = callback.getState("dirigera:scene:test-device:last-trigger");
        assertNotNull(dateTimeState);
        assertTrue(dateTimeState instanceof DateTimeType);
        assertEquals("2024-10-15T14:49:03.028+0200", ((DateTimeType) dateTimeState).toFullString(), "Last trigger");
    }

    @Test
    void testRepeater() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_REPEATER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        RepeaterHandler handler = new RepeaterHandler(thing, REPEATER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "044b63e7-999d-4caa-8a76-fb8cfd32b381_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        // test only ota
        State otaStatus = callback.getState("dirigera:repeater:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:repeater:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:repeater:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");
    }

    @Test
    void testLightController() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_LIGHT_CONTROLLER, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        RepeaterHandler handler = new RepeaterHandler(thing, LIGHT_CONTROLLER_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "22e4b77b-9a60-4727-944b-0d5e3e33b58f_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        // test ota & battery
        State otaStatus = callback.getState("dirigera:light-controller:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:light-controller:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:light-controller:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");
        State batteryState = callback.getState("dirigera:light-controller:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(85, ((QuantityType) batteryState).intValue(), "Battery level");
    }

    @Test
    void testAirQuality() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_AIR_QUALITY, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        AirQualityHandler handler = new AirQualityHandler(thing, AIR_QUALITY_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "f80cac12-65a4-47b4-9f68-a0456a349a43_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        // test ota & battery
        State otaStatus = callback.getState("dirigera:air-quality:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:air-quality:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:air-quality:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");

        State temperatureState = callback.getState("dirigera:air-quality:test-device:temperature");
        assertNotNull(temperatureState);
        assertTrue(temperatureState instanceof QuantityType);
        assertTrue(((QuantityType) temperatureState).getUnit().equals(SIUnits.CELSIUS));
        assertEquals(20, ((QuantityType) temperatureState).intValue(), "Temperature");
        State humidityState = callback.getState("dirigera:air-quality:test-device:humidity");
        assertNotNull(humidityState);
        assertTrue(humidityState instanceof QuantityType);
        assertTrue(((QuantityType) humidityState).getUnit().equals(Units.PERCENT));
        assertEquals(76, ((QuantityType) humidityState).intValue(), "Hunidity");
        State ppmState = callback.getState("dirigera:air-quality:test-device:particulate-matter");
        assertNotNull(ppmState);
        assertTrue(ppmState instanceof QuantityType);
        assertTrue(((QuantityType) ppmState).getUnit().equals(Units.MICROGRAM_PER_CUBICMETRE));
        assertEquals(11, ((QuantityType) ppmState).intValue(), "ppm");
        State vocState = callback.getState("dirigera:air-quality:test-device:voc-index");
        assertNotNull(vocState);
        assertTrue(vocState instanceof QuantityType);
        assertTrue(((QuantityType) vocState).getUnit().toString().equals("mg/m³"));
        assertEquals(100, ((QuantityType) vocState).intValue(), "VOC Index");
    }

    @Test
    void testWaterSensor() {
        Bridge hubBridge = prepareBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_WATER_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        WaterSensorHandler handler = new WaterSensorHandler(thing, WATER_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "9af826ad-a8ad-40bf-8aed-125300bccd20_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        // test ota & battery
        State otaStatus = callback.getState("dirigera:water-sensor:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:water-sensor:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:water-sensor:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType) otaProgess).intValue(), "OTA Progress");

        State onOffState = callback.getState("dirigera:water-sensor:test-device:detection");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Off");
        State batteryState = callback.getState("dirigera:water-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(55, ((QuantityType) batteryState).intValue(), "Battery level");
    }
}
