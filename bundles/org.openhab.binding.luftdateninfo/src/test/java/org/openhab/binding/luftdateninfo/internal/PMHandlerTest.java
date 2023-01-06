/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.luftdateninfo.internal.handler.BaseSensorHandler.ConfigStatus;
import org.openhab.binding.luftdateninfo.internal.handler.BaseSensorHandler.LifecycleStatus;
import org.openhab.binding.luftdateninfo.internal.handler.BaseSensorHandler.UpdateStatus;
import org.openhab.binding.luftdateninfo.internal.mock.PMHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PMHandlerTest} Test Particualte Matter Handler - Config and updates
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PMHandlerTest {
    private Logger logger = LoggerFactory.getLogger(PMHandlerTest.class);

    @Test
    public void testValidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        logger.info("LC status: {}", pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != LifecycleStatus.RUNNING && retryCount < 20) {
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
        assertEquals(ConfigStatus.EXTERNAL_SENSOR_OK, pmHandler.getConfigStatus(), "Handler Configuration status");
    }

    @Test
    public void testInvalidConfigStatus() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", -1);
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        logger.info("LC status: {}", pmHandler.getLifecycleStatus());
        int retryCount = 0; // Test shall fail after max 10 seconds
        while (pmHandler.getLifecycleStatus() != LifecycleStatus.RUNNING && retryCount < 20) {
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
        assertEquals(ConfigStatus.SENSOR_ID_NEGATIVE, pmHandler.getConfigStatus(), "Handler Configuration status");
    }

    @Test
    public void testValidUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        String pmJson = FileReader.readFileInString("src/test/resources/pm-result.json");
        if (pmJson != null) {
            UpdateStatus result = pmHandler.updateChannels(pmJson);
            assertEquals(UpdateStatus.OK, result, "Valid update");
            assertEquals(QuantityType.valueOf(2.9, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM25Cache(), "PM25");
            assertEquals(QuantityType.valueOf(5.2, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM100Cache(), "PM100");
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

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        String pmJson = FileReader.readFileInString("src/test/resources/noise-result.json");
        if (pmJson != null) {
            UpdateStatus result = pmHandler.updateChannels(pmJson);
            assertEquals(UpdateStatus.VALUE_ERROR, result, "Valid update");
            assertEquals(QuantityType.valueOf(-1, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM25Cache(),
                    "Values undefined");
            assertEquals(QuantityType.valueOf(-1, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM100Cache(),
                    "Values undefined");
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

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        UpdateStatus result = pmHandler.updateChannels("[]");
        assertEquals(UpdateStatus.VALUE_EMPTY, result, "Valid update");
    }

    @Test
    public void testNullUpdate() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("ipAdress", "192.168.178.1");
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        UpdateStatus result = pmHandler.updateChannels(null);
        assertEquals(UpdateStatus.CONNECTION_ERROR, result, "Valid update");
    }

    @Test
    public void testInternalPMSensor() {
        ThingMock t = new ThingMock();

        HashMap<String, Object> properties = new HashMap<String, Object>();
        // String sensorid taken from thing-types.xml
        properties.put("sensorid", 12345);
        t.setConfiguration(properties);

        PMHandlerExtension pmHandler = new PMHandlerExtension(t);
        pmHandler.initialize();
        String pmJson = FileReader.readFileInString("src/test/resources/internal-data.json");
        if (pmJson != null) {
            UpdateStatus result = pmHandler.updateChannels("[" + pmJson + "]");
            assertEquals(UpdateStatus.OK, result, "Valid update");
            assertEquals(QuantityType.valueOf(4.3, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM25Cache(), "PM25");
            assertEquals(QuantityType.valueOf(10.5, Units.MICROGRAM_PER_CUBICMETRE), pmHandler.getPM100Cache(),
                    "PM100");
        } else {
            assertTrue(false);
        }
    }
}
