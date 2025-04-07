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
package org.openhab.binding.dirigera.internal.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.FileReader;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.DirigeraHandler;
import org.openhab.binding.dirigera.internal.handler.sensor.WaterSensorHandler;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DicoveryServiceMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * {@link TestModel} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestModel {

    @Test
    void testCustomName() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        // test device with given custom name
        assertEquals("Loft Floor Lamp", gateway.model().getCustonNameFor("891790db-8c17-483a-a1a6-c85bffd3a373_1"),
                "Floor Lamp name");
        // test device without custom name - take model name
        assertEquals("VALLHORN Wireless Motion Sensor",
                gateway.model().getCustonNameFor("5ac5e131-44a4-4d75-be78-759a095d31fb_3"), "Motion Sensor name");
        // test device without custom name and no model name
        assertEquals("light", gateway.model().getCustonNameFor("c27faa27-4c18-464f-81a0-a31ce57d83d5_1"), "Lamp");
    }

    @Test
    void testMotionSensors() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        // VALLHORN
        String vallhornId = "5ac5e131-44a4-4d75-be78-759a095d31fb_1";
        ThingTypeUID motionLightUID = gateway.model().identifyDeviceFromModel(vallhornId);
        assertEquals(THING_TYPE_MOTION_LIGHT_SENSOR, motionLightUID, "VALLHORN TTUID");
        Map<String, String> relationsMap = gateway.model().getRelations("5ac5e131-44a4-4d75-be78-759a095d31fb");
        assertEquals(2, relationsMap.size(), "Relations");
        assertTrue(relationsMap.containsKey("5ac5e131-44a4-4d75-be78-759a095d31fb_1"), "Motion Sensor");
        assertEquals("motionSensor", relationsMap.get("5ac5e131-44a4-4d75-be78-759a095d31fb_1"), "Motion Sensor");
        assertTrue(relationsMap.containsKey("5ac5e131-44a4-4d75-be78-759a095d31fb_3"), "Light Sensor");
        assertEquals("lightSensor", relationsMap.get("5ac5e131-44a4-4d75-be78-759a095d31fb_3"), "Light Sensor");

        // TRADFRI
        String tradfriId = "ee61c57f-8efa-44f4-ba8a-d108ae054138_1";
        ThingTypeUID motionUID = gateway.model().identifyDeviceFromModel(tradfriId);
        assertEquals(THING_TYPE_MOTION_SENSOR, motionUID, "TRADFRI TTUID");
        relationsMap = gateway.model().getRelations(tradfriId);
        assertEquals(0, relationsMap.size(), "Twins");
    }

    @Test
    void testPlugs() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        // VALLHORN
        String tretaktId = "a4c6a33a-9c6a-44bf-bdde-f99aff00eca4_1";
        ThingTypeUID plugTTUID = gateway.model().identifyDeviceFromModel(tretaktId);
        assertEquals(THING_TYPE_POWER_PLUG, plugTTUID, "TRETAKT TTUID");

        // TRADFRI
        String inspelningId = "ec549fa8-4e35-4f27-90e9-bb67e68311f2_1";
        ThingTypeUID motionUID = gateway.model().identifyDeviceFromModel(inspelningId);
        assertEquals(THING_TYPE_SMART_PLUG, motionUID, "INSPELNING TTUID");
    }

    @Test
    void testSceneName() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        String lightSceneId = "3090ba82-3f5e-442f-8e49-f3eac9b7b0eb";
        ThingTypeUID sceneTTUID = gateway.model().identifyDeviceFromModel(lightSceneId);
        assertEquals(THING_TYPE_SCENE, sceneTTUID, "Scene TTUID");
    }

    @Test
    void testInitialDiscovery() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home.json", true,
                List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(25, discovery.discoveries.size(), "Initial discoveries");
    }

    @Test
    void testDiscoveryDisabled() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home.json", false,
                List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertTrue(discovery.discoveries.isEmpty(), "Discovery disabled");
    }

    @Test
    void testKnownDevices() {
        String knownDevice = "9af826ad-a8ad-40bf-8aed-125300bccd20_1";
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home.json", true,
                List.of(knownDevice));
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(24, discovery.discoveries.size(), "Initial discoveries");
        assertFalse(discovery.discoveries.containsKey(knownDevice));
    }

    @Test
    void testDiscoveryAfterHandlerRemoval() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home-one-device.json",
                true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(1, discovery.discoveries.size(), "Initial discoveries");

        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(THING_TYPE_WATER_SENSOR, hubBridge,
                "9af826ad-a8ad-40bf-8aed-125300bccd20_1");
        assertTrue(factoryHandler instanceof WaterSensorHandler);
        WaterSensorHandler handler = (WaterSensorHandler) factoryHandler;
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        CallbackMock callback = (CallbackMock) proxyCallback;
        callback.waitForOnline();

        DirigeraHandler.detectionTimeSeonds = 0;
        discovery.discoveries.clear();
        assertEquals(0, discovery.discoveries.size(), "Cleanup after handler creation");
        handler.dispose();
        handler.handleRemoval();
        discovery.waitForDetection();
        assertEquals(1, discovery.discoveries.size(), "After removal new discovery result shall be present ");
    }

    @Test
    void testResolvedRelations() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/device-added/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);
        List<String> all = gateway.model().getAllDeviceIds();
        List<String> resolved = gateway.model().getResolvedDeviceList();

        all.removeAll(resolved);
        // 2 resolved devices
        assertEquals(2, all.size(), "2 devices resolved");
    }

    @Test
    void testDeviceAdded() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/device-added/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(25, discovery.discoveries.size(), "Initial discoveries");

        // Prepare update message
        String update = FileReader.readFileInString("src/test/resources/websocket/device-added/device-added.json");
        // prepare mock
        DirigeraAPISimu.fileName = "src/test/resources/websocket/device-added/home-after.json";
        try {
            gateway.websocketUpdate(update);
            // give the gateway some time to handle the message
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(25 + 1, discovery.discoveries.size(), "One more discovery");
    }

    @Test
    void testDeviceRemoved() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/device-removed/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(26, discovery.discoveries.size(), "Initial discoveries");

        // Prepare update message
        String update = FileReader.readFileInString("src/test/resources/websocket/device-removed/device-removed.json");
        // prepare mock
        DirigeraAPISimu.fileName = "src/test/resources/websocket/device-removed/home-after.json";
        try {
            gateway.websocketUpdate(update);
            // give the gateway some time to handle the message
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(26 - 1, discovery.discoveries.size(), "One less discovery");
        assertEquals(1, discovery.deletes.size(), "One deletion");
    }

    @Test
    void testSceneCreated() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/scene-created/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(24, discovery.discoveries.size(), "Initial discoveries");

        // Prepare update message
        String update = FileReader.readFileInString("src/test/resources/websocket/scene-created/scene-created.json");
        // prepare mock
        DirigeraAPISimu.fileName = "src/test/resources/websocket/scene-created/home-after.json";
        try {
            gateway.websocketUpdate(update);
            // give the gateway some time to handle the message
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(24 + 1, discovery.discoveries.size(), "One more discovery");
    }

    @Test
    void testSceneDeleted() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/scene-deleted/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DicoveryServiceMock discovery = (DicoveryServiceMock) gateway.discovery();
        assertEquals(25, discovery.discoveries.size(), "Initial discoveries");

        // Prepare update message
        String update = FileReader.readFileInString("src/test/resources/websocket/scene-deleted/scene-deleted.json");
        // prepare mock
        DirigeraAPISimu.fileName = "src/test/resources/websocket/scene-deleted/home-after.json";
        try {
            gateway.websocketUpdate(update);
            // give the gateway some time to handle the message
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(25 - 1, discovery.discoveries.size(), "One more discovery");
        assertEquals(1, discovery.deletes.size(), "One deletion");
    }
}
