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

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;

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
    private static final Duration MAX_RANGE_DURATION = Duration.ofHours(48);

    public String forecastItem = "";
    public @Nullable String persistenceService;
    public LocalTime rangeStart = LocalTime.MIDNIGHT;
    public Duration rangeDuration = Duration.ofHours(24);
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

        result.rangeStart = getTime(config, CONFIG_RANGE_START, result.rangeStart);

        result.rangeDuration = getDuration(config, CONFIG_RANGE_DURATION, result.rangeDuration);
        if (result.rangeDuration.isNegative() || result.rangeDuration.isZero()
                || result.rangeDuration.compareTo(MAX_RANGE_DURATION) > 0) {
            throw new IllegalArgumentException("Range duration must be positive and not longer than 48 hours");
        }

        if (getValue(config, CONFIG_LENGTH) == null) {
            throw new IllegalArgumentException("Length is not set");
        }
        result.length = getDuration(config, CONFIG_LENGTH, Duration.ZERO);
        if (result.length.isNegative() || result.length.isZero() || result.length.compareTo(result.rangeDuration) > 0) {
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

    /**
     * Get a time of day like {@code 22:00}, in whole minutes.
     */
    private static LocalTime getTime(Configuration config, String key, LocalTime defaultValue) {
        Object value = getValue(config, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            LocalTime time = LocalTime.parse(value.toString());
            if (time.getSecond() != 0 || time.getNano() != 0) {
                throw new IllegalArgumentException(
                        "'" + key + "' must be given in whole minutes, but is '" + value + "'");
            }
            return time;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("'" + key + "' must be a time like 22:00, but is '" + value + "'");
        }
    }

    /**
     * Get a duration like {@code 8h} or {@code 1h30m}, in whole minutes. The unit is required.
     */
    private static Duration getDuration(Configuration config, String key, Duration defaultValue) {
        Object value = getValue(config, key);
        if (value == null) {
            return defaultValue;
        }
        Duration duration;
        try {
            duration = DurationUtils.parse(value.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "'" + key + "' must be a duration like 8h or 1h30m, but is '" + value + "'");
        }
        if (duration.toSecondsPart() != 0 || duration.toNanosPart() != 0) {
            throw new IllegalArgumentException("'" + key + "' must be given in whole minutes, but is '" + value + "'");
        }
        return duration;
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
        return String.format("{ item: %s, s: %s, d: %s, l: %s, c: %b, max: %b, p: %b }", forecastItem, rangeStart,
                rangeDuration, length, consecutive, maximum, preferStart);
    }
}
