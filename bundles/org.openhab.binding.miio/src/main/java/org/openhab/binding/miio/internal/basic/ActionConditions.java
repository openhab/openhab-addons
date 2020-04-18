/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
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
            Map<String, Object> deviceVariables, @Nullable JsonElement value) {
        // TODO: placeholder for firmware version check to allow for firmware dependent actions
        return value;
    }

    /**
     * Check if the value is a valid brightness between 1-99.
     * If <1 returns Off, if >99 returns On to activate the power On/Off switch
     *
     * @param value
     * @return
     */
    private static @Nullable JsonElement brightness(@Nullable JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            int intVal = value.getAsInt();
            if (intVal > 99) {
                return new JsonPrimitive("on");
            }
            if (intVal < 1) {
                return new JsonPrimitive("off");
            }
        } else {
            LOGGER.debug("Could not parse brightness. Value '{}' is not an int", value);
        }
        return null;
    }

    /**
     * Check if the value is a valid brightness between 1-99 which can be send to brightness channel.
     * If not returns a null
     *
     * @param value
     * @return
     */
    private static @Nullable JsonElement brightnessExists(@Nullable JsonElement value) {
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            int intVal = value.getAsInt();
            if (intVal > 0 && intVal < 99) {
                return value;
            }
        } else {
            LOGGER.debug("Could not parse brightness. Value '{}' is not an int", value);
        }
        return null;
    }

    public static @Nullable JsonElement executeAction(MiIoDeviceActionCondition condition,
            Map<String, Object> deviceVariables, @Nullable JsonElement value) {
        switch (condition.getName().toUpperCase()) {
            case "FIRMWARE":
                return firmwareCheck(condition, deviceVariables, value);
            case "BRIGHTNESSEXISTING":
                return brightnessExists(value);
            case "BRIGHTNESSONOFF":
                return brightness(value);
            default:
                LOGGER.debug("Condition {} not found. Returning '{}'", condition,
                        value != null ? value.toString() : "");
                return value;
        }
    }
}
