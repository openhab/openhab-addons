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
package org.openhab.binding.dirigera.internal.handlerplug;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.plug.SmartPlugHandler;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DateTimeType;
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
 * {@link TestSmartPlug} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestSmartPlug {
    String deviceId = "ec549fa8-4e35-4f27-90e9-bb67e68311f2_1";
    ThingTypeUID thingTypeUID = THING_TYPE_SMART_PLUG;

    private static SmartPlugHandler handler = mock(SmartPlugHandler.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/devices/home-all-devices.json",
                false, List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof SmartPlugHandler);
        handler = (SmartPlugHandler) factoryHandler;
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
        checkSmartPlugStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CHILD_LOCK), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_DISABLE_STATUS_LIGHT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CURRENT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POTENTIAL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ENERGY_TOTAL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ENERGY_RESET), RefreshType.REFRESH);
        checkSmartPlugStates(callback);
    }

    @Test
    void testCommands() {
        DirigeraAPISimu.patchMap.clear();
        testHandlerCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ENERGY_RESET_DATE), new DateTimeType());
        DirigeraAPISimu.waitForPatch();
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"energyConsumedAtLastReset\":0}}", patch, "Reset Power");
        DirigeraAPISimu.patchMap.clear();
    }

    void checkSmartPlugStates(CallbackMock callback) {
        State otaStatus = callback.getState("dirigera:smart-plug:test-device:ota-status");
        assertNotNull(otaStatus);
        assertTrue(otaStatus instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaStatus).intValue(), "OTA Status");
        State otaState = callback.getState("dirigera:smart-plug:test-device:ota-state");
        assertNotNull(otaState);
        assertTrue(otaState instanceof DecimalType);
        assertEquals(0, ((DecimalType) otaState).intValue(), "OTA State");
        State otaProgess = callback.getState("dirigera:smart-plug:test-device:ota-progress");
        assertNotNull(otaProgess);
        assertTrue(otaProgess instanceof QuantityType);
        assertTrue(((QuantityType<?>) otaProgess).getUnit().equals(Units.PERCENT));
        assertEquals(0, ((QuantityType<?>) otaProgess).intValue(), "OTA Progress");

        State disableLightState = callback.getState("dirigera:smart-plug:test-device:disable-status-light");
        assertNotNull(disableLightState);
        assertTrue(disableLightState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((disableLightState)), "Disable Light Off");
        State childlockState = callback.getState("dirigera:smart-plug:test-device:child-lock");
        assertNotNull(childlockState);
        assertTrue(childlockState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((childlockState)), "Child Lock Off");
        State onOffState = callback.getState("dirigera:smart-plug:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.ON.equals((onOffState)), "Power On");

        // Measurement channels
        State ampereState = callback.getState("dirigera:smart-plug:test-device:electric-current");
        assertNotNull(ampereState);
        assertTrue(ampereState instanceof QuantityType);
        assertTrue(((QuantityType<?>) ampereState).getUnit().equals(Units.AMPERE));
        assertEquals(0, ((QuantityType<?>) ampereState).intValue(), "Ampere");
        State voltState = callback.getState("dirigera:smart-plug:test-device:electric-voltage");
        assertNotNull(voltState);
        assertTrue(voltState instanceof QuantityType);
        assertTrue(((QuantityType<?>) voltState).getUnit().equals(Units.VOLT));
        assertEquals(236, ((QuantityType<?>) voltState).intValue(), "Volt");
        State powerState = callback.getState("dirigera:smart-plug:test-device:electric-power");
        assertNotNull(powerState);
        assertTrue(powerState instanceof QuantityType);
        assertTrue(((QuantityType<?>) powerState).getUnit().equals(Units.WATT));
        assertEquals(0, ((QuantityType<?>) powerState).intValue(), "Watt");
        State energyTotalState = callback.getState("dirigera:smart-plug:test-device:energy-total");
        assertNotNull(energyTotalState);
        assertTrue(energyTotalState instanceof QuantityType);
        assertTrue(((QuantityType<?>) energyTotalState).getUnit().equals(Units.KILOWATT_HOUR));
        assertEquals(0, ((QuantityType<?>) energyTotalState).intValue(), "Watt");
        State energyReset = callback.getState("dirigera:smart-plug:test-device:energy-reset");
        assertNotNull(energyReset);
        assertTrue(energyReset instanceof QuantityType);
        assertTrue(((QuantityType<?>) energyReset).getUnit().equals(Units.KILOWATT_HOUR));
        assertEquals(0, ((QuantityType<?>) energyReset).intValue(), "Watt");

        State startupState = callback.getState("dirigera:smart-plug:test-device:startup");
        assertNotNull(startupState);
        assertTrue(startupState instanceof DecimalType);
        assertEquals(0, ((DecimalType) startupState).intValue(), "Startup Behavior");
    }
}
