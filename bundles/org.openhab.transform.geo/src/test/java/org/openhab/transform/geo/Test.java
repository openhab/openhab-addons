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
package org.openhab.transform.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PointType;
import org.openhab.transform.geo.internal.profiles.Geocoding;
import org.openhab.transform.geo.internal.profiles.ReverseGeocoding;

/**
 * Profile for geo coding and reverse geo coding
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class Test {

    @org.junit.jupiter.api.Test
    void testReverse() {
        String fileContent = readFile("src/test/resources/geo-reverse-result.json");
        String expectedResult = "Am Friedrichshain 22, 10407 Berlin Pankow";
        assertEquals(expectedResult, ReverseGeocoding.decode(fileContent));

        fileContent = readFile("src/test/resources/geo-reverse-result-no-road.json");
        expectedResult = "10407 Berlin Pankow";
        assertEquals(expectedResult, ReverseGeocoding.decode(fileContent));
    }

    @org.junit.jupiter.api.Test
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
