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
package org.openhab.binding.dirigera.internal.handler.sensor;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestMotionLightSensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestMotionLightSensor {

    @Test
    void testMotionLightSensorDevice() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_MOTION_LIGHT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        MotionLightSensorHandler handler = new MotionLightSensorHandler(thing, MOTION_LIGHT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "5ac5e131-44a4-4d75-be78-759a095d31fb_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkMotionLightStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ILLUMINANCE), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_MOTION_DETECTION), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CUSTOM_NAME), RefreshType.REFRESH);
        checkMotionLightStates(callback);
    }

    void checkMotionLightStates(CallbackMock callback) {
        State batteryState = callback.getState("dirigera:motion-light-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(85, ((QuantityType<?>) batteryState).intValue(), "Battery level");
        State onOffState = callback.getState("dirigera:motion-light-sensor:test-device:motion");
        assertNotNull(onOffState);
        assertTrue(onOffState instanceof OnOffType);
        assertTrue(OnOffType.OFF.equals((onOffState)), "Off");
        State luxState = callback.getState("dirigera:motion-light-sensor:test-device:illuminance");
        assertNotNull(luxState);
        assertTrue(luxState instanceof QuantityType);
        assertTrue(((QuantityType<?>) luxState).getUnit().equals(Units.LUX));
        assertEquals(1, ((QuantityType<?>) luxState).intValue(), "Lux level");
    }
}
