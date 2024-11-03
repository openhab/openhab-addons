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

import static org.junit.jupiter.api.Assertions.fail;
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.FileReader;
import org.openhab.binding.dirigera.internal.discovery.DirigeraDiscoveryManager;
import org.openhab.binding.dirigera.mock.CallbackMock;
import org.openhab.binding.dirigera.mock.DirigeraHandlerManipulator;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * {@link DirigeraBridgeProvider} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class DirigeraBridgeProvider {
    public static final TimeZoneProvider TZP = new TimeZoneProvider() {

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
    public static Bridge prepareBridge() {
        /**
         * Prepare https replies
         */
        String ipAddress = "1.2.3.4";
        HttpClient httpMock = mock(HttpClient.class);
        Request requestMock = mock(Request.class);
        when(httpMock.isRunning()).thenReturn(true);
        when(httpMock.getSslContextFactory()).thenReturn(new SslContextFactory.Client(true));
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

        /**
         * new version with api simulation in background
         */
        DirigeraHandlerManipulator hubHandler = new DirigeraHandlerManipulator(hubBridge, httpMock, mockStorage,
                mock(DirigeraDiscoveryManager.class), TZP);
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
        /**
         * Old working version
         */
        // DirigeraHandler hubHandler = new DirigeraHandler(hubBridge, httpMock, mockStorage,
        // mock(DirigeraDiscoveryManager.class), TZP);
        // hubBridge.setHandler(hubHandler);
        // CallbackMock bridgeCallback = new CallbackMock();
        // hubHandler.setCallback(bridgeCallback);
        //
        // // set handler to full configured with token, ipAddress and if
        // Map<String, Object> config = new HashMap<>();
        // config.put("ipAddress", ipAddress);
        // config.put("id", "594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1");
        // hubHandler.handleConfigurationUpdate(config);
        //
        // hubHandler.initialize();
        // bridgeCallback.waitForOnline();
        // return hubBridge;
    }
}
