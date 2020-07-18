/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.handler.BaseSensorHandler.UpdateStatus;
import org.openhab.binding.luftdateninfo.internal.mock.ConditionHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;

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
            assertEquals("Valid update", UpdateStatus.OK, result);
            assertEquals("Temperature", QuantityType.valueOf(22.7, SIUnits.CELSIUS), condHandler.getTemperature());
            assertEquals("Humidity", QuantityType.valueOf(61.0, SmartHomeUnits.PERCENT), condHandler.getHumidity());
            assertEquals("Pressure", QuantityType.valueOf(-1, SIUnits.PASCAL), condHandler.getPressure());
            assertEquals("Pressure Sea", QuantityType.valueOf(-1, SIUnits.PASCAL), condHandler.getPressureSea());
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
            assertEquals("Valid update", UpdateStatus.OK, result);
            assertEquals("Temperature", QuantityType.valueOf(21.5, SIUnits.CELSIUS), condHandler.getTemperature());
            assertEquals("Humidity", QuantityType.valueOf(58.5, SmartHomeUnits.PERCENT), condHandler.getHumidity());
            assertEquals("Pressure", QuantityType.valueOf(100200.0, SIUnits.PASCAL), condHandler.getPressure());
            assertEquals("Pressure Sea", QuantityType.valueOf(101968.7, SIUnits.PASCAL), condHandler.getPressureSea());
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
            assertEquals("Valid update", UpdateStatus.VALUE_ERROR, result);
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
        assertEquals("Valid update", UpdateStatus.VALUE_EMPTY, result);
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
        assertEquals("Valid update", UpdateStatus.CONNECTION_ERROR, result);
    }
}
