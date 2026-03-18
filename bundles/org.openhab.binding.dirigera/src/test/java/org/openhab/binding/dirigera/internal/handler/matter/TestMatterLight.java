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
import org.openhab.binding.dirigera.internal.handler.BaseHandler;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link TestMatterLight} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestMatterLight {
    private static String deviceId = "8d89e4f6-cb60-443b-9c68-3094fc15e0e6_1";
    private static ThingTypeUID thingTypeUID = THING_TYPE_MATTER_LIGHT;

    BaseHandler getHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge("src/test/resources/home/matter-home.json", false,
                List.of());
        ThingHandler factoryHandler = DirigeraBridgeProvider.createHandler(thingTypeUID, hubBridge, deviceId);
        assertTrue(factoryHandler instanceof BaseHandler);
        return (BaseHandler) factoryHandler;
    }

    @Test
    void testHandlerCreation() {
        ThingHandler factoryHandler = getHandler();
        assertTrue(factoryHandler instanceof MatterLight);
        MatterLight handler = (MatterLight) factoryHandler;
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);
        callback.waitForOnline();
        assertEquals(10, thing.getChannels().size(), "Number of channels");
        assertEquals(11, thing.getProperties().size(), "Number of properties");
    }

    @Test
    void testInitialization() {
        MatterLight handler = (MatterLight) getHandler();
        Thing thing = handler.getThing();
        CallbackMock callback = (CallbackMock) handler.getCallback();
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
        MatterLight handler = (MatterLight) getHandler();
        Thing thing = handler.getThing();
        DirigeraAPISimu api = (DirigeraAPISimu) handler.gateway().api();

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LIGHT_TEMPERATURE), new PercentType(50));
        String patch = api.getPatch(deviceId);
        assertEquals("{\"attributes\":{\"colorTemperature\":4168}}", patch, "Light Temperature command");
    }

    @Test
    void testOffline() {
        MatterLight handler = (MatterLight) getHandler();
        CallbackMock callback = (CallbackMock) handler.getCallback();
        assertNotNull(callback);

        String update = FileReader.readFileInString("src/test/resources/devices/matter-kajplats-offline.json");
        handler.handleUpdate(new JSONObject(update));
        State onOffState = callback.getState("dirigera:matter-light:test-device:power");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof UnDefType);
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
    }
}
