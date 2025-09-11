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
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestLightSensor} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestLightSensor {

    void testLightSensorDevice() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_LIGHT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        LightSensorHandler handler = new LightSensorHandler(thing, LIGHT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "ca856a7d-a715-42f7-84a1-7caae41e6ff2_3");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();

        State luxState = callback.getState("dirigera:light-sensor:test-device:illuminance");
        assertNotNull(luxState);
        assertTrue(luxState instanceof QuantityType);
        assertTrue(((QuantityType<?>) luxState).getUnit().equals(Units.LUX));
        assertEquals(1, ((QuantityType<?>) luxState).intValue(), "Lux level");

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_ILLUMINANCE), RefreshType.REFRESH);
        luxState = callback.getState("dirigera:light-sensor:test-device:illuminance");
        assertNotNull(luxState);
        assertTrue(luxState instanceof QuantityType);
        assertTrue(((QuantityType<?>) luxState).getUnit().equals(Units.LUX));
        assertEquals(1, ((QuantityType<?>) luxState).intValue(), "Lux level");
    }
}
