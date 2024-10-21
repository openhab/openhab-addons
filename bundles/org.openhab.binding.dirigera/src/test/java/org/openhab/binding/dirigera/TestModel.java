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
        ThingTypeUID motionLightUID = gateway.model().identifyDevice(vallhornId);
        assertEquals(THING_TYPE_MOTION_LIGHT_SENSOR, motionLightUID, "VALLHORN TTUID");
        List<String> twinList = gateway.model().getTwins(vallhornId);
        assertEquals(1, twinList.size(), "Twins");
        assertEquals("5ac5e131-44a4-4d75-be78-759a095d31fb_3", twinList.get(0), "Twin id");

        // TRADFRI
        String tradfriId = "ee61c57f-8efa-44f4-ba8a-d108ae054138_1";
        ThingTypeUID motionUID = gateway.model().identifyDevice(tradfriId);
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
        ThingTypeUID plugTTUID = gateway.model().identifyDevice(tretaktId);
        assertEquals(THING_TYPE_PLUG, plugTTUID, "TRETAKT TTUID");

        // TRADFRI
        String inspelningId = "ec549fa8-4e35-4f27-90e9-bb67e68311f2_1";
        ThingTypeUID motionUID = gateway.model().identifyDevice(inspelningId);
        assertEquals(THING_TYPE_SMART_PLUG, motionUID, "INSPELNING TTUID");
    }

    @Test
    void testSceneName() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        Gateway gateway = (Gateway) hubBridge.getHandler();
        assertNotNull(gateway);

        // VALLHORN
        String lightSceneId = "3090ba82-3f5e-442f-8e49-f3eac9b7b0eb";
        ThingTypeUID sceneTTUID = gateway.model().identifyDevice(lightSceneId);
        assertEquals(THING_TYPE_SCENE, sceneTTUID, "Scene TTUID");
    }
}
