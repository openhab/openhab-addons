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
package org.openhab.binding.entsoe;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.entsoe.internal.client.Client;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.Units;
import org.xml.sax.SAXException;

/**
 * {@link TestResponse} checks the parsing of a sample response XML file.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestResponse {

    @Test
    void test() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            Map<Instant, SpotPrice> map = parseResponse(content);

            assertEquals(2 * 24 * 4, map.size(), "Unexpected number of spot prices parsed");

            // Check some prices
            verifySpotPrice(map, "2025-10-30T23:00:00Z", 0.10281);
            verifySpotPrice(map, "2025-10-31T08:45:00Z", 0.06615);
            verifySpotPrice(map, "2025-11-01T17:00:00Z", 0.08721);
        } catch (FileNotFoundException e) {
            fail("Test file not found: " + e.getMessage());
        }
    }

    private Map<Instant, SpotPrice> parseResponse(String content) {
        try {
            return Client.parseXmlResponse(content, "PT15M");
        } catch (ParserConfigurationException | SAXException | IOException | EntsoeResponseException
                | EntsoeConfigurationException e) {
            fail("Failed to parse XML response: " + e.getMessage());
            return Map.of();
        }
    }

    private void verifySpotPrice(Map<Instant, SpotPrice> map, String timestamp, double expectedValue) {
        SpotPrice testPrice = map.get(Instant.parse(timestamp));
        assertNotNull(testPrice, "No spot price at " + timestamp);
        DecimalType testState = testPrice.getState(Units.KILOWATT_HOUR).as(DecimalType.class);
        assertNotNull(testState, "No DecimalType state at " + timestamp);
        assertEquals(expectedValue, testState.doubleValue(), 0.00001, "Unexpected spot price at " + timestamp);
    }
}
