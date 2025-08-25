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
package org.openhab.binding.tibber.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.PRICE_INFO_JSON_PATH;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.dto.PriceInfo;
import org.openhab.binding.tibber.internal.dto.ScheduleEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;
import org.openhab.binding.tibber.internal.exception.PriceCalculationException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TestPriceCalculator} tests price calculations.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestPriceCalculator {

    private String priceResponseFile = "src/test/resources/price-query-response.json";
    private @NonNullByDefault({}) PriceCalculator priceCalculator;

    @BeforeEach
    void setup() {
        priceCalculator = TestPriceCalculator.getPriceCalculator(priceResponseFile);
    }

    public static PriceCalculator getPriceCalculator(String priceResponseFile) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(priceResponseFile)));
            JsonObject rootJsonObject = (JsonObject) JsonParser.parseString(content);
            JsonObject priceInfo = Utils.getJsonObject(rootJsonObject, PRICE_INFO_JSON_PATH);
            if (!priceInfo.isEmpty()) {
                JsonArray spotPrices = new JsonArray();
                spotPrices.addAll(priceInfo.getAsJsonArray("today"));
                spotPrices.addAll(priceInfo.getAsJsonArray("tomorrow"));
                PriceCalculator calc = new PriceCalculator(spotPrices);
                return calc;
            } else {
                fail("Prices empty");
            }
        } catch (IOException e) {
            fail("Error reading file " + priceResponseFile);
        }
        fail("Error reading file " + priceResponseFile);
        throw new RuntimeException("Failed to create PriceCalculator");
    }

    @Test
    void testLimits() {
        assertEquals(Instant.parse("2025-05-17T22:00:00Z"), priceCalculator.priceInfoStart());
        assertEquals(Instant.parse("2025-05-19T22:00:00Z"), priceCalculator.priceInfoEnd());
    }

    @Test
    void testPriceCalculation() throws PriceCalculationException {
        // Price of first available hour
        Instant start = priceCalculator.priceInfoStart();
        double price = priceCalculator.calculatePrice(start, 1000, 3600);
        assertEquals(0.2972, price, 0.0001, "Price of first hour");

        // Price half first, half second hour
        price = priceCalculator.calculatePrice(start.plus(30, ChronoUnit.MINUTES), 1000, 3600);
        // 0.2972 + 0.2933 / 2 = 0.29525
        assertEquals(0.29525, price, 0.0001, "Half price first, half price second hour");

        // odd numbers
        price = priceCalculator.calculatePrice(start.plus(73, ChronoUnit.MINUTES), 823, 3600 * 2 + 39 * 60 + 23); // 9563
                                                                                                                  // seconds
        /**
         * 0.3073 * 2820 s + 0.3039 * 3600 + 0.3025 * 3143
         * 866,586 + 1094,04 + 950,7575
         * 2911,3835 * 823 / 1000 / 3600 = 0,665574......
         */
        assertEquals(0.6331, price, 0.0001, "Odd numbers");

        // Price of last available hour
        start = priceCalculator.priceInfoEnd().minus(60, ChronoUnit.MINUTES);
        price = priceCalculator.calculatePrice(start, 1000, 3600);
        assertEquals(0.3130, price, 0.0001, "Price of first hour");

        // out of bounds
        start = Instant.parse("2025-05-18T00:00:00.000+02:00").truncatedTo(ChronoUnit.SECONDS);
        try {
            priceCalculator.calculatePrice(start, 1000, 3600 * 98);
            fail("Calculation out of range");
        } catch (PriceCalculationException pce) {
            String message = pce.getMessage();
            assertNotNull(message);
            assertTrue(message.endsWith("Please respect priceInfoEnd boundary."));
        }
        start = start.minus(2, ChronoUnit.DAYS);
        try {
            priceCalculator.calculatePrice(start, 1000, 3600 * 98);
            fail("Calculation out of range");
        } catch (PriceCalculationException pce) {
            String message = pce.getMessage();
            assertNotNull(message);
            assertTrue(message.endsWith("Please respect priceInfoStart boundary."));
        }
    }

    @Test
    void testPriceList() throws PriceCalculationException {
        // out of bounds
        Instant start = Instant.parse("2025-05-18T05:23:14.000+02:00");
        Instant end = Instant.parse("2025-05-18T14:49:58.000+02:00");
        List<PriceInfo> priceInfos = priceCalculator.listPrices(start, end, true);
        double previousPrice = Double.MIN_VALUE;
        int totalDuration = 0;
        for (Iterator<PriceInfo> iterator = priceInfos.iterator(); iterator.hasNext();) {
            PriceInfo priceInfo = iterator.next();
            assertTrue(previousPrice <= priceInfo.price);
            assertFalse(priceInfo.startsAt.isBefore(start));
            assertFalse(priceInfo.startsAt.plus(priceInfo.durationSeconds, ChronoUnit.SECONDS).isAfter(end));
            previousPrice = priceInfo.price;
            totalDuration += priceInfo.durationSeconds;
        }
        assertEquals(totalDuration, Duration.between(start, end).getSeconds(), "Total duration");
    }

    @Test
    void testCurveCalculation() throws CalculationParameterException, PriceCalculationException {
        String fileName = "src/test/resources/laundry-curve.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            List<CurveEntry> curve = Utils.convertCurve(JsonParser.parseString(content));
            Map<String, Object> result = priceCalculator.calculateBestPrice(priceCalculator.priceInfoStart(),
                    priceCalculator.priceInfoEnd(), curve);
            assertEquals("2025-05-18T11:00:00Z", result.get("cheapestStart"), "Cheapest Start");
            assertEquals("2025-05-19T18:00:00Z", result.get("mostExpensiveStart"), "Most Expensive Start");
            Object cheapestPrice = result.get("lowestPrice");
            assertNotNull(cheapestPrice);
            assertEquals(0.0623528, (double) cheapestPrice, 0.0001, "Cheapest Price");
            Object highestPrice = result.get("highestPrice");
            assertNotNull(highestPrice);
            assertEquals(0.1496, (double) highestPrice, 0.0001, "Most Expensive Price");
            Object averagePrice = result.get("averagePrice");
            assertNotNull(averagePrice);
            assertEquals(0.0968, (double) averagePrice, 0.0001, "Average Price");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testBestPriceCalculation() throws PriceCalculationException {
        Map<String, Object> result = priceCalculator.calculateBestPrice(priceCalculator.priceInfoStart(),
                priceCalculator.priceInfoEnd(), List.of(new CurveEntry(1786, 1800)));
        assertEquals("2025-05-18T11:00:00Z", result.get("cheapestStart"), "Cheapest Start");
        assertEquals("2025-05-19T18:00:00Z", result.get("mostExpensiveStart"), "Most Expensive Start");
        Object cheapestPrice = result.get("lowestPrice");
        assertNotNull(cheapestPrice);
        assertEquals(0.1697593, (double) cheapestPrice, 0.0001, "Cheapest Price");
        Object highestPrice = result.get("highestPrice");
        assertNotNull(highestPrice);
        assertEquals(0.4074, (double) highestPrice, 0.0001, "Most Expensive Price");
        Object averagePrice = result.get("averagePrice");
        assertNotNull(averagePrice);
        assertEquals(0.2647, (double) averagePrice, 0.0001, "Average Price");
    }

    @Test
    void testBestPriceScheduleCalculation() throws PriceCalculationException {
        List<ScheduleEntry> schedule = priceCalculator.calculateNonConsecutive(priceCalculator.priceInfoStart(),
                priceCalculator.priceInfoEnd(), 11000, 8 * 3600 + 54 * 60);
        assertEquals(3, schedule.size(), "Number of schedules");

        assertEquals(11.4653, schedule.get(0).cost, 0.0001, "Cost Element 1");
        assertEquals(18000, schedule.get(0).duration, "Duration Element 1");
        assertEquals("2025-05-18T09:00:00Z", schedule.get(0).start.toString(), "Start Element 1");
        assertEquals("2025-05-18T14:00:00Z", schedule.get(0).stop.toString(), "Stop Element 1");

        assertEquals(7.6472, schedule.get(1).cost, 0.0001, "Cost Element 2");
        assertEquals(10800, schedule.get(1).duration, "Duration Element 2");
        assertEquals("2025-05-19T10:00:00Z", schedule.get(1).start.toString(), "Start Element 2");
        assertEquals("2025-05-19T13:00:00Z", schedule.get(1).stop.toString(), "Stop Element 2");

        assertEquals(2.6502, schedule.get(2).cost, 0.0001, "Cost Element 3");
        assertEquals(3240, schedule.get(2).duration, "Duration Element 3");
        assertEquals("2025-05-19T09:00:00Z", schedule.get(2).start.toString(), "Start Element 3");
        assertEquals("2025-05-19T09:54:00Z", schedule.get(2).stop.toString(), "Stop Element 3");
    }
}
