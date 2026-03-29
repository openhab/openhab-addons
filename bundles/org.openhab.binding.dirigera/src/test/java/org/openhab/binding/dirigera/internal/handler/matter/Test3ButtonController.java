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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.FileReader;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
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
    private static String deviceIdGroup1 = "4fec48cd-6da8-4075-af78-09fff0423f78_3";
    private static String deviceIdGroup2 = "4fec48cd-6da8-4075-af78-09fff0423f78_6";
    private static String deviceIdGroup3 = "4fec48cd-6da8-4075-af78-09fff0423f78_9";
    List<String> deviceIds = List.of(deviceIdGroup1, deviceIdGroup2, deviceIdGroup3);
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_3_BUTTON_CONTROLLER;

    BaseMatterHandler getHandler(String deviceId) {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof BaseMatterHandler);
        return (BaseMatterHandler) factoryHandler;
    }

    @Test
    void testHandlerCreation() {
        deviceIds.forEach(deviceId -> {
            BaseMatterHandler factoryHandler = getHandler(deviceId);
            assertNotNull(factoryHandler);
            assertTrue(factoryHandler instanceof Matter3ButtonController);
            Matter3ButtonController handler = (Matter3ButtonController) factoryHandler;
            Thing thing = handler.getThing();
            ThingHandlerCallback proxyCallback = handler.getCallback();
            assertNotNull(proxyCallback);
            assertTrue(proxyCallback instanceof CallbackMock);
            CallbackMock callback = (CallbackMock) proxyCallback;
            callback.waitForOnline();
            assertEquals(23, thing.getChannels().size(), "Number of channels");
        });
    }

    @Test
    void testInitialization() {
        Matter3ButtonController handler = (Matter3ButtonController) getHandler(deviceIdGroup1);
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        checkDeviceStatus(callback);

        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkDeviceStatus(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), "switch#" + CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), "switch-3#control-mode"), RefreshType.REFRESH);
        checkDeviceStatus(callback);
    }

    @Test
    void testControlMode() {
        Matter3ButtonController handler = (Matter3ButtonController) getHandler(deviceIdGroup2);
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        String targetChannel = "switch-2#control-mode";
        String targetId = deviceIdGroup2;
        handler.handleCommand(new ChannelUID(thing.getUID(), targetChannel), new DecimalType(2));
        String patch = api.getPatch(targetId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"controlMode\":\"light\"}}", patch, "Light attributes");

        api.clear();
        String command = "HollaDieWaldfee";
        handler.handleCommand(new ChannelUID(thing.getUID(), "switch#" + CHANNEL_CUSTOM_NAME), new StringType(command));
        patch = api.getPatch(deviceIdGroup2);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"customName\":\"" + command + "\"}}", patch, "Custom Name");
    }

    @Test
    void testTrigger() {
        Matter3ButtonController handler = (Matter3ButtonController) getHandler(deviceIdGroup2);
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);

        String remotePressEvent = FileReader
                .readFileInString("src/test/resources/devices/remote-press-event-3-button-controller.json");
        JSONObject eventObj = new JSONObject(remotePressEvent);
        testHandlerCreation();
        handler.handleUpdate(eventObj.getJSONObject("data"));
        String trigger1 = callback.triggerMap.get("dirigera:three-button-switch:test-device:switch-1#scroll-up");
        assertNotNull(trigger1);
        assertEquals("SINGLE_PRESS", trigger1, "Scroll Up Press");
    }

    @Test
    void testLinks() {
        Matter3ButtonController handler = (Matter3ButtonController) getHandler(deviceIdGroup2);
        CallbackMock callback = (CallbackMock) handler.getCallback();
        Thing thing = handler.getThing();
        assertNotNull(callback);
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        // wait until link candidates are initialized, otherwise the command will be ignored and test will fail
        callback.getState("dirigera:three-button-switch:test-device:switch-1#link-candidates");
        String lightDeviceId = "8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1";
        String targetChannel = "switch-1#" + CHANNEL_LINK_CANDIDATES;
        handler.handleCommand(new ChannelUID(thing.getUID(), targetChannel), StringType.valueOf(lightDeviceId));
        assertEquals("{\"remoteLinks\":[\"4fec48cd-6da8-4075-af78-09fff0423f78_3\"]}",
                api.getPatch("8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1"), "Link Candidate Patch");
    }

    void checkDeviceStatus(CallbackMock callback) {
        State batteryLevel = callback.getState("dirigera:three-button-switch:test-device:switch#battery-level");
        assertNotNull(batteryLevel);
        assertTrue(batteryLevel instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryLevel).getUnit().equals(Units.PERCENT));
        assertEquals(60, ((QuantityType<?>) batteryLevel).intValue(), "Battery Level");

        State controlMode = callback.getState("dirigera:three-button-switch:test-device:switch-3#control-mode");
        assertNotNull(controlMode);
    }
}
