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
package org.openhab.binding.dirigera.internal.handler.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.FileReader;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TestDoubleShortcutController} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestDoubleShortcutController {
    private final Logger logger = LoggerFactory.getLogger(TestDoubleShortcutController.class);

    String deviceId = "854bdf30-86b8-48f5-b070-16ff5ab12be4_1";
    ThingTypeUID thingTypeUID = THING_TYPE_DOUBLE_SHORTCUT_CONTROLLER;

    private static DoubleShortcutControllerHandler handler = mock(DoubleShortcutControllerHandler.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof DoubleShortcutControllerHandler);
        handler = (DoubleShortcutControllerHandler) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
    }

    @Test
    void testInitialization() {
        testHandlerCreation();

        handler.initialize();
        callback.waitForOnline();
        checkStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        checkStates(callback);
    }

    void checkStates(CallbackMock callback) {
        State otaStatus = callback.getState("dirigera:double-shortcut:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:double-shortcut:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:double-shortcut:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");
        State batteryState = callback.getState("dirigera:double-shortcut:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(89, ((QuantityType<?>) batteryState).intValue(), "Battery level");
    }

    @Test
    void testTriggers() {
        testInitialization();
        String updateSequence = FileReader
                .readFileInString("src/test/resources/websocket/scene-pressed/scene-trigger-sequence.json");
        JSONArray sequence = new JSONArray(updateSequence);
        Map<String, String> sceneMapping = handler.sceneMapping;
        // adapt id of scene to match created one
        String sceneId = sceneMapping.get(deviceId + ":" + CHANNEL_BUTTON_1 + ":singlePress");
        assertNotNull(sceneId);
        JSONObject first = sequence.getJSONObject(0).getJSONObject("data");
        first.put(PROPERTY_DEVICE_ID, sceneId);
        handler.handleUpdate(first);
        JSONObject second = sequence.getJSONObject(1).getJSONObject("data");
        second.put(PROPERTY_DEVICE_ID, sceneId);
        handler.handleUpdate(second);
        assertEquals("SHORT_PRESSED", callback.triggerMap.get("dirigera:double-shortcut:test-device:button1"),
                "Pressed trigger sent");
    }

    @Test
    void testRemoval() {
        logger.warn("####### REMOVAL START ############");
        DirigeraAPISimu.scenesAdded.clear();
        DirigeraAPISimu.scenesDeleted.clear();
        testTriggers();
        logger.warn("####### TRIGGER CALLED ############");
        handler.dispose();
        handler.handleRemoval();
        Collections.sort(DirigeraAPISimu.scenesAdded);
        Collections.sort(DirigeraAPISimu.scenesDeleted);
        assertEquals(6, DirigeraAPISimu.scenesAdded.size(), "Scenes added size");
        assertEquals(6, DirigeraAPISimu.scenesDeleted.size(), "Scenes removed size");
        assertEquals(DirigeraAPISimu.scenesAdded, DirigeraAPISimu.scenesDeleted, "Scenes added equals scnes removed");
    }
}
