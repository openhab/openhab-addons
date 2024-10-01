/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mcd.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Dengler - Initial contribution
 */
@NonNullByDefault
public class SensorThingHandlerTest {

    private final Gson gson = new Gson();

    @Test
    public void getLatestValueFromJsonObjectTest() {
        String arrayString = """
                [
                  {
                    "IdPatient": 1,
                    "LastName": "Mustermann",
                    "FirstName": "Max",
                    "Devices": [
                      {
                        "IdDevice": 2,
                        "SerialNumber": "001",
                        "Name": "Test Sitzkissen",
                        "Events": [
                          {
                            "EventDef": "Alarm",
                            "DateEntry": "2021-11-22T10:17:56.2866667"
                          }
                        ]
                      }
                    ]
                  }
                ]\
                """;
        JsonArray array = gson.fromJson(arrayString, JsonArray.class);
        JsonObject object = SensorThingHandler.getLatestValueFromJsonArray(array);
        String string = object != null ? object.toString() : null;
        assertEquals("{\"EventDef\":\"Alarm\",\"DateEntry\":\"2021-11-22T10:17:56.2866667\"}", string);
        arrayString = """
                [
                  {
                    "IdPatient": 1,
                    "LastName": "Mustermann",
                    "FirstName": "Max",
                    "Devices": [
                      {
                        "IdDevice": 2,
                        "SerialNumber": "001",
                        "Name": "Test Sitzkissen"
                      }
                    ]
                  }
                ]\
                """;
        array = gson.fromJson(arrayString, JsonArray.class);
        assertNull(SensorThingHandler.getLatestValueFromJsonArray(array));
    }

    @Test
    public void dateFormatTest2() {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2021-11-22T14:00:09.9933333");
            String dateString = new SimpleDateFormat("yyyy-MM-dd', 'HH:mm:ss").format(date);
            assertEquals("2021-11-22, 14:00:09", dateString);
        } catch (Exception e) {
        }
    }
}
