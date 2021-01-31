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
package org.openhab.binding.mikrotik.internal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The {@link Converter} is a utility class having functions to convert RouterOS-specific data representation strings
 * to regular Java types.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class Converter {
    private static final DateTimeFormatter ROUTEROS_FORMAT = DateTimeFormat.forPattern("MMM/dd/yyyy kk:mm:ss");

    private static final Pattern PERIOD_PATTERN = Pattern.compile("(\\d+)([a-z]+){1,3}");

    @Nullable
    public static DateTime fromRouterosTime(@Nullable String dateTimeString) {
        if (dateTimeString == null)
            return null;
        String fixedTs = dateTimeString.substring(0, 1).toUpperCase() + dateTimeString.substring(1);
        return ROUTEROS_FORMAT.parseDateTime(fixedTs);
    }

    @Nullable
    public static Period fromRouterosPeriod(@Nullable String durationString) {
        if (durationString == null)
            return null;

        Matcher m = PERIOD_PATTERN.matcher(durationString);
        MutablePeriod per = new MutablePeriod();
        while (m.find()) {
            int amount = Integer.parseInt(m.group(1));
            String period = m.group(2);
            switch (period) {
                case "y":
                    per.addYears(amount);
                    break;
                case "w":
                    per.addWeeks(amount);
                    break;
                case "d":
                    per.addDays(amount);
                    break;
                case "h":
                    per.addHours(amount);
                    break;
                case "m":
                    per.addMinutes(amount);
                    break;
                case "s":
                    per.addSeconds(amount);
                    break;
                case "ms":
                    per.addMillis(amount);
                    break;
                default:
                    throw new NotImplementedException(
                            String.format("Unable to parse duration %s - %s is unknown", durationString, period));
            }
        }
        return per.toPeriod();
    }
}
