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

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestMatterOutlet} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestMatterOutlet {

    private static String outletDeviceId = "27887dc3-151b-462f-bda6-105138ccc0ba_1";
    private static String electricSensorDeviceId = "27887dc3-151b-462f-bda6-105138ccc0ba_2";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_OUTLET;

    MatterOutlet getHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, outletDeviceId);
        assertTrue(factoryHandler instanceof MatterOutlet);
        return (MatterOutlet) factoryHandler;
    }

    @Test
    void testIdentification() {
        MatterOutlet handler = getHandler();
        Thing thing = handler.getThing();
        assertNotNull(thing);
        Model model = handler.gateway().model();
        assertEquals(thingTypeUID, model.identifyDeviceFromModel(outletDeviceId), "ThingTypeUID");
    }

    @Test
    void testHandlerCreation() {
        ThingHandler factoryHandler = getHandler();
        assertTrue(factoryHandler instanceof MatterOutlet);
        MatterOutlet handler = (MatterOutlet) factoryHandler;
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        callback.waitForOnline();
        assertEquals(12, thing.getChannels().size(), "Number of channels");
        assertEquals(10, thing.getProperties().size(), "Number of properties");
    }

    @Test
    void testInitialization() {
        MatterOutlet handler = getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkPowerPlugStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POTENTIAL), RefreshType.REFRESH);
        checkPowerPlugStates(callback);
    }

    @Test
    void testCommand() {
        MatterOutlet handler = getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), OnOffType.ON);
        String patch = api.getPatch(outletDeviceId);
        assertEquals("{\"attributes\":{\"isOn\":true}}", patch, "Power ON");
        api.clear();

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), new DecimalType(3));
        patch = api.getPatch(outletDeviceId);
        assertEquals("{\"attributes\":{\"startupOnOff\":\"startToggle\"}}", patch, "Startup behavior");
        api.clear();

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ENERGY_RESET_DATE),
                new DateTimeType(Instant.now()));
        patch = api.getPatch(electricSensorDeviceId);
        assertEquals("{\"attributes\":{\"energyConsumedAtLastReset\":0}}", patch, "Energy reset date");
        api.clear();
    }

    void checkPowerPlugStates(CallbackMock callback) {
        State onOffState = callback.getState("dirigera:matter-outlet:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Power State");

        State voltageState = callback.getState("dirigera:matter-outlet:test-device:electric-voltage");
        assertNotNull(voltageState);
        assertTrue(voltageState instanceof QuantityType);
        assertEquals(237, ((QuantityType<?>) voltageState).intValue(), "Voltage");
    }
}
