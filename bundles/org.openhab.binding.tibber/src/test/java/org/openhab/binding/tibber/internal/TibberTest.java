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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.dto.PriceInfo;
import org.openhab.core.library.unit.Units;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TibberTest} defines some basic tests.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberTest {

    JsonArray readPriceResponse() {
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
                return spotPrices;
            } else {
                fail("Prices empty");
            }
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
        fail("Error reading file " + fileName);
        return new JsonArray();
    }

    @Test
    void testAveragePrices() {
        PriceCalculator calc = new PriceCalculator(readPriceResponse());
        // Instant start = Instant.now().with(ChronoField.MONTH_OF_YEAR, 5).with(ChronoField.DAY_OF_MONTH, 18)
        // .with(ChronoField.HOUR_OF_DAY, 13).with(ChronoField.MINUTE_OF_HOUR, 0)
        // .with(ChronoField.SECOND_OF_MINUTE, 0);
        Instant start = Instant.parse("2025-05-18T11:00:00.000+02:00");
        Instant end = start.plus(1, ChronoUnit.DAYS);
        // System.out.println(calc.averagePrice(start, end));
        calc.calculateAveragePrices();
    }

    @Test
    void testPriceInfo() {
        Instant test1 = Instant.parse("2025-05-18T11:00:00.000+02:00");
        Instant test2 = test1.truncatedTo(ChronoUnit.SECONDS);
        System.out.println(test1 + " " + test2);
        PriceInfo pInfo = new PriceInfo(0.3456, 30,
                Instant.parse("2025-05-18T11:00:00.000+02:00").truncatedTo(ChronoUnit.SECONDS));
        JsonObject pInfoJson = (JsonObject) JsonParser.parseString(pInfo.toString());
        System.out.println(pInfoJson);
        assertEquals(0.3456, pInfoJson.get("price").getAsDouble(), "Price JSON");
        assertEquals(30, pInfoJson.get("duration").getAsInt(), "Duration JSON");
        assertEquals("2025-05-18T09:00:00Z", pInfoJson.get("startsAt").getAsString(), "Timestamp JSON");
        Map<String, Object> pInfoMap = pInfo.toMap();
        assertEquals(0.3456, (Double) pInfoMap.get("price"), "Price Map");
        assertEquals(30, (Integer) pInfoMap.get("duration"), "Duration Map");
        assertEquals("2025-05-18T09:00:00Z", pInfoMap.get("startsAt").toString(), "Timestamp Map");
    }

    @Test
    void testJson() {
        String jsonString = "{\"key\": null}";
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
        boolean isNull = jsonObject.get("key").isJsonNull();
        System.out.println(isNull);
        System.err.println(Units.KILOWATT_HOUR.getName());
    }

    /**
     * Converts a rule output from getAllStatesBetween from rrd4j into a JsonArray
     */
    void laundryCurve() {
        String usPattern = "M/dd/yy, H:mm";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(usPattern);
        JsonArray curve = new JsonArray();
        String fileName = "src/test/resources/laundry-curve.raw";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            String[] split = content.split("AM:");
            for (int i = 0; i < split.length; i++) {
                String[] split2 = split[i].split("ShellyPlugWaschmaschine_Leistung ->");
                for (int j = 0; j < split2.length; j++) {
                    // System.out.println(split2[j]);
                    String[] split3 = split2[j].split("W,");
                    JsonObject entry = new JsonObject();
                    for (int k = 0; k < split3.length; k++) {
                        // System.out.println(split3[k]);
                        try {
                            double power = Double.valueOf(split3[k]);
                            entry.addProperty("power", power);
                        } catch (Exception e) {
                        }
                        try {
                            Instant timestamp = LocalDateTime.parse(split3[k].strip(), formatter)
                                    .atZone(ZoneId.systemDefault()).toInstant();
                            entry.addProperty("timestamp", timestamp.toString());
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }
                    }
                    // System.out.println(entry.toString());
                    if (!entry.isEmpty()) {
                        curve.add(entry);
                    }
                }
                // System.out.println(split[i]);
            }
            System.out.println(curve);
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void generate15MinPrices() {
        String fileName = "src/test/resources/price-query-response.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            JsonObject rootJsonObject = (JsonObject) JsonParser.parseString(content);
            JsonObject priceInfo = Utils.getJsonObject(rootJsonObject, PRICE_INFO_JSON_PATH);
            if (!priceInfo.isEmpty()) {
                JsonArray spotPrices = new JsonArray();
                JsonArray spotPrices15 = new JsonArray();
                spotPrices.addAll(priceInfo.getAsJsonArray("today"));
                spotPrices.addAll(priceInfo.getAsJsonArray("tomorrow"));
                Random rand = new Random();
                for (JsonElement entry : spotPrices) {
                    JsonObject entryObject = entry.getAsJsonObject();
                    spotPrices15.add(entry);
                    Instant startsAt = Instant.parse(entryObject.get("startsAt").getAsString());
                    double price = entryObject.get("total").getAsDouble();
                    // add prices for +15, +30 and +45 minutes
                    for (int i = 1; i < 4; i++) {
                        JsonObject entry15 = new JsonObject();
                        entry15.addProperty("startsAt", startsAt.plus(i * 15, ChronoUnit.MINUTES).toString());
                        double priceVariance = rand.nextDouble(-0.01, 0.01);
                        entry15.addProperty("total", price + priceVariance);
                        entry15.addProperty("level", entryObject.get("level").getAsString());
                        spotPrices15.add(entry15);
                    }
                }
                System.out.println(spotPrices15);
            }
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }

    @Test
    void laundryCurveConversion() {
        String fileName = "src/test/resources/laundry-curve.json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            List<CurveEntry> curve = Utils.convertCurve(JsonParser.parseString(content));
            System.out.println("Curve converted to Java" + curve);
        } catch (IOException e) {
            fail("Error reading file " + fileName);
        }
    }
}
