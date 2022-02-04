/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Dengler - Initial contribution
 */
public class SensorThingHandlerTest {

    private final Gson gson = new Gson();
    private final SensorThingHandler sensorThingHandler = new SensorThingHandler(null);

    @Test
    public void getLatestValueFromJsonObjectTest() {
        String arrayString = "[\n" + "  {\n" + "    \"IdPatient\": 1,\n" + "    \"LastName\": \"Mustermann\",\n"
                + "    \"FirstName\": \"Max\",\n" + "    \"Devices\": [\n" + "      {\n" + "        \"IdDevice\": 2,\n"
                + "        \"SerialNumber\": \"001\",\n" + "        \"Name\": \"Test Sitzkissen\",\n"
                + "        \"Events\": [\n" + "          {\n" + "            \"EventDef\": \"Alarm\",\n"
                + "            \"DateEntry\": \"2021-11-22T10:17:56.2866667\"\n" + "          }\n" + "        ]\n"
                + "      }\n" + "    ]\n" + "  }\n" + "]";
        JsonArray array = gson.fromJson(arrayString, JsonArray.class);
        assertEquals("{\"EventDef\":\"Alarm\",\"DateEntry\":\"2021-11-22T10:17:56.2866667\"}",
                sensorThingHandler.getLatestValueFromJsonArray(array).toString());
        arrayString = "[\n" + "  {\n" + "    \"IdPatient\": 1,\n" + "    \"LastName\": \"Mustermann\",\n"
                + "    \"FirstName\": \"Max\",\n" + "    \"Devices\": [\n" + "      {\n" + "        \"IdDevice\": 2,\n"
                + "        \"SerialNumber\": \"001\",\n" + "        \"Name\": \"Test Sitzkissen\"\n" + "      }\n"
                + "    ]\n" + "  }\n" + "]";
        array = gson.fromJson(arrayString, JsonArray.class);
        assertNull(sensorThingHandler.getLatestValueFromJsonArray(array));
    }

    @Test
    public void getUrlStringFromDeviceInfoTest() {
        sensorThingHandler.setSerialNumber();
        String deviceInfo = "{\n" + "  \"PatientDevices\": [\n" + "    {\n"
                + "      \"UuidPerson\": \"32611292-1b8c-49fa-ba78-ef972c367653\",\n" + "      \"IdPerson\": 1,\n"
                + "      \"FirstName\": \"Max\",\n" + "      \"LastName\": \"Mustermann\",\n"
                + "      \"Birthday\": null\n" + "    }\n" + "  ],\n" + "  \"OrganisationUnitDevices\": [\n" + "    {\n"
                + "      \"IdOrganisationUnit\": 4,\n"
                + "      \"UuidOrganisationUnit\": \"17f15887-6376-4c9f-9475-282c421caf46\"\n" + "    }\n" + "  ],\n"
                + "  \"IdDevice\": 0,\n" + "  \"IdDeviceKind\": null,\n" + "  \"IdDeviceType\": 2,\n"
                + "  \"IdDeviceProvider\": 0,\n" + "  \"SerialNumber\": \"001\",\n" + "  \"UDI\": null,\n"
                + "  \"Name\": \"Test Sitzkissen\",\n" + "  \"Generation\": \"1\",\n" + "  \"IsUsable\": true,\n"
                + "  \"IsUpdateRequired\": false,\n" + "  \"IsMedical\": false,\n" + "  \"IsActive\": true\n" + "}";
        JsonObject devInfo = gson.fromJson(deviceInfo, JsonObject.class);
        assertEquals(
                "https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetLatestApiSensorEvents?UuidPatient=32611292-1b8c-49fa-ba78-ef972c367653&SerialNumber=001&Count=1",
                sensorThingHandler.getUrlStringFromDeviceInfo(devInfo));
        deviceInfo = "{\n" + "  \"PatientDevices\": [],\n" + "  \"OrganisationUnitDevices\": [],\n"
                + "  \"IdDevice\": 0,\n" + "  \"IdDeviceKind\": null,\n" + "  \"IdDeviceType\": 2,\n"
                + "  \"IdDeviceProvider\": 0,\n" + "  \"SerialNumber\": \"001\",\n" + "  \"UDI\": null,\n"
                + "  \"Name\": \"Test Sitzkissen\",\n" + "  \"Generation\": \"1\",\n" + "  \"IsUsable\": true,\n"
                + "  \"IsUpdateRequired\": false,\n" + "  \"IsMedical\": false,\n" + "  \"IsActive\": true\n" + "}";
        devInfo = gson.fromJson(deviceInfo, JsonObject.class);
        assertNull(sensorThingHandler.getUrlStringFromDeviceInfo(devInfo));
    }

    @Test
    public void dateFormatTest() {
        Date date = new Date();
        long time = date.getTime();
        time -= 24L * 60L * 60L * 1000L; // one day in ms
        date.setTime(time);
        String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
        System.out.println(dateString);
    }

    @Test
    public void dateFormatTest2() {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2021-11-22T14:00:09.9933333");
            String dateString = new SimpleDateFormat("yyyy-MM-dd', 'HH:mm:ss").format(date);
            System.out.println(dateString);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
