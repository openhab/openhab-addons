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
import org.openhab.binding.luftdateninfo.internal.mock.NoiseHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NoiseHandlerTest} Test Noise Handler updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NoiseHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(PMHandlerTest.class);

    @Test
    public void testValidUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        NoiseHandlerExtension noiseHandler = new NoiseHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/noise-result.json");
        if (pmJson != null) {
            int result = noiseHandler.updateChannels(pmJson);
            assertEquals("Valid update", 0, result);
            assertEquals("Noise EQ", new DecimalType("51.0"), noiseHandler.getNoiseEQCache());
            assertEquals("Noise Min", new DecimalType("47.2"), noiseHandler.getNoiseMinCache());
            assertEquals("Noise Max", new DecimalType("57.0"), noiseHandler.getNoiseMaxCache());
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

        NoiseHandlerExtension noiseHandler = new NoiseHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        if (pmJson != null) {
            int result = noiseHandler.updateChannels(pmJson);
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

        NoiseHandlerExtension noiseHandler = new NoiseHandlerExtension(t);
        int result = noiseHandler.updateChannels("[]");
        assertEquals("Valid update", 3, result);
    }

    @Test
    public void testNullUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        NoiseHandlerExtension noiseHandler = new NoiseHandlerExtension(t);
        int result = noiseHandler.updateChannels(null);
        assertEquals("Valid update", 1, result);
    }
}
