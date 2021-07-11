/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
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
    public static JsonElement bRGBtoHSV(JsonElement bRGB) throws ClassCastException {
        if (bRGB.isJsonPrimitive() && bRGB.getAsJsonPrimitive().isNumber()) {
            Color rgb = new Color(bRGB.getAsInt());
            HSBType hsb = HSBType.fromRGB(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
            hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(bRGB.getAsInt() >>> 24));
            return new JsonPrimitive(hsb.toFullString());
        }
        return bRGB;
    }

    public static JsonElement secondsToHours(JsonElement seconds) throws ClassCastException {
        double value = seconds.getAsDouble() / 3600;
        return new JsonPrimitive(value);
    }

    public static JsonElement yeelightSceneConversion(JsonElement intValue)
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

    public static JsonElement divideTen(JsonElement value10) throws ClassCastException, IllegalStateException {
        double value = value10.getAsDouble() / 10.0;
        return new JsonPrimitive(value);
    }

    public static JsonElement divideHundred(JsonElement value10) throws ClassCastException, IllegalStateException {
        double value = value10.getAsDouble() / 100.0;
        return new JsonPrimitive(value);
    }

    public static JsonElement tankLevel(JsonElement value12) throws ClassCastException, IllegalStateException {
        // 127 without water tank. 120 = 100% water
        if (value12.getAsInt() == 127) {
            return new JsonPrimitive(-1);
        } else {
            double value = value12.getAsDouble();
            return new JsonPrimitive(value / 1.2);
        }
    }

    public static JsonElement execute(String transfortmation, JsonElement value) {
        try {
            switch (transfortmation.toUpperCase()) {
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
                case "BRGBTOHSV":
                    return bRGBtoHSV(value);
                default:
                    LOGGER.debug("Transformation {} not found. Returning '{}'", transfortmation, value.toString());
                    return value;
            }
        } catch (ClassCastException | IllegalStateException e) {
            LOGGER.debug("Transformation {} failed. Returning '{}'", transfortmation, value.toString());
            return value;
        }
    }
}
