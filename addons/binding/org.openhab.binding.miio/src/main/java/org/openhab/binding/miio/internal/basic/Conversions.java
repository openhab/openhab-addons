/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Conversion for values
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class Conversions {
    private static final Logger logger = LoggerFactory.getLogger(Conversions.class);

    public static JsonElement secondsToHours(JsonElement seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds.getAsInt());
        return new JsonPrimitive(hours);
    }

    public static JsonElement yeelightSceneConversion(JsonElement intValue) {
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

    public static JsonElement divideTen(JsonElement value10) {
        double value = value10.getAsDouble() / 10;
        return new JsonPrimitive(value);
    }

    public static JsonElement execute(String transfortmation, JsonElement value) {
        switch (transfortmation.toUpperCase()) {
            case "YEELIGHTSCENEID":
                return yeelightSceneConversion(value);
            case "SECONDSTOHOURS":
                return secondsToHours(value);
            case "/10":
                return divideTen(value);
            default:
                logger.debug("Transformation {} not found. Returning '{}'", transfortmation, value.toString());
                return value;
        }
    }

}
