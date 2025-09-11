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
import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.action.TibberActions;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TestActions} is testing the action implementation.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestActions {

    static @Nullable TibberActions getActions() {
        String fileName = "src/test/resources/price-query-response.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JsonObject rootJsonObject = (JsonObject) JsonParser.parseString(content);
            JsonObject priceInfo = Utils.getJsonObject(rootJsonObject, PRICE_INFO_JSON_PATH);
            if (!priceInfo.isEmpty()) {
                JsonArray spotPrices = new JsonArray();
                spotPrices.addAll(priceInfo.getAsJsonArray("today"));
                spotPrices.addAll(priceInfo.getAsJsonArray("tomorrow"));
                PriceCalculator calc = new PriceCalculator(spotPrices);
                TibberHandlerMock handlerMock = new TibberHandlerMock();
                handlerMock.setPriceCalculator(calc);
                TibberActions actions = new TibberActions();
                actions.setThingHandler(handlerMock);
                return actions;
            } else {
                fail("Prices empty");
            }
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
        return null;
    }

    @Test
    void testBoundaries() {
        TibberActions actions = getActions();
        assertNotNull(actions);

        assertEquals(Instant.parse("2025-05-17T22:00:00Z"), actions.priceInfoStart());
        assertEquals(Instant.parse("2025-05-19T22:00:00Z"), actions.priceInfoEnd());
    }

    @Test
    void testPriceList() {
        TibberActions actions = getActions();
        assertNotNull(actions);

        String result = actions.listPrices(Map.of(PARAM_EARLIEST_START, actions.priceInfoStart()));
        JsonObject resultJson = (JsonObject) JsonParser.parseString(result);
        assertEquals(48, resultJson.get("size").getAsInt());
        JsonArray priceList = resultJson.get("priceList").getAsJsonArray();
        int previousPrice = Integer.MIN_VALUE;
        for (JsonElement entry : priceList) {
            int comparePrice = ((JsonObject) entry).get("price").getAsInt();
            assertTrue(previousPrice <= comparePrice, previousPrice + "<=" + comparePrice);
            previousPrice = comparePrice;
        }

        result = actions.listPrices(Map.of(PARAM_ASCENDING, false, PARAM_EARLIEST_START, actions.priceInfoStart()));
        resultJson = (JsonObject) JsonParser.parseString(result);
        assertEquals(48, resultJson.get("size").getAsInt());
        priceList = resultJson.get("priceList").getAsJsonArray();
        previousPrice = Integer.MAX_VALUE;
        for (JsonElement entry : priceList) {
            int comparePrice = ((JsonObject) entry).get("price").getAsInt();
            assertTrue(previousPrice >= comparePrice, previousPrice + ">=" + comparePrice);
            previousPrice = comparePrice;
        }
    }

    @Test
    void testConstantBestPrice() {
        TibberActions actions = getActions();
        assertNotNull(actions);
        Instant start = Instant.parse("2025-05-18T00:00:00.000+02:00").truncatedTo(ChronoUnit.SECONDS);

        Map<String, Object> params = Map.of("earliestStart", start, "power", 1000, "duration", "1h 3m");
        String result = actions.bestPricePeriod(params);
        JsonObject resultJson = (JsonObject) JsonParser.parseString(result);
        assertEquals("2025-05-18T11:00:00Z", resultJson.get("cheapestStart").getAsString(), "Cheapest Start");
        assertEquals("2025-05-19T17:57:00Z", resultJson.get("mostExpensiveStart").getAsString(),
                "Most Expensive Start");
        assertEquals(0.1997, resultJson.get("lowestPrice").getAsDouble(), 0.0001, "Cheapest Price");
        assertEquals(0.4771, resultJson.get("highestPrice").getAsDouble(), 0.0001, "Most Expensive Price");
        assertEquals(0.3111, resultJson.get("averagePrice").getAsDouble(), 0.0001, "Average Price");
    }

    @Test
    void testCurve() throws CalculationParameterException {
        TibberActions actions = getActions();
        assertNotNull(actions);

        String fileName = "src/test/resources/laundry-curve.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            List<CurveEntry> curve = Utils.convertCurve(JsonParser.parseString(content));
            Map<String, Object> params = Map.of("earliestStart", actions.priceInfoStart(), "curve", curve);
            String result = actions.bestPricePeriod(params);
            JsonObject resultJson = (JsonObject) JsonParser.parseString(result);
            assertEquals("2025-05-18T11:00:00Z", resultJson.get("cheapestStart").getAsString(), "Cheapest Start");
            assertEquals("2025-05-19T18:00:00Z", resultJson.get("mostExpensiveStart").getAsString(),
                    "Most Expensive Start");
            assertEquals(0.0623, resultJson.get("lowestPrice").getAsDouble(), 0.0001, "Cheapest Price");
            assertEquals(0.1496, resultJson.get("highestPrice").getAsDouble(), 0.0001, "Most Expensive Price");
            assertEquals(0.0968, resultJson.get("averagePrice").getAsDouble(), 0.0001, "Average Price");
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void testSchedule() {
        TibberActions actions = getActions();
        assertNotNull(actions);

        Instant start = Instant.parse("2025-05-18T00:00:00.000+02:00").truncatedTo(ChronoUnit.SECONDS);
        Map<String, Object> params = Map.of("earliestStart", start, "power", 1000, "duration", "8h 15m");
        String result = actions.bestPriceSchedule(params);
        JsonObject resultJson = (JsonObject) JsonParser.parseString(result);
        assertEquals(3, resultJson.get("size").getAsInt(), "Number of schedules");
        assertEquals(1.804425, resultJson.get("cost").getAsDouble(), 0.0001, "Price");
        JsonArray scheduleArray = resultJson.get("schedule").getAsJsonArray();
        assertNotNull(scheduleArray);
        assertEquals(1.0423, scheduleArray.get(0).getAsJsonObject().get("cost").getAsDouble(), 0.0001,
                "Cost Element 1");
        assertEquals(0.6952, scheduleArray.get(1).getAsJsonObject().get("cost").getAsDouble(), 0.0001,
                "Cost Element 2");
        assertEquals(18000, scheduleArray.get(0).getAsJsonObject().get("duration").getAsInt(), "Duration Element 1");
        assertEquals(10800, scheduleArray.get(1).getAsJsonObject().get("duration").getAsInt(), "Duration Element 2");
        assertEquals(900, scheduleArray.get(2).getAsJsonObject().get("duration").getAsInt(), "Duration Element 3");
        assertEquals("2025-05-18T09:00:00Z", scheduleArray.get(0).getAsJsonObject().get("start").getAsString(),
                "Start Element 1");
        assertEquals("2025-05-19T10:00:00Z", scheduleArray.get(1).getAsJsonObject().get("start").getAsString(),
                "Start Element 2");
        assertEquals("2025-05-19T09:00:00Z", scheduleArray.get(2).getAsJsonObject().get("start").getAsString(),
                "Start Element 3");
        assertEquals("2025-05-18T14:00:00Z", scheduleArray.get(0).getAsJsonObject().get("stop").getAsString(),
                "Stop Element 1");
        assertEquals("2025-05-19T13:00:00Z", scheduleArray.get(1).getAsJsonObject().get("stop").getAsString(),
                "Stop Element 2");
        assertEquals("2025-05-19T09:15:00Z", scheduleArray.get(2).getAsJsonObject().get("stop").getAsString(),
                "Stop Element 3");
    }

    @Test
    void testParameterError() {
        TibberActions actions = getActions();
        assertNotNull(actions);

        // will not work because now() as default parameter earliestStart is out of bounds
        String actual = actions.bestPriceSchedule(Map.of());
        assertEquals("", actual);
    }
}
