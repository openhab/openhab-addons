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
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestContactDevice} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestContactDevice {
    @Test
    void testContactDevice() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_CONTACT_SENSOR, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        ContactSensorHandler handler = new ContactSensorHandler(thing, CONTACT_SENSOR_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "07cca6c2-f2b6-4f57-bfd9-a788a16d1eef_1");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkContactStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_CONTACT), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        checkContactStates(callback);
    }

    void checkContactStates(CallbackMock callback) {
        State batteryState = callback.getState("dirigera:contact-sensor:test-device:battery-level");
        assertNotNull(batteryState);
        assertTrue(batteryState instanceof QuantityType);
        assertTrue(((QuantityType<?>) batteryState).getUnit().equals(Units.PERCENT));
        assertEquals(84, ((QuantityType<?>) batteryState).intValue(), "Battery level");
        State openCloseState = callback.getState("dirigera:contact-sensor:test-device:contact");
        assertNotNull(openCloseState);
        assertTrue(openCloseState instanceof OpenClosedType);
        assertTrue(OpenClosedType.CLOSED.equals((openCloseState)), "Closed");
    }
}
