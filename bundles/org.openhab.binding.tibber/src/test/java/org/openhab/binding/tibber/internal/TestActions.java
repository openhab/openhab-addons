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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.action.TibberActions;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TestActions} checks the conversion of price calculation parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestActions {

    static @Nullable TibberActions getActions() {
        TreeMap<Instant, Double> spotPriceMap = new TreeMap<>();
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
    void testEarliestStart() {
        TibberActions actions = getActions();
        assertNotNull(actions);
        assertEquals(Instant.parse("2025-05-17T22:00:00Z"), actions.priceInfoStart());
        assertEquals(Instant.parse("2025-05-19T22:00:00Z"), actions.priceInfoEnd());
        System.out.println(actions.priceInfoStart());
        System.out.println(actions.priceInfoEnd());
    }

    @Test
    void testMapToMapConversion() {
        TibberActions actions = getActions();
        assertNotNull(actions);
        System.out.println(actions.priceInfoStart());
        System.out.println(actions.priceInfoEnd());
        System.out.println(actions.listPrices("{\"earliestStart\":\"" + actions.priceInfoStart().toString() + "\"}"));
        Map<String, Object> params = new HashMap<>();
        params.put("earliestStart", Instant.parse("2025-05-17T22:00:00Z"));
        params.put("duration", "3600 s");
        String result = actions.bestPricePeriod(params);
        System.out.println(result);
    }

    @Test
    void testSchedule() {
        TibberActions actions = getActions();
        assertNotNull(actions);
        Instant start = Instant.parse("2025-05-18T00:00:00.000+02:00").truncatedTo(ChronoUnit.SECONDS);

        Map<String, Object> params = Map.of("earliestStart", start, "power", 1000, "duration", "8 h 15 m");
        String result = actions.bestPriceSchedule(params);
        JsonObject resultJson = (JsonObject) JsonParser.parseString(result);
        System.out.println(result);
        JsonArray scheduleArray = resultJson.get("schedule").getAsJsonArray();
        System.out.println("###" + scheduleArray);
        System.out.println("###" + scheduleArray.get(0));
    }
}
