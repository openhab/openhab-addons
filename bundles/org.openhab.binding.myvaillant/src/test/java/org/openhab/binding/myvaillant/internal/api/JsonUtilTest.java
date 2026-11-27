/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myvaillant.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests {@link JsonUtil}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class JsonUtilTest {

    @Test
    void convertsKeysLikeMyPyllant() {
        assertEquals("cylinder_temperature_sensor_top_d_h_w", JsonUtil.toSnakeCase("cylinderTemperatureSensorTopDHW"));
        assertEquals("outdoor_temperature_average24h", JsonUtil.toSnakeCase("outdoorTemperatureAverage24h"));
        assertEquals("device_uuid", JsonUtil.toSnakeCase("device_uuid"));
        assertEquals("emf_valid", JsonUtil.toSnakeCase("emfValid"));
        assertEquals("cylinderTemperatureSensorTopDHW", JsonUtil.toCamelCase("cylinder_temperature_sensor_top_d_h_w"));
        assertEquals("startTime", JsonUtil.toCamelCase("start_time"));
    }

    @Test
    void convertsNestedObjects() {
        JsonObject converted = JsonUtil
                .toSnakeCase(JsonParser
                        .parseString("{\"zoneList\":[{\"startTime\":1}],\"metaInfo\":{\"maxSlotsPerDay\":3}}"))
                .getAsJsonObject();
        assertEquals(1, converted.getAsJsonArray("zone_list").get(0).getAsJsonObject().get("start_time").getAsInt());
        assertEquals(3, JsonUtil.getInteger(JsonUtil.getObject(converted, "meta_info"), "max_slots_per_day"));
        assertEquals("{\"zoneList\":[{\"startTime\":1}],\"metaInfo\":{\"maxSlotsPerDay\":3}}",
                JsonUtil.toCamelCase(converted).toString());
    }

    @Test
    void readsDatesAndTreatsPlaceholdersAsUnset() {
        JsonObject json = JsonParser.parseString(
                "{\"a\":\"2026-11-20T08:00:00Z\",\"b\":\"2026-11-20T09:00:00.123+01:00\",\"epoch\":\"1970-01-01T00:00:00Z\","
                        + "\"unset\":\"2015-01-01T00:00:00Z\",\"invalid\":\"tomorrow\",\"empty\":null,"
                        + "\"epochLocal\":\"1969-12-31T23:00:00Z\",\"unset2019Local\":\"2018-12-31T23:00:00Z\","
                        + "\"unset2019\":\"2019-01-01T00:00:00Z\",\"unset2019Offset\":\"2019-01-01T00:00:00+00:00\","
                        + "\"afterPlaceholder\":\"2019-01-01T00:00:01Z\"}")
                .getAsJsonObject();
        assertEquals(Instant.parse("2026-11-20T08:00:00Z"), JsonUtil.getInstant(json, "a"));
        assertEquals(Instant.parse("2026-11-20T08:00:00.123Z"), JsonUtil.getInstant(json, "b"));
        assertNull(JsonUtil.getInstant(json, "epoch"));
        assertNull(JsonUtil.getInstant(json, "unset"));
        assertNull(JsonUtil.getInstant(json, "invalid"));
        assertNull(JsonUtil.getInstant(json, "empty"));
        assertNull(JsonUtil.getInstant(json, "missing"));
        // Placeholders of the manual cooling and holiday dates
        assertNull(JsonUtil.getInstant(json, "epochLocal"));
        assertNull(JsonUtil.getInstant(json, "unset2019Local"));
        assertNull(JsonUtil.getInstant(json, "unset2019"));
        assertNull(JsonUtil.getInstant(json, "unset2019Offset"));
        assertEquals(Instant.parse("2019-01-01T00:00:01Z"), JsonUtil.getInstant(json, "afterPlaceholder"));
    }

    @Test
    void readsNumbersLeniently() {
        JsonObject json = JsonParser.parseString("{\"n\":1.5,\"s\":\"2.5\",\"x\":\"abc\",\"o\":{}}").getAsJsonObject();
        assertEquals(1.5, JsonUtil.getDouble(json, "n"));
        assertEquals(2.5, JsonUtil.getDouble(json, "s"));
        assertNull(JsonUtil.getDouble(json, "x"));
        assertNull(JsonUtil.getDouble(json, "o"));
        assertEquals(2, JsonUtil.getInteger(json, "n"));
    }
}
