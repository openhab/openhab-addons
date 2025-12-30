/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.geocoding.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.transform.geocoding.internal.OSMGeoConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PointType;
import org.openhab.transform.geocoding.internal.config.OSMGeoConfig;
import org.openhab.transform.geocoding.internal.osm.OSMGeocoding;
import org.openhab.transform.geocoding.internal.osm.OSMReverseGeocoding;

/**
 * Test responses from OpenStreetMap
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class OSMGeocodingTest {

    @Test
    void testReverseAddressFormat() {
        OSMReverseGeocoding toObserve = new OSMReverseGeocoding(mock(PointType.class), new OSMGeoConfig(),
                mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-reverse-result.json");
        String expectedResult = "Am Friedrichshain 22, 10407 Berlin Pankow";
        assertEquals(expectedResult, toObserve.format(fileContent));

        fileContent = readFile("src/test/resources/geo-reverse-result-no-road.json");
        expectedResult = "10407 Berlin Pankow";
        assertEquals(expectedResult, toObserve.format(fileContent));
    }

    @Test
    void testReverseJsonFormat() {
        OSMGeoConfig configuration = new OSMGeoConfig();
        configuration.format = JSON_FORMAT;

        OSMReverseGeocoding toObserve = new OSMReverseGeocoding(mock(PointType.class), configuration,
                mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-reverse-result.json");
        String expectedResult = (new JSONObject(fileContent)).getJSONObject(ADDRESS_KEY).toString();
        assertEquals(expectedResult, toObserve.format(fileContent));

        fileContent = readFile("src/test/resources/geo-reverse-result-no-road.json");
        expectedResult = (new JSONObject(fileContent)).getJSONObject(ADDRESS_KEY).toString();
        assertEquals(expectedResult, toObserve.format(fileContent));
    }

    @Test
    void testReverseUSFormat() {
        OSMGeoConfig configuration = new OSMGeoConfig();
        configuration.format = US_ADDRESS_FORMAT;

        OSMReverseGeocoding toObserve = new OSMReverseGeocoding(mock(PointType.class), configuration,
                mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-reverse-nyc.json");
        String expectedResult = "6 West 23rd Street, City of New York Manhattan 10010";
        assertEquals(expectedResult, toObserve.format(fileContent));
    }

    @Test
    void testGeocoding() {
        OSMGeocoding toObserve = new OSMGeocoding("Not necessary", mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-search-result.json");
        String expectedResult = "52.5252949,13.3706843";
        PointType computedResult = toObserve.parse(fileContent);
        assertNotNull(computedResult);
        assertEquals(expectedResult, computedResult.toFullString());
    }

    static String readFile(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }
}
