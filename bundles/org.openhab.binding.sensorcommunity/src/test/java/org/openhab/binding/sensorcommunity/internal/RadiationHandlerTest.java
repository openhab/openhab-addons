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
package org.openhab.binding.sensorcommunity.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sensorcommunity.internal.handler.BaseSensorHandler.UpdateStatus;
import org.openhab.binding.sensorcommunity.internal.handler.RadiationHandler;
import org.openhab.binding.sensorcommunity.internal.mock.CallbackMock;
import org.openhab.binding.sensorcommunity.internal.mock.ThingMock;
import org.openhab.binding.sensorcommunity.internal.util.FileReader;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

/**
 * The {@link RadiationHandlerTest} Test Radiation Handler updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RadiationHandlerTest {

    @Test
    public void testValidUpdate() {
        ThingMock t = new ThingMock();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        RadiationHandler radiationHandler = new RadiationHandler(t);
        CallbackMock callback = new CallbackMock();
        radiationHandler.setCallback(callback);
        String radiationJson = FileReader.readFileInString("src/test/resources/radiation-result.json");
        if (radiationJson != null) {
            UpdateStatus result = radiationHandler.updateChannels(radiationJson);
            assertEquals(UpdateStatus.OK, result, "Valid update");

            State radiationState = callback.getState("sensorcommunity::test:radiation");
            assertNotNull(radiationState, "Radiation state should not be null");
            assertTrue(radiationState instanceof QuantityType, "Radiation state should be of type QuantityType");
            assertEquals(0.0979, ((QuantityType<?>) radiationState).doubleValue(), 0.0001, "Radiation in Sv");

            State cpmState = callback.getState("sensorcommunity::test:counts-per-minute");
            assertNotNull(cpmState, "CPM state should not be null");
            assertTrue(cpmState instanceof DecimalType, "Radiation state should be of type DecimalType");
            assertEquals(72, ((DecimalType) cpmState).intValue(), "Counts per Minute");

            State radiationLevelState = callback.getState("sensorcommunity::test:radiation-level");
            assertNotNull(radiationLevelState, "Radiation level state should not be null");
            assertTrue(radiationLevelState instanceof DecimalType,
                    "Radiation level state should be of type DecimalType");
            assertEquals(0, ((DecimalType) radiationLevelState).intValue(), "Radiation level as number");

            State pulseState = callback.getState("sensorcommunity::test:hv-pulses");
            assertNotNull(pulseState, "Pulse state should not be null");
            assertTrue(pulseState instanceof DecimalType, "Pulse state should be of type QuantityType");
            assertEquals(30024, ((DecimalType) pulseState).intValue(), "Pulses as number");
        } else {
            assertTrue(false);
        }
    }
}
