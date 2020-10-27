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
package org.openhab.binding.withings.internal.api.measure;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.withings.internal.api.AbstractAPIHandlerTest;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class MeasuresHandlerTest extends AbstractAPIHandlerTest {

    private MeasuresHandler measuresHandler;

    @Before
    public void before() {
        measuresHandler = new MeasuresHandler(accessTokenServiceMock, httpClientMock);
    }

    @Test
    public void testLoadLatestMeasureData() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"updatetime\": 1604867763,\n"
                + "        \"timezone\": \"Europe/Berlin\",\n" + "        \"measuregrps\": [\n" + "            {\n"
                + "                \"grpid\": 2309572147,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1604818838,\n" + "                \"created\": 1604819496,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 82685,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    },\n" + "                    {\n"
                + "                        \"value\": 18891,\n" + "                        \"type\": 8,\n"
                + "                        \"unit\": -3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            },\n" + "\t\t\t{\n"
                + "                \"grpid\": 2176658529,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1599495956,\n" + "                \"created\": 1599496371,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 81321,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            },\n" + "\t\t\t{\n"
                + "                \"grpid\": 662417852,\n" + "                \"attrib\": 2,\n"
                + "                \"date\": 1480624103,\n" + "                \"created\": 1480624103,\n"
                + "                \"category\": 1,\n" + "                \"deviceid\": null,\n"
                + "                \"hash_deviceid\": null,\n" + "                \"measures\": [\n"
                + "                    {\n" + "                        \"value\": 180,\n"
                + "                        \"type\": 4,\n" + "                        \"unit\": -2,\n"
                + "                        \"algo\": 0,\n" + "                        \"fm\": 0\n"
                + "                    }\n" + "                ],\n" + "                \"comment\": null\n"
                + "            }\n" + "\t\t]\n" + "    }\n" + "}");

        Optional<LatestMeasureData> measure = measuresHandler.loadLatestMeasureData();
        assertTrue(measure.isPresent());

        LatestMeasureData measureData = measure.get();
        assertEquals(BigDecimal.valueOf(1.80).setScale(2, RoundingMode.HALF_UP), measureData.getHeight());
        assertEquals(BigDecimal.valueOf(82.7), measureData.getWeight());
        assertEquals(BigDecimal.valueOf(18.9), measureData.getFatMass());
    }

    @Test
    public void testLoadLatestMeasureData_HeightMissing() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"updatetime\": 1604867763,\n"
                + "        \"timezone\": \"Europe/Berlin\",\n" + "        \"measuregrps\": [\n" + "            {\n"
                + "                \"grpid\": 2309572147,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1604818838,\n" + "                \"created\": 1604819496,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 82685,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    },\n" + "                    {\n"
                + "                        \"value\": 18891,\n" + "                        \"type\": 8,\n"
                + "                        \"unit\": -3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            },\n" + "\t\t\t{\n"
                + "                \"grpid\": 2176658529,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1599495956,\n" + "                \"created\": 1599496371,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 81321,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            }\n" + "\t\t]\n" + "    }\n" + "}");

        Optional<LatestMeasureData> measure = measuresHandler.loadLatestMeasureData();
        assertTrue(measure.isPresent());

        LatestMeasureData measureData = measure.get();
        assertNull(measureData.getHeight());
        assertEquals(BigDecimal.valueOf(82.7), measureData.getWeight());
        assertEquals(BigDecimal.valueOf(18.9), measureData.getFatMass());
    }

    @Test
    public void testLoadLatestMeasureData_NotSuccessful() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 1,\n" + "    \"body\": {\n" + "        \"updatetime\": 1604867763,\n"
                + "        \"timezone\": \"Europe/Berlin\",\n" + "        \"measuregrps\": [\n" + "            {\n"
                + "                \"grpid\": 2309572147,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1604818838,\n" + "                \"created\": 1604819496,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 82685,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    },\n" + "                    {\n"
                + "                        \"value\": 18891,\n" + "                        \"type\": 8,\n"
                + "                        \"unit\": -3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            },\n" + "\t\t\t{\n"
                + "                \"grpid\": 2176658529,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1599495956,\n" + "                \"created\": 1599496371,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [\n" + "                    {\n"
                + "                        \"value\": 81321,\n" + "                        \"type\": 1,\n"
                + "                        \"unit\": -3,\n" + "                        \"algo\": 0,\n"
                + "                        \"fm\": 3\n" + "                    }\n" + "                ],\n"
                + "                \"comment\": null\n" + "            },\n" + "\t\t\t{\n"
                + "                \"grpid\": 662417852,\n" + "                \"attrib\": 2,\n"
                + "                \"date\": 1480624103,\n" + "                \"created\": 1480624103,\n"
                + "                \"category\": 1,\n" + "                \"deviceid\": null,\n"
                + "                \"hash_deviceid\": null,\n" + "                \"measures\": [\n"
                + "                    {\n" + "                        \"value\": 180,\n"
                + "                        \"type\": 4,\n" + "                        \"unit\": -2,\n"
                + "                        \"algo\": 0,\n" + "                        \"fm\": 0\n"
                + "                    }\n" + "                ],\n" + "                \"comment\": null\n"
                + "            }\n" + "\t\t]\n" + "    }\n" + "}");

        Optional<LatestMeasureData> measure = measuresHandler.loadLatestMeasureData();
        assertFalse(measure.isPresent());
    }

    @Test
    public void testLoadLatestMeasureData_BodyMissing() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0\n}");

        Optional<LatestMeasureData> latestMeasureData = measuresHandler.loadLatestMeasureData();
        assertFalse(latestMeasureData.isPresent());
    }

    @Test
    public void testLoadLatestMeasureData_BodyEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n    \"body\": {\n    }\n" + "}");

        Optional<LatestMeasureData> latestMeasureData = measuresHandler.loadLatestMeasureData();
        assertFalse(latestMeasureData.isPresent());
    }

    @Test
    public void testLoadLatestMeasureData_MeasureGroupsEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"updatetime\": 1604867763,\n"
                + "        \"timezone\": \"Europe/Berlin\",\n" + "        \"measuregrps\": [\n" + "\t\t]\n" + "    }\n"
                + "}");

        Optional<LatestMeasureData> latestMeasureData = measuresHandler.loadLatestMeasureData();
        assertFalse(latestMeasureData.isPresent());
    }

    @Test
    public void testLoadLatestMeasureData_MeasuresEmpty() {
        mockAccessToken();
        mockRequest("{\n" + "    \"status\": 0,\n" + "    \"body\": {\n" + "        \"updatetime\": 1604867763,\n"
                + "        \"timezone\": \"Europe/Berlin\",\n" + "        \"measuregrps\": [\n" + "            {\n"
                + "                \"grpid\": 2309572147,\n" + "                \"attrib\": 0,\n"
                + "                \"date\": 1604818838,\n" + "                \"created\": 1604819496,\n"
                + "                \"category\": 1,\n"
                + "                \"deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"hash_deviceid\": \"5116c0a294e4ce90c9f0af500119c531bef0aa50\",\n"
                + "                \"measures\": [" + "                ],\n"
                + "                \"comment\": null\n            }\n" + "\t\t]\n" + "    }\n" + "}");

        Optional<LatestMeasureData> latestMeasureData = measuresHandler.loadLatestMeasureData();
        assertTrue(latestMeasureData.isPresent());

        LatestMeasureData measureData = latestMeasureData.get();
        assertNull(measureData.getHeight());
        assertNull(measureData.getWeight());
        assertNull(measureData.getFatMass());
    }

    @Test
    public void testLoadLatestMeasureData_Exception() {
        mockAccessToken();
        mockRequestWithException();

        // TimeoutException occurs, but it is only logged to fulfill the coding guidelines of OpenHAB
        Optional<LatestMeasureData> measure = measuresHandler.loadLatestMeasureData();
        assertFalse(measure.isPresent());
    }

    @Test
    public void testLoadLatestMeasureData_AccessTokenMissing() {
        Optional<LatestMeasureData> measure = measuresHandler.loadLatestMeasureData();
        assertFalse(measure.isPresent());
    }
}
