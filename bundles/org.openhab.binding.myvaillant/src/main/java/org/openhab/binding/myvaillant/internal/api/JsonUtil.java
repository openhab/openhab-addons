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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Access to the JSON returned by the myVAILLANT API.
 *
 * <p>
 * The API mixes camelCase (system state) and snake_case (devices, statistics). Like myPyllant, all keys are converted
 * to snake_case after reading, so the field names match myPyllant's models (e.g.
 * {@code cylinderTemperatureSensorTopDHW}
 * becomes {@code cylinder_temperature_sensor_top_d_h_w}).
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public final class JsonUtil {

    private static final Pattern UPPER_CASE = Pattern.compile("(?<!^)(?=[A-Z])");

    /** The latest date the API uses as placeholder for an unset date, see {@link #getInstant(JsonObject, String)} */
    static final Instant LATEST_PLACEHOLDER = Instant.parse("2019-01-01T00:00:00Z");

    private JsonUtil() {
    }

    public static JsonElement toSnakeCase(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                result.add(toSnakeCase(entry.getKey()), toSnakeCase(entry.getValue()));
            }
            return result;
        } else if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            element.getAsJsonArray().forEach(item -> result.add(toSnakeCase(item)));
            return result;
        }
        return element;
    }

    public static JsonElement toCamelCase(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                result.add(toCamelCase(entry.getKey()), toCamelCase(entry.getValue()));
            }
            return result;
        } else if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            element.getAsJsonArray().forEach(item -> result.add(toCamelCase(item)));
            return result;
        }
        return element;
    }

    static String toSnakeCase(String key) {
        return UPPER_CASE.matcher(key).replaceAll("_").toLowerCase(Locale.ROOT);
    }

    static String toCamelCase(String key) {
        String[] parts = key.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(parts[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(parts[i].substring(1));
            }
        }
        return result.toString();
    }

    public static @Nullable JsonObject getObject(@Nullable JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    public static @Nullable JsonArray getArray(@Nullable JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static @Nullable JsonPrimitive getPrimitive(@Nullable JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsJsonPrimitive() : null;
    }

    public static @Nullable String getString(@Nullable JsonObject object, String key) {
        JsonPrimitive primitive = getPrimitive(object, key);
        return primitive == null ? null : primitive.getAsString();
    }

    public static @Nullable Double getDouble(@Nullable JsonObject object, String key) {
        JsonPrimitive primitive = getPrimitive(object, key);
        if (primitive == null) {
            return null;
        }
        if (primitive.isNumber()) {
            return primitive.getAsDouble();
        }
        try {
            return Double.valueOf(primitive.getAsString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static @Nullable Integer getInteger(@Nullable JsonObject object, String key) {
        Double value = getDouble(object, key);
        return value == null ? null : (int) Math.round(value);
    }

    public static @Nullable Boolean getBoolean(@Nullable JsonObject object, String key) {
        JsonPrimitive primitive = getPrimitive(object, key);
        if (primitive == null) {
            return null;
        }
        return primitive.isBoolean() ? primitive.getAsBoolean() : Boolean.valueOf(primitive.getAsString());
    }

    /**
     * Reads a date like {@code 2024-10-12T10:28:43Z} or one with an offset. The API marks unset dates with
     * placeholders: the epoch (also as {@code 1969-12-31T23:00:00Z}), {@code 2015-01-01T00:00:00Z} and midnight of
     * 2019-01-01 in UTC or in the system's time zone (e.g. {@code 2018-12-31T23:00:00Z}). Dates up to
     * {@link #LATEST_PLACEHOLDER} are therefore returned as {@code null}.
     */
    public static @Nullable Instant getInstant(@Nullable JsonObject object, String key) {
        String value = getString(object, key);
        if (value == null || value.isBlank()) {
            return null;
        }
        Instant instant;
        try {
            instant = OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
        return instant.isAfter(LATEST_PLACEHOLDER) ? instant : null;
    }

    public static String requireString(@Nullable JsonObject object, String key) throws MyVaillantException {
        String value = getString(object, key);
        if (value == null) {
            throw new MyVaillantException("Missing '" + key + "' in response");
        }
        return value;
    }

    public static JsonObject requireObject(@Nullable JsonElement element, String description)
            throws MyVaillantException {
        if (element == null || !element.isJsonObject()) {
            throw new MyVaillantException("Expected a JSON object for " + description);
        }
        return element.getAsJsonObject();
    }

    public static JsonArray requireArray(@Nullable JsonElement element, String description) throws MyVaillantException {
        if (element == null || !element.isJsonArray()) {
            throw new MyVaillantException("Expected a JSON array for " + description);
        }
        return element.getAsJsonArray();
    }
}
