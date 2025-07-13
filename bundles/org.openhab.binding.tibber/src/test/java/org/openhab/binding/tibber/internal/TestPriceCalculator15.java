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

import com.google.gson.JsonParser;

/**
 * The {@link TestPriceCalculator15} tests price calculation based on 15 minutes price periods
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestPriceCalculator15 {

    private String priceResponseFile = "src/test/resources/price15-query-response.json";
    private @NonNullByDefault({}) PriceCalculator priceCalculator;

    @BeforeEach
    void setup() {
        priceCalculator = TestPriceCalculator.getPriceCalculator(priceResponseFile);
    }

    @Test
    void testPriceCalculation() throws PriceCalculationException {
        // Price of first available hour
        Instant start = priceCalculator.priceInfoStart();
        double price = priceCalculator.calculatePrice(start, 1000, 3600);
        // assertEquals(0.3136, price, 0.0001, "Price of first hour");
        assertEquals(0.3105, price, 0.0001, "Price of first hour");

        // Price half first, half second hour
        price = priceCalculator.calculatePrice(start.plus(30, ChronoUnit.MINUTES), 1000, 3600);
        // 0.3136 * 0.5 + 0.3073 * 0.5 = 0.3104
        // assertEquals(0.3104, price, 0.0001, "Half price first, half price second hour");
        assertEquals(0.3095, price, 0.0001, "Half price first, half price second hour");

        // odd numbers
        price = priceCalculator.calculatePrice(start.plus(73, ChronoUnit.MINUTES), 823, 3600 * 2 + 39 * 60 + 23); // 9563
                                                                                                                  // seconds
        /**
         * 0.3073 * 2820 s + 0.3039 * 3600 + 0.3025 * 3143
         * 866,586 + 1094,04 + 950,7575
         * 2911,3835 * 823 / 1000 / 3600 = 0,665574......
         */
        // assertEquals(0.6655, price, 0.0001, "Odd numbers");
        assertEquals(0.6613, price, 0.0001, "Odd numbers");

        // Price of last available hour
        start = priceCalculator.priceInfoEnd().minus(60, ChronoUnit.MINUTES);
        price = priceCalculator.calculatePrice(start, 1000, 3600);
        // assertEquals(0.3197, price, 0.0001, "Price of first hour");
        assertEquals(0.3231, price, 0.0001, "Price of first hour");
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
            assertEquals("2025-05-18T12:45:00Z", result.get("cheapestStart"), "Cheapest Start");
            assertEquals("2025-05-19T18:45:00Z", result.get("mostExpensiveStart"), "Most Expensive Start");
            Object cheapestPrice = result.get("lowestPrice");
            assertNotNull(cheapestPrice);
            assertEquals(0.05459, (double) cheapestPrice, 0.0001, "Cheapest Price");
            Object highestPrice = result.get("highestPrice");
            assertNotNull(highestPrice);
            assertEquals(0.15112, (double) highestPrice, 0.0001, "Most Expensive Price");
            Object averagePrice = result.get("averagePrice");
            assertNotNull(averagePrice);
            assertEquals(0.09123, (double) averagePrice, 0.0001, "Average Price");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testBestPriceCalculation() throws PriceCalculationException {
        Map<String, Object> result = priceCalculator.calculateBestPrice(priceCalculator.priceInfoStart(),
                priceCalculator.priceInfoEnd(), List.of(new CurveEntry(1786, 1800)));
        assertEquals("2025-05-18T12:30:00Z", result.get("cheapestStart"), "Cheapest Start");
        assertEquals("2025-05-19T18:00:00Z", result.get("mostExpensiveStart"), "Most Expensive Start");
        Object cheapestPrice = result.get("lowestPrice");
        assertNotNull(cheapestPrice);
        assertEquals(0.14965, (double) cheapestPrice, 0.0001, "Cheapest Price");
        Object highestPrice = result.get("highestPrice");
        assertNotNull(highestPrice);
        assertEquals(00.40863, (double) highestPrice, 0.0001, "Most Expensive Price");
        Object averagePrice = result.get("averagePrice");
        assertNotNull(averagePrice);
        assertEquals(0.25015, (double) averagePrice, 0.0001, "Average Price");
    }

    @Test
    void testBestPriceScheduleCalculation() throws PriceCalculationException {
        List<ScheduleEntry> schedule = priceCalculator.calculateNonConsecutive(priceCalculator.priceInfoStart(),
                priceCalculator.priceInfoEnd(), 11000, 8 * 3600 + 54 * 60);
        assertEquals(7, schedule.size(), "Number of schedules");

        assertEquals(1.04940, schedule.get(0).cost, 0.0001, "Cost Element 1");
        assertEquals(1800, schedule.get(0).duration, "Duration Element 1");
        assertEquals("2025-05-18T15:15:00Z", schedule.get(0).start.toString(), "Start Element 1");
        assertEquals("2025-05-18T15:45:00Z", schedule.get(0).stop.toString(), "Stop Element 1");

        assertEquals(0.53489, schedule.get(1).cost, 0.0001, "Cost Element 2");
        assertEquals(900, schedule.get(1).duration, "Duration Element 2");
        assertEquals("2025-05-19T10:15:00Z", schedule.get(1).start.toString(), "Start Element 2");
        assertEquals("2025-05-19T10:30:00Z", schedule.get(1).stop.toString(), "Stop Element 2");
    }
}
