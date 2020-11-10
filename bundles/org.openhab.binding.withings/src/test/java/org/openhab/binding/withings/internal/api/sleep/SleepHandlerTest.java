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
package org.openhab.binding.withings.internal.api.sleep;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.withings.internal.api.AbstractAPIHandlerTest;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class SleepHandlerTest extends AbstractAPIHandlerTest {

    private SleepHandler sleepHandler;

    @BeforeEach
    public void before() {
        sleepHandler = new SleepHandler(accessTokenServiceMock, httpClientMock);
    }

    @Test
    public void testLoadLatestSleepData() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"series\": [\n" + "            {\n"
                + "                \"id\": 1685840493,\n" + "                \"timezone\": \"Europe/Berlin\",\n"
                + "                \"model\": 32,\n" + "                \"model_id\": 63,\n"
                + "                \"startdate\": 1604271360,\n" + "                \"enddate\": 1604296800,\n"
                + "                \"date\": \"2020-11-02\",\n" + "                \"data\": {\n"
                + "                    \"sleep_score\": 89\n" + "                },\n"
                + "                \"created\": 1604296887,\n" + "                \"modified\": 1604304183\n"
                + "            },\n" + "            {\n" + "                \"id\": 1687501146,\n"
                + "                \"timezone\": \"Europe/Berlin\",\n" + "                \"model\": 32,\n"
                + "                \"model_id\": 63,\n" + "                \"startdate\": 1604355900,\n"
                + "                \"enddate\": 1604384640,\n" + "                \"date\": \"2020-11-03\",\n"
                + "                \"data\": {\n" + "                    \"sleep_score\": 96\n" + "                },\n"
                + "                \"created\": 1604384733,\n" + "                \"modified\": 1604392026\n"
                + "            },\n" + "            {\n" + "                \"id\": 1689080801,\n"
                + "                \"timezone\": \"Europe/Berlin\",\n" + "                \"model\": 32,\n"
                + "                \"model_id\": 63,\n" + "                \"startdate\": 1604441880,\n"
                + "                \"enddate\": 1604469420,\n" + "                \"date\": \"2020-11-04\",\n"
                + "                \"data\": {\n" + "                    \"sleep_score\": 90\n" + "                },\n"
                + "                \"created\": 1604469507,\n" + "                \"modified\": 1604476807\n"
                + "            }\n" + "        ],\n" + "        \"more\": false,\n" + "        \"offset\": 0\n"
                + "    }\n" + "}");

        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertTrue(latestSleepData.isPresent());

        LatestSleepData sleepData = latestSleepData.get();
        assertEquals(90, sleepData.getSleepScore().intValue());
        assertEquals(1604441880000L, sleepData.getSleepStart().getTime());
        assertEquals(1604469420000L, sleepData.getSleepEnd().getTime());
    }

    @Test
    public void testLoadLatestSleepData_NotSuccessful() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 1,\n" + "    \"body\": {\n" + "        \"series\": [\n" + "            {\n"
                + "                \"id\": 1685840493,\n" + "                \"timezone\": \"Europe/Berlin\",\n"
                + "                \"model\": 32,\n" + "                \"model_id\": 63,\n"
                + "                \"startdate\": 1604271360,\n" + "                \"enddate\": 1604296800,\n"
                + "                \"date\": \"2020-11-02\",\n" + "                \"data\": {\n"
                + "                    \"sleep_score\": 89\n" + "                },\n"
                + "                \"created\": 1604296887,\n" + "                \"modified\": 1604304183\n"
                + "            },\n" + "            {\n" + "                \"id\": 1687501146,\n"
                + "                \"timezone\": \"Europe/Berlin\",\n" + "                \"model\": 32,\n"
                + "                \"model_id\": 63,\n" + "                \"startdate\": 1604355900,\n"
                + "                \"enddate\": 1604384640,\n" + "                \"date\": \"2020-11-03\",\n"
                + "                \"data\": {\n" + "                    \"sleep_score\": 96\n" + "                },\n"
                + "                \"created\": 1604384733,\n" + "                \"modified\": 1604392026\n"
                + "            },\n" + "            {\n" + "                \"id\": 1689080801,\n"
                + "                \"timezone\": \"Europe/Berlin\",\n" + "                \"model\": 32,\n"
                + "                \"model_id\": 63,\n" + "                \"startdate\": 1604441880,\n"
                + "                \"enddate\": 1604469420,\n" + "                \"date\": \"2020-11-04\",\n"
                + "                \"data\": {\n" + "                    \"sleep_score\": 90\n" + "                },\n"
                + "                \"created\": 1604469507,\n" + "                \"modified\": 1604476807\n"
                + "            }\n" + "        ],\n" + "        \"more\": false,\n" + "        \"offset\": 0\n"
                + "    }\n" + "}");

        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertFalse(latestSleepData.isPresent());
    }

    @Test
    public void testLoadLatestSleepData_BodyEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n    }\n" + "}");

        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertFalse(latestSleepData.isPresent());
    }

    @Test
    public void testLoadLatestSleepData_SeriesEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"series\": [\n" + "        ],\n"
                + "        \"more\": false,\n" + "        \"offset\": 0\n" + "    }\n" + "}");

        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertFalse(latestSleepData.isPresent());
    }

    @Test
    public void testLoadLatestSleepData_Exception() {
        mockAccessToken();
        mockRequestWithException();

        // TimeoutException occurs, but it is only logged to fulfill the coding guidelines of OpenHAB
        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertFalse(latestSleepData.isPresent());
    }

    @Test
    public void testLoadLatestSleepData_AccessTokenMissing() {
        Optional<LatestSleepData> latestSleepData = sleepHandler.loadLatestSleepData();
        assertFalse(latestSleepData.isPresent());
    }
}
