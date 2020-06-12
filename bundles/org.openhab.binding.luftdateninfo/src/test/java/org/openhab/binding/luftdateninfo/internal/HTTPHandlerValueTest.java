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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;

/**
 * The {@link HTTPHandlerValueTest} test values decoding of HTTPHandler
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class HTTPHandlerValueTest {

    /**
     * test if really the latest values are returned
     * resource1 is json with ordering according to time while resource2 the entries flipped
     */
    @Test
    public void testValueDecoding() {

        String resource1 = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        assertNotNull(resource1);
        List<SensorDataValue> l = HTTPHandler.getLatestValues(resource1);
        assertNotNull(l);
        Iterator<SensorDataValue> iter = l.iterator();
        while (iter.hasNext()) {
            SensorDataValue s = iter.next();
            testSensorValue(s);
        }

        String resource2 = FileReader
                .readFileInString("src/test/resources/condition-result-no-pressure-flipped-values.json");

        assertNotNull(resource2);
        l = HTTPHandler.getLatestValues(resource2);
        assertNotNull(l);
        iter = l.iterator();
        while (iter.hasNext()) {
            SensorDataValue s = iter.next();
            testSensorValue(s);
        }

    }

    private void testSensorValue(SensorDataValue s) {
        if (s.getValue_type().equals(HTTPHandler.TEMPERATURE)) {
            assertEquals("Temperature resource 1", "22.70", s.getValue());
        } else if (s.getValue_type().equals(HTTPHandler.HUMIDITY)) {
            assertEquals("Humidity resource 1", "61.00", s.getValue());
        } else {
            assertTrue(false);
        }
        // System.out.println(s.getValue_type() + ":" + s.getValue());
    }
}
