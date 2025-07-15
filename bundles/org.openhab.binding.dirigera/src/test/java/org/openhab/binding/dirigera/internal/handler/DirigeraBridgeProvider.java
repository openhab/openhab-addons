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
package org.openhab.binding.dirigera.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.Constants;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DicoveryServiceMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.internal.mock.DirigeraHandlerManipulator;
import org.openhab.binding.dirigera.internal.mock.HandlerFactoryMock;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * {@link DirigeraBridgeProvider} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
public class DirigeraBridgeProvider {
    public static Bridge prepareSimuBridge() {
        return prepareSimuBridge("src/test/resources/home/home.json", false, List.of());
    }

    /**
     * Prepare bridge which can be used with DirigraAPISimu Provider
     *
     * @return Bridge
     */
    public static Bridge prepareSimuBridge(String homeFile, boolean discovery, List<String> knownDevicesd) {
        String ipAddress = "1.2.3.4";
        HttpClient httpMock = mock(HttpClient.class);
        DirigeraAPISimu.fileName = homeFile;
        /**
         * Prepare persistence
         */
        // prepare persistence data
        VolatileStorageService storageService = new VolatileStorageService();
        Storage<String> mockStorage = storageService.getStorage(Constants.BINDING_ID);

        JSONObject storageObject = new JSONObject();
        JSONArray knownDevices = new JSONArray(knownDevicesd);
        knownDevices.put("594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1");
        storageObject.put(PROPERTY_DEVICES, knownDevices.toString());
        storageObject.put(PROPERTY_TOKEN, "unit-test");
        mockStorage.put(ipAddress, storageObject.toString());

        // prepare instances
        BridgeImpl hubBridge = new BridgeImpl(THING_TYPE_GATEWAY, new ThingUID(BINDING_ID + ":" + "gateway:9876"));
        hubBridge.setBridgeUID(new ThingUID(BINDING_ID + ":" + "gateway:9876"));

        /**
         * new version with api simulation in background
         */
        DirigeraHandlerManipulator hubHandler = new DirigeraHandlerManipulator(hubBridge, httpMock, mockStorage,
                new DicoveryServiceMock());
        hubBridge.setHandler(hubHandler);
        CallbackMock bridgeCallback = new CallbackMock();
        hubHandler.setCallback(bridgeCallback);

        // set handler to full configured with token, ipAddress and if
        Map<String, Object> config = new HashMap<>();
        config.put("ipAddress", ipAddress);
        config.put("id", "594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1");
        config.put("discovery", discovery);
        hubHandler.handleConfigurationUpdate(config);

        hubHandler.initialize();
        bridgeCallback.waitForOnline();
        return hubBridge;
    }

    public static ThingHandler createHandler(ThingTypeUID thingTypeUID, Bridge hubBridge, String deviceId) {
        VolatileStorageService storageService = new VolatileStorageService();
        HandlerFactoryMock hfm = new HandlerFactoryMock(storageService);
        assertTrue(hfm.supportsThingType(thingTypeUID));
        ThingImpl thing = new ThingImpl(thingTypeUID, "test-device");
        thing.setLabel("Unit Test Device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ThingHandler handler = hfm.createHandler(thing);
        assertNotNull(handler);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);
        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", deviceId);
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        return handler;
    }
}
