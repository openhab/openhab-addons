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
package org.openhab.binding.dirigera.internal.handler.plug;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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
 * {@link TestSimplePlug} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestSimplePlug {
    String deviceId = "6379a590-dc0a-47b5-b35b-7b46dfefd282_1";
    ThingTypeUID thingTypeUID = THING_TYPE_SIMPLE_PLUG;

    BaseHandler getHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof SimplePlugHandler);
        assertTrue(factoryHandler instanceof BaseHandler);
        BaseHandler handler = (SimplePlugHandler) factoryHandler;
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        return handler;
    }

    @Test
    void testInitialization() {
        BaseHandler handler = getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkPowerPlugStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CHILD_LOCK), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_DISABLE_STATUS_LIGHT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), RefreshType.REFRESH);
        checkPowerPlugStates(callback);
    }

    void checkPowerPlugStates(CallbackMock callback) {
        State otaStatus = callback.getState("dirigera:simple-plug:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:simple-plug:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:simple-plug:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");

        State onOffState = callback.getState("dirigera:simple-plug:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals((onOffState)), "Power On");
        State startupState = callback.getState("dirigera:simple-plug:test-device:startup");
        assertNotNull(startupState);
        assertTrue(startupState instanceof DecimalType);
        assertEquals(0, ((DecimalType) startupState).intValue(), "Startup Behavior");
    }
}
