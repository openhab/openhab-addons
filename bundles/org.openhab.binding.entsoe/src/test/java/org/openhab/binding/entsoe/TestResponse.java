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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.openhab.binding.entsoe.internal.client.EntsoeClient;
import org.openhab.binding.entsoe.internal.client.EntsoeDocumentParser;
import org.openhab.binding.entsoe.internal.client.EntsoeRequest;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
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
    void test60To15() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT60M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(1, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT60M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            String sequenceKey = parser.getSequences().firstKey();

            Map<Instant, SpotPrice> map = parser.getPriceMap(sequenceKey);
            int expectedMapSize = 2 * 24;
            assertEquals(expectedMapSize, map.size(), "Unexpected number of spot prices parsed");

            Map<Instant, SpotPrice> downScale = parser.transform(sequenceKey, Duration.parse("PT15M"));
            assertEquals(expectedMapSize * 4, downScale.size(), "Unexpected number of spot prices parsed");
            verifySpotPrice(downScale, "2025-11-27T09:30:00Z", 0.125);
            verifySpotPrice(downScale, "2025-11-27T22:45:00Z", 0.1118);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getMessage());
        } catch (EntsoeResponseException e) {
            fail("Response error : " + e.getMessage());
        }
    }

    @Test
    void test15To60() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT15M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(2, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT15M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            String sequenceKey = parser.getSequences().firstKey();

            Map<Instant, SpotPrice> map = parser.getPriceMap(sequenceKey);
            int expectedMapSize = 2 * 24 * 4;
            assertEquals(expectedMapSize - 1, map.size(), "Unexpected number of spot prices parsed");

            Map<Instant, SpotPrice> upScale = parser.transform(sequenceKey, Duration.parse("PT60M"));
            assertEquals(expectedMapSize / 4, upScale.size(), "Unexpected number of spot prices parsed");
            verifySpotPrice(upScale, "2025-10-31T15:00:00Z", 0.1320475);
            verifySpotPrice(upScale, "2025-11-01T22:00:00Z", 0.0775075);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getMessage());
        } catch (EntsoeResponseException e) {
            fail("Response error : " + e.getMessage());
        }
    }

    @Test
    void test15To30() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT15M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(2, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT15M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            String sequenceKey = parser.getSequences().firstKey();

            Map<Instant, SpotPrice> map = parser.getPriceMap(sequenceKey);
            int expectedMapSize = 2 * 24 * 4;
            assertEquals(expectedMapSize - 1, map.size(), "Unexpected number of spot prices parsed");

            Map<Instant, SpotPrice> upScale = parser.transform(sequenceKey, Duration.parse("PT30M"));
            assertEquals(expectedMapSize / 2, upScale.size(), "Unexpected number of spot prices parsed");
            verifySpotPrice(upScale, "2025-10-31T15:00:00Z", 0.13331);
            verifySpotPrice(upScale, "2025-11-01T22:00:00Z", 0.083775);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getMessage());
        } catch (EntsoeResponseException e) {
            fail("Response error : " + e.getMessage());
        }
    }

    @Test
    void test15ToCustom() {
        try (Scanner inputScanner = new Scanner(new File("src/test/resources/response-PT15M.xml"))) {
            String content = inputScanner.useDelimiter("\\Z").next();
            EntsoeDocumentParser parser = new EntsoeDocumentParser(content);
            assertEquals(2, parser.getSequences().size(), "Unexpected number of sequences parsed");
            assertEquals("PT15M", parser.getSequences().firstEntry().getValue(), "Wrong duration for first sequence");
            String sequenceKey = parser.getSequences().firstKey();

            Map<Instant, SpotPrice> map = parser.getPriceMap(sequenceKey);
            int expectedMapSize = 2 * 24 * 4;
            assertEquals(expectedMapSize - 1, map.size(), "Unexpected number of spot prices parsed");

            Map<Instant, SpotPrice> upScale = parser.transform(sequenceKey, Duration.parse("PT5M"));
            assertEquals(expectedMapSize * 3, upScale.size(), "Unexpected number of spot prices parsed");
            verifySpotPrice(upScale, "2025-10-31T15:00:00Z", 0.1339);
            verifySpotPrice(upScale, "2025-11-01T22:00:00Z", 0.08573);
        } catch (FileNotFoundException e) {
            fail("File not found: " + e.getMessage());
        } catch (EntsoeResponseException e) {
            fail("Response error : " + e.getMessage());
        }
    }

    @Test
    void testClient401() {
        HttpClient httpClient = mock(HttpClient.class);
        Request request = mock(Request.class);
        when(httpClient.newRequest(anyString())).thenReturn(request);
        when(request.timeout(anyLong(), any())).thenReturn(request);
        when(request.agent(anyString())).thenReturn(request);
        when(request.method(HttpMethod.GET)).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED_401);
        try {
            when(request.send()).thenReturn(contentResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail(e.getMessage());
        }
        EntsoeClient entsoeClient = new EntsoeClient(httpClient);
        EntsoeRequest entsoeRequest = new EntsoeRequest("token", "eicCode", Instant.now().minus(0, ChronoUnit.DAYS),
                Instant.now().plus(2, ChronoUnit.SECONDS));
        try {
            entsoeClient.doGetRequest(entsoeRequest, 60);
        } catch (EntsoeResponseException | EntsoeConfigurationException e) {
            String failureMessage = e.getMessage();
            assertNotNull(failureMessage);
            assertTrue(failureMessage.contains("Authentication failed"),
                    "Unexpected exception message: " + e.getMessage());
            return;
        }
        fail("EntsoeResponseException expected");
    }

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
