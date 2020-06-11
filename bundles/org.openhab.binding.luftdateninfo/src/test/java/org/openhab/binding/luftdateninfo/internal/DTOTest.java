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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.luftdateninfo.internal.dto.SensorData;
import org.openhab.binding.luftdateninfo.internal.dto.SensorDataValue;
import org.openhab.binding.luftdateninfo.internal.handler.HTTPHandler;
import org.openhab.binding.luftdateninfo.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link DTOTest} Data Transfer Object - test conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class DTOTest {

    @Test
    public void testConditions() {
        String result = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        Gson gson = new Gson();
        SensorData[] valueArray = gson.fromJson(result, SensorData[].class);
        // System.out.println(valueArray.length);
        assertEquals("Array size", 2, valueArray.length);

        SensorData d = valueArray[0];
        List<SensorDataValue> sensorDataVaueList = d.getSensordatavalues();
        Iterator<SensorDataValue> iter = sensorDataVaueList.iterator();
        while (iter.hasNext()) {
            SensorDataValue v = iter.next();
            // System.out.println(v.getValue_type() + ":" + v.getValue());
            if (v.getValue_type().equals(HTTPHandler.TEMPERATURE)) {
                assertEquals("Temperature", "22.70", v.getValue());
            } else if (v.getValue_type().equals(HTTPHandler.HUMIDITY)) {
                assertEquals("Humidiry", "61.00", v.getValue());
            }
        }
    }

}
