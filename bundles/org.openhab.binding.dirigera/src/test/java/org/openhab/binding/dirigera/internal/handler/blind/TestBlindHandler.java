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
package org.openhab.binding.dirigera.internal.handler.blind;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
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
 * {@link TestBlindHandler} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestBlindHandler {
    String deviceId = "eadfad54-9d23-4475-92b6-0ee3d6f8b481_1";
    ThingTypeUID thingTypeUID = THING_TYPE_BLIND;

    private static BlindHandler handler = mock(BlindHandler.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof BlindHandler);
        handler = (BlindHandler) factoryHandler;
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
        checkBlindStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BLIND_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BLIND_LEVEL), RefreshType.REFRESH);
        checkBlindStates(callback);
    }

    @Test
    void testCommands() {
        testHandlerCreation();
        // DirigeraAPISimu api = (DirigeraAPISimu) ((DirigeraHandler) hubBridge.getHandler()).api();
        handler.handleCommand(new ChannelUID(thing.getUID(), "blind-level"), new PercentType(20));
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"blindsTargetLevel\":20}}", patch, "Target Blind Level");
    }

    void checkBlindStates(CallbackMock callback) {
        State decimalState = callback.getState("dirigera:blind:test-device:blind-state");
        assertNotNull(decimalState);
        assertTrue(decimalState instanceof DecimalType);
        assertEquals(0, ((DecimalType) decimalState).intValue(), "Blind State");

        State currentLevelState = callback.getState("dirigera:blind:test-device:blind-level");
        assertNotNull(currentLevelState);
        assertTrue(currentLevelState instanceof PercentType);
        assertEquals(60, ((PercentType) currentLevelState).intValue(), "Blind Level");

        State batteryState = callback.getState("dirigera:blind:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(92, ((QuantityType<?>) batteryState).intValue(), "Battery level");

        // test ota
        State otaStatus = callback.getState("dirigera:blind:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:blind:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:blind:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");
    }
}
