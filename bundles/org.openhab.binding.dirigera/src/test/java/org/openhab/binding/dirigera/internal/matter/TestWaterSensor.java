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
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.handler.matter.MatterSensor;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
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
 * {@link TestWaterSensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestWaterSensor {
    private static String deviceId = "9bf826ad-a8ad-40bf-8aed-125300bccd20_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_WATER_LEAK_SENSOR;

    private static MatterSensor handler = mock(MatterSensor.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof MatterSensor);
        handler = (MatterSensor) factoryHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = handler.getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        System.out.println("thing channels: " + thing.getChannels().size());
        thing.getChannels().forEach(channel -> {
            System.out.println(" Channel: " + channel.getUID() + " type: " + channel.getChannelTypeUID());
        });
        System.out.println("Properties: " + thing.getProperties().size());
        thing.getProperties().forEach((key, value) -> {
            System.out.println(" Property: " + key + " value: " + value);
        });
        callback.waitForOnline();
    }

    @Test
    void testInitialization() {
        testHandlerCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);
        checkEnvironmentSensorStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LEAK_DETECTION), RefreshType.REFRESH);
        checkEnvironmentSensorStates(callback);
    }

    void checkEnvironmentSensorStates(CallbackMock callback) {
        State leakState = callback.getState("dirigera:water-leak-sensor:test-device:leak");
        assertNotNull(leakState);
        assertTrue(leakState instanceof OnOffType);
        assertEquals(OnOffType.OFF, leakState, "Battery Level");
    }
}
