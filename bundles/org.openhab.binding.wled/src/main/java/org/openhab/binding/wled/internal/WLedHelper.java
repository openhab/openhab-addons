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
package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.BIG_DECIMAL_2_55;
import static org.openhab.binding.wled.internal.WLedBindingConstants.BINDING_ID;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link WLedHelper} Provides helper classes that are used from multiple classes in the binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class WLedHelper {
    public static HSBType parseToHSBType(String message) {
        // example message rgb in array brackets [255.0, 255.0, 255.0]
        List<String> colors = Arrays.asList(message.replaceAll("\\[|\\]", "").split("\\s*,\\s*"));
        try {
            int r = new BigDecimal(colors.get(0)).intValue();
            int g = new BigDecimal(colors.get(1)).intValue();
            int b = new BigDecimal(colors.get(2)).intValue();
            return HSBType.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            return new HSBType();
        }
    }

    public static PercentType parseWhitePercent(String message) {
        // example message rgb in array brackets [255.0, 255.0, 255.0, 255.0]
        List<String> colors = Arrays.asList(message.replaceAll("\\[|\\]", "").split("\\s*,\\s*"));
        try {
            return new PercentType(new BigDecimal(colors.get(3)).divide(BIG_DECIMAL_2_55, RoundingMode.HALF_UP));
        } catch (IllegalArgumentException e) {
            return new PercentType();
        }
    }

    /**
     * Turns a text like "1, 2.5, 3" into a list of numbers.
     *
     * @throws NumberFormatException when a value is not a number
     */
    public static List<Double> parseNumberList(String text) {
        List<Double> numbers = new ArrayList<>();
        for (String part : text.split(",")) {
            if (!part.isBlank()) {
                numbers.add(Double.parseDouble(part.trim()));
            }
        }
        return numbers;
    }

    /**
     * Reads one value from a JSON text, the path is the names of the objects separated by dots like
     * um.AudioReactive.sync.mode
     *
     * @return The value as JSON, or null if the path does not exist
     * @throws JsonSyntaxException when the JSON is not valid
     */
    public static @Nullable String getJsonPath(String json, String path) {
        JsonElement element = JsonParser.parseString(json);
        for (String name : path.split("\\.")) {
            if (name.isBlank()) {
                continue;
            }
            if (!element.isJsonObject() || !element.getAsJsonObject().has(name)) {
                return null;
            }
            element = element.getAsJsonObject().get(name);
        }
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString()
                : element.toString();
    }

    /**
     * Builds a JSON object from a path and a value, getting um.AudioReactive.enabled and true makes
     * {"um":{"AudioReactive":{"enabled":true}}}. The value is used as JSON when it is valid JSON, otherwise as text.
     *
     * @throws IllegalArgumentException when the path is empty
     */
    public static String buildJsonPath(String path, String value) {
        String[] names = path.split("\\.");
        JsonElement element;
        try {
            element = JsonParser.parseString(value);
        } catch (JsonSyntaxException e) {
            element = new JsonPrimitive(value);
        }
        boolean used = false;
        for (int count = names.length - 1; count >= 0; count--) {
            if (names[count].isBlank()) {
                continue;
            }
            JsonObject wrapper = new JsonObject();
            wrapper.add(names[count], element);
            element = wrapper;
            used = true;
        }
        if (!used) {
            throw new IllegalArgumentException("The path is empty");
        }
        return element.toString();
    }

    /**
     * Creates a channel for a Thing that was created before the channel was added to the binding.
     */
    public static Channel buildChannel(ThingUID thingUID, String channelId, String itemType) {
        return ChannelBuilder.create(new ChannelUID(thingUID, channelId), itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelId)).build();
    }

    /**
     * Converts a value that is between 0 and max into a percentage, values outside of the range are limited.
     */
    public static PercentType toPercent(int value, int max) {
        int limited = Math.max(0, Math.min(max, value));
        return new PercentType(new BigDecimal(limited).multiply(BigDecimal.valueOf(100)).divide(new BigDecimal(max), 0,
                RoundingMode.HALF_UP));
    }

    /**
     * Converts a percentage into a value that is between 0 and max.
     */
    public static int fromPercent(PercentType percent, int max) {
        return percent.toBigDecimal().multiply(new BigDecimal(max))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * Describes a request body for logging. A WLED configuration ({@code /json/cfg}) can hold secrets like the Wi-Fi,
     * access point, OTA and MQTT passwords, so for it only the paths of the settings are returned, never the values.
     */
    public static String describeForLog(String url, String json) {
        if (!url.startsWith("/json/cfg")) {
            return json;
        }
        try {
            List<String> paths = new ArrayList<>();
            collectSettingPaths(JsonParser.parseString(json), "", paths);
            return "<values hidden> settings:" + paths;
        } catch (JsonSyntaxException e) {
            return "<hidden>";
        }
    }

    private static void collectSettingPaths(JsonElement element, String prefix, List<String> paths) {
        if (element instanceof JsonObject object) {
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                collectSettingPaths(entry.getValue(), prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(),
                        paths);
            }
        } else if (!prefix.isEmpty()) {
            paths.add(prefix);
        }
    }

    /**
     * @return A string that starts after finding the element and terminates when it finds the first occurrence of the
     *         end string after the element.
     */
    static String getValue(String message, String element, String end) {
        int startIndex = message.indexOf(element);
        if (startIndex != -1) // -1 means "not found"
        {
            int endIndex = message.indexOf(end, startIndex + element.length());
            if (endIndex != -1) {
                return message.substring(startIndex + element.length(), endIndex);
            }
        }
        return "";
    }

    public static String getMacAddress(String message) {
        String macAddress = getValue(message, "\"mac\":\"", "\"");
        if (!macAddress.isBlank()) {
            return macAddress.replaceAll("[^A-Fa-f0-9]", "").toUpperCase().replaceAll("(.{2})(?!$)", "$1:");
        }
        return "";
    }
}
