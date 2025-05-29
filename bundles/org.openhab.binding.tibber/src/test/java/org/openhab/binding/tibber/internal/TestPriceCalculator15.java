/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.dto.PriceInfo;
import org.openhab.binding.tibber.internal.dto.ScheduleEntry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TestPriceCalculator15} checks the conversion of price calculation parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestPriceCalculator15 {

    static @Nullable PriceCalculator getPriceCalculator() {
        TreeMap<Instant, Double> spotPriceMap = new TreeMap<>();
        String fileName = "src/test/resources/price15-query-response.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
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
            fail("Error reading file " + fileName);
        }
        return null;
    }

    @Test
    void testPriceCalculation() {
        PriceCalculator calc = getPriceCalculator();
        assertNotNull(calc);

        // Price of first available hour
        Instant start = calc.priceInfoStart();
        double price = calc.calculatePrice(start, 1000, 3600);
        // assertEquals(0.3136, price, 0.0001, "Price of first hour");
        assertEquals(0.3105, price, 0.0001, "Price of first hour");

        // Price half first, half second hour
        price = calc.calculatePrice(start.plus(30, ChronoUnit.MINUTES), 1000, 3600);
        // 0.3136 * 0.5 + 0.3073 * 0.5 = 0.3104
        // assertEquals(0.3104, price, 0.0001, "Half price first, half price second hour");
        assertEquals(0.3095, price, 0.0001, "Half price first, half price second hour");

        // odd numbers
        price = calc.calculatePrice(start.plus(73, ChronoUnit.MINUTES), 823, 3600 * 2 + 39 * 60 + 23); // 9563 seconds
        /**
         * 0.3073 * 2820 s + 0.3039 * 3600 + 0.3025 * 3143
         * 866,586 + 1094,04 + 950,7575
         * 2911,3835 * 823 / 1000 / 3600 = 0,665574......
         */
        // assertEquals(0.6655, price, 0.0001, "Odd numbers");
        assertEquals(0.6613, price, 0.0001, "Odd numbers");

        // Price of last available hour
        start = calc.priceInfoEnd().minus(60, ChronoUnit.MINUTES);
        price = calc.calculatePrice(start, 1000, 3600);
        // assertEquals(0.3197, price, 0.0001, "Price of first hour");
        assertEquals(0.3231, price, 0.0001, "Price of first hour");
    }

    @Test
    void testPriceList() {
        PriceCalculator calc = getPriceCalculator();
        assertNotNull(calc);

        // out of bounds
        Instant start = Instant.parse("2025-05-18T05:23:14.000+02:00");
        Instant end = Instant.parse("2025-05-18T14:49:58.000+02:00");
        List<PriceInfo> priceInfos = calc.listPrices(start, end, true);
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
    void testCurveCalculation() {
        PriceCalculator calc = getPriceCalculator();
        assertNotNull(calc);

        String fileName = "src/test/resources/laundry-curve.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            List<CurveEntry> curve = Utils.convertCurve(JsonParser.parseString(content));
            Map<String, Object> cost = calc.calculateBestPrice(calc.priceInfoStart(), calc.priceInfoEnd(), curve);
            System.out.println("Best Price Curve Calculation " + cost);
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testBestPriceCalculation() {
        PriceCalculator calc = getPriceCalculator();
        assertNotNull(calc);
    }

    @Test
    void testBestPriceScheduleCalculation() {
        PriceCalculator calc = getPriceCalculator();
        assertNotNull(calc);

        List<ScheduleEntry> schedule = calc.calculateNonConsecutive(calc.priceInfoStart(), calc.priceInfoEnd(), 11000,
                8 * 3600 + 54 * 60);
        System.out.println(schedule);
    }
}
