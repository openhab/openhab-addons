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
package org.openhab.binding.mikrotik.internal.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Converter} is a utility class having functions to convert RouterOS-specific data representation strings
 * to regular Java types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class Converter {
    private static final DateTimeFormatter ROUTEROS_FORMAT = DateTimeFormatter.ofPattern("MMM/dd/yyyy kk:mm:ss",
            Locale.ENGLISH);

    private static final Pattern PERIOD_PATTERN = Pattern.compile("(\\d+)([a-z]+){1,3}");

    public @Nullable static LocalDateTime fromRouterosTime(@Nullable String dateTimeString) {
        if (dateTimeString == null) {
            return null;
        }
        String fixedTs = dateTimeString.substring(0, 1).toUpperCase() + dateTimeString.substring(1);
        return LocalDateTime.parse(fixedTs, ROUTEROS_FORMAT);
    }

    public @Nullable static LocalDateTime routerosPeriodBack(@Nullable String durationString) {
        return routerosPeriodBack(durationString, LocalDateTime.now());
    }

    public @Nullable static LocalDateTime routerosPeriodBack(@Nullable String durationString,
            LocalDateTime fromDateTime) {
        if (durationString == null) {
            return null;
        }

        Matcher m = PERIOD_PATTERN.matcher(durationString);
        LocalDateTime ts = fromDateTime;
        while (m.find()) {
            int amount = Integer.parseInt(m.group(1));
            String periodKey = m.group(2);
            switch (periodKey) {
                case "y":
                    ts = ts.minusYears(amount);
                    break;
                case "w":
                    ts = ts.minusWeeks(amount);
                    break;
                case "d":
                    ts = ts.minusDays(amount);
                    break;
                case "h":
                    ts = ts.minusHours(amount);
                    break;
                case "m":
                    ts = ts.minusMinutes(amount);
                    break;
                case "s":
                    ts = ts.minusSeconds(amount);
                    break;
                case "ms":
                    ts = ts.plus(amount, ChronoField.MILLI_OF_SECOND.getBaseUnit());
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unable to parse duration %s - %s is unknown", durationString, periodKey));
            }
        }
        return ts;
    }
}
