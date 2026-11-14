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
package org.openhab.binding.dreame.internal.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameVacuumCapabilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Produces bounded vacuum diagnostics with an explicit allowlist for numeric status values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameVacuumDiagnostics {
    private static final int MAX_BYTES = 65536;
    private static final int MAX_PARAMETERS = 32;
    private static final int MAX_INFLATED_BYTES = 1024 * 1024;
    private static final int MAP_HEADER_SIZE = 27;

    private DreameVacuumDiagnostics() {
    }

    public static String describe(DreameDevice device) {
        return "model=" + (device.isVacuum() ? device.model() : "<unknown>") + ", firmwarePresent="
                + !device.version().isBlank() + ", ownerPresent=" + !device.masterUid().isBlank() + ", brokerPresent="
                + !device.bindDomain().isBlank();
    }

    public static String describeMessage(byte[] payload) {
        return describeMessage(payload, "");
    }

    public static String describeMessage(byte[] payload, String model) {
        if (payload.length == 0 || payload.length > MAX_BYTES) {
            return "message=omitted-size";
        }
        String json = new String(payload, StandardCharsets.UTF_8);
        if (!boundedDepth(json)) {
            return "message=omitted-depth";
        }
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!(parsed instanceof JsonObject root)) {
                return "message=unknown-envelope";
            }
            JsonObject data = root.get("data") instanceof JsonObject nested ? nested : root;
            String method = data.get("method") instanceof JsonPrimitive value && value.isString() ? value.getAsString()
                    : "";
            String knownMethod = switch (method) {
                case "properties_changed", "event_occured" -> method;
                default -> "unknown";
            };
            StringJoiner addresses = new StringJoiner(",", "[", "]");
            if (data.get("params") instanceof JsonArray params) {
                for (int i = 0; i < Math.min(params.size(), MAX_PARAMETERS); i++) {
                    if (params.get(i) instanceof JsonObject parameter) {
                        String service = address(parameter.get("siid"));
                        String property = address(parameter.get("piid"));
                        String numericValue = isSupportedModel(model) && "properties_changed".equals(knownMethod)
                                ? statusValue(service, property, parameter) + mapMetadata(service, property, parameter)
                                : "";
                        addresses.add("siid=" + service + "/piid=" + property + "/type="
                                + valueType(parameter.get("value")) + numericValue);
                    }
                }
                return "method=" + knownMethod + ", params=" + params.size() + ", properties=" + addresses
                        + ", truncated=" + (params.size() > MAX_PARAMETERS);
            }
            return "method=" + knownMethod + ", paramsType=" + valueType(data.get("params"));
        } catch (RuntimeException e) {
            return "message=invalid-json";
        }
    }

    /** Returns only validated numeric property updates for mapped vacuum models. */
    public static Map<String, Integer> readProperties(byte[] payload, String model) {
        if (!isSupportedModel(model) || payload.length == 0 || payload.length > MAX_BYTES) {
            return Map.of();
        }
        String json = new String(payload, StandardCharsets.UTF_8);
        if (!boundedDepth(json)) {
            return Map.of();
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!(parsed instanceof JsonObject root)) {
                return Map.of();
            }
            JsonObject data = root.get("data") instanceof JsonObject nested ? nested : root;
            if (!(data.get("method") instanceof JsonPrimitive method) || !method.isString()
                    || !"properties_changed".equals(method.getAsString())
                    || !(data.get("params") instanceof JsonArray params)) {
                return Map.of();
            }
            for (int i = 0; i < Math.min(params.size(), MAX_PARAMETERS); i++) {
                if (params.get(i) instanceof JsonObject parameter) {
                    String service = address(parameter.get("siid"));
                    String property = address(parameter.get("piid"));
                    Integer value = propertyValue(service, property, parameter);
                    if (value != null) {
                        result.put(service + "/" + property, value);
                    }
                }
            }
            return Map.copyOf(result);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    private static boolean isSupportedModel(String model) {
        return DreameVacuumCapabilities.isSupported(model);
    }

    /** Parses successful results from an explicit vacuum property request. */
    public static Map<String, Integer> readPropertyResults(JsonArray parameters) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(parameters.size(), MAX_PARAMETERS); i++) {
            if (parameters.get(i) instanceof JsonObject parameter && boundedInteger(parameter.get("code"), 0) != null) {
                String service = address(parameter.get("siid"));
                String property = address(parameter.get("piid"));
                Integer value = propertyValue(service, property, parameter);
                if (value != null) {
                    result.put(service + "/" + property, value);
                }
            }
        }
        return Map.copyOf(result);
    }

    /** Parses bounded string results needed for structured vacuum settings. */
    public static Map<String, String> readTextPropertyResults(JsonArray parameters) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(parameters.size(), MAX_PARAMETERS); i++) {
            if (parameters.get(i) instanceof JsonObject parameter && boundedInteger(parameter.get("code"), 0) != null
                    && parameter.get("value") instanceof JsonPrimitive value && value.isString()) {
                String service = address(parameter.get("siid"));
                String property = address(parameter.get("piid"));
                String text = value.getAsString();
                if ("4".equals(service) && "50".equals(property) && text.length() <= 8192) {
                    result.put("4/50", text);
                }
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Addresses follow Tasshack/dreame-vacuum's DreameVacuumPropertyMapping in dreame/types.py.
     * Diagnostics preserve raw codes independently of the channel state names.
     */
    private static String statusValue(String service, String property, JsonObject parameter) {
        Integer value = propertyValue(service, property, parameter);
        return value == null ? "" : "/value=" + value;
    }

    /** Development diagnostics only; never expose map contents, object names or embedded keys. */
    private static String mapMetadata(String service, String property, JsonObject parameter) {
        if (!"6".equals(service) || !("1".equals(property) || "3".equals(property) || "8".equals(property))
                || parameter.has("code") && boundedInteger(parameter.get("code"), 0) == null
                || !(parameter.get("value") instanceof JsonPrimitive value) || !value.isString()) {
            return "";
        }
        String raw = value.getAsString();
        String metadata = "/chars=" + raw.length();
        if (!"1".equals(property)) {
            return metadata;
        }
        // Tasshack/dreame-vacuum map.py accepts URL-safe base64 and an optional comma-separated key.
        int separator = raw.indexOf(',');
        metadata += "/keyPresent=" + (separator >= 0 && separator + 1 < raw.length());
        String encoded = (separator < 0 ? raw : raw.substring(0, separator)).replace('-', '+').replace('_', '/')
                .replaceAll("[\\t\\n\\r ]", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            boolean zlibHeader = decoded.length >= 2 && (decoded[0] & 0x0F) == 8 && (decoded[0] & 0xFF) >>> 4 <= 7
                    && (((decoded[0] & 0xFF) << 8) + (decoded[1] & 0xFF)) % 31 == 0;
            // A matching header is only a format hint, not validation of a complete map.
            return metadata + "/base64=true/decodedBytes=" + decoded.length + "/zlibHeader=" + zlibHeader
                    + (separator < 0 && zlibHeader ? inflatedMapMetadata(decoded) : "");
        } catch (IllegalArgumentException e) {
            return metadata + "/base64=false";
        }
    }

    private static String inflatedMapMetadata(byte[] compressed) {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            byte[] buffer = new byte[4096];
            byte[] header = new byte[MAP_HEADER_SIZE];
            int total = 0;
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && !inflater.finished()) {
                    return "/inflate=incomplete";
                }
                if (total < MAP_HEADER_SIZE) {
                    System.arraycopy(buffer, 0, header, total, Math.min(count, MAP_HEADER_SIZE - total));
                }
                total += count;
                if (total > MAX_INFLATED_BYTES) {
                    return "/inflate=omitted-size";
                }
            }
            String summary = "/inflate=ok/inflatedBytes=" + total + "/trailingBytes=" + inflater.getRemaining();
            if (total < MAP_HEADER_SIZE) {
                return summary + "/mapHeader=false";
            }
            // Reference layout: signed little-endian dimensions at 19/21, after a 27-byte header.
            // Retain only that header and expose structural checks, never coordinates or pixel data.
            int width = (short) ((header[19] & 0xFF) | ((header[20] & 0xFF) << 8));
            int height = (short) ((header[21] & 0xFF) | ((header[22] & 0xFF) << 8));
            boolean pixelBlockFits = width >= 0 && height >= 0
                    && (long) MAP_HEADER_SIZE + (long) width * height <= total;
            return summary + "/mapHeader=true/pixelBlockFits=" + pixelBlockFits;
        } catch (DataFormatException e) {
            return "/inflate=invalid";
        } finally {
            inflater.end();
        }
    }

    private static @Nullable Integer propertyValue(String service, String property, JsonObject parameter) {
        int maximum = switch (service + "/" + property) {
            case "3/1", "9/2", "10/2", "11/1", "16/1", "18/1", "20/1" -> 100;
            case "2/2", "4/23" -> 65535;
            case "4/2", "4/3", "9/1", "10/1", "11/2", "12/2", "12/3", "12/4", "16/2", "18/2", "20/2" ->
                Integer.MAX_VALUE;
            case "2/1", "3/2", "4/1", "4/4", "4/5", "4/7", "4/25", "4/40", "15/3" -> 255;
            default -> -1;
        };
        if (maximum < 0 || parameter.has("code") && boundedInteger(parameter.get("code"), 0) == null) {
            return null;
        }
        return boundedInteger(parameter.get("value"), maximum);
    }

    private static @Nullable Integer boundedInteger(@Nullable JsonElement value, int maximum) {
        if (value instanceof JsonPrimitive primitive && primitive.isNumber()) {
            try {
                int number = primitive.getAsBigDecimal().intValueExact();
                return number >= 0 && number <= maximum ? number : null;
            } catch (ArithmeticException | NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String address(@Nullable JsonElement value) {
        if (value instanceof JsonPrimitive primitive && primitive.isNumber()) {
            try {
                int number = primitive.getAsBigDecimal().intValueExact();
                if (number > 0 && number <= 65535) {
                    return Integer.toString(number);
                }
            } catch (ArithmeticException | NumberFormatException ignored) {
                return "unknown";
            }
        }
        return "unknown";
    }

    private static String valueType(@Nullable JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "null";
        }
        if (value.isJsonObject()) {
            return "object";
        }
        if (value.isJsonArray()) {
            return "array";
        }
        JsonPrimitive primitive = value.getAsJsonPrimitive();
        return primitive.isBoolean() ? "boolean" : primitive.isNumber() ? "number" : "string";
    }

    static boolean boundedDepth(String json) {
        boolean quoted = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = 0; i < json.length(); i++) {
            char character = json.charAt(i);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    quoted = false;
                }
            } else if (character == '"') {
                quoted = true;
            } else if (character == '{' || character == '[') {
                if (++depth > 32) {
                    return false;
                }
            } else if (character == '}' || character == ']') {
                depth--;
            }
        }
        return true;
    }
}
