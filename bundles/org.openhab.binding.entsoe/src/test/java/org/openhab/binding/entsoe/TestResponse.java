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
import java.time.Instant;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.entsoe.internal.client.EntsoeDocumentParser;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.library.types.DecimalType;

/**
 * {@link TestResponse} checks the parsing of a sample response XML file.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestResponse {

    @Test
    void testSpotPrice() {
        SpotPrice price;
        try {
            price = new SpotPrice("EUR", "MWH", 123.4);
            assertEquals(0.1234, ((DecimalType) price.getState()).doubleValue(), 0.00001,
                    "Unexpected spot price conversion from MWH to KWH");
        } catch (EntsoeResponseException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testPT15M() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT15M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();

            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(2, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT15M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            assertEquals("PT15M", parser.getSequences().lastEntry().getValue(), "Wrong duration for second sequence");
            Map<Instant, SpotPrice> map = parser.getPriceMap(parser.getSequences().firstKey());

            assertEquals(2 * 24 * 4 - 1, map.size(), "Unexpected number of spot prices parsed");

            // Check some prices
            verifySpotPrice(map, "2025-10-30T23:00:00Z", 0.10281);
            verifySpotPrice(map, "2025-10-31T08:45:00Z", 0.06615);
            verifySpotPrice(map, "2025-11-01T17:00:00Z", 0.08721);
        } catch (FileNotFoundException e) {
            fail("Test file not found: " + e.getMessage());
        }
    }

    @Test
    void testPT60M() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT60M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(1, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT60M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            Map<Instant, SpotPrice> map = parser.getPriceMap(parser.getSequences().firstKey());

            assertEquals(2 * 24, map.size(), "Unexpected number of spot prices parsed");

            // Check some prices
            verifySpotPrice(map, "2025-11-26T14:00:00Z", 0.27661);
            verifySpotPrice(map, "2025-11-26T19:00:00Z", 0.15303);
            verifySpotPrice(map, "2025-11-27T17:00:00Z", 0.13646);
        } catch (FileNotFoundException e) {
            fail("Test file not found: " + e.getMessage());
        }
    }

    private void verifySpotPrice(Map<Instant, SpotPrice> map, String timestamp, double expectedValue) {
        SpotPrice testPrice = map.get(Instant.parse(timestamp));
        assertNotNull(testPrice, "No spot price at " + timestamp);
        DecimalType testState = testPrice.getState().as(DecimalType.class);
        assertNotNull(testState, "No DecimalType state at " + timestamp);
        assertEquals(expectedValue, testState.doubleValue(), 0.00001, "Unexpected spot price at " + timestamp);
    }
}
