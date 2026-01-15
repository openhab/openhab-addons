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
import org.openhab.binding.dirigera.internal.handler.matter.Matter2ButtonCotroller;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
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

/**
 * {@link Test2ButtonController} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class Test2ButtonController {
    private static String deviceId = "040bf20b-b1b0-463b-af4b-1227a711d70e_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_2_BUTTON_CONTROLLER;

    private static Matter2ButtonCotroller handler = mock(Matter2ButtonCotroller.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof Matter2ButtonCotroller);
        handler = (Matter2ButtonCotroller) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        callback.waitForOnline();
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
        checkDeviceStatus(callback);
    }

    @Test
    void testTrigger() {
        String remotePressEvent = FileReader.readFileInString("src/test/resources/devices/remote-press-event.json");
        testHandlerCreation();
        handler.handleUpdate(new JSONObject(remotePressEvent));
        String trigger1 = callback.triggerMap.get("dirigera:2-button-controller:test-device:button2");
        assertNotNull(trigger1);
        assertEquals("SINGLE_PRESS", trigger1, "Button 2 Single Press");
    }

    void checkDeviceStatus(CallbackMock callback) {
        State batteryLevel = callback.getState("dirigera:2-button-controller:test-device:battery-level");
        assertNotNull(batteryLevel);
        assertTrue(batteryLevel instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryLevel).getUnit().equals(Units.PERCENT));
        assertEquals(61, ((QuantityType<?>) batteryLevel).intValue(), "Battery Level");
    }
}
