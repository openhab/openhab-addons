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
import org.openhab.binding.dirigera.internal.handler.matter.MatterLight;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestMatterLight} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestMatterLight {
    private static String deviceId = "8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_LIGHT;

    private static MatterLight handler = mock(MatterLight.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);

    @Test
    void testHandlerCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof MatterLight);
        handler = (MatterLight) factoryHandler;
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
        checkEnvironmentSensorStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_BRIGHTNESS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE_ABS), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_COLOR), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR), RefreshType.REFRESH);
        checkEnvironmentSensorStates(callback);
    }

    @Test
    void testCommand() {
        testHandlerCreation();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE), new PercentType(50));
        String patch = DirigeraAPISimu.patchMap.get("8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1");
        assertEquals("{\"attributes\":{\"colorTemperature\":4168}}", patch, "Light Temperature command");
    }

    void checkEnvironmentSensorStates(CallbackMock callback) {
        State onOffState = callback.getState("dirigera:matter-light:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Power State");

        State brighnessState = callback.getState("dirigera:matter-light:test-device:brightness");
        assertNotNull(brighnessState);
        assertTrue(brighnessState instanceof PercentType);
        assertEquals(0, ((PercentType) brighnessState).intValue(), "Brightness");

        State colorTemperatureState = callback.getState("dirigera:matter-light:test-device:color-temperature");
        assertNotNull(colorTemperatureState);
        assertTrue(colorTemperatureState instanceof PercentType);
        assertEquals(87, ((PercentType) colorTemperatureState).intValue(), "Color Temperature");

        State colorTemperatureStateAbs = callback.getState("dirigera:matter-light:test-device:color-temperature-abs");
        assertNotNull(colorTemperatureStateAbs);
        assertTrue(colorTemperatureStateAbs instanceof QuantityType);
        assertEquals(2450, ((QuantityType<?>) colorTemperatureStateAbs).intValue(), "Color Temperature Absolute");

        State hsbState = callback.getState("dirigera:matter-light:test-device:color");
        assertNotNull(hsbState);
        assertTrue(hsbState instanceof HSBType);
        assertEquals(29, ((HSBType) hsbState).getHue().intValue(), "Hue");
        assertEquals(35, ((HSBType) hsbState).getSaturation().intValue(), "Saturation");
        // brightness of device is 100 (previous state) but due to power OFF state it's reflected as 0
        assertEquals(0, ((HSBType) hsbState).getBrightness().intValue(), "Brightness");
        // assertEquals(100, ((HSBType) hsbState).getBrightness().intValue(), "Brightness");
    }
}
