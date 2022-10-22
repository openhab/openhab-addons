/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.reflect.TypeToken;

/**
 * Detailed sensor information
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FullSensor extends FullHueObject {
    public static final Type GSON_TYPE = new TypeToken<Map<String, FullSensor>>() {
    }.getType();

    public static final String STATE_LAST_UPDATED = "lastupdated";
    public static final String STATE_BUTTON_EVENT = "buttonevent";
    public static final String STATE_PRESENCE = "presence";
    public static final String STATE_TEMPERATURE = "temperature";
    public static final String STATE_LIGHT_LEVEL = "lightlevel";
    public static final String STATE_DARK = "dark";
    public static final String STATE_DAYLIGHT = "daylight";
    public static final String STATE_STATUS = "status";
    public static final String STATE_FLAG = "flag";

    public static final String CONFIG_REACHABLE = "reachable";
    public static final String CONFIG_BATTERY = "battery";
    public static final String CONFIG_ON = "on";
    public static final String CONFIG_LED_INDICATION = "ledindication";

    public static final String CONFIG_PRESENCE_SENSITIVITY = "sensitivity";
    public static final String CONFIG_PRESENCE_SENSITIVITY_MAX = "sensitivitymax";

    public static final String CONFIG_LIGHT_LEVEL_THRESHOLD_DARK = "tholddark";
    public static final String CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET = "tholdoffset";

    private @NonNullByDefault({}) Map<String, Object> state;
    private @NonNullByDefault({}) Map<String, Object> config;

    public Map<String, Object> getState() {
        return state;
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
