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
package org.openhab.binding.sensorcommunity.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sensorcommunity.internal.dto.SensorData;
import org.openhab.binding.sensorcommunity.internal.dto.SensorDataValue;
import org.openhab.binding.sensorcommunity.internal.util.FileReader;
import org.openhab.binding.sensorcommunity.internal.utils.Constants;

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
        assertEquals(2, valueArray.length, "Array size");

        SensorData d = valueArray[0];
        // Assure latest data is taken
        String dateStr = d.getTimeStamp();
        if ("2020-06-09 06:38:08".equals(dateStr)) {
            // take newer one
            d = valueArray[1];
        }
        List<SensorDataValue> sensorDataVaueList = d.getSensorDataValues();
        assertNotNull(d);
        sensorDataVaueList.forEach(v -> {
            if (Constants.TEMPERATURE.equals(v.getValueType())) {
                assertEquals("22.70", v.getValue(), "Temperature");
            } else if (Constants.HUMIDITY.equals(v.getValueType())) {
                assertEquals("61.00", v.getValue(), "Humidity");
            }
        });
    }

    @Test
    public void testDecoding() {
        String result = FileReader.readFileInString("src/test/resources/condition-result-no-pressure.json");
        Gson gson = new Gson();
        SensorData[] valueArray = gson.fromJson(result, SensorData[].class);
        // System.out.println(valueArray.length);
        assertEquals(2, valueArray.length, "Array size");

        SensorData d = valueArray[0];
        // Assure latest data is taken
        String dateStr = d.getTimeStamp();
        if ("2020-06-09 06:38:08".equals(dateStr)) {
            // take newer one
            d = valueArray[1];
        }

        // test decoding a small part
        String json = gson.toJson(d);
        // System.out.println(json);
        // check if correct timestamp is included
        assertTrue(json.contains("\"timestamp\":\"2020-06-09 06:40:34\""));
    }
}
