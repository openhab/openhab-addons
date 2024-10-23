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
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.mock.DiscoveryMangerMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;

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
        List<String> twinList = gateway.model().getTwins(vallhornId);
        assertEquals(1, twinList.size(), "Twins");
        assertEquals("5ac5e131-44a4-4d75-be78-759a095d31fb_3", twinList.get(0), "Twin id");

        // TRADFRI
        String tradfriId = "ee61c57f-8efa-44f4-ba8a-d108ae054138_1";
        ThingTypeUID motionUID = gateway.model().identifyDeviceFromModel(tradfriId);
        assertEquals(THING_TYPE_MOTION_SENSOR, motionUID, "TRADFRI TTUID");
        twinList = gateway.model().getTwins(tradfriId);
        assertEquals(0, twinList.size(), "Twins");
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

        // VALLHORN
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

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(27, discovery.discoveries.size(), "Initial discoveries");
    }

    @Test
    void testDiscoveryDisabled() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home.json", false,
                List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertTrue(discovery.discoveries.isEmpty(), "Discovery disabled");
    }

    @Test
    void testKnownDevices() {
        String knownDevice = "9af826ad-a8ad-40bf-8aed-125300bccd20_1";
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/home.json", true,
                List.of(knownDevice));
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(26, discovery.discoveries.size(), "Initial discoveries");
        assertFalse(discovery.discoveries.containsKey(knownDevice));
    }

    @Test
    void testDeviceAdded() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/device-added/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(27, discovery.discoveries.size(), "Initial discoveries");

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
        assertEquals(27 + 1, discovery.discoveries.size(), "One more discovery");
    }

    @Test
    void testDeviceRemoved() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/device-removed/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(28, discovery.discoveries.size(), "Initial discoveries");

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
        assertEquals(28 - 1, discovery.discoveries.size(), "One less discovery");
        assertEquals(1, discovery.deletes.size(), "One deletion");
    }

    @Test
    void testSCeneCreated() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/scene-created/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(26, discovery.discoveries.size(), "Initial discoveries");

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
        assertEquals(26 + 1, discovery.discoveries.size(), "One more discovery");
    }

    @Test
    void testSceneDeleted() {
        Bridge hubBridge = DirigeraBridgeProvider
                .prepareSimuBridge("src/test/resources/websocket/scene-deleted/home-before.json", true, List.of());
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        DiscoveryMangerMock discovery = (DiscoveryMangerMock) gateway.discovery();
        assertEquals(27, discovery.discoveries.size(), "Initial discoveries");

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
        assertEquals(27 - 1, discovery.discoveries.size(), "One more discovery");
        assertEquals(1, discovery.deletes.size(), "One deletion");
    }
}
