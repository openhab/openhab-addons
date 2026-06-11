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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link Test2ButtonController} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class Test2ButtonController {
    private static String deviceId = "040bf20b-b1b0-463b-af4b-1227a711d70e_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_2_BUTTON_CONTROLLER;

    BaseMatterHandler getHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof BaseMatterHandler);
        return (BaseMatterHandler) factoryHandler;
    }

    @Test
    void testHandlerCreation() {
        ThingHandler factoryHandler = getHandler();
        assertTrue(factoryHandler instanceof Matter2ButtonController);
        Matter2ButtonController handler = (Matter2ButtonController) factoryHandler;
        Thing thing = handler.getThing();
        assertEquals(10, thing.getChannels().size(), "Number of channels");
    }

    @Test
    void testInitialization() {
        Matter2ButtonController handler = (Matter2ButtonController) getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        checkDeviceStatus(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), "control-mode"), RefreshType.REFRESH);
        checkDeviceStatus(callback);
    }

    @Test
    void testControlMode() {
        Matter2ButtonController handler = (Matter2ButtonController) getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        handler.handleCommand(new ChannelUID(thing.getUID(), "control-mode"), new DecimalType(2));
        String patch = api.getPatch(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"controlMode\":\"light\"}}", patch, "Light attributes");

        api.clear();
        String command = "HollaDieWaldfee";
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME), new StringType(command));
        patch = api.getPatch(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"customName\":\"" + command + "\"}}", patch, "Custom Name");
    }

    @Test
    void testTrigger() {
        Matter2ButtonController handler = (Matter2ButtonController) getHandler();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);

        String remotePressEvent = FileReader.readFileInString("src/test/resources/devices/remote-press-event.json");
        JSONObject eventObj = new JSONObject(remotePressEvent);
        testHandlerCreation();
        handler.handleUpdate(eventObj.getJSONObject("data"));
        String trigger1 = callback.triggerMap.get("dirigera:two-button-switch:test-device:lower-button");
        assertNotNull(trigger1);
        assertEquals("SINGLE_PRESS", trigger1, "Button 2 Single Press");
    }

    @Test
    void testLinks() {
        Matter2ButtonController handler = (Matter2ButtonController) getHandler();
        testHandlerCreation();
        handler.updateLinksDone();
    }

    void checkDeviceStatus(CallbackMock callback) {
        State batteryLevel = callback.getState("dirigera:two-button-switch:test-device:battery-level");
        assertNotNull(batteryLevel);
        assertTrue(batteryLevel instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryLevel).getUnit().equals(Units.PERCENT));
        assertEquals(61, ((QuantityType<?>) batteryLevel).intValue(), "Battery Level");

        State controlMode = callback.getState("dirigera:two-button-switch:test-device:control-mode");
        assertNotNull(controlMode);
    }
}
