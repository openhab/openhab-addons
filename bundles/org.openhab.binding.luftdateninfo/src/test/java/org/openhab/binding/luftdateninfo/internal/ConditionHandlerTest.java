/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luftdateninfo.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.luftdateninfo.internal.handler.BaseSensorHandler.UpdateStatus;
import org.openhab.binding.luftdateninfo.internal.mock.ConditionHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link ConditionHandlerTest} Test Condition Handler updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConditionHandlerTest {

    @Test
    public void testValidNoPressureUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        if (pmJson != null) {
            UpdateStatus result = condHandler.updateChannels(pmJson);
            assertEquals(UpdateStatus.OK, result, "Valid update");
            assertEquals(QuantityType.valueOf(22.7, SIUnits.CELSIUS), condHandler.getTemperature(), "Temperature");
            assertEquals(QuantityType.valueOf(61., Units.PERCENT), condHandler.getHumidity(), "Humidity");
            assertEquals(QuantityType.valueOf(-1, HECTO(SIUnits.PASCAL)), condHandler.getPressure(), "Pressure");
            assertEquals(QuantityType.valueOf(-1, HECTO(SIUnits.PASCAL)), condHandler.getPressureSea(), "Pressure Sea");
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testValidWithPressureUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/condition-result-plus-pressure.json");
        if (pmJson != null) {
            UpdateStatus result = condHandler.updateChannels(pmJson);
            assertEquals(UpdateStatus.OK, result, "Valid update");
            assertEquals(QuantityType.valueOf(21.5, SIUnits.CELSIUS), condHandler.getTemperature(), "Temperature");
            assertEquals(QuantityType.valueOf(58.5, Units.PERCENT), condHandler.getHumidity(), "Humidity");
            assertEquals(QuantityType.valueOf(1002.0, HECTO(SIUnits.PASCAL)), condHandler.getPressure(), "Pressure");
            assertEquals(QuantityType.valueOf(1019.7, HECTO(SIUnits.PASCAL)), condHandler.getPressureSea(),
                    "Pressure Sea");
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testInvalidUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/noise-result.json");
        if (pmJson != null) {
            UpdateStatus result = condHandler.updateChannels(pmJson);
            assertEquals(UpdateStatus.VALUE_ERROR, result, "Valid update");
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testEmptyUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        UpdateStatus result = condHandler.updateChannels("[]");
        assertEquals(UpdateStatus.VALUE_EMPTY, result, "Valid update");
    }

    @Test
    public void testNullUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        UpdateStatus result = condHandler.updateChannels(null);
        assertEquals(UpdateStatus.CONNECTION_ERROR, result, "Valid update");
    }

    @Test
    public void testInternalUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("ipAddress", "192.168.178.1");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/internal-data.json");
        if (pmJson != null) {
            UpdateStatus result = condHandler.updateChannels("[" + pmJson + "]");
            assertEquals(UpdateStatus.OK, result, "Valid update");
            assertEquals(QuantityType.valueOf(17.6, SIUnits.CELSIUS), condHandler.getTemperature(), "Temperature");
            assertEquals(QuantityType.valueOf(57.8, Units.PERCENT), condHandler.getHumidity(), "Humidity");
            assertEquals(QuantityType.valueOf(986.8, HECTO(SIUnits.PASCAL)), condHandler.getPressure(), "Pressure");
            assertEquals(QuantityType.valueOf(-1, HECTO(SIUnits.PASCAL)), condHandler.getPressureSea(), "Pressure Sea");
        } else {
            assertTrue(false);
        }
    }
}
