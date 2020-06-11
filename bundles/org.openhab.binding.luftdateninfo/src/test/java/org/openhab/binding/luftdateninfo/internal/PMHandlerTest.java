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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.mock.PMHandlerExtension;
import org.openhab.binding.luftdateninfo.internal.mock.ThingMock;
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
}
