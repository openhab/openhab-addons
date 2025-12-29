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
package org.openhab.transform.geo.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.transform.geo.internal.GeoConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PointType;
import org.openhab.transform.geo.internal.config.GeoConfig;
import org.openhab.transform.geo.internal.profiles.Geocoding;
import org.openhab.transform.geo.internal.profiles.ReverseGeocoding;

/**
 * Profile for geo coding and reverse geo coding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class GeocodingTest {

    @Test
    void testReverseAddressFormat() {
        ReverseGeocoding toObserve = new ReverseGeocoding(mock(PointType.class), new GeoConfig(),
                mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-reverse-result.json");
        String expectedResult = "Am Friedrichshain 22, 10407 Berlin Pankow";
        assertEquals(expectedResult, toObserve.decode(fileContent));

        fileContent = readFile("src/test/resources/geo-reverse-result-no-road.json");
        expectedResult = "10407 Berlin Pankow";
        assertEquals(expectedResult, toObserve.decode(fileContent));
    }

    @Test
    void testReverseJsonFormat() {
        GeoConfig configuration = new GeoConfig();
        configuration.format = JSON_FORMAT;

        ReverseGeocoding toObserve = new ReverseGeocoding(mock(PointType.class), configuration, mock(HttpClient.class));
        String fileContent = readFile("src/test/resources/geo-reverse-result.json");
        String expectedResult = (new JSONObject(fileContent)).getJSONObject(ADDRESS_FORMAT).toString();
        assertEquals(expectedResult, toObserve.decode(fileContent));

        fileContent = readFile("src/test/resources/geo-reverse-result-no-road.json");
        expectedResult = (new JSONObject(fileContent)).getJSONObject(ADDRESS_FORMAT).toString();
        assertEquals(expectedResult, toObserve.decode(fileContent));
    }

    @Test
    void testGeocoding() {
        String fileContent = readFile("src/test/resources/geo-search-result.json");
        String expectedResult = "52.5252949,13.3706843";
        PointType computedResult = Geocoding.parse(fileContent);
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
