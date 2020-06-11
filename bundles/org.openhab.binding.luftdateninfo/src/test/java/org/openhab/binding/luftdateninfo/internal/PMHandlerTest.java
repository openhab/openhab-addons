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
import org.openhab.binding.luftdateninfo.internal.mock.PMHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PMHandlerTest} Test Particualte Matter Handler - Config and updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PMHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(PMHandlerTest.class);

    @Test
    public void testValidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        logger.info("LC status: {}", pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != 0 && retryCount < 20) {
            try {
                logger.info("LC running not reached - wait");
                Thread.sleep(500);
                retryCount++;
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
        /*
         * Test if config status is 0 = CONFIG_OK for valid configuration. Take real int for comparison instead of
         * BaseHandler constants - in case of change test needs to be adapted
         */
        assertEquals("Handler Configuration status", 0, pmHandler.getConfigStatus());
    }

    @Test
    public void testInvalidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "abcdefg");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        logger.info("LC status: {}", pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != 0 && retryCount < 20) {
            try {
                logger.info("LC running not reached - wait");
                Thread.sleep(500);
                retryCount++;
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
        /*
         * Test if config status is 3 = CONFIG_SENSOR_NUMBER for invalid configuration with non-number sensorid. Take
         * real int for comparison instead of BaseHandler constants - in case of change test needs to be adapted
         */
        assertEquals("Handler Configuration status", 3, pmHandler.getConfigStatus());
    }

    @Test
    public void testValidUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/pm-result.json");
        if (pmJson != null) {
            int result = pmHandler.updateChannels(pmJson);
            assertEquals("Valid update", 0, result);
            assertEquals("PM25", new DecimalType("2.87"), pmHandler.getPM25Cache());
            assertEquals("PM100", new DecimalType("5.15"), pmHandler.getPM100Cache());
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

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/noise-result.json");
        if (pmJson != null) {
            int result = pmHandler.updateChannels(pmJson);
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

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        int result = pmHandler.updateChannels("[]");
        assertEquals("Valid update", 3, result);
    }

    @Test
    public void testNullUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", "12345");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        int result = pmHandler.updateChannels(null);
        assertEquals("Valid update", 1, result);
    }
}
