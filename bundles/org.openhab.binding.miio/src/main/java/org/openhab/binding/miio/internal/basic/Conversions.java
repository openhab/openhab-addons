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
package org.openhab.binding.miio.internal.basic;

import java.awt.Color;
import java.util.Map;

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
 */
@NonNullByDefault
public class Conversions {
    private static final Logger LOGGER = LoggerFactory.getLogger(Conversions.class);

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
     * Returns the element from the Json response. If not found, returns the input
     *
     * @param element to be found
     * @param responseValue
     * @return
     */
    private static JsonElement getJsonElement(String element, JsonElement responseValue) {
        try {
            if (responseValue.isJsonPrimitive() || responseValue.isJsonObject()) {
                JsonElement jsonElement = responseValue.isJsonObject() ? responseValue
                        : JsonParser.parseString(responseValue.getAsString());
                if (jsonElement.isJsonObject()) {
                    JsonObject value = jsonElement.getAsJsonObject();
                    if (value.has(element)) {
                        return value.get(element);
                    }
                }
            }
        } catch (JsonParseException e) {
            // ignore
        }
        LOGGER.debug("JsonElement '{}' not found in '{}'", element, responseValue);
        return responseValue;
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
