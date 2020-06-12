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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.junit.Test;
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
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        if (pmJson != null) {
            int result = condHandler.updateChannels(pmJson);
            assertEquals("Valid update", 0, result);
            assertEquals("Temperature", new DecimalType("22.70"), condHandler.getTemperature());
            assertEquals("Humidity", new DecimalType("61.00"), condHandler.getHumidity());
            assertEquals("Pressure", new DecimalType(-1), condHandler.getPressure());
            assertEquals("Pressure Sea", new DecimalType(-1), condHandler.getPressureSea());
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testValidWithPressureUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/condition-result-plus-pressure.json");
        if (pmJson != null) {
            int result = condHandler.updateChannels(pmJson);
            assertEquals("Valid update", 0, result);
            assertEquals("Temperature", new DecimalType("16.72"), condHandler.getTemperature());
            assertEquals("Humidity", new DecimalType("74.84"), condHandler.getHumidity());
            assertEquals("Pressure", new DecimalType("996.53"), condHandler.getPressure());
            assertEquals("Pressure Sea", new DecimalType("1014.42"), condHandler.getPressureSea());
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testInvalidUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/noise-result.json");
        if (pmJson != null) {
            int result = condHandler.updateChannels(pmJson);
            assertEquals("Valid update", 2, result);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testEmptyUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        int result = condHandler.updateChannels("[]");
        assertEquals("Valid update", 3, result);
    }

    @Test
    public void testNullUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        ConditionHandlerExtension condHandler = new ConditionHandlerExtension(t);
        int result = condHandler.updateChannels(null);
        assertEquals("Valid update", 1, result);
    }
}
