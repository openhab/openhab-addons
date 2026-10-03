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
package org.openhab.binding.miio.internal.basic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Conversion for values
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Marcel Verpaalen - Add path and member selection to getJsonElement
 */
@NonNullByDefault
public class Conversions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Conversions.class);
    private static final Pattern PATH_SEGMENT = Pattern.compile("([^\\[\\]{}]*)(?:\\[(\\*|\\d{1,9})\\])?");

    /**
     * Converts a RGB+brightness input to a HSV value.
     * *
     *
     * @param RGB + brightness value (note brightness in the first byte)
     * @return HSV
     */
    private static JsonElement bRGBtoHSV(JsonElement bRGB) throws ClassCastException {
        if (bRGB.isJsonPrimitive() && bRGB.getAsJsonPrimitive().isNumber()) {
            Color rgb = new Color(bRGB.getAsInt());
            HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
            hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(bRGB.getAsInt() >>> 24));
            return new JsonPrimitive(hsb.toFullString());
        }
        return bRGB;
    }

    /**
     * Adds the brightness info (from separate channel) to a HSV value.
     * *
     *
     * @param RGB
     * @param map with device variables containing the brightness info
     * @param report brightness 0 on power off
     * @return HSV
     */
    private static JsonElement addBrightToHSV(JsonElement rgbValue, @Nullable Map<String, Object> deviceVariables,
            boolean powerDependent) throws ClassCastException, IllegalStateException {
        int bright = 100;
        if (deviceVariables != null) {
            JsonElement lastBright = (JsonElement) deviceVariables.getOrDefault("bright", new JsonPrimitive(100));
            bright = lastBright.getAsInt();
            if (powerDependent) {
                String lastPower = ((JsonElement) deviceVariables.getOrDefault("power", new JsonPrimitive("on")))
                        .getAsString();
                if (lastPower.toLowerCase().contentEquals("off")) {
                    bright = 0;
                }
            }
        }
        if (rgbValue.isJsonPrimitive()
                && (rgbValue.getAsJsonPrimitive().isNumber() || rgbValue.getAsString().matches("^[0-9]+$"))) {
            Color rgb = new Color(rgbValue.getAsInt());
            HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
            hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(bright));
            return new JsonPrimitive(hsb.toFullString());
        }
        return rgbValue;
    }

    public static JsonElement deviceDataTab(JsonElement deviceLog, @Nullable Map<String, Object> deviceVariables)
            throws ClassCastException, IllegalStateException {
        if (!deviceLog.isJsonObject() && !deviceLog.isJsonPrimitive()) {
            return deviceLog;
        }
        JsonObject deviceLogJsonObj = deviceLog.isJsonObject() ? deviceLog.getAsJsonObject()
                : (JsonObject) JsonParser.parseString(deviceLog.getAsString());
        JsonArray resultLog = new JsonArray();
        if (deviceLogJsonObj.has("data") && deviceLogJsonObj.get("data").isJsonArray()) {
            for (JsonElement element : deviceLogJsonObj.get("data").getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject dataObject = element.getAsJsonObject();
                    if (dataObject.has("value")) {
                        String value = dataObject.get("value").getAsString();
                        JsonElement val = JsonParser.parseString(value);
                        if (val.isJsonArray()) {
                            resultLog.add(JsonParser.parseString(val.getAsString()));
                        } else {
                            resultLog.add(val);
                        }
                    }
                }
            }
        }
        return resultLog;
    }

    private static JsonElement secondsToHours(JsonElement seconds) throws ClassCastException {
        double value = seconds.getAsDouble() / 3600;
        return new JsonPrimitive(value);
    }

    private static JsonElement yeelightSceneConversion(JsonElement intValue)
            throws ClassCastException, IllegalStateException {
        switch (intValue.getAsInt()) {
            case 1:
                return new JsonPrimitive("color");
            case 2:
                return new JsonPrimitive("hsv");
            case 3:
                return new JsonPrimitive("ct");
            case 4:
                return new JsonPrimitive("nightlight");
            case 5: // don't know the number for colorflow...
                return new JsonPrimitive("cf");
            case 6: // don't know the number for auto_delay_off, or if it is even in the properties visible...
                return new JsonPrimitive("auto_delay_off");
            default:
                return new JsonPrimitive("unknown");
        }
    }

    private static JsonElement divideTen(JsonElement value10) throws ClassCastException, IllegalStateException {
        double value = value10.getAsDouble() / 10.0;
        return new JsonPrimitive(value);
    }

    private static JsonElement divideHundred(JsonElement value10) throws ClassCastException, IllegalStateException {
        double value = value10.getAsDouble() / 100.0;
        return new JsonPrimitive(value);
    }

    private static JsonElement tankLevel(JsonElement value12) throws ClassCastException, IllegalStateException {
        // 127 without water tank. 120 = 100% water
        if (value12.getAsInt() == 127) {
            return new JsonPrimitive(-1);
        } else {
            double value = value12.getAsDouble();
            return new JsonPrimitive(value / 1.2);
        }
    }

    /**
     * Returns the deviceId element value from the Json response. If not found, returns the input
     *
     * @param responseValue
     * @param deviceVariables containing the deviceId
     * @return
     */
    private static JsonElement getDidElement(JsonElement responseValue, Map<String, Object> deviceVariables) {
        String did = (String) deviceVariables.get("deviceId");
        if (did != null) {
            return getJsonElement(did, responseValue);
        }
        LOGGER.debug("deviceId not Found, no conversion");
        return responseValue;
    }

    /**
     * Returns the element from the Json response. If not found, returns the input.
     * <p>
     * If the (top level) Json object has a member with exactly this name, that member is returned. Otherwise the
     * element is evaluated as a path. Empty segments are not allowed; a segment without a name is only valid when it
     * has an index, like {@code [*].name} on an array:
     * <ul>
     * <li>segments are separated by a dot, e.g. {@code result.recipes}</li>
     * <li>{@code name[n]} selects the n-th element (starting at 0) of the array {@code name}</li>
     * <li>{@code name[*]} applies the rest of the path to every element of the array {@code name} and returns the
     * results as an array; elements for which the path does not resolve are left out</li>
     * <li>a last segment {@code {a,b}} returns only the listed members of an object, or of every object in an
     * array; it must be the last segment and blank entries are ignored. A listed member can be a path (without
     * brackets), like
     * {@code {id,cook.time}}; it is returned with the last segment of the path as name, here {@code time}</li>
     * </ul>
     * While following a path, a string value that contains a Json object or array (as some cloud responses have) is
     * parsed, so that its members can be selected as well. A string at the end of the path is returned as is.
     * <p>
     * For example {@code recipes[*].{recipeID,recipeName}} turns {@code {"recipes":[{"recipeID":1,"recipeName":"A",
     * "tips":"..."}]}} into {@code [{"recipeID":1,"recipeName":"A"}]}.
     *
     * @param element name or path of the element to be found
     * @param responseValue Json object, array or a string containing Json
     * @return the element found or the unchanged input
     */
    private static JsonElement getJsonElement(String element, JsonElement responseValue) {
        try {
            if (responseValue.isJsonPrimitive() || responseValue.isJsonObject() || responseValue.isJsonArray()) {
                JsonElement jsonElement = responseValue.isJsonPrimitive()
                        ? JsonParser.parseString(responseValue.getAsString())
                        : responseValue;
                if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(element)) {
                    return jsonElement.getAsJsonObject().get(element);
                }
                if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                    final @Nullable JsonElement selected = selectPath(jsonElement, splitPath(element), 0);
                    if (selected != null) {
                        return selected;
                    }
                }
            }
        } catch (JsonParseException e) {
            // ignore
        }
        LOGGER.debug("JsonElement '{}' not found in '{}'", element, responseValue);
        return responseValue;
    }

    /**
     * Splits a path at the dots, except for dots within braces
     */
    private static List<String> splitPath(String path) {
        List<String> segments = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            } else if (c == '.' && depth <= 0) {
                segments.add(path.substring(start, i));
                start = i + 1;
            }
        }
        segments.add(path.substring(start));
        return segments;
    }

    private static @Nullable JsonElement selectPath(JsonElement element, List<String> segments, int pos) {
        if (pos >= segments.size()) {
            return element;
        }
        final JsonElement current = unwrapJson(element);
        final String segment = segments.get(pos);
        if (segment.startsWith("{") && segment.endsWith("}")) {
            if (pos != segments.size() - 1) {
                return null;
            }
            return selectMembers(current, List.of(segment.substring(1, segment.length() - 1).split(",")));
        }
        final Matcher m = PATH_SEGMENT.matcher(segment);
        if (!m.matches()) {
            return null;
        }
        JsonElement next = current;
        final String name = m.group(1);
        final @Nullable String index = m.group(2);
        if (name.isEmpty() && index == null) {
            return null;
        }
        if (!name.isEmpty()) {
            if (!next.isJsonObject() || !next.getAsJsonObject().has(name)) {
                return null;
            }
            next = next.getAsJsonObject().get(name);
        }
        if (index == null) {
            return selectPath(next, segments, pos + 1);
        }
        next = unwrapJson(next);
        if (!next.isJsonArray()) {
            return null;
        }
        final JsonArray array = next.getAsJsonArray();
        if (!"*".equals(index)) {
            final int i = Integer.parseInt(index);
            return i < array.size() ? selectPath(array.get(i), segments, pos + 1) : null;
        }
        final JsonArray results = new JsonArray();
        for (JsonElement item : array) {
            final @Nullable JsonElement result = selectPath(item, segments, pos + 1);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Returns the Json contained in a string value. Any other value, or a string that is not a Json object or array,
     * is returned unchanged.
     */
    private static JsonElement unwrapJson(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            final String text = element.getAsString().trim();
            if (text.startsWith("{") || text.startsWith("[")) {
                try {
                    return JsonParser.parseString(text);
                } catch (JsonParseException e) {
                    // not Json, keep the text
                }
            }
        }
        return element;
    }

    private static @Nullable JsonElement selectMembers(JsonElement element, List<String> members) {
        final JsonElement current = unwrapJson(element);
        if (current.isJsonArray()) {
            final JsonArray results = new JsonArray();
            for (JsonElement item : current.getAsJsonArray()) {
                final @Nullable JsonElement result = selectMembers(item, members);
                if (result != null) {
                    results.add(result);
                }
            }
            return results;
        }
        if (!current.isJsonObject()) {
            return null;
        }
        final JsonObject source = current.getAsJsonObject();
        final JsonObject result = new JsonObject();
        for (String member : members) {
            final String path = member.trim();
            if (path.isEmpty()) {
                continue;
            }
            if (source.has(path)) {
                result.add(path, source.get(path));
            } else if (!path.contains("{") && !path.contains("}")) {
                final List<String> segments = splitPath(path);
                final @Nullable JsonElement value = selectPath(source, segments, 0);
                if (value != null) {
                    result.add(segments.get(segments.size() - 1), value);
                }
            }
        }
        return result.size() > 0 ? result : null;
    }

    public static JsonElement execute(String transformation, JsonElement value, Map<String, Object> deviceVariables) {
        try {
            if (transformation.toUpperCase().startsWith("GETJSONELEMENT")) {
                if (transformation.length() > 15) {
                    return getJsonElement(transformation.substring(15), value);
                } else {
                    LOGGER.info("Transformation {} missing element. Returning '{}'", transformation, value.toString());
                }
            }
            switch (transformation.toUpperCase()) {
                case "YEELIGHTSCENEID":
                    return yeelightSceneConversion(value);
                case "SECONDSTOHOURS":
                    return secondsToHours(value);
                case "/10":
                    return divideTen(value);
                case "/100":
                    return divideHundred(value);
                case "TANKLEVEL":
                    return tankLevel(value);
                case "ADDBRIGHTTOHSV":
                    return addBrightToHSV(value, deviceVariables, false);
                case "ADDBRIGHTTOHSVPOWER":
                    return addBrightToHSV(value, deviceVariables, true);
                case "BRGBTOHSV":
                    return bRGBtoHSV(value);
                case "DEVICEDATATAB":
                    return deviceDataTab(value, deviceVariables);
                case "GETDIDELEMENT":
                    return getDidElement(value, deviceVariables);
                default:
                    LOGGER.debug("Transformation {} not found. Returning '{}'", transformation, value.toString());
                    return value;
            }
        } catch (ClassCastException | IllegalStateException e) {
            LOGGER.debug("Transformation {} failed. Returning '{}'", transformation, value.toString());
            return value;
        }
    }
}
