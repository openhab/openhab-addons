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
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.sensor.ContactSensorHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * {@link TestWrongHandler} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestWrongHandler {
    @Test
    void testWrongHandlerForId() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
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
        assertTrue(
                "@text/dirigera.device.status.ttuid-mismatch [\"dirigera:contact-sensor\",\"dirigera:motion-light-sensor\"]"
                        .equals(description),
                "Description");
    }

    @Test
    void testMissingId() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_CONTACT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ContactSensorHandler handler = new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "5ac5e131-1234-4d75-be78-759a095d31fb_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        ThingStatusInfo status = callback.getStatus();
        assertEquals(ThingStatus.OFFLINE, status.getStatus(), "OFFLINE");
        assertEquals(ThingStatusDetail.GONE, status.getStatusDetail(), "Device disappeared");
        String description = status.getDescription();
        assertNotNull(description);
        assertTrue("@text/dirigera.device.status.id-not-found [\"5ac5e131-1234-4d75-be78-759a095d31fb_1\"]"
                .equals(description), "Description");
    }
}
