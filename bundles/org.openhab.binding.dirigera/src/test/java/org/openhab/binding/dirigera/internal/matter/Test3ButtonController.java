/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.matter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.FileReader;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.matter.Matter3ButtonCotroller;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link Test3ButtonController} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class Test3ButtonController {
    private static String deviceId = "4fec48cd-6da8-4075-af78-09fff0423f78_3";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_3_BUTTON_CONTROLLER;

    private static Matter3ButtonCotroller handler = mock(Matter3ButtonCotroller.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testAllGroups() {
        deviceId = "4fec48cd-6da8-4075-af78-09fff0423f78_9";
        testHandlerCreation();
        deviceId = "4fec48cd-6da8-4075-af78-09fff0423f78_6";
        testHandlerCreation();
        deviceId = "4fec48cd-6da8-4075-af78-09fff0423f78_3";
        testHandlerCreation();
    }

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof Matter3ButtonCotroller);
        handler = (Matter3ButtonCotroller) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        callback.waitForOnline();
        thing.getChannels().forEach(channel -> {
            System.out.println("Channels: " + channel.getUID().toString());
        });
        assertEquals(11, thing.getChannels().size(), "Number of channels");
    }

    @Test
    void testInitialization() {
        testHandlerCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkDeviceStatus(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), "control-mode"), RefreshType.REFRESH);
        checkDeviceStatus(callback);
    }

    @Test
    void testControlMode() {
        testHandlerCreation();
        handler.handleCommand(new ChannelUID(thing.getUID(), "control-mode"), new DecimalType(0));
        System.out.println("Patch Map: " + DirigeraAPISimu.patchMap);
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"controlMode\":\"light\"}}", patch, "Light attributes");

        DirigeraAPISimu.patchMap.clear();
        String command = "HollaDieWaldfee";
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME), new StringType(command));
        System.out.println("Patch Map: " + DirigeraAPISimu.patchMap);
        patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"customName\":\"" + command + "\"}}", patch, "Custom Name");
    }

    @Test
    void testTrigger() {
        String remotePressEvent = FileReader
                .readFileInString("src/test/resources/devices/remote-press-event-3-button-controller.json");
        JSONObject eventObj = new JSONObject(remotePressEvent);
        testHandlerCreation();
        handler.handleUpdate(eventObj.getJSONObject("data"));
        String trigger1 = callback.triggerMap.get("dirigera:three-button-switch:test-device:scroll-up");
        System.out.println("Trigger Map: " + callback.triggerMap);
        assertNotNull(trigger1);
        assertEquals("SINGLE_PRESS", trigger1, "Scroll Up Press");
    }

    @Test
    void testLinks() {
        testHandlerCreation();
        System.out.println("--start update ");
        handler.updateCandidateLinks();
    }

    void checkDeviceStatus(CallbackMock callback) {
        State batteryLevel = callback.getState("dirigera:three-button-switch:test-device:battery-level");
        assertNotNull(batteryLevel);
        assertTrue(batteryLevel instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryLevel).getUnit().equals(Units.PERCENT));
        assertEquals(60, ((QuantityType<?>) batteryLevel).intValue(), "Battery Level");

        State controlMode = callback.getState("dirigera:three-button-switch:test-device:control-mode");
        assertNotNull(controlMode);
    }
}
