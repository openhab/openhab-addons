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
package org.openhab.binding.awtrixng.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link Helper} converts the JSON payloads exchanged with the clock. Gson omits null fields, which is what keeps a
 * payload free of keys AWTRIX NG would reject.
 *
 * @author Keith T. Garner - Initial contribution
 */
@NonNullByDefault
public class Helper {

    private static final Pattern HEX_COLOR = Pattern.compile("#?([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})");

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Helper() {
    }

    public static String toJson(Object payload) {
        return GSON.toJson(payload);
    }

    /**
     * Parses a payload received from the clock.
     *
     * @return the parsed payload, or null if it is empty or not valid JSON for the given type
     */
    public static <T> @Nullable T fromJson(String json, Class<T> type) {
        try {
            return GSON.fromJson(json, type);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * @return the color as the {@code #RRGGBB} string AWTRIX NG accepts
     */
    public static String toHex(HSBType color) {
        int[] rgb = ColorUtil.hsbToRgb(color);
        return String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }

    /**
     * @return the color for a {@code #RRGGBB} string as reported by the clock, or null if it cannot be parsed
     */
    public static @Nullable HSBType fromHex(@Nullable String hex) {
        if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
            return null;
        }
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return ColorUtil.rgbToHsb(new int[] { (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF });
    }

    /**
     * @return the value of a percent or plain number command, or null for any other command
     */
    public static @Nullable Double toPercent(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> percent = quantity.toUnit(Units.PERCENT);
            return percent == null ? null : percent.doubleValue();
        }
        return command instanceof DecimalType decimal ? decimal.doubleValue() : null;
    }

    /**
     * @return the milliseconds of a time quantity, a plain number being taken as milliseconds, or null for any other
     *         command
     */
    public static @Nullable Double toMillis(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> millis = quantity.toUnit(MetricPrefix.MILLI(Units.SECOND));
            return millis == null ? null : millis.doubleValue();
        }
        return command instanceof DecimalType decimal ? decimal.doubleValue() : null;
    }

    /**
     * @return the color as {@code #RRGGBB} or {@code #RGB} in uppercase, or null if it is not a hex color
     */
    public static @Nullable String normalizeColor(@Nullable String color) {
        if (color == null) {
            return null;
        }
        Matcher matcher = HEX_COLOR.matcher(color.trim());
        return matcher.matches() ? "#" + matcher.group(1).toUpperCase() : null;
    }

    /**
     * Builds a payload whose keys keep the given order, which a {@link java.util.Map} does not guarantee.
     *
     * @param keyValues alternating key and value; a null value leaves the key out
     */
    public static String jsonObject(Object... keyValues) {
        JsonObject object = new JsonObject();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                object.add((String) keyValues[i], GSON.toJsonTree(value));
            }
        }
        return GSON.toJson(object);
    }
}
