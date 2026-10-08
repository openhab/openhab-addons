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

package org.openhab.automation.optimalwindow.internal.calc;

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.math.BigDecimal;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.util.DurationUtils;

/**
 * Stores the window configuration
 *
 * @author Wolfgang Klimt - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@NonNullByDefault
public class WindowConfiguration {
    public String forecastItem = "";
    public @Nullable String persistenceService;
    public int rangeStart = 0;
    public int rangeDuration = 24;
    public Duration length = Duration.ZERO;
    public boolean consecutive = true;
    public boolean maximum = false;
    public boolean preferStart = false;

    /**
     * Read and validate the window configuration of a module.
     *
     * @param config the module configuration
     * 
     * @return the window configuration
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public static WindowConfiguration from(Configuration config) throws IllegalArgumentException {
        WindowConfiguration result = new WindowConfiguration();

        if (!(getValue(config, CONFIG_FORECAST_ITEM) instanceof String item)) {
            throw new IllegalArgumentException("Forecast item is not set");
        }
        result.forecastItem = item;

        if (getValue(config, CONFIG_PERSISTENCE_SERVICE) instanceof String service) {
            result.persistenceService = service;
        }

        result.rangeStart = getInt(config, CONFIG_RANGE_START, result.rangeStart);
        if (result.rangeStart < 0 || result.rangeStart > 23) {
            throw new IllegalArgumentException("Range start must be between 0 and 23");
        }

        result.rangeDuration = getInt(config, CONFIG_RANGE_DURATION, result.rangeDuration);
        if (result.rangeDuration < 1 || result.rangeDuration > 48) {
            throw new IllegalArgumentException("Range duration must be between 1 and 48 hours");
        }

        Object length = getValue(config, CONFIG_LENGTH);
        if (length == null) {
            throw new IllegalArgumentException("Length is not set");
        }
        result.length = DurationUtils.parse(length.toString());
        if (result.length.isNegative() || result.length.isZero()
                || result.length.compareTo(Duration.ofHours(result.rangeDuration)) > 0) {
            throw new IllegalArgumentException("Length must be positive and not longer than the range duration");
        }

        result.consecutive = getBoolean(config, CONFIG_CONSECUTIVE, result.consecutive);
        result.preferStart = getBoolean(config, CONFIG_PREFER_START, result.preferStart);

        Object goal = getValue(config, CONFIG_GOAL);
        if (goal != null && !GOAL_MINIMUM.equals(goal) && !GOAL_MAXIMUM.equals(goal)) {
            throw new IllegalArgumentException("Goal must be '" + GOAL_MINIMUM + "' or '" + GOAL_MAXIMUM + "'");
        }
        result.maximum = GOAL_MAXIMUM.equals(goal);

        return result;
    }

    /**
     * Get a configuration value, treating blank strings and unresolved rule template references as not set.
     *
     * A rule created from a template keeps the reference, e.g. {@code {{persistenceService}}}, when the user leaves
     * an optional template parameter empty.
     *
     * @param config the module configuration
     * @param key the parameter name
     *
     * @return the value, or {@code null} if it is not set
     */
    public static @Nullable Object getValue(Configuration config, String key) {
        Object value = config.get(key);
        if (value instanceof String string) {
            String trimmed = string.trim();
            if (trimmed.isEmpty() || (trimmed.startsWith("{{") && trimmed.endsWith("}}"))) {
                return null;
            }
            return trimmed;
        }
        return value;
    }

    private static int getInt(Configuration config, String key, int defaultValue) {
        Object value = getValue(config, key);
        if (value instanceof Number number) {
            return number.intValue();
        } else if (value instanceof String string) {
            try {
                return new BigDecimal(string).intValue();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + key + "' must be a number, but is '" + string + "'");
            }
        }
        return defaultValue;
    }

    private static boolean getBoolean(Configuration config, String key, boolean defaultValue) {
        Object value = getValue(config, key);
        if (value instanceof Boolean bool) {
            return bool;
        } else if (value instanceof String string) {
            return Boolean.parseBoolean(string);
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return String.format("{ item: %s, s: %d, d: %d, l: %s, c: %b, max: %b, p: %b }", forecastItem, rangeStart,
                rangeDuration, length, consecutive, maximum, preferStart);
    }
}
