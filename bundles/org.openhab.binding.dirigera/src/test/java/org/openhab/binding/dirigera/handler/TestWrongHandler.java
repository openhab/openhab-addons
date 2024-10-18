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
package org.openhab.binding.dirigera.handler;

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
import org.openhab.binding.dirigera.FileReader;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.internal.handler.ContactSensorHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.WaterSensorHandler;
import org.openhab.binding.dirigera.mock.CallbackMock;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
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
 * {@link TestWrongHandler} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestWrongHandler {
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
