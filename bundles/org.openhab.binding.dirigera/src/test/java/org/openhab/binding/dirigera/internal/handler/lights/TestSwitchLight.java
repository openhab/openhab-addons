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
package org.openhab.binding.dirigera.internal.handler.lights;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.light.SwitchLightHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestSwitchLight} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestSwitchLight {
    String deviceId = "eb9a4367-9e23-4d37-9566-401a7ae7caf0_2";
    ThingTypeUID thingTypeUID = THING_TYPE_SWITCH_LIGHT;

    private static SwitchLightHandler handler = mock(SwitchLightHandler.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof SwitchLightHandler);
        handler = (SwitchLightHandler) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        handler.initialize();
        callback.waitForOnline();
    }

    @Test
    void testInitialization() {
        testHandlerCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        chackStatus(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), RefreshType.REFRESH);
        chackStatus(callback);
    }

    @Test
    void testCommands() {
        DirigeraAPISimu.patchMap.clear();
        testHandlerCreation();
        // DirigeraAPISimu api = (DirigeraAPISimu) ((DirigeraHandler) hubBridge.getHandler()).api();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.ON);
        DirigeraAPISimu.waitForPatch();
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"isOn\":true}}", patch, "Power on");
        DirigeraAPISimu.patchMap.clear();

        // simulate feedback
        handler.handleUpdate(new JSONObject("{\"attributes\": {\"isOn\": true}}"));

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.OFF);
        DirigeraAPISimu.waitForPatch();
        patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"isOn\":false}}", patch, "Power off");
    }

    void chackStatus(CallbackMock callback) {
        State onOffState = callback.getState("dirigera:switch-light:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertEquals(OnOffType.OFF, onOffState, "Power Status");

        State startupState = callback.getState("dirigera:switch-light:test-device:startup");
        assertNotNull(startupState);
        assertTrue(startupState instanceof DecimalType);
        assertEquals(1, ((DecimalType) startupState).intValue(), "Startup State");
    }
}
