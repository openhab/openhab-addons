/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.pirateweather.internal.config;

import java.time.Duration;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PirateWeatherChannelConfiguration} is the class used to match the sunrise and sunset event configuration.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PirateWeatherChannelConfiguration {

    private final Logger logger = LoggerFactory.getLogger(PirateWeatherChannelConfiguration.class);

    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");
    private static final String TIME_SEPARATOR = ":";

    private int offset;
    private @Nullable String earliest;
    private @Nullable String latest;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public @Nullable String getEarliest() {
        return earliest;
    }

    public long getEarliestInMinutes() {
        return getMinutesFromTime(earliest);
    }

    public void setEarliest(String earliest) {
        this.earliest = earliest;
    }

    public @Nullable String getLatest() {
        return latest;
    }

    public long getLatestInMinutes() {
        return getMinutesFromTime(latest);
    }

    public void setLatest(String latest) {
        this.latest = latest;
    }

    @Override
    public String toString() {
        return String.format("[offset=%d, earliest='%s', latest='%s']", offset, earliest, latest);
    }

    /**
     * Parses a hh:mm string and returns the minutes.
     */
    public long getMinutesFromTime(@Nullable String configTime) {
        if (configTime == null || configTime.isBlank()) {
            return 0;
        }

        String time = configTime.trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalTime localTime = LocalTime.parse(time, formatter);
            return Duration.ofHours(localTime.getHour())
                           .plusMinutes(localTime.getMinute())
                           .toMinutes();
        } catch (DateTimeParseException ex) {
            logger.warn("Cannot parse channel configuration '{}' to hour and minutes, use pattern HH:mm, ignoring!", time);
            return 0;
        }
    }
}
