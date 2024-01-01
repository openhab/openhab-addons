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
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Conditional Execution of rules
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class ActionConditions {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActionConditions.class);

    /**
     * Check if it matches the firmware version.
     *
     * @param condition
     * @param deviceVariables
     * @param value
     * @return value in case firmware is matching, return null if not
     */
    private static @Nullable JsonElement firmwareCheck(MiIoDeviceActionCondition condition,
            @Nullable Map<String, Object> deviceVariables, @Nullable JsonElement value) {
        // TODO: placeholder for firmware version check to allow for firmware dependent actions
        return value;
    }

    /**
     * Check if the value is a valid brightness for operating power On/Off switch.
     * If brightness <1 returns Off, if >=1 returns On
     *
     * @param value
     * @return
     */
    private static @Nullable JsonElement brightness(@Nullable JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            if (value.getAsInt() < 1) {
                return new JsonPrimitive("off");
            } else {
                return new JsonPrimitive("on");
            }
        } else {
            LOGGER.debug("Could not parse brightness. Value '{}' is not an int", value);
        }
        return value;
    }

    /**
     * Convert HSV value to RGB+Brightness
     *
     * @param value
     * @return RGB value + brightness as first byte
     */
    private static @Nullable JsonElement hsvToBRGB(@Nullable Command command, @Nullable JsonElement value) {
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            Color color = Color.getHSBColor(hsb.getHue().floatValue() / 360, hsb.getSaturation().floatValue() / 100,
                    hsb.getBrightness().floatValue() / 100);
            return new JsonPrimitive((hsb.getBrightness().byteValue() << 24) + (color.getRed() << 16)
                    + (color.getGreen() << 8) + color.getBlue());
        }
        return null;
    }

    /**
     * Check if the value is a valid brightness between 1-100 which can be send to brightness channel.
     * If not returns a null
     *
     * @param value
     * @return
     */
    private static @Nullable JsonElement brightnessExists(@Nullable JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            int intVal = value.getAsInt();
            if (intVal > 0 && intVal <= 100) {
                return value;
            } else if (intVal > 100) {
                return new JsonPrimitive(100);
            }
            return null;
        } else {
            LOGGER.debug("Could not parse brightness. Value '{}' is not an int", value);
        }
        return value;
    }

    /**
     * Check if the value is a color which can be send to Color channel.
     * If not returns a null
     *
     * @param command
     *
     * @param value
     * @return
     */
    private static @Nullable JsonElement hsbOnly(@Nullable Command command, @Nullable JsonElement value) {
        if (command instanceof HSBType) {
            return value;
        }
        return null;
    }

    /**
     * Check if the command value matches the condition value.
     * The condition parameter element should be a Json array, containing Json objects with a matchValue element.
     * Optionally it can contain a 'returnValue'element which will be returned in case of match.
     * If no match this function will return a null
     *
     * @param condition.
     * @param command
     * @param value
     * @return returnValue or value in case matching, return null if no match
     */
    private static @Nullable JsonElement matchValue(MiIoDeviceActionCondition condition, @Nullable Command command,
            @Nullable JsonElement value) {
        if (condition.getParameters().isJsonArray() && command != null) {
            JsonArray conditionArray = condition.getParameters().getAsJsonArray();
            for (int i = 0; i < conditionArray.size(); i++) {
                if (conditionArray.get(i).isJsonObject() && conditionArray.get(i).getAsJsonObject().has("matchValue")) {
                    JsonObject matchCondition = conditionArray.get(i).getAsJsonObject();
                    String matchvalue = matchCondition.get("matchValue").getAsString();
                    boolean matching = command.toString().matches(matchvalue);
                    LOGGER.trace("Matching '{}' with '{}': {}", matchvalue, command, matching);
                    if (matching) {
                        if (matchCondition.has("returnValue")) {
                            return matchCondition.get("returnValue");
                        } else {
                            return value;
                        }
                    }
                } else {
                    LOGGER.debug("Json DB condition is missing matchValue element in match parameter array.");
                }
            }
        } else {
            LOGGER.debug("Json DB condition is missing match parameter array.");
        }
        return null;
    }

    public static @Nullable JsonElement executeAction(MiIoDeviceActionCondition condition,
            @Nullable Map<String, Object> deviceVariables, @Nullable JsonElement value, @Nullable Command command) {
        switch (condition.getName().toUpperCase()) {
            case "FIRMWARE":
                return firmwareCheck(condition, deviceVariables, value);
            case "BRIGHTNESSEXISTING":
                return brightnessExists(value);
            case "HSVTOBRGB":
                return hsvToBRGB(command, value);
            case "BRIGHTNESSONOFF":
                return brightness(value);
            case "HSBONLY":
                return hsbOnly(command, value);
            case "MATCHVALUE":
                return matchValue(condition, command, value);
            default:
                LOGGER.debug("Condition {} not found. Returning '{}'", condition,
                        value != null ? value.toString() : "");
                return value;
        }
    }
}
